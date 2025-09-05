package com.example.rijekabusapp.fragments

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.TimelineAdapter
import com.example.rijekabusapp.databinding.FragmentLineScheduleBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.factory.ScheduleViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LineScheduleFragment(private val s: String) : Fragment() {
    private lateinit var binding: FragmentLineScheduleBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private val busLocViewModel: BusLocationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLineScheduleBinding.inflate(inflater, container, false)
        val lineItem = arguments?.getSerializable(EXTRA_LINE) as? Line
        val stationItem = arguments?.getSerializable(EXTRA_STATION) as? Station

        scheduleViewModel =
            ViewModelProvider(
                this, ScheduleViewModelFactory(requireContext().applicationContext as Application),
            )[ScheduleViewModel::class.java]
        if (lineItem != null) {
            fillScheduleLine(lineItem)
        } else if (stationItem != null) {
            fillScheduleStation(stationItem)
        }

        return binding.root
    }

    private fun fillScheduleLine(lineItem: Line) {
        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())

        binding.tvTimelineTitle.text = getString(R.string.bus_line_departures)

        binding.progressBar.visibility = ProgressBar.VISIBLE

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            val allSchedulesForLine = scheduleList.filter {
                it.variantLineName == lineItem.name &&
                        it.lineNumber == lineItem.lineNumber &&
                        it.direction == lineItem.direction &&
                        it.linVarId == lineItem.linVarId &&
                        it.variant == lineItem.variant
            }

            val departuresList = allSchedulesForLine
                .filter { it.stationOrdial == 1 }
                .sortedBy { it.startTime }

            busLocViewModel.busLocationsLiveData.observe(viewLifecycleOwner) { locationsList ->
                val activeSchedules = departuresList.filter { schedule ->
                    locationsList.any { location ->
                        location.voznjaBusId == schedule.startId
                    }
                }

                val adapter = TimelineAdapter(
                    requireContext(),
                    departuresList,
                    activeSchedules,
                    false,
                    null,
                    allSchedulesForLine
                )

                binding.rvSchedule.adapter = adapter
                binding.progressBar.visibility = ProgressBar.GONE
            }
        }

        if (!requireContext().isOnline()) {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        } else {
            scheduleViewModel.getScheduleList(s)
            busLocViewModel.getBusLocations()
        }
    }

    private fun fillScheduleStation(stationItem: Station) {
        binding.rvSchedule.layoutManager = LinearLayoutManager(requireContext())

        binding.tvTimelineTitle.text = getString(R.string.station_schedule)

        binding.tvActiveLegend.visibility = View.GONE

        binding.progressBar.visibility = ProgressBar.VISIBLE

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            busLocViewModel.busLocationsLiveData.observe(viewLifecycleOwner) { locationsList ->
                val stationSchedules = scheduleList.filter {
                    it.stationId == stationItem.id
                }.sortedBy { it.startTime }

                val activeSchedules = stationSchedules.filter { schedule ->
                    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                    schedule.startTime > currentTime
                }

                val adapter = TimelineAdapter(
                    requireContext(),
                    stationSchedules,
                    activeSchedules,
                    true,
                    stationItem.id,
                    scheduleList
                )

                binding.rvSchedule.adapter = adapter
                binding.progressBar.visibility = ProgressBar.GONE
            }
        }

        if (!requireContext().isOnline()) {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        } else {
            scheduleViewModel.getScheduleList(s)
            busLocViewModel.getBusLocations()
        }
    }
}
