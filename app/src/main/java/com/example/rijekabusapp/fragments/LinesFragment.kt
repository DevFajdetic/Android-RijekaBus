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
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.LineRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentLinesBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.LinesViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class LinesFragment : Fragment() {
    private val viewModel: LinesViewModel by activityViewModels()
    private val stationsViewModel: StationsViewModel by activityViewModels()
    private lateinit var binding: FragmentLinesBinding
    private var selectedDirection: String = ""
    private var varijanta: String = "40"

    // Filter state variables
    private var selectedStation: Station? = null
    private var selectedBusTypes = mutableSetOf<BusType>()

    // Define bus types
    enum class BusType {
        CITY,
        SUBURBAN,
        NIGHT,
    }

    // Define bus line numbers for each type
    private val cityBusLines = setOf("1", "1a", "1b", "2", "2a", "3a", "3", "4", "4a", "5", "5a", "5b", "6", "7", "7a", "8", "13")
    private val suburbanBusLines =
        setOf(
            "10", "10a", "12", "12a", "12b", "14", "15", "18", "18a", "18b", "18c", "25", "26", "32",
            "32a", "35", "11", "17", "19", "20", "21", "27", "29", "29a", "34", "37", "36", "22", "23", "23a", "30",
        )
    private val nightBusLines = setOf<String>() // Currently not operating

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLinesBinding.inflate(inflater, container, false)
        binding.rvLines.layoutManager = LinearLayoutManager(requireContext())

        setupFilters()
        setupSpinner()
        setupFilterChips()

        return binding.root
    }

    private fun setupFilters() {
        binding.filter.setOnClickListener {
            val bsDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val bsView =
                LayoutInflater.from(binding.root.context)
                    .inflate(R.layout.bottom_sheet_filter, binding.root.findViewById(R.id.bs_container))
            val atvNames = bsView.findViewById<AutoCompleteTextView>(R.id.atv_names)
            val stationAdapter =
                ArrayAdapter<String>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    mutableListOf(),
                )
            atvNames.setAdapter(stationAdapter)

            // Change the hint text to "station" instead of "lines"
            val textInputLayout = bsView.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.til_lines)
            textInputLayout.hint = getString(R.string.stations)

            // Setup chip group
            val chipsGroup = bsView.findViewById<ChipGroup>(R.id.chips_group)
            val cityChip = bsView.findViewById<Chip>(R.id.c_gradski)
            val suburbanChip = bsView.findViewById<Chip>(R.id.c_prigradski)
            val nightChip = bsView.findViewById<Chip>(R.id.c_nocni)

            // Set initial chip states based on current selections
            cityChip.isChecked = selectedBusTypes.contains(BusType.CITY)
            suburbanChip.isChecked = selectedBusTypes.contains(BusType.SUBURBAN)
            nightChip.isChecked = selectedBusTypes.contains(BusType.NIGHT)

            // Set the selected station in the AutoCompleteTextView
            selectedStation?.let { station ->
                atvNames.setText(station.shortName)
            }

            stationsViewModel.getStationsList()
            stationsViewModel.stationsList.observe(viewLifecycleOwner) { stations ->
                val filteredStations = mutableListOf<Station>()
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
                            if ((s?.length ?: 0) >= 3) {
                                val query = s.toString().lowercase()
                                filteredStations.clear()
                                filteredStations.addAll(
                                    stations.filter {
                                        it.shortName.lowercase()
                                            .contains(query)
                                    },
                                )
                                val filteredStationNames = filteredStations.map { it.shortName }
                                stationAdapter.clear()
                                stationAdapter.addAll(filteredStationNames)
                                stationAdapter.notifyDataSetChanged()
                            } else {
                                stationAdapter.clear()
                                stationAdapter.notifyDataSetChanged()
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    },
                )

                atvNames.setOnItemClickListener { _, _, position, _ ->
                    if (position >= 0 && position < filteredStations.size) {
                        selectedStation = filteredStations[position]
                    }
                }
            }

            bsView.findViewById<Button>(R.id.b_apply).setOnClickListener {
                // Update bus type selections
                selectedBusTypes.clear()
                if (cityChip.isChecked) selectedBusTypes.add(BusType.CITY)
                if (suburbanChip.isChecked) selectedBusTypes.add(BusType.SUBURBAN)
                if (nightChip.isChecked) selectedBusTypes.add(BusType.NIGHT)

                // Update filter chips and apply filters
                updateFilterChips()
                applyFilters()
                bsDialog.dismiss()
            }

            bsView.findViewById<Button>(R.id.b_reset).setOnClickListener {
                // Clear all selections
                selectedStation = null
                selectedBusTypes.clear()
                updateFilterChips()
                applyFilters()
                bsDialog.dismiss()
            }

            bsDialog.setContentView(bsView)
            bsDialog.show()
        }
    }

    private fun setupFilterChips() {
        // Clear chip group initially
        binding.cgFilters.removeAllViews()

        // Set initial visibility of the "all lines" text
        binding.tvAll.visibility = View.VISIBLE
    }

    private fun updateFilterChips() {
        binding.cgFilters.removeAllViews()

        // Add chips for selected bus types
        selectedBusTypes.forEach { busType ->
            val chip = Chip(requireContext())
            chip.text =
                when (busType) {
                    BusType.CITY -> getString(R.string.city_bus)
                    BusType.SUBURBAN -> getString(R.string.suburban_bus)
                    BusType.NIGHT -> getString(R.string.night_bus)
                }
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedBusTypes.remove(busType)
                updateFilterChips()
                applyFilters()
            }
            binding.cgFilters.addView(chip)
        }

        // Add chip for selected station
        selectedStation?.let { station ->
            val chip = Chip(requireContext())
            chip.text = station.shortName
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                selectedStation = null
                updateFilterChips()
                applyFilters()
            }
            binding.cgFilters.addView(chip)
        }

        // Show or hide the chip group based on whether there are any filters
        val hasFilters = binding.cgFilters.childCount > 0
        binding.cgFilters.visibility = if (hasFilters) View.VISIBLE else View.GONE

        // Show or hide the "all lines" text based on whether there are any filters
        binding.tvAll.visibility = if (hasFilters) View.GONE else View.VISIBLE
    }

    private fun applyFilters() {
        viewModel.fullLinesList.observe(viewLifecycleOwner) { lines ->
            var filteredLines = lines

            // Apply station filter if selected
            selectedStation?.let { station ->
                filteredLines =
                    filteredLines.filter { line ->
                        line.currentStationId == station.id
                    } as ArrayList<Line>?
            }

            // Apply bus type filters if any selected
            if (selectedBusTypes.isNotEmpty()) {
                filteredLines =
                    filteredLines.filter { line ->
                        val lineNumber = line.lineNumber
                        when {
                            selectedBusTypes.contains(BusType.CITY) && lineNumber in cityBusLines -> true
                            selectedBusTypes.contains(BusType.SUBURBAN) && lineNumber in suburbanBusLines -> true
                            selectedBusTypes.contains(BusType.NIGHT) && lineNumber in nightBusLines -> true
                            else -> false
                        }
                    } as ArrayList<Line>?
            }

            // Apply direction filter
            if (selectedDirection.isNotEmpty()) {
                filteredLines =
                    filteredLines.filter { line ->
                        line.direction == selectedDirection && varijanta >= line.variant
                    } as ArrayList<Line>?
            }

            val distinctedLines = viewModel.getDistinctedBusLines(filteredLines)

            // Check if there are any results
            if (distinctedLines.isEmpty()) {
                // Show empty state view with appropriate message
                binding.emptyState.setupEmptyStateView(getString(R.string.filters_search_error_desc))
                binding.emptyState.visibility = View.VISIBLE
                binding.rvLines.visibility = View.GONE
            } else {
                // Show results
                binding.emptyState.visibility = View.GONE
                binding.rvLines.visibility = View.VISIBLE

                // Update the adapter with filtered lines
                val adapter =
                    LineRecyclerAdapter(
                        requireContext(),
                        distinctedLines,
                        viewModel.favoriteLines.value,
                        false,
                        insertFavoriteLine,
                        deleteFavoriteLine,
                    )
                binding.rvLines.adapter = adapter

                // Set up search functionality for the adapter
                setLinesSearchListener(adapter)
            }
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
                    if (selectedStation != null || selectedBusTypes.isNotEmpty()) {
                        applyFilters()
                    } else {
                        getLinesList(selectedDirection)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private var insertFavoriteLine = fun(it: Line) {
        viewModel.insertFavoriteLine(it)
    }
    private var deleteFavoriteLine = fun(it: Line) {
        viewModel.deleteFavoriteLine(it)
    }

    fun getLinesList(direction: String) {
        viewModel.getFavoriteLines()
        val favoriteLines = viewModel.favoriteLines.value

        binding.progressBar.visibility = ProgressBar.VISIBLE

        viewModel.linesList.observe(viewLifecycleOwner) { lines ->
            var filteredLines = lines

            // Apply direction filter
            if (direction != "") {
                filteredLines = lines.filter { it.direction == direction && varijanta >= it.variant } as ArrayList<Line>
            }

            val distinctedLines = viewModel.getDistinctedBusLines(filteredLines)

            // Check if there are any results
            if (distinctedLines.isEmpty()) {
                // Show empty state view with appropriate message
                binding.emptyState.setupEmptyStateView(getString(R.string.no_buses_error_desc))
                binding.emptyState.visibility = View.VISIBLE
                binding.rvLines.visibility = View.GONE
            } else {
                // Show results
                binding.emptyState.visibility = View.GONE
                binding.rvLines.visibility = View.VISIBLE

                val adapter =
                    LineRecyclerAdapter(
                        requireContext(),
                        distinctedLines,
                        favoriteLines,
                        false,
                        insertFavoriteLine,
                        deleteFavoriteLine,
                    )
                binding.rvLines.adapter = adapter

                // Set up search functionality for the adapter
                setLinesSearchListener(adapter)
            }

            binding.progressBar.visibility = ProgressBar.GONE

            // Make sure the "all lines" text is visible when no filters are applied
            if (selectedStation == null && selectedBusTypes.isEmpty()) {
                binding.tvAll.visibility = View.VISIBLE
            }
        }

        if (requireContext().isOnline()) {
            viewModel.getLinesList()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    private fun setLinesSearchListener(adapter: LineRecyclerAdapter) {
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
                                android.util.Log.d("Filter3", "Count: $count, Text: ${text?.length ?: 0}")

                                // This is called when filtering is complete
                                if (count == 0 && !text.isNullOrEmpty()) {
                                    // No results and we have a search query - show empty state
                                    binding.emptyState.setupEmptyStateView(getString(R.string.filters_search_error_desc))
                                    binding.emptyState.visibility = View.VISIBLE
                                    binding.rvLines.visibility = View.GONE
                                } else {
                                    // Has results or empty query - show recycler view
                                    binding.emptyState.visibility = View.GONE
                                    binding.rvLines.visibility = View.VISIBLE
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
