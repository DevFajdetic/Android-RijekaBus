package com.example.rijekabusapp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.rijekabusapp.databinding.ActivityMapsBinding
import com.example.rijekabusapp.helpers.LOCATION_PERMISSION_REQUEST_CODE
import com.example.rijekabusapp.helpers.USER_LOCATION_MARKER_TITLE
import com.example.rijekabusapp.helpers.createLineMarkerIcon
import com.example.rijekabusapp.helpers.createStationMarkerIcon
import com.example.rijekabusapp.network.models.BusLocation
import com.example.rijekabusapp.network.models.Schedule
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.example.rijekabusapp.viewmodels.factory.ScheduleViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var googleMap: GoogleMap

    private lateinit var busLocationViewModel: BusLocationViewModel
    private lateinit var stationsViewModel: StationsViewModel
    private lateinit var scheduleViewModel: ScheduleViewModel

    private lateinit var mapFragment: SupportMapFragment
    private val busMarkers = mutableMapOf<Int, Marker>()
    private val stationMarkers = mutableMapOf<Int, Marker>()

    // Threading
    private val busLocationHandler = Handler()
    private lateinit var busLocationRunnable: Runnable

    private val BUS_LOCATION_UPDATE_INTERVAL = 50000L

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        busLocationViewModel = ViewModelProvider(this)[BusLocationViewModel::class.java]
        stationsViewModel = ViewModelProvider(this)[StationsViewModel::class.java]
        scheduleViewModel = ViewModelProvider(
            this, ScheduleViewModelFactory(application)
        )[ScheduleViewModel::class.java]

        mapFragment = (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)!!
        mapFragment.getMapAsync(this)
    }

    private fun startFetchingBusLocations() {
        busLocationRunnable = Runnable {
            busLocationViewModel.getBusLocations()
            busLocationHandler.postDelayed(busLocationRunnable, BUS_LOCATION_UPDATE_INTERVAL)
        }
        busLocationHandler.post(busLocationRunnable)
    }

    override fun onStart() {
        super.onStart()
        mapFragment.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapFragment.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapFragment.onResume()
        startFetchingBusLocations()
    }

    override fun onPause() {
        super.onPause()
        mapFragment.onPause()
        busLocationHandler.removeCallbacks(busLocationRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapFragment.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapFragment.onLowMemory()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Set initial map position to Rijeka, Croatia
        val rijeka = LatLng(45.3271, 14.4422)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rijeka, 12f))

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                setupUserLocation()
            }
        }

        scheduleViewModel.getScheduleList()
        busLocationViewModel.getBusLocations()
        stationsViewModel.getStationsList()
        busLocationViewModel.busLocationsLiveData.observe(this) { busLocations ->
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    setupUserLocation()
                }
            }
            scheduleViewModel.scheduleList.observe(this) { scheduleList ->
                stationsViewModel.stationsList.observe(this) { stationsList ->
                    updateBusMarkers(busLocations, scheduleList, stationsList)
                    updateStationMarkers(stationsList, scheduleList)
                }
            }
        }
    }

    private suspend fun setupUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { showUserLocationOnMap(it) }
            }.addOnFailureListener { exception: Exception ->
                Toast.makeText(
                    this,
                    "Failed to retrieve user location: ${exception.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showUserLocationOnMap(location: Location) {
        val userLatLng = LatLng(location.latitude, location.longitude)

        val markerOptions = MarkerOptions()
            .position(userLatLng)
            .title(USER_LOCATION_MARKER_TITLE)
            .icon(BitmapDescriptorFactory.defaultMarker(180f))

        googleMap.addMarker(markerOptions)
    }

    private fun updateStationMarkers(
        stationsList: ArrayList<Station>,
        scheduleList: ArrayList<Schedule>
    ) {
        stationMarkers.values.forEach { marker -> marker.remove() }
        stationMarkers.clear()

        for (station in stationsList) {
            val direction = findDirection(station, scheduleList)
            val stationPosition = LatLng(
                (station.gpsY),
                (station.gpsX)
            )
            val markerOptions = MarkerOptions()
                .position(stationPosition)
                .title(station.shortName)
                .snippet(station.longName)
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createStationMarkerIcon(
                            this,
                            direction
                        )
                    )
                )

            val marker = googleMap.addMarker(markerOptions)
            stationMarkers[station.id] = marker!!
        }
    }

    private fun findDirection(station: Station, scheduleList: ArrayList<Schedule>): String {
        val matchingSchedules = scheduleList.filter { it.stationId == station.id }
        if (matchingSchedules.isNotEmpty()) {
            return matchingSchedules.first().direction
        }
        return "A"
    }

    private fun updateBusMarkers(
        busLocations: List<BusLocation>,
        scheduleList: List<Schedule>,
        stationList: List<Station>
    ) {
        // Remove previous markers
        busMarkers.values.forEach { marker -> marker.remove() }
        busMarkers.clear()

        // Add new markers
        for (busLocation in busLocations) {
            val ran = Math.random() / 10000
            val bus = findBus(busLocation, scheduleList)
            val station = findStation(busLocation, stationList)

            val busPosition = LatLng(
                (station?.gpsY?.plus(ran) ?: 45.3271),
                (station?.gpsX?.plus(ran) ?: 14.4422)
            )
            val markerOptions = MarkerOptions()
                .position(busPosition)
                .title(bus?.variantLineName ?: getString(R.string.departure_failed))
                .snippet(
                    busLocation.provideTime + " " + busLocation.busId + " " +
                        busLocation.nextStationId
                )
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        createLineMarkerIcon(
                            this,
                            bus?.lineNumber ?: "",
                            bus?.direction ?: ""
                        )
                    )
                )

            val marker = googleMap.addMarker(markerOptions)
            busMarkers[busLocation.busId.toInt()] = marker!!
        }
    }

    private fun findStation(busLocation: BusLocation, stationList: List<Station>): Station? {
        val matchingStation = stationList.filter { it.id == busLocation.nextStationId }
        if (matchingStation.isNotEmpty()) {
            return matchingStation.first()
        }
        return null
    }

    private fun findBus(busLocation: BusLocation, scheduleList: List<Schedule>): Schedule? {
        val matchingSchedules = scheduleList.filter {
            it.startId == busLocation.startId.toString()
        }
        if (matchingSchedules.isNotEmpty()) {
            return matchingSchedules.first()
        }
        return null
    }
}
