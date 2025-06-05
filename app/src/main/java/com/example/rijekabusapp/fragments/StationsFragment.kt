package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Filter
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.MainActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.StationRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentStationsBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.LinesViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputLayout

class StationsFragment : Fragment() {
    private val viewModel: StationsViewModel by activityViewModels()
    private val linesViewModel: LinesViewModel by activityViewModels()
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var binding: FragmentStationsBinding
    private var selectedDirection: String = ""

    // Filter state variables
    private var selectedLine: Line? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStationsBinding.inflate(inflater, container, false)
        binding.rvStations.layoutManager = LinearLayoutManager(requireContext())

        // Initialize ScheduleViewModel
        scheduleViewModel = (requireActivity() as MainActivity).scheduleViewModel

        // Load data first
        if (requireContext().isOnline()) {
            viewModel.getStationsList()
            scheduleViewModel.getScheduleList("tjedan")
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }

        setupFilters()
        setupSpinner()
        setupFilterChips()

        return binding.root
    }

    private fun setupFilterChips() {
        // Clear chip group initially
        binding.cgFilters.removeAllViews()

        // Set initial visibility of the "all stations" text
        binding.tvAll.visibility = View.VISIBLE
    }

    private fun updateFilterChips() {
        binding.cgFilters.removeAllViews()

        // Add chip for selected line
        selectedLine?.let { line ->
            val chip = Chip(requireContext())
            chip.text = "${line.lineNumber} - ${line.name}"
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedLine = null
                updateFilterChips()
                getStationsList(selectedDirection)
            }
            binding.cgFilters.addView(chip)
        }

        // Add chip for selected direction if not empty
        if (selectedDirection.isNotEmpty()) {
            val directionText =
                when (selectedDirection) {
                    "A" -> "A"
                    "B" -> "B"
                    else -> selectedDirection
                }

            val chip = Chip(requireContext())
            chip.text = directionText
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedDirection = ""
                // Update spinner selection to match
                binding.spinner.setSelection(0)
                updateFilterChips()
                getStationsList(selectedDirection)
            }
            binding.cgFilters.addView(chip)
        }

        // Show or hide the chip group based on whether there are any filters
        val hasFilters = binding.cgFilters.childCount > 0
        binding.cgFilters.visibility = if (hasFilters) View.VISIBLE else View.GONE

        // Show or hide the "all stations" text based on whether there are any filters
        binding.tvAll.visibility = if (hasFilters) View.GONE else View.VISIBLE
    }

    private fun setupFilters() {
        binding.filter.setOnClickListener {
            val bsDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val bsView =
                LayoutInflater.from(binding.root.context)
                    .inflate(
                        R.layout.bottom_sheet_filter,
                        binding.root.findViewById(R.id.bs_container),
                    )

            // Change the filter title text
            val filterTitle = bsView.findViewById<View>(R.id.tv_filter_lines) as android.widget.TextView
            filterTitle.text = getString(R.string.filter_stations)

            // Change the hint text to "line" instead of "lines"
            val textInputLayout = bsView.findViewById<TextInputLayout>(R.id.til_lines)
            textInputLayout.hint = getString(R.string.lines)

            // Hide the chip group for zones since we don't need it for stations
            val chipsGroup = bsView.findViewById<ChipGroup>(R.id.chips_group)
            chipsGroup.visibility = View.GONE

            // Also hide the zones title
            val zonesTitle = bsView.findViewById<View>(R.id.tv_filter_zones)
            zonesTitle.visibility = View.GONE

            // Setup autocomplete for lines
            val atvNames = bsView.findViewById<AutoCompleteTextView>(R.id.atv_names)
            val lineAdapter =
                ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    mutableListOf(),
                )
            atvNames.setAdapter(lineAdapter)

            // Create a temporary variable to store the selected line during filter selection
            var tempSelectedLine = selectedLine
            android.util.Log.d("StationsFilter", "Initial tempSelectedLine: ${tempSelectedLine?.lineNumber}")

            // Set the selected line in the AutoCompleteTextView
            selectedLine?.let { line ->
                atvNames.setText("${line.lineNumber} - ${line.name}")
            }

            // Get lines for autocomplete - use a separate list to avoid observer issues
            val allLines = mutableListOf<Line>()

            linesViewModel.getLinesList()
            linesViewModel.fullLinesList.observe(viewLifecycleOwner) { lines ->
                allLines.clear()
                allLines.addAll(lines.distinctBy { it.lineNumber + it.name })

                android.util.Log.d("StationsFilter", "Loaded ${allLines.size} distinct lines")

                // Update the adapter with initial data
                val allLineNames = allLines.map { "${it.lineNumber} - ${it.name}" }
                lineAdapter.clear()
                lineAdapter.addAll(allLineNames)
                lineAdapter.notifyDataSetChanged()
            }

            atvNames.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        if ((s?.length ?: 0) >= 1) { // Allow searching with just 1 character for line numbers
                            val query = s.toString().lowercase()

                            // Filter lines by name or number
                            val filteredLines =
                                allLines.filter {
                                    it.name.lowercase().contains(query) ||
                                        it.lineNumber.lowercase().contains(query)
                                }

                            android.util.Log.d("StationsFilter", "Filtered lines: ${filteredLines.size} for query: $query")

                            // Create display strings for the dropdown
                            val filteredLineNames = filteredLines.map { "${it.lineNumber} - ${it.name}" }
                            lineAdapter.clear()
                            lineAdapter.addAll(filteredLineNames)
                            lineAdapter.notifyDataSetChanged()
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {}
                },
            )

            atvNames.setOnItemClickListener { _, view, position, _ ->
                val selectedText = atvNames.text.toString()
                android.util.Log.d("StationsFilter", "Selected text: $selectedText at position: $position")

                // Find the matching line from the text
                val lineParts = selectedText.split(" - ")
                if (lineParts.isNotEmpty()) {
                    val lineNumber = lineParts[0]
                    tempSelectedLine = allLines.find { it.lineNumber == lineNumber }
                    android.util.Log.d("StationsFilter", "Found line: ${tempSelectedLine?.lineNumber} - ${tempSelectedLine?.name}")
                }
            }

            bsView.findViewById<Button>(R.id.b_apply).setOnClickListener {
                // Update the actual selected line from the temporary variable
                selectedLine = tempSelectedLine
                android.util.Log.d("StationsFilter", "Applied line filter: ${selectedLine?.lineNumber}")

                // Update filter chips and apply filters
                updateFilterChips()
                getStationsList(selectedDirection)
                bsDialog.dismiss()
            }

            bsView.findViewById<Button>(R.id.b_reset).setOnClickListener {
                // Clear all selections
                atvNames.setText("")
                tempSelectedLine = null
                selectedLine = null
                updateFilterChips()
                getStationsList(selectedDirection)
                bsDialog.dismiss()
            }

            bsDialog.setContentView(bsView)
            bsDialog.show()
        }
    }

    private fun setupSpinner() {
        val directions = resources.getStringArray(R.array.SpinnerDirectionsItems)
        val spinnerAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.drop_down_toolbar_item,
                directions,
            )

        binding.spinner.adapter = spinnerAdapter

        binding.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    when (position) {
                        0 -> selectedDirection = "" // All lines
                        1 -> selectedDirection = "B"
                        2 -> selectedDirection = "A"
                    }
                    // Update filter chips to reflect direction change
                    updateFilterChips()
                    getStationsList(selectedDirection)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private var insertFavoriteStation = fun(it: Station) {
        viewModel.insertFavoriteStation(it)
    }
    private var deleteFavoriteStation = fun(it: Station) {
        viewModel.deleteFavoriteStation(it)
    }

    fun getStationsList(direction: String) {
        viewModel.getFavoriteStations()
        val favoriteStations = viewModel.favoriteStations.value

        binding.progressBar.visibility = ProgressBar.VISIBLE

        viewModel.stationsList.observe(viewLifecycleOwner) { stations ->
            android.util.Log.d("StationsFilter", "Total stations: ${stations.size}")

            scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
                android.util.Log.d("StationsFilter", "Total schedule entries: ${scheduleList.size}")

                // Debug the schedule data
                if (scheduleList.isEmpty()) {
                    android.util.Log.e("StationsFilter", "Schedule list is empty! Cannot filter stations by line.")
                    // If schedule list is empty, try loading it again
                    scheduleViewModel.getScheduleList("tjedan")
                    return@observe
                }

                var filteredStations = ArrayList<Station>(stations)

                // Apply direction filter
                if (direction.isNotEmpty()) {
                    android.util.Log.d("StationsFilter", "Filtering by direction: $direction")
                    filteredStations =
                        ArrayList(
                            stations.filter { st ->
                                scheduleList.any { sch ->
                                    sch.stationId == st.id && sch.direction == direction
                                }
                            },
                        )
                    android.util.Log.d("StationsFilter", "After direction filter: ${filteredStations.size} stations")
                }

                // Apply line filter if selected
                if (selectedLine != null) {
                    val line = selectedLine!!
                    android.util.Log.d("StationsFilter", "Filtering by line: ${line.lineNumber}")

                    // Get all station IDs that this line passes through
                    val stationIdsForLine =
                        scheduleList
                            .filter { sch -> sch.lineNumber == line.lineNumber }
                            .map { it.stationId }
                            .distinct()

                    android.util.Log.d("StationsFilter", "Found ${stationIdsForLine.size} distinct stations for line ${line.lineNumber}")

                    if (stationIdsForLine.isEmpty()) {
                        android.util.Log.e("StationsFilter", "No stations found for line ${line.lineNumber}!")
                    } else {
                        // Filter stations by these IDs
                        filteredStations =
                            ArrayList(
                                filteredStations.filter { station ->
                                    stationIdsForLine.contains(station.id)
                                },
                            )

                        android.util.Log.d("StationsFilter", "After line filtering: ${filteredStations.size} stations")
                    }
                }

                // Check if there are any results
                if (filteredStations.isEmpty()) {
                    // Show empty state view with appropriate message
                    binding.emptyState.setupEmptyStateView(getString(R.string.no_stations_error_desc))
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvStations.visibility = View.GONE
                    android.util.Log.d("StationsFilter", "No stations found after filtering - showing empty state")
                } else {
                    // Show results
                    binding.emptyState.visibility = View.GONE
                    binding.rvStations.visibility = View.VISIBLE

                    val adapter =
                        StationRecyclerAdapter(
                            requireContext(),
                            filteredStations,
                            favoriteStations,
                            false,
                            insertFavoriteStation,
                            deleteFavoriteStation,
                        )
                    binding.rvStations.adapter = adapter
                    binding.progressBar.visibility = ProgressBar.GONE

                    setStationSearchListener(adapter)
                    android.util.Log.d("StationsFilter", "Showing ${filteredStations.size} stations in adapter")
                }

                // Make sure the "all stations" text is visible when no filters are applied
                binding.tvAll.visibility = if (selectedLine == null && direction.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun setStationSearchListener(adapter: StationRecyclerAdapter) {
        binding.SVLines.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(text: String?): Boolean {
                    // Apply the filter with a listener to check results
                    adapter.filter.filter(
                        text,
                        object : Filter.FilterListener {
                            override fun onFilterComplete(count: Int) {
                                // This is called when filtering is complete
                                if (count == 0 && !text.isNullOrEmpty() && text.length >= 3) {
                                    // No results and we have a search query of at least 3 chars - show empty state
                                    binding.emptyState.setupEmptyStateView(getString(R.string.filters_search_error_desc))
                                    binding.emptyState.visibility = View.VISIBLE
                                    binding.rvStations.visibility = View.GONE
                                } else {
                                    // Has results, empty query, or query too short - show recycler view
                                    binding.emptyState.visibility = View.GONE
                                    binding.rvStations.visibility = View.VISIBLE
                                }
                            }
                        },
                    )

                    return false
                }
            },
        )
    }
}
