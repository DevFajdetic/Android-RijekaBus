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
import com.example.rijekabusapp.databinding.FragmentStationLinesBinding
import com.example.rijekabusapp.helpers.compareWithCurrentTime
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.helpers.stringToTime
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StationLinesFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentStationLinesBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var stationItem: Station

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStationLinesBinding.inflate(inflater, container, false)
        // Watch out here
        stationItem = (arguments?.getSerializable(EXTRA_STATION) as? Station)!!

        binding.googleMap.onCreate(savedInstanceState)
        latitude = stationItem.gpsY
        longitude = stationItem.gpsX
        binding.googleMap.getMapAsync(this)
        setupCurrentSchedule(stationItem, savedInstanceState)

        return binding.root
    }

    private fun setupCurrentSchedule(stationItem: Station?, savedInstanceState: Bundle?) {
        binding.progressBar.visibility = ProgressBar.VISIBLE

        scheduleViewModel = (requireActivity() as StationActivity).scheduleViewModel
        binding.rvStationLines.layoutManager = LinearLayoutManager(requireContext())

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            val filteredStations = scheduleList.filter {
                it.stationId == stationItem?.id &&
                    compareWithCurrentTime(stringToTime(it.startTime)) >= 0
            }

            val adapter = StationLinesRecyclerAdapter(
                requireContext(),
                filteredStations,
                true
            )
            binding.rvStationLines.adapter = adapter
            binding.progressBar.visibility = ProgressBar.GONE

            if (filteredStations.isEmpty()) {
                binding.emptyState.setupEmptyStateView(getString(R.string.no_stations_error_desc))
                binding.ivMissing.visibility = View.VISIBLE
                binding.emptyState.visibility = View.VISIBLE
                binding.rvStationLines.visibility = View.GONE
            }
        }

        if (!requireContext().isOnline()) {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
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
        if (latitude.toString()[0].toString() == "0") {
            return
        }

        val markerOptions = MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title(stationItem.longName)
        googleMap.addMarker(markerOptions)

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(16f)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
}
