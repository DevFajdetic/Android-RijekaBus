package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.LineActivity
import com.example.rijekabusapp.MapsActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.LineStationsRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentLineStationsBinding
import com.example.rijekabusapp.databinding.LayoutBusRatingBinding
import com.example.rijekabusapp.databinding.LayoutChatBinding
import com.example.rijekabusapp.databinding.LayoutCommentsBinding
import com.example.rijekabusapp.helpers.getPreferantTimeFormat
import com.example.rijekabusapp.network.models.BusLocation
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Schedule
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import android.content.Intent

class LineStationsFragment : Fragment(), OnMapReadyCallback {
    private val busLocationViewModel: BusLocationViewModel by activityViewModels()

    private lateinit var binding: FragmentLineStationsBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var googleMap: GoogleMap? = null
    private var currentBusVoznjaBusId: String? = null
    private var lineItem: Line? = null
    private var currentBusMarker: Marker? = null
    private var recyclerViewState: Int = 0
    private var recyclerViewOffset: Int = 0
    private var isInitialMapSetup = true
    private var isMapReady = false
    private var pendingBusLocation: BusLocation? = null
    
    // Social features
    private lateinit var ratingBinding: LayoutBusRatingBinding
    private lateinit var commentsBinding: LayoutCommentsBinding
    private lateinit var chatBinding: LayoutChatBinding
    private lateinit var socialExtension: LineStationsSocialExtension

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLineStationsBinding.inflate(inflater, container, false)
        lineItem = arguments?.getSerializable(EXTRA_LINE) as? Line
        
        // Initialize view bindings for social features
        ratingBinding = LayoutBusRatingBinding.bind(binding.containerRating.root)
        commentsBinding = LayoutCommentsBinding.bind(binding.containerComments.root)
        chatBinding = LayoutChatBinding.bind(binding.containerChat.root)

        binding.googleMap.onCreate(savedInstanceState)
        
        // Initialize the map with a delay to ensure proper initialization
        Handler(Looper.getMainLooper()).postDelayed({
            binding.googleMap.getMapAsync(this)
        }, 300)
        
        // Setup map expand button
        binding.expandMapButton.setOnClickListener {
            openFullscreenMap()
        }
        
        setupCurrentSchedule(lineItem, savedInstanceState)

