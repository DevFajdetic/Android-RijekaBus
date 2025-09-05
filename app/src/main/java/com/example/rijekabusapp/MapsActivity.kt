package com.example.rijekabusapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.adapters.LineFilterAdapter
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.databinding.ActivityMapsBinding
import com.example.rijekabusapp.fragments.bottomsheet.RoutesListBottomSheet
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.helpers.PREF_METRIC
import com.example.rijekabusapp.helpers.PREF_SELECTED_LANGUAGE
import com.example.rijekabusapp.helpers.getBoolFromPreferences
import com.example.rijekabusapp.helpers.getStringFromPreferences
import com.example.rijekabusapp.network.models.BusLocation
import com.example.rijekabusapp.network.models.Route
import com.example.rijekabusapp.network.models.Schedule
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.network.models.Step
import com.example.rijekabusapp.network.response.DirectionsResponse
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.DirectionsViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.example.rijekabusapp.viewmodels.factory.ScheduleViewModelFactory
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    // Binding
    private lateinit var binding: ActivityMapsBinding

    // Map related
    private lateinit var googleMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private val busMarkers = mutableMapOf<Int, Marker>()
    private val stationMarkers = mutableMapOf<Int, Marker>()
    private var destination: String? = null // Destination coordinates
    private var origin: String? = null // Destination coordinates

    // Viewmodels
    private lateinit var busLocationViewModel: BusLocationViewModel
    private lateinit var stationsViewModel: StationsViewModel
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var directionsViewModel: DirectionsViewModel

    // WebSocket connection management
    private var isWebSocketConnected = false

    // My Locaton related
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var userLocation: LatLng? = null
    
    // Station filtering
    private val STATION_FILTER_RADIUS_KM = 2.0 // Show stations within 2km of user
    private var allStations = ArrayList<Station>()
    private var allSchedules = ArrayList<Schedule>()
    
    // Specific bus tracking
    private var followSpecificBus = false
    private var specificBusId: String? = null
    private var specificBusLatitude = 0.0
    private var specificBusLongitude = 0.0
    private var specificLineNumber: String? = null
    private var specificLineVariantId: Int? = null
    private var specificLineDirection: String? = null
    private var specificBusMarker: Marker? = null
    
    // Line stations data
    private var lineStationIds: ArrayList<Int>? = null
    private var lineStationNames: ArrayList<String>? = null
    private var lineStationLatitudes: DoubleArray? = null
    private var lineStationLongitudes: DoubleArray? = null
    private val lineStationMarkers = mutableListOf<Marker>()
    private var lineRoutePolyline: com.google.android.gms.maps.model.Polyline? = null
    
    // Line filtering
    private var isFilterContainerVisible = false
    private lateinit var lineFilterAdapter: LineFilterAdapter
    private var filteredLines = mutableListOf<Pair<String, String>>()
    private val allBusLocations = mutableListOf<BusLocation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings
        LanguageHelper.applyLanguage(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)

        try {
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)
            Log.d("MapsActivity", "Places API initialized successfully")
        } catch (e: Exception) {
            Log.e("MapsActivity", "Error initializing Places API: ${e.message}", e)
            showCustomSnackbar(this, binding.root, "Error initializing location search")
        }
        
        // Check for specific bus tracking intent
        followSpecificBus = intent.getBooleanExtra("FOLLOW_SPECIFIC_BUS", false)
        if (followSpecificBus) {
            specificBusId = intent.getStringExtra("BUS_ID")
            specificBusLatitude = intent.getDoubleExtra("BUS_LATITUDE", 0.0)
            specificBusLongitude = intent.getDoubleExtra("BUS_LONGITUDE", 0.0)
            specificLineNumber = intent.getStringExtra("LINE_NUMBER")
            specificLineVariantId = intent.getIntExtra("LINE_VARIANT_ID", -1)
            specificLineDirection = intent.getStringExtra("LINE_DIRECTION")
            
            // Get line station data if available
            lineStationIds = intent.getIntegerArrayListExtra("STATION_IDS")
            lineStationNames = intent.getStringArrayListExtra("STATION_NAMES")
            lineStationLatitudes = intent.getDoubleArrayExtra("STATION_LATITUDES")
            lineStationLongitudes = intent.getDoubleArrayExtra("STATION_LONGITUDES")
            
            // Set title to indicate tracking mode
            binding.tvTitle.text = getString(R.string.map) + " - " + getString(R.string.Line) + " ${specificLineNumber ?: ""}"
        }

        setOnClickListeners()

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // CURRENT LOCATION LOGIC
        binding.myLocation.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Request location updates to get the current location
                requestLocationUpdates()
            } else {
                // Request location permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE,
                )
            }
        }
        
        // Setup line filter button
        binding.btnLineFilter.setOnClickListener {
            toggleFilterContainer()
        }
        
        // Setup filter container buttons
        binding.btnApplyFilter.setOnClickListener {
            applyLineFilter()
        }
        
        binding.btnClearFilter.setOnClickListener {
            clearLineFilter()
        }

        mapFragment = (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)!!
        mapFragment.getMapAsync(this)

        busLocationViewModel = ViewModelProvider(this)[BusLocationViewModel::class.java]
        stationsViewModel = ViewModelProvider(this)[StationsViewModel::class.java]
        scheduleViewModel =
            ViewModelProvider(
                this, ScheduleViewModelFactory(application),
            )[ScheduleViewModel::class.java]
        directionsViewModel = ViewModelProvider(this)[DirectionsViewModel::class.java]
        
        // If following specific bus, hide the search container
        if (followSpecificBus) {
            binding.searchViewsContainer.visibility = View.GONE
            binding.fabMyLocation.visibility = View.GONE
        }
        
        // Setup RecyclerView for line filters
        setupLineFilterRecyclerView()
    }
    
    private fun setupLineFilterRecyclerView() {
        binding.rvLineFilters.layoutManager = LinearLayoutManager(this)
        
        // Initially set with empty data, will be populated when bus data is available
        lineFilterAdapter = LineFilterAdapter(this, emptyList(), emptyList())
        binding.rvLineFilters.adapter = lineFilterAdapter
    }
    
    private fun toggleFilterContainer() {
        isFilterContainerVisible = !isFilterContainerVisible
        
        if (isFilterContainerVisible) {
            binding.filterContainer.visibility = View.VISIBLE
            binding.searchViewsContainer.visibility = View.GONE
            
            // Update filter adapter with current bus lines
            updateLineFilterAdapter()
        } else {
            binding.filterContainer.visibility = View.GONE
        }
    }
    
    private fun updateLineFilterAdapter() {
        if (allBusLocations.isEmpty()) {
            showCustomSnackbar(this, binding.root, "No bus data available yet")
            return
        }
        
        // Extract unique line numbers and directions
        val lineData = allBusLocations.map { 
            Pair(it.brojLinije ?: "", it.smjer ?: "") 
        }.distinct().sortedBy { it.first }
        
        val lineNumbers = lineData.map { it.first }
        val lineDirections = lineData.map { it.second }
        
        // Create new adapter with the data
        lineFilterAdapter = LineFilterAdapter(this, lineNumbers, lineDirections)
        binding.rvLineFilters.adapter = lineFilterAdapter
        
        // Set current filter selections
        if (filteredLines.isNotEmpty()) {
            lineFilterAdapter.selectLines(filteredLines)
        }
    }
    
    private fun applyLineFilter() {
        // Get selected lines from adapter
        filteredLines = lineFilterAdapter.getSelectedLines().toMutableList()
        
        if (filteredLines.isEmpty()) {
            showCustomSnackbar(this, binding.root, "No lines selected")
            return
        }
        
        // Hide filter container
        binding.filterContainer.visibility = View.GONE
        isFilterContainerVisible = false
        
        // Update markers on map
        updateBusMarkersWithFilter(allBusLocations)
        
        // Show message
        val linesList = filteredLines.joinToString(", ") { "${it.first}${it.second}" }
        showCustomSnackbar(this, binding.root, "Filtered to lines: $linesList")
    }
    
    private fun clearLineFilter() {
        // Clear selections in adapter
        lineFilterAdapter.clearSelections()
        
        // Clear filtered lines
        filteredLines.clear()
        
        // Update markers on map with all buses
        updateBusMarkersWithFilter(allBusLocations)
        
        // Show message
        showCustomSnackbar(this, binding.root, "Filter cleared")
    }
    
    // Override attachBaseContext to apply language settings
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.createConfigurationContext(newBase))
    }
    
    // Apply language when resuming the activity
    override fun onResume() {
        super.onResume()
        LanguageHelper.applyLanguage(this)
        mapFragment.onResume()
        // Only reconnect to WebSocket if it's not already connected
        if (!isWebSocketConnected) {
            busLocationViewModel.connectToWebSocket()
        }
    }
    
    override fun onPause() {
        super.onPause()
        mapFragment.onPause()
    }

    private fun setOnClickListeners() {
        binding.destinationButton.setOnClickListener {
            if (binding.searchViewsContainer.isVisible) {
                binding.searchViewsContainer.visibility = View.GONE
            } else {
                binding.searchViewsContainer.visibility = View.VISIBLE
            }
        }

        val lang = if (getPreferantLanguage(this) == "English") "en" else "hr"
        val unit = if (getPreferantUnit(this)) "metric" else "imperial"

        binding.applyBtn.setOnClickListener {
            if (destination != null && origin != null) {
                directionsViewModel.getDirections(
                    destination!!,
                    origin!!,
                    "hr",
                    "transit",
                    "metric",
                )
                showCustomSnackbar(this, binding.root, "Getting directions..")
            } else if (destination == "" || destination == null) {
                showCustomSnackbar(this, binding.root, "Destination is empty")
            } else {
                showCustomSnackbar(this, binding.root, "Origin is empty")
            }
        }

        binding.fakeSearchOrigin.setOnClickListener {
            try {
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                val bounds =
                    RectangularBounds.newInstance(
                        LatLng(45.065559878032644, 14.148164040937912),
                        // Southwest bound
                        LatLng(45.56482545660713, 14.68100096259229),
                        // Northeast bound
                    )
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .setLocationRestriction(bounds)
                        .build(this)
                startActivityForResult(intent, AUTOCOMPLETE_ORIG_REQUEST_CODE)
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error starting origin search: ${e.message}", e)
                showCustomSnackbar(this, binding.root, "Error starting location search")
            }
        }

        binding.fakeSearchDestination.setOnClickListener {
            try {
                val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)
                val bounds =
                    RectangularBounds.newInstance(
                        LatLng(45.065559878032644, 14.148164040937912),
                        // Southwest bound
                        LatLng(45.56482545660713, 14.68100096259229),
                        // Northeast bound
                    )
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .setLocationRestriction(bounds)
                        .build(this)
                startActivityForResult(intent, AUTOCOMPLETE_DEST_REQUEST_CODE)
            } catch (e: Exception) {
                Log.e("MapsActivity", "Error starting destination search: ${e.message}", e)
                showCustomSnackbar(this, binding.root, "Error starting location search")
            }
        }
        
        binding.fabMyLocation.setOnClickListener {
            if (userLocation != null) {
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
                )
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    requestLocationUpdates()
                    showCustomSnackbar(this, binding.root, "Getting your location...")
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun openRouteListBottomSheet(steps: List<Step>?) {
        if (steps != null) {
            val bottomSheet = RoutesListBottomSheet(ArrayList(steps))
            bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
            bottomSheet.show(this.supportFragmentManager, "RouteList")
        } else {
            Toast.makeText(this, "Steps data is null", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location updates
                requestLocationUpdates()
            } else {
                Log.d("tag", "location denied")
                showCustomSnackbar(this, binding.root, "Location permission denied")
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == AUTOCOMPLETE_ORIG_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    try {
                        val place = Autocomplete.getPlaceFromIntent(data!!)
                        origin = "${place.latLng?.latitude},${place.latLng?.longitude}"
                        binding.svOrigin.text = place.name
                        Log.d("MapsActivity", "Origin selected: ${place.name}")
                    } catch (e: Exception) {
                        Log.e("MapsActivity", "Error processing origin result: ${e.message}", e)
                        showCustomSnackbar(this, binding.root, "Error processing location selection")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.e("MapsActivity", "Origin search error: ${status.statusMessage}")
                    showCustomSnackbar(this, binding.root, "Search error: ${status.statusMessage}")
                }
                RESULT_CANCELED -> {
                    Log.d("MapsActivity", "Origin search canceled by user")
                }
            }
        } else if (requestCode == AUTOCOMPLETE_DEST_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    try {
                        val place = Autocomplete.getPlaceFromIntent(data!!)
                        binding.svDestination.text = place.name
                        destination = "${place.latLng?.latitude},${place.latLng?.longitude}"
                        Log.d("MapsActivity", "Destination selected: ${place.name}")
                    } catch (e: Exception) {
                        Log.e("MapsActivity", "Error processing destination result: ${e.message}", e)
                        showCustomSnackbar(this, binding.root, "Error processing location selection")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    Log.e("MapsActivity", "Destination search error: ${status.statusMessage}")
                    showCustomSnackbar(this, binding.root, "Search error: ${status.statusMessage}")
                }
                RESULT_CANCELED -> {
                    Log.d("MapsActivity", "Destination search canceled by user")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        // Create the location request
        locationRequest =
            LocationRequest.create().apply {
                interval = 10000 // Interval for location updates
                fastestInterval = 5000 // Fastest interval for location updates
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

        // Create the location callback
        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult
                    for (location in locationResult.locations) {
                        // Get the current location coordinates
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        userLocation = currentLocation

                        // Update the origin EditText with the current location
                        binding.svOrigin.text =
                            "${currentLocation.latitude},${currentLocation.longitude}"
                        origin = "${currentLocation.latitude},${currentLocation.longitude}"

                        // Stop location updates as we have obtained the current location
                        stopLocationUpdates()
                    }
                }
            }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are satisfied, start location updates
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show a dialog to prompt the user to enable location services
                    exception.startResolutionForResult(
                        this@MapsActivity,
                        LOCATION_SETTINGS_REQUEST_CODE,
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("tag", sendEx.toString())
                }
            } else {
                Log.d("tag", exception.toString())
            }
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
        private const val LOCATION_SETTINGS_REQUEST_CODE = 456
        private const val AUTOCOMPLETE_ORIG_REQUEST_CODE = 551
        private const val AUTOCOMPLETE_DEST_REQUEST_CODE = 552
    }

    override fun onStart() {
        super.onStart()
        mapFragment.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapFragment.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment.onDestroy()
        // Ensure WebSocket is disconnected when activity is destroyed
        busLocationViewModel.disconnectWebSocket()
        // Make sure to unsubscribe from specific bus updates
        if (followSpecificBus && specificBusId != null) {
            busLocationViewModel.unsubscribeFromSpecificBus()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        try {
            // Try to set map style
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if (!success) {
                Log.e("MapsActivity", "Style parsing failed")
            }
        } catch (e: Exception) {
            Log.e("MapsActivity", "Can't find style. Error: ", e)
        }
        
        // Enable location button if we have permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
        
        // Check if we're in specific bus tracking mode
        if (followSpecificBus && specificBusLatitude != 0.0 && specificBusLongitude != 0.0) {
            // Set initial map position to the bus location
            val busPosition = LatLng(specificBusLatitude, specificBusLongitude)
            
            // Add line stations if available
            if (lineStationLatitudes != null && lineStationLongitudes != null && 
                lineStationLatitudes!!.isNotEmpty() && lineStationNames != null) {
                
                // Draw the line route connecting all stations
                drawLineRoute()
                
                // Add markers for all stations on this line
                addLineStationMarkers()
                
                // Calculate bounds to show all stations and the bus
                val boundsBuilder = LatLngBounds.builder()
                boundsBuilder.include(busPosition)
                
                // Add all station positions to bounds
                for (i in 0 until (lineStationLatitudes?.size ?: 0)) {
                    val stationLat = lineStationLatitudes!![i]
                    val stationLng = lineStationLongitudes!![i]
                    boundsBuilder.include(LatLng(stationLat, stationLng))
                }
                
                // Zoom to show all markers with padding
                try {
                    val bounds = boundsBuilder.build()
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
                } catch (e: Exception) {
                    // Fallback if bounds calculation fails
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busPosition, 14f))
                }
            } else {
                // Just zoom to bus if no station data
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(busPosition, 15f))
            }
            
            // Add initial marker for the bus
            val markerOptions = MarkerOptions()
                .position(busPosition)
                .title("Line ${specificLineNumber ?: ""}")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            specificBusMarker = googleMap.addMarker(markerOptions)
            
            // Subscribe to updates for this specific bus
            if (specificBusId != null) {
                showCustomSnackbar(this, binding.root, "Tracking bus ${specificLineNumber ?: ""}")
                busLocationViewModel.subscribeToSpecificBus(specificBusId!!)
            }
            
            // Observe the specific bus location updates
            busLocationViewModel.currentBusLocation.observe(this) { busLocation ->
                if (busLocation != null && busLocation.voznjaBusId == specificBusId) {
                    // Update marker position
                    val newPosition = LatLng(busLocation.lat, busLocation.lon)
                    specificBusMarker?.position = newPosition
                    
                    // Smoothly move camera to follow the bus
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLng(newPosition)
                    )
                }
            }
        } else {
            // Set initial map position to Rijeka, Croatia
            val rijeka = LatLng(45.3271, 14.4422)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rijeka, 14f))

            // Load data in background
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    setupUserLocation()
                }
            }

            // Load data in parallel
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    scheduleViewModel.getScheduleList(null)
                    stationsViewModel.getStationsList()
                }
            }
            
            // Connect to WebSocket for real-time bus location updates
            showCustomSnackbar(this, binding.root, "Loading bus locations...")
            busLocationViewModel.connectToWebSocket()
            
            // Observe WebSocket connection status
            busLocationViewModel.isWebSocketConnected.observe(this) { connected ->
                isWebSocketConnected = connected
                if (connected) {
                    Log.d("MapsActivity", "WebSocket connected successfully")
                    showCustomSnackbar(this, binding.root, "Real-time bus tracking connected")
                } else {
                    Log.d("MapsActivity", "WebSocket disconnected")
                    showCustomSnackbar(this, binding.root, "Real-time connection lost")
                }
            }

            // Observe bus location updates from WebSocket
            busLocationViewModel.busLocationsLiveData.observe(this) { busLocations ->
                // Store all bus locations for filtering
                allBusLocations.clear()
                allBusLocations.addAll(busLocations)
                
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        updateBusMarkersWithFilter(busLocations)
                    }
                }
            }
            
            // Observe schedule and station data
            scheduleViewModel.scheduleList.observe(this) { scheduleList ->
                allSchedules = scheduleList
                stationsViewModel.stationsList.observe(this) { stationsList ->
                    allStations = stationsList
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            updateStationMarkers()
                        }
                    }
                }
            }
            
            directionsViewModel.directionsLiveData.observe(this) { directions ->
                if (directions != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            drawRouteOnMap(directions.routes.firstOrNull()?.overviewPolyline?.points, directions)
                        }
                    }
                }
                if (directions != null) {
                    openRouteListBottomSheet(directions.routes.firstOrNull()?.legs?.firstOrNull()?.steps)
                }
            }
        }
    }

    private fun drawRouteOnMap(
        polyline: String?,
        response: DirectionsResponse,
    ) {
        lifecycleScope.launch {
            polyline?.let {
                // Process data on background thread
                val routeData = withContext(Dispatchers.IO) {
                    val decodedPath = PolyUtil.decode(polyline)
                    val latLngList = decodedPath.map { LatLng(it.latitude, it.longitude) }
                    var busLine = "0"
                    var routeType = "WALK" // Default to WALK

                    response.routes.firstOrNull()?.let { route ->
                        val origin = route.legs.firstOrNull()?.startAddress ?: ""
                        val destination = route.legs.firstOrNull()?.endAddress ?: ""
                        val distance = route.legs.firstOrNull()?.distance?.text ?: ""
                        val duration = route.legs.firstOrNull()?.duration?.text ?: ""
                        val startTime = route.legs.firstOrNull()?.depTime?.text ?: ""
                        val endTime = route.legs.firstOrNull()?.arrivalTime?.text ?: ""
                        val steps = route.legs.firstOrNull()?.steps ?: emptyList()

                        // Determine route type based on steps
                        var hasBus = false
                        var hasWalk = false
                        
                        // Check if transit mode was used
                        route.legs.forEach { leg ->
                            leg.steps.forEach {
                                if (it.travelMode == "TRANSIT") {
                                    busLine = it.transitDetails!!.line?.shortName ?: "0"
                                    hasBus = true
                                } else if (it.travelMode == "WALKING") {
                                    hasWalk = true
                                }
                            }
                        }
                        
                        // Set route type based on transportation modes
                        routeType = when {
                            hasBus && hasWalk -> "MIXED"
                            hasBus -> "BUS"
                            else -> "WALK"
                        }

                        // Create a Route object with the extracted information
                        val routeInfo = FavoriteRoute(
                            origin, startTime, destination, endTime, distance, duration, busLine,
                            getCurrentDateTime(), distance.hashCode().toString(),
                            routeType, steps
                        )

                        // Save the route information to the database
                        directionsViewModel.saveRouteInformation(routeInfo)

                        RouteData(
                            latLngList = latLngList,
                            busLine = busLine,
                            route = route,
                            routeType = routeType
                        )
                    }
                }
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    routeData?.let { data ->
                        val message = when (data.routeType) {
                            "BUS" -> "Bus route saved: Line ${data.busLine}"
                            "WALK" -> "Walking route saved"
                            else -> "Mixed route saved: Line ${data.busLine}"
                        }
                        showCustomSnackbar(this@MapsActivity, binding.root, message)
                        
                        // Draw route polylines
                        for (leg in data.route.legs) {
                            for (step in leg.steps) {
                                val stepPolyline = step.polyline.points
                                val stepLatLngList = PolyUtil.decode(stepPolyline)
                                    .map { LatLng(it.latitude, it.longitude) }

                                // Draw dotted line for steps and solid line for bus segments
                                val stepPolylineOptions = PolylineOptions()
                                    .addAll(stepLatLngList)
                                    .width(15f)
                                    .color(if (step.travelMode == "TRANSIT") Color.BLUE else Color.CYAN)
                                    .pattern(
                                        if (step.travelMode == "TRANSIT") {
                                            null
                                        } else {
                                            listOf(Dash(30f), Gap(20f))
                                        },
                                    )
                                googleMap.addPolyline(stepPolylineOptions)
                            }
                        }

                        val builder = LatLngBounds.builder()
                        data.latLngList.forEach { builder.include(it) }
                        val bounds = builder.build()
                        val padding = 100 // Padding around the route (in pixels)

                        // Move camera to show the entire route
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
                    }
                }
            }
        }
    }

    private data class RouteData(
        val latLngList: List<LatLng>,
        val busLine: String,
        val route: Route,
        val routeType: String
    )

    private fun setupUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { 
                    userLocation = LatLng(location.latitude, location.longitude)
                    showUserLocationOnMap(location)
                    
                    // Zoom to user location on main thread
                    lifecycleScope.launch {
                        withContext(Dispatchers.Main) {
                            googleMap.animateCamera(
                                CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f)
                            )
                        }
                    }
                }
            }.addOnFailureListener { exception: Exception ->
                Toast.makeText(
                    this,
                    "Failed to retrieve user location: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE,
            )
        }
    }

    private fun showUserLocationOnMap(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)

        val markerOptions =
            MarkerOptions()
                .position(userLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(180f))

        googleMap.addMarker(markerOptions)
    }

    private fun updateStationMarkers() {
        lifecycleScope.launch {
            // Process data on background thread
            val stationData = withContext(Dispatchers.IO) {
                // Prepare station data for all stations
                allStations.map { station ->
                    val direction = findDirection(station, allSchedules)
                    val stationPosition = LatLng(station.gpsY, station.gpsX)
                    
                    StationMarkerData(
                        id = station.id,
                        position = stationPosition,
                        title = station.shortName,
                        direction = direction
                    )
                }
            }
            
            // Update UI on main thread
            withContext(Dispatchers.Main) {
                // Remove previous station markers
                stationMarkers.values.forEach { marker -> marker.remove() }
                stationMarkers.clear()

                // Add markers for all stations
                for (stationData in stationData) {
                    val markerOptions = MarkerOptions()
                        .position(stationData.position)
                        .title(stationData.title)
                        .icon(
                            BitmapDescriptorFactory.fromBitmap(
                                createStationMarkerIcon(this@MapsActivity, stationData.direction)
                            )
                        )
                        .zIndex(1f) // Station markers behind

                    val marker = googleMap.addMarker(markerOptions)
                    stationMarkers[stationData.id] = marker!!
                }
            }
        }
    }
    
    private data class StationMarkerData(
        val id: Int,
        val position: LatLng,
        val title: String,
        val direction: String
    )

    private fun findDirection(
        station: Station,
        scheduleList: ArrayList<Schedule>,
    ): String {
        val matchingSchedules = scheduleList.filter { it.stationId == station.id }
        if (matchingSchedules.isNotEmpty()) {
            return matchingSchedules.first().direction
        }
        return "A"
    }

    private fun updateBusMarkersWithFilter(busLocations: List<BusLocation>) {
        lifecycleScope.launch {
            // Process data on background thread
            val markerData = withContext(Dispatchers.IO) {
                // Filter bus locations if filters are active
                val filteredBusLocations = if (filteredLines.isNotEmpty()) {
                    busLocations.filter { busLocation ->
                        val lineNumber = busLocation.brojLinije ?: ""
                        val direction = busLocation.smjer ?: ""
                        filteredLines.any { it.first == lineNumber && it.second == direction }
                    }
                } else {
                    busLocations
                }
                
                filteredBusLocations.map { busLocation ->
                    val busPosition = LatLng(busLocation.lat, busLocation.lon)
                    val lineNumber = busLocation.brojLinije ?: ""
                    val direction = busLocation.smjer ?: ""
                    val variantName = busLocation.nazivVarijanteLinije ?: ""
                    
                    Triple(busLocation.gbr, busPosition, MarkerData(
                        title = variantName.ifEmpty { "Bus ${lineNumber}" },
                        lineNumber = lineNumber,
                        direction = direction
                    ))
                }
            }
            
            // Update UI on main thread
            withContext(Dispatchers.Main) {
                // Get current bus IDs
                val currentBusIds = busMarkers.keys.toSet()
                val newBusIds = markerData.map { it.first }.toSet()
                
                // Remove markers for buses that are no longer present
                val busesToRemove = currentBusIds - newBusIds
                busesToRemove.forEach { busId ->
                    busMarkers[busId]?.remove()
                    busMarkers.remove(busId)
                }
                
                // Update or create markers for each bus
                for ((gbr, position, markerData) in markerData) {
                    val existingMarker = busMarkers[gbr]
                    
                    if (existingMarker != null) {
                        // Update existing marker position and info
                        existingMarker.position = position
                        existingMarker.title = markerData.title
                    } else {
                        // Create new marker
                        val markerOptions = MarkerOptions()
                            .position(position)
                            .title(markerData.title)
                            .icon(
                                BitmapDescriptorFactory.fromBitmap(
                                    createLineMarkerIcon(
                                        this@MapsActivity,
                                        markerData.lineNumber,
                                        markerData.direction,
                                    ),
                                ),
                            )
                            .zIndex(2f) // Bus markers in front

                        val marker = googleMap.addMarker(markerOptions)
                        busMarkers[gbr] = marker!!
                    }
                }
            }
        }
    }
    
    private data class MarkerData(
        val title: String,
        val lineNumber: String,
        val direction: String
    )

    private fun drawLineRoute() {
        if (lineStationLatitudes == null || lineStationLongitudes == null || 
            lineStationLatitudes!!.isEmpty()) {
            return
        }
        
        // Create list of LatLng points for the polyline
        val routePoints = mutableListOf<LatLng>()
        for (i in 0 until lineStationLatitudes!!.size) {
            val stationLat = lineStationLatitudes!![i]
            val stationLng = lineStationLongitudes!![i]
            routePoints.add(LatLng(stationLat, stationLng))
        }
        
        // Get line color based on line number
        val lineColor = getColorForLine(specificLineNumber)
        
        // Create polyline options
        val polylineOptions = PolylineOptions()
            .addAll(routePoints)
            .width(10f)
            .color(lineColor)
            .geodesic(true)
        
        // Add the polyline to the map
        lineRoutePolyline = googleMap.addPolyline(polylineOptions)
    }
    
    private fun getColorForLine(lineNumber: String?): Int {
        // Generate a consistent color based on line number
        return when (lineNumber) {
            "1" -> Color.parseColor("#FF5722") // Deep Orange
            "2" -> Color.parseColor("#2196F3") // Blue
            "3" -> Color.parseColor("#4CAF50") // Green
            "4" -> Color.parseColor("#9C27B0") // Purple
            "5" -> Color.parseColor("#FFC107") // Amber
            "6" -> Color.parseColor("#795548") // Brown
            "7" -> Color.parseColor("#607D8B") // Blue Grey
            "8" -> Color.parseColor("#E91E63") // Pink
            "9" -> Color.parseColor("#00BCD4") // Cyan
            else -> {
                // Generate a color based on hash of line number
                val hash = lineNumber?.hashCode() ?: 0
                val hue = (hash % 360).toFloat()
                val hsv = floatArrayOf(hue, 0.8f, 0.8f)
                Color.HSVToColor(hsv)
            }
        }
    }
    
    private fun addLineStationMarkers() {
        if (lineStationLatitudes == null || lineStationLongitudes == null || 
            lineStationNames == null || lineStationLatitudes!!.isEmpty()) {
            return
        }
        
        // Clear any existing line station markers
        lineStationMarkers.forEach { it.remove() }
        lineStationMarkers.clear()
        
        // Add markers for each station
        for (i in 0 until lineStationLatitudes!!.size) {
            val stationLat = lineStationLatitudes!![i]
            val stationLng = lineStationLongitudes!![i]
            val stationName = lineStationNames!![i]
            val stationId = if (lineStationIds != null && i < lineStationIds!!.size) {
                lineStationIds!![i]
            } else {
                -1
            }
            
            // Create a custom marker for line stations
            val markerOptions = MarkerOptions()
                .position(LatLng(stationLat, stationLng))
                .title(stationName)
                .icon(BitmapDescriptorFactory.fromBitmap(
                    createLineStationMarkerIcon(
                        this, 
                        specificLineNumber ?: "", 
                        specificLineDirection ?: "A",
                        stationId
                    )
                ))
            
            // Add marker to map and store reference
            val marker = googleMap.addMarker(markerOptions)
            if (marker != null) {
                lineStationMarkers.add(marker)
            }
        }
    }
    
    private fun createLineStationMarkerIcon(
        context: Context,
        lineNumber: String,
        lineDirection: String,
        stationId: Int
    ): Bitmap {
        val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_station_size)
        val bitmap = createBitmap(markerSize, markerSize)
        val canvas = Canvas(bitmap)

        // Draw circle background with line-specific color
        val paint = Paint()
        paint.color = getColorForLine(lineNumber)
        
        // Draw outer circle
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)
        
        // Draw inner circle in white
        paint.color = Color.WHITE
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 3f, paint)
        
        // Draw station number or symbol
        paint.color = Color.BLACK
        paint.textSize = context.resources.getDimensionPixelSize(R.dimen.marker_station_text_size).toFloat()
        paint.textAlign = Paint.Align.CENTER
        val x = markerSize / 2f
        val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
        
        // Use a short identifier for the station
        val stationText = if (stationId > 0) {
            (stationId % 100).toString()
        } else {
            "•"
        }
        
        canvas.drawText(stationText, x, y, paint)

        return bitmap
    }
}

