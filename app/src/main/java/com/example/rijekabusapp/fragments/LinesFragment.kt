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

class LinesFragment : Fragment() {
    private val viewModel: LinesViewModel by activityViewModels()
    private val stationsViewModel: StationsViewModel by activityViewModels()
    private lateinit var binding: FragmentLinesBinding
    private var selectedDirection: String = ""
    private var varijanta: String = "40"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLinesBinding.inflate(inflater, container, false)
        binding.rvLines.layoutManager = LinearLayoutManager(requireContext())

        setupFilters()
        setupSpinner()

        return binding.root
    }

    private fun filterBusesByStation(stationId: Int) {
        viewModel.fullLinesList.observe(viewLifecycleOwner) { lines ->
            val filteredLines =
                lines.filter { line ->
                    line.currentStationId == stationId
                }
            val adapter =
                LineRecyclerAdapter(
                    requireContext(),
                    viewModel.getDistinctedBusLines(filteredLines),
                    viewModel.favoriteLines.value,
                    false,
                    insertFavoriteLine,
                    deleteFavoriteLine,
                )
            binding.rvLines.adapter = adapter
        }
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
                    val selectedStation = filteredStations[position]
                    selectedStation?.let { station ->
                        filterBusesByStation(station.id)
                        bsDialog.dismiss()
                    }
                }
            }

            bsView.findViewById<Button>(R.id.b_apply).setOnClickListener {
                bsDialog.dismiss()
            }
            bsView.findViewById<Button>(R.id.b_reset).setOnClickListener {
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
                    getLinesList(selectedDirection)
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
            val adapter =
                LineRecyclerAdapter(
                    requireContext(),
                    (
                        (
                            if (direction != "") {
                                lines.filter { it.direction == direction && varijanta >= it.variant }
                            } else {
                                lines
                            }
                        ) as ArrayList<Line>
                    ),
                    favoriteLines,
                    false,
                    insertFavoriteLine,
                    deleteFavoriteLine,
                )
            binding.rvLines.adapter = adapter
            binding.progressBar.visibility = ProgressBar.GONE

            setLinesSearchListener(adapter)
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
                    adapter.filter.filter(text)
                    return false
                }
            },
        )
    }
}
