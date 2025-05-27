package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.StationActivity
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.StationLinesRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentStationLinesListBinding
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.ScheduleViewModel

class StationLinesListFragment : Fragment() {
    private lateinit var binding: FragmentStationLinesListBinding
    private lateinit var scheduleViewModel: ScheduleViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStationLinesListBinding.inflate(inflater, container, false)
        val stationItem = (arguments?.getSerializable(EXTRA_STATION) as? Station)!!

        getLinesList(stationItem)

        return binding.root
    }

    private fun getLinesList(stationItem: Station) {
        binding.progressBar.visibility = ProgressBar.VISIBLE

        scheduleViewModel = (requireActivity() as StationActivity).scheduleViewModel
        binding.rvLines.layoutManager = LinearLayoutManager(requireContext())

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            val filteredLines =
                scheduleList.filter { it.stationId == stationItem.id }
                    .distinctBy { it.lineNumber + it.direction }

            val adapter =
                StationLinesRecyclerAdapter(
                    requireContext(),
                    filteredLines,
                    false,
                )
            binding.rvLines.adapter = adapter
            binding.progressBar.visibility = ProgressBar.GONE

            if (filteredLines.isEmpty()) {
                binding.emptyState.setupEmptyStateView(getString(R.string.no_stations_error_desc))
                binding.emptyState.visibility = View.VISIBLE
                binding.rvLines.visibility = View.GONE
            }
        }
    }
}