        return binding.root
    }
    
    private fun openFullscreenMap() {
        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(context, "Bus location not available yet", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get the current schedule data for this line
        val lineStations = scheduleViewModel.scheduleList.value?.filter { schedule ->
            schedule.lineNumber == lineItem?.lineNumber && 
            schedule.linVarId == lineItem?.linVarId
        }
        
        // Convert to ArrayList for passing through intent
        val stationIds = ArrayList<Int>()
        val stationNames = ArrayList<String>()
        val stationLatitudes = ArrayList<Double>()
        val stationLongitudes = ArrayList<Double>()
        
        lineStations?.forEach { schedule ->
            stationIds.add(schedule.stationOrdial)
            stationNames.add(schedule.name ?: "")
            stationLatitudes.add(schedule.gpsY ?: 0.0)
            stationLongitudes.add(schedule.gpsX ?: 0.0)
        }
        
        val intent = Intent(context, MapsActivity::class.java).apply {
            // Pass the current bus location and ID to MapsActivity
            putExtra("BUS_LATITUDE", latitude)
            putExtra("BUS_LONGITUDE", longitude)
            putExtra("BUS_ID", currentBusVoznjaBusId)
            putExtra("LINE_NUMBER", lineItem?.lineNumber)
            putExtra("LINE_VARIANT_ID", lineItem?.linVarId)
            putExtra("LINE_DIRECTION", lineItem?.direction)
            putExtra("FOLLOW_SPECIFIC_BUS", true)
            
            // Pass station data for this line
            putIntegerArrayListExtra("STATION_IDS", stationIds)
            putStringArrayListExtra("STATION_NAMES", stationNames)
            putExtra("STATION_LATITUDES", stationLatitudes.toDoubleArray())
            putExtra("STATION_LONGITUDES", stationLongitudes.toDoubleArray())
        }
        startActivity(intent)
    }

    private fun setupCurrentSchedule(
        lineItem: Line?,
        savedInstanceState: Bundle?,
    ) {
        scheduleViewModel = (requireActivity() as LineActivity).scheduleViewModel
        binding.rvLineStations.layoutManager = LinearLayoutManager(requireContext())

        // Find currently active bus by that line number and direction and show currently active schedule for it
        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            busLocationViewModel
                .busLocationsLiveData.observe(viewLifecycleOwner) { busLocations ->
                    val filteredSchedules =
                        scheduleList.filter { schedule ->
                            schedule.lineNumber == lineItem?.lineNumber &&
                                    schedule.linVarId == lineItem.linVarId &&
                                    busLocations.any { it.voznjaBusId == schedule.startId }
                        }.sortedBy { it.startTime }

                    if (filteredSchedules.isNotEmpty()) {
                        // Find the bus for this line
                        val busLocation = busLocations.find { busLoc ->
                            busLoc.voznjaBusId == filteredSchedules[0].startId
                        }

                        if (busLocation != null) {
                            updateStationsList(filteredSchedules, busLocation)
                            if (currentBusVoznjaBusId == null) {
                                updateBusLocationOnMap(busLocation)
                                // Store the current bus ID and subscribe to updates
                                currentBusVoznjaBusId = busLocation.voznjaBusId
                                busLocationViewModel.subscribeToSpecificBus(currentBusVoznjaBusId!!)
                                // Initialize social features when we have the bus ID
                                initSocialFeatures(currentBusVoznjaBusId!!, lineItem?.lineNumber ?: "")
                            }
                        }
                    } else {
                        showEmptyState()
                    }

                    binding.progressBar.visibility = ProgressBar.GONE
                }
            busLocationViewModel.getBusLocations()
        }

        // Listen for specific bus location updates
        busLocationViewModel.currentBusLocation.observe(viewLifecycleOwner) { busLocation ->
            if (busLocation != null && busLocation.voznjaBusId == currentBusVoznjaBusId) {
                updateBusLocationOnMap(busLocation)

                // Update the stations list with the new next station info
                scheduleViewModel.scheduleList.value?.let { scheduleList ->
                    val filteredSchedules = scheduleList.filter { schedule ->
                        schedule.lineNumber == lineItem?.lineNumber &&
                                schedule.variantLineName == lineItem.name &&
                                schedule.startId == currentBusVoznjaBusId
                    }.sortedBy { it.startTime }

                    // Save current scroll position before updating
                    val layoutManager = binding.rvLineStations.layoutManager as LinearLayoutManager
                    if (binding.rvLineStations.childCount > 0) {
                        recyclerViewState = layoutManager.findFirstVisibleItemPosition()
                        
                        // Also save the pixel offset of the first visible item
                        val firstVisibleView = binding.rvLineStations.getChildAt(0)
                        recyclerViewOffset = if (firstVisibleView != null) {
                            firstVisibleView.top
                        } else {
                            0
                        }
                    }
                    
                    updateStationsList(filteredSchedules, busLocation)
                }
            }
        }

        scheduleViewModel.getScheduleList(null)
    }
    
    private fun initSocialFeatures(busId: String, lineNumber: String) {
        // Create social extension with our view bindings
        socialExtension = LineStationsSocialExtension(
            this,
            binding,
            ratingBinding,
            commentsBinding,
            chatBinding
        )
        
        // Setup the social features
        socialExtension.setup(busId, lineNumber)
    }

    private fun updateBusLocationOnMap(busLocation: BusLocation) {
        latitude = busLocation.lat
        longitude = busLocation.lon

        if (!isMapReady) {
            // Save the location for when the map is ready
            pendingBusLocation = busLocation
            return
        }

        // Update marker on the map
        val position = LatLng(latitude, longitude)
        
        if (currentBusMarker == null) {
            // Create a new marker if one doesn't exist
            val markerOptions = MarkerOptions()
                .position(position)
                .title(lineItem?.lineNumber ?: "Bus")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            currentBusMarker = googleMap?.addMarker(markerOptions)
            
            // Only move camera on first setup
            if (isInitialMapSetup) {
                val cameraPosition = CameraPosition.Builder()
                    .target(position)
                    .zoom(15f)
                    .build()
                googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
                isInitialMapSetup = false
            }
        } else {
            // Update existing marker position
            currentBusMarker?.position = position
        }
    }

    private fun updateStationsList(
        filteredSchedules: List<Schedule>,
        busLocation: BusLocation
    ) {
        val nextStation = filteredSchedules.find { schedule ->
            busLocation.nextStationId == schedule.stationId
        }

        // Create and set the adapter
        val adapter = LineStationsRecyclerAdapter(
            requireContext(),
            filteredSchedules,
            nextStation,
            getPreferantTimeFormat(requireContext()),
            busLocation.distanceToNext
        )
        
        // Store the current adapter to compare items
        val oldAdapter = binding.rvLineStations.adapter as? LineStationsRecyclerAdapter
        
        // Only update if necessary
        if (oldAdapter == null || oldAdapter.itemCount != adapter.itemCount ||
            nextStation?.stationId != (oldAdapter.getNextStationId())) {
            
            binding.rvLineStations.adapter = adapter
            
            // Restore scroll position after updating adapter
            if (recyclerViewState > 0) {
                val layoutManager = binding.rvLineStations.layoutManager as LinearLayoutManager
                layoutManager.scrollToPositionWithOffset(recyclerViewState, recyclerViewOffset)
            }
        }

        binding.emptyState.visibility = View.GONE
        binding.ivMissing.visibility = View.GONE
        binding.rvLineStations.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        binding.emptyState.setupEmptyStateView(
            getString(R.string.no_buses_error_desc),
        )
        binding.ivMissing.visibility = View.VISIBLE
        binding.emptyState.visibility = View.VISIBLE
        binding.rvLineStations.visibility = View.GONE
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

        // Unsubscribe from specific bus when fragment is destroyed
        if (currentBusVoznjaBusId != null) {
            busLocationViewModel.unsubscribeFromSpecificBus()
        }
        
        // Cleanup social features
        if (::socialExtension.isInitialized) {
            socialExtension.cleanup()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.googleMap.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        
        try {
            // Try to set map style
            val success = googleMap?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(), R.raw.map_style
                )
            )
            if (success != true) {
                Log.e("LineStationsFragment", "Style parsing failed")
            }
        } catch (e: Exception) {
            Log.e("LineStationsFragment", "Can't find style. Error: ", e)
        }
        
        // Disable most user interactions with the map preview
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = true
            isScrollGesturesEnabled = true
            isMapToolbarEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
        }

        // If we have a pending bus location, update the map now
        pendingBusLocation?.let {
            updateBusLocationOnMap(it)
            pendingBusLocation = null
        }
    }
}
