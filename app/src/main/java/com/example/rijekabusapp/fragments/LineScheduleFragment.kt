package com.example.rijekabusapp.fragments

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.GridAdapter
import com.example.rijekabusapp.databinding.FragmentLineScheduleBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.factory.ScheduleViewModelFactory

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
        val minItemWidth = resources.getDimensionPixelSize(R.dimen.schedule_item_min_width)
        val spanCount = calculateSpanCount(requireContext(), minItemWidth)

        binding.rvSchedule.layoutManager =
            GridLayoutManager(
                requireContext(),
                spanCount,
                GridLayoutManager.VERTICAL,
                false,
            )

        binding.rvSchedule.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateSpanCount(requireContext())
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            updateSpanCount(requireContext())
        }

        binding.progressBar.visibility = ProgressBar.VISIBLE
        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            busLocViewModel.busLocationsLiveData.observe(viewLifecycleOwner) { locationsList ->
                val filteredList =
                    scheduleList.filter {
                        it.variantLineName == lineItem.name &&
                            it.lineNumber == lineItem.lineNumber &&
                            it.direction == lineItem.direction &&
                            it.linVarId == lineItem.linVarId &&
                            it.variant == lineItem.variant &&
                            it.stationOrdial == 1
                    }.sortedBy { it.startTime }
                val adapter =
                    GridAdapter(
                        requireContext(),
                        filteredList,
                        filteredList.filter { schedule ->
                            locationsList.any { location ->
                                location.startId.toString() == schedule.startId
                            }
                        },
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
        val minItemWidth = resources.getDimensionPixelSize(R.dimen.schedule_item_min_width)
        val spanCount = calculateSpanCount(requireContext(), minItemWidth)

        binding.rvSchedule.layoutManager =
            GridLayoutManager(
                requireContext(),
                spanCount,
                GridLayoutManager.VERTICAL,
                false,
            )

        binding.rvSchedule.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateSpanCount(requireContext())
        }
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            updateSpanCount(requireContext())
        }

        binding.progressBar.visibility = ProgressBar.VISIBLE
        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            busLocViewModel.busLocationsLiveData.observe(viewLifecycleOwner) { locationsList ->
                val filteredList =
                    scheduleList.filter {
                        it.stationId == stationItem.id
                    }.sortedBy { it.startTime }
                val adapter =
                    GridAdapter(
                        requireContext(),
                        filteredList,
                        filteredList.filter { schedule ->
                            locationsList.any { location ->
                                location.startId.toString() == schedule.startId
                            }
                        },
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

    private fun updateSpanCount(context: Context) {
        val gridLayoutManager = binding.rvSchedule.layoutManager as GridLayoutManager
        val minItemWidth = resources.getDimensionPixelSize(R.dimen.schedule_item_min_width)
        val newSpanCount = calculateSpanCount(context, minItemWidth)
        if (gridLayoutManager.spanCount != newSpanCount) {
            gridLayoutManager.spanCount = newSpanCount
            binding.rvSchedule.layoutManager = gridLayoutManager
            binding.rvSchedule.requestLayout()
        }
    }

    private fun calculateSpanCount(
        context: Context,
        minItemWidth: Int,
    ): Int {
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        return screenWidth / minItemWidth
    }
}
