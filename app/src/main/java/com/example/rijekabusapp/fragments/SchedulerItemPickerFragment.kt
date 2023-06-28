package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.StationLinesRecyclerAdapter
import com.example.rijekabusapp.adapters.StationsScheduleRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentSchedulerItemPickerBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Schedule
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import kotlinx.coroutines.launch

class SchedulerItemPickerFragment : Fragment(R.layout.fragment_explore) {

    private val viewModel: ScheduleViewModel by activityViewModels()
    private val stationsViewModel: StationsViewModel by activityViewModels()

    private lateinit var binding: FragmentSchedulerItemPickerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSchedulerItemPickerBinding.inflate(inflater, container, false)

        binding.logo.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_schedulerItemPickerFragment_to_exploreFragment)
        }

        binding.rv.layoutManager = LinearLayoutManager(requireContext())
        val spinnerItems = resources.getStringArray(R.array.SchedulerPickerItems)
        val spinnerAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                spinnerItems
            )
        binding.spinner.adapter = spinnerAdapter

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> getBusLines()
                    1 -> getStations()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        return binding.root
    }

    fun getBusLines() {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        binding.tvAll.text = getString(R.string.all_bus_lines_rijeka)

        viewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            val filteredLines = scheduleList.distinctBy {
                it.lineNumber + it.direction + it.linVarId + it.variantLineName
            }.sortedWith(comparator)

            val adapter = StationLinesRecyclerAdapter(
                requireContext(),
                filteredLines,
                false
            )
            binding.rv.adapter = adapter
            binding.progressBar.visibility = ProgressBar.GONE

            if (filteredLines.isEmpty()) {
                binding.emptyState.setupEmptyStateView(getString(R.string.no_stations_error_desc))
                binding.emptyState.visibility = View.VISIBLE
                binding.rv.visibility = View.GONE
            }
        }

        if (requireContext().isOnline()) {
            lifecycleScope.launch {
                viewModel.getScheduleList(null)
            }
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    fun getStations() {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        binding.tvAll.text = getString(R.string.all_bus_stations_rijeka)

        viewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            stationsViewModel.stationsList.observe(viewLifecycleOwner) { stationsList ->
                val adapter = StationsScheduleRecyclerAdapter(
                    requireContext(), scheduleList, stationsList
                )
                binding.rv.adapter = adapter
                binding.progressBar.visibility = ProgressBar.GONE
            }
        }

        if (requireContext().isOnline()) {
            stationsViewModel.getStationsList()
            viewModel.getScheduleList(null)
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    val comparator = Comparator<Schedule> { line1, line2 ->
        val isStr1Numeric = line1.lineNumber.length == 1 && line1.lineNumber[0].isDigit()
        val isStr2Numeric = line2.lineNumber.length == 1 && line2.lineNumber[0].isDigit()

        when {
            isStr1Numeric && isStr2Numeric -> line1.lineNumber.compareTo(line2.lineNumber)
            isStr1Numeric -> -1
            isStr2Numeric -> 1
            else -> line1.lineNumber.compareTo(line2.lineNumber)
        }
    }
}
