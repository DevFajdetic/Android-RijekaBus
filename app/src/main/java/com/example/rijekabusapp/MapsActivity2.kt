package com.example.rijekabusapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.rijekabusapp.databinding.ActivityMapsBinding
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.network.response.Re
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class MapsActivity2 : AppCompatActivity() {
    // Binding
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    private val busLocationViewModel: BusLocationViewModel by viewModels()
    private val stationsViewModel: StationsViewModel by viewModels()
    private val tag = "MapsActivity2"

    private val busMarkers = mutableMapOf<Int, Pair<Marker, Long>>()
    private val stationMarkers = mutableMapOf<Int, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)

        // Initialize the OSMDroid configuration
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        // Initialize the OSMDroid map
        setContentView(R.layout.activity_maps)
        mapView = findViewById(R.id.map)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(15.0)
        val startPoint = GeoPoint(45.3271, 14.4422)
        mapView.controller.setCenter(startPoint)

        // Setup my location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mapView)
            locationOverlay.enableMyLocation() // Enable location overlay
            mapView.overlays.add(locationOverlay)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION,
            )
        }

        // Set up the FAB for centering the map on the user's location
        binding.fabMyLocation.setOnClickListener {
            if (locationOverlay.myLocation != null) {
                val userLocation =
                    GeoPoint(
                        locationOverlay.myLocation.latitude,
                        locationOverlay.myLocation.longitude,
                    )
                mapView.controller.animateTo(userLocation, 15.0, 2000)
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Load data - bus stop markers, bus locations
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                setupUserLocation()
                loadData()
            }
        }
    }

    private fun loadData() {
        // For getting buses current location we connect to a web socket
        busLocationViewModel.connectToWebSocket()
        // Start a thread for updaitng markers
        lifecycleScope.launch(Dispatchers.IO) {
            stationsViewModel.getStationsList()
            withContext(Dispatchers.Main) {
                stationsViewModel.stationsList.observe(this@MapsActivity2) { stationsList ->
                    updateStationMarkers(stationsList) // Update UI with the station list
                }
                busLocationViewModel.busLocationsLiveData2.observe(this@MapsActivity2) {
                        busLocations ->
                    updateBusMarkers(busLocations) // Update UI with the latest bus locations
                }
            }
        }
    }

    private fun setupUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationOverlay.lastFix?.let { showUserLocationOnMap(it) }
        }
    }

    private fun showUserLocationOnMap(location: Location) {
        val userLocation = GeoPoint(location.latitude, location.longitude)
        mapView.controller.animateTo(userLocation, 15.0, 2000)
    }

    private fun updateBusMarkers(busLocations: List<Re>) {
        val currentTime = System.currentTimeMillis()
        busLocations.forEach { busLocation ->
            val busPosition = GeoPoint(busLocation.lat, busLocation.lon)

            if (busMarkers.containsKey(busLocation.gbr)) {
                val (marker, _) = busMarkers[busLocation.gbr]!!
                marker.position = busPosition
                marker.icon =
                    BitmapDrawable(
                        resources,
                        createLineMarkerIcon(
                            this, busLocation.busId, "a",
                        ),
                    )
                marker.title = "Bus ID: ${busLocation.voznjaBusId}"
                marker.snippet = "Next Station: ${busLocation.voznjaId}"
                marker.setInfoWindowAnchor(0.5f, 0.5f)
                busMarkers[busLocation.gbr] = Pair(marker, currentTime)
            } else {
                // Create a new marker if it doesn't exist
                val newMarker =
                    Marker(mapView).apply {
                        position = busPosition
                        icon =
                            BitmapDrawable(
                                resources,
                                createLineMarkerIcon(
                                    this@MapsActivity2, busLocation.busId, "a",
                                ),
                            )
                        title = "Bus ID: ${busLocation.voznjaBusId}"
                        snippet = "Next Station: ${busLocation.voznjaId}"
                        setOnMarkerClickListener { _, _ ->
                            Toast.makeText(
                                this@MapsActivity2,
                                "Bus ID: ${busLocation.voznjaBusId}",
                                Toast.LENGTH_SHORT,
                            ).show()
                            true
                        }
                    }
                busMarkers[busLocation.gbr] = Pair(newMarker, currentTime)
                mapView.overlays.add(newMarker)
            }
        }

        val inactiveThreshold = 180000L
        val inactiveMarkers =
            busMarkers.filterValues { (_, lastUpdated) ->
                currentTime - lastUpdated > inactiveThreshold
            }
        inactiveMarkers.keys.forEach { gbr ->
            mapView.overlays.remove(busMarkers[gbr]?.first)
            busMarkers.remove(gbr)
            Log.d(tag, "Removed inactive marker: $gbr")
        }
        // Refresh the map
        mapView.invalidate()
    }

    private fun updateStationMarkers(stationsList: List<Station>) {
        stationMarkers.values.forEach { mapView.overlays.remove(it) }
        stationMarkers.clear()

        stationsList.forEach { station ->
            val stationPosition = GeoPoint(station.gpsY, station.gpsX)
            val marker =
                Marker(mapView).apply {
                    icon = BitmapDrawable(resources, createStationMarkerIcon(this@MapsActivity2, "A"))
                    position = stationPosition
                    title = station.shortName
                    snippet = station.longName
                    setOnMarkerClickListener { _, _ ->
                        Toast.makeText(
                            this@MapsActivity2,
                            "Station: ${station.shortName}",
                            Toast.LENGTH_SHORT,
                        ).show()
                        true
                    }
                }
            stationMarkers[station.id] = marker
            mapView.overlays.add(marker)
        }
        mapView.invalidate()
    }

    private fun createStationMarkerIcon(
        context: Context,
        stationDirection: String,
    ): Bitmap {
        val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_station_size)
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint =
            Paint().apply {
                color =
                    if (stationDirection == "A") {
                        ContextCompat.getColor(context, R.color.greenish)
                    } else {
                        ContextCompat.getColor(context, R.color.fade_blue)
                    }
            }
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)
        paint.apply {
            color = ContextCompat.getColor(context, R.color.black)
            textSize =
                context.resources.getDimensionPixelSize(R.dimen.marker_station_text_size).toFloat()
            textAlign = Paint.Align.CENTER
        }
        val x = markerSize / 2f
        val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(stationDirection, x, y, paint)
        return bitmap
    }

    private fun createLineMarkerIcon(
        context: Context,
        lineNumber: String,
        lineDirection: String,
    ): Bitmap {
        val markerSize = context.resources.getDimensionPixelSize(R.dimen.marker_size)
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint =
            Paint().apply {
                color =
                    if (lineDirection == "A") {
                        ContextCompat.getColor(context, R.color.wave)
                    } else {
                        ContextCompat.getColor(context, R.color.some)
                    }
            }
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)
        paint.apply {
            color = ContextCompat.getColor(context, R.color.black)
            textSize = context.resources.getDimensionPixelSize(R.dimen.marker_text_size).toFloat()
            textAlign = Paint.Align.CENTER
        }
        val x = markerSize / 2f
        val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(lineNumber, x, y, paint)
        return bitmap
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationOverlay.enableMyLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
        locationOverlay.enableMyLocation()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
        locationOverlay.disableMyLocation()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDetach()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}
