package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.LineActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.LineStationsRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentLineStationsBinding
import com.example.rijekabusapp.helpers.getPreferantTimeFormat
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class LineStationsFragment : Fragment(), OnMapReadyCallback {

    private val busLocationViewModel: BusLocationViewModel by activityViewModels()
    private val stationsViewModel: StationsViewModel by activityViewModels()

    private lateinit var binding: FragmentLineStationsBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLineStationsBinding.inflate(inflater, container, false)
        val lineItem = arguments?.getSerializable(EXTRA_LINE) as? Line

        binding.googleMap.onCreate(savedInstanceState)
        setupCurrentSchedule(lineItem, savedInstanceState)

        return binding.root
    }

    private fun setupCurrentSchedule(lineItem: Line?, savedInstanceState: Bundle?) {
        scheduleViewModel = (requireActivity() as LineActivity).scheduleViewModel
        binding.rvLineStations.layoutManager = LinearLayoutManager(requireContext())

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            stationsViewModel.stationsList.observe(viewLifecycleOwner) { stationsList ->
                busLocationViewModel
                    .busLocationsLiveData.observe(viewLifecycleOwner) { busLocations ->
                        val filteredSchedules = scheduleList.filter { schedule ->
                            schedule.lineNumber == lineItem?.lineNumber &&
                                schedule.variantLineName == lineItem.name &&
                                busLocations.any { it.startId.toString() == schedule.startId }
                        }.sortedBy { it.startTime }

                        val nextStation = filteredSchedules.find { schedule ->
                            busLocations.any { it.nextStationId == schedule.stationId }
                        }

                        val adapter = LineStationsRecyclerAdapter(
                            requireContext(), filteredSchedules, stationsList, nextStation,
                            getPreferantTimeFormat(requireContext())
                        )
                        binding.rvLineStations.adapter = adapter

                        if (filteredSchedules.isEmpty()) {
                            binding.emptyState.setupEmptyStateView(
                                getString(R.string.no_buses_error_desc)
                            )
                            binding.ivMissing.visibility = View.VISIBLE
                            binding.emptyState.visibility = View.VISIBLE
                            binding.rvLineStations.visibility = View.GONE
                        } else {
                            for (busLoc in busLocations) {
                                if (busLoc.startId.toString() == filteredSchedules[0].startId) {
                                    latitude = busLoc.gpsX
                                    longitude = busLoc.gpsY
                                }
                            }
                            binding.googleMap.getMapAsync(this)
                        }

                        binding.progressBar.visibility = ProgressBar.GONE
                    }
                busLocationViewModel.getBusLocations()
            }
            stationsViewModel.getStationsList()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.googleMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.googleMap.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.googleMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.googleMap.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (latitude.toString()[0] == '0') {
            return
        }

        val markerOptions = MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title("Marker Title")
            .snippet("Marker Snippet")
        googleMap.addMarker(markerOptions)

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(12f)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}