@SuppressLint("RestrictedApi")
fun showCustomSnackbar(
    ctx: Context,
    view: View,
    message: String,
) {
    val snackbar = Snackbar.make(view, "", Snackbar.LENGTH_SHORT)
    snackbar.view.setBackgroundColor(Color.TRANSPARENT)
    val snl = snackbar.view as Snackbar.SnackbarLayout

    val inflater = LayoutInflater.from(ctx)
    val customSnackbarView = inflater.inflate(R.layout.custom_snackbar, null)

    customSnackbarView.findViewById<TextView>(R.id.snackbar_text).apply {
        text = message
    }
    customSnackbarView.findViewById<ImageButton>(R.id.snackbar_dismiss).setOnClickListener {
        snackbar.dismiss()
    }

    // Add bottom margin to the view
    val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(0, 0, 0, 75)
    view.layoutParams = layoutParams

    snl.addView(customSnackbarView)
    snackbar.show()
}

fun getPreferantLanguage(ctx: Context): String {
    return getStringFromPreferences(
        PREF_SELECTED_LANGUAGE,
        "English",
        ctx,
    )
}

fun getPreferantUnit(ctx: Context): Boolean {
    return getBoolFromPreferences(PREF_METRIC, true, ctx)
}

fun compareWithCurrentTime(time: LocalTime): Int {
    val currentTime = LocalTime.now()
    println(currentTime)
    return when {
        time.isBefore(currentTime) -> -1
        time.isAfter(currentTime) -> 1
        else -> 0
    }
}

fun createStationMarkerIcon(
    context: Context,
    stationDirection: String,
): Bitmap {
    val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_station_size)
    val bitmap = createBitmap(markerSize, markerSize)
    val canvas = Canvas(bitmap)

    // Draw circle background
    val paint = Paint()
    if (stationDirection == "A") {
        paint.color = ContextCompat.getColor(context, R.color.greenish)
    } else {
        paint.color = ContextCompat.getColor(context, R.color.fade_blue)
    }

    canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)

    // Draw line number
    paint.color = ContextCompat.getColor(context, R.color.black)
    paint.textSize =
        context.resources
            .getDimensionPixelSize(R.dimen.marker_station_text_size).toFloat()
    paint.textAlign = Paint.Align.CENTER
    val x = markerSize / 2f
    val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(stationDirection, x, y, paint)

    return bitmap
}

fun createLineMarkerIcon(
    context: Context,
    lineNumber: String,
    lineDirection: String,
): Bitmap {
    val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_size)
    val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Draw circle background
    val paint = Paint()
    if (lineDirection == "A") {
        paint.color = ContextCompat.getColor(context, R.color.wave)
    } else {
        paint.color = ContextCompat.getColor(context, R.color.some)
    }

    canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)

    // Draw line number
    paint.color = ContextCompat.getColor(context, R.color.black)
    paint.textSize = context.resources.getDimensionPixelSize(R.dimen.marker_text_size).toFloat()
    paint.textAlign = Paint.Align.CENTER
    val x = markerSize / 2f
    val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(lineNumber, x, y, paint)

    return bitmap
}

fun getCurrentDateTime(): String {
    val croatianLocale = Locale.ENGLISH
    val croatianZoneId = ZoneId.of("Europe/Zagreb")

    val currentDateTime = ZonedDateTime.now(croatianZoneId)
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", croatianLocale)
    return currentDateTime.format(formatter).toUpperCase(croatianLocale)
}
