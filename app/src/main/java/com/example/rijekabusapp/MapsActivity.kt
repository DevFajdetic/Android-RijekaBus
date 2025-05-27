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
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.databinding.ActivityMapsBinding
import com.example.rijekabusapp.fragments.bottomsheet.RoutesListBottomSheet
import com.example.rijekabusapp.helpers.PREF_METRIC
import com.example.rijekabusapp.helpers.PREF_SELECTED_LANGUAGE
import com.example.rijekabusapp.helpers.getBoolFromPreferences
import com.example.rijekabusapp.helpers.getStringFromPreferences
import com.example.rijekabusapp.network.models.BusLocation
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

    // Threading & updating
    private val busLocationHandler = Handler()
    private lateinit var busLocationRunnable: Runnable
    private val busLocationUpdateInterval = 50000L

    // My Locaton related
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)

        Places.initialize(applicationContext, "AIzaSyCYRCaIRT_p72odDx2jgj38Ls4DF-h8ODI")

        setOnClickListeners()

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // CURRENT LOCATION LOGIC
        binding.filter.setOnClickListener {
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

        mapFragment = (supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)!!
        mapFragment.getMapAsync(this)

        busLocationViewModel = ViewModelProvider(this)[BusLocationViewModel::class.java]
        stationsViewModel = ViewModelProvider(this)[StationsViewModel::class.java]
        scheduleViewModel =
            ViewModelProvider(
                this, ScheduleViewModelFactory(application),
            )[ScheduleViewModel::class.java]
        directionsViewModel = ViewModelProvider(this)[DirectionsViewModel::class.java]
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
                    "en",
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
        }

        binding.fakeSearchDestination.setOnClickListener {
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
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    origin = "${place.latLng?.latitude},${place.latLng?.longitude}"
                    binding.svOrigin.text = place.name
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    // Handle error
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation
                }
            }
        } else if (requestCode == AUTOCOMPLETE_DEST_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    binding.svDestination.text = place.name
                    destination = "${place.latLng?.latitude},${place.latLng?.longitude}"
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(data!!)
                    // Handle error
                }
                RESULT_CANCELED -> {
                    // The user canceled the operation
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

    private fun startFetchingBusLocations() {
        busLocationRunnable =
            Runnable {
                busLocationViewModel.getBusLocations()
                busLocationHandler.postDelayed(busLocationRunnable, busLocationUpdateInterval)
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

        scheduleViewModel.getScheduleList(null)
        busLocationViewModel.getBusLocations()
        stationsViewModel.getStationsList()
        // directionsViewModel.getDirections(
        //    destination,
        //    origin, "en", "driving", "metric"
        // )

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
        directionsViewModel.directionsLiveData.observe(this) { directions ->
            drawRouteOnMap(directions.routes.firstOrNull()?.overviewPolyline?.points, directions)
            openRouteListBottomSheet(directions.routes.firstOrNull()?.legs?.firstOrNull()?.steps)
        }
    }

    private fun drawRouteOnMap(
        polyline: String?,
        response: DirectionsResponse,
    ) {
        polyline?.let {
            val decodedPath = PolyUtil.decode(polyline)
            val latLngList =
                decodedPath.map {
                    LatLng(it.latitude, it.longitude)
                }
            var busLine = "0"

            response.routes.firstOrNull()?.let { route ->
                val origin = route.legs.firstOrNull()?.startAddress ?: ""
                val destination = route.legs.firstOrNull()?.endAddress ?: ""
                val distance = route.legs.firstOrNull()?.distance?.text ?: ""
                val duration = route.legs.firstOrNull()?.duration?.text ?: ""
                val startTime = route.legs.firstOrNull()?.depTime?.text ?: ""
                val endTime = route.legs.firstOrNull()?.arrivalTime?.text ?: ""

                // Check if transit mode was used
                route.legs.forEach { leg ->
                    leg.steps.forEach {
                        if (it.travelMode == "TRANSIT") {
                            busLine = it.transitDetails!!.line?.name ?: "0"
                        }
                    }
                }

                // Create a Route object with the extracted information
                showCustomSnackbar(this, binding.root, busLine)
                val routeInfo =
                    FavoriteRoute(
                        origin, startTime, destination, endTime, distance, duration, busLine,
                        getCurrentDateTime(), distance.hashCode().toString(),
                    )

                // Save the route information to the database
                directionsViewModel.saveRouteInformation(routeInfo)

                for (leg in route.legs) {
                    for (step in leg.steps) {
                        val stepPolyline = step.polyline.points
                        val stepLatLngList =
                            PolyUtil.decode(stepPolyline)
                                .map { LatLng(it.latitude, it.longitude) }

                        // Draw dotted line for steps and solid line for bus segments
                        val stepPolylineOptions =
                            PolylineOptions()
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
            }

            val builder = LatLngBounds.builder()
            latLngList.forEach { builder.include(it) }
            val bounds = builder.build()
            val padding = 100 // Padding around the route (in pixels)

            // Move camera to show the entire route
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
        }
    }

    private fun setupUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let { showUserLocationOnMap(it) }
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
                .title("My Location")
                .icon(BitmapDescriptorFactory.defaultMarker(180f))

        googleMap.addMarker(markerOptions)
    }

    private fun updateStationMarkers(
        stationsList: ArrayList<Station>,
        scheduleList: ArrayList<Schedule>,
    ) {
        stationMarkers.values.forEach { marker -> marker.remove() }
        stationMarkers.clear()

        for (station in stationsList) {
            val direction = findDirection(station, scheduleList)
            val stationPosition =
                LatLng(
                    (station.gpsY),
                    (station.gpsX),
                )
            val markerOptions =
                MarkerOptions()
                    .position(stationPosition)
                    .title(station.shortName)
                    .snippet(station.longName)
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            createStationMarkerIcon(
                                this,
                                direction,
                            ),
                        ),
                    )

            val marker = googleMap.addMarker(markerOptions)
            stationMarkers[station.id] = marker!!
        }
    }

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

    private fun updateBusMarkers(
        busLocations: List<BusLocation>,
        scheduleList: List<Schedule>,
        stationList: List<Station>,
    ) {
        // Remove previous markers
        busMarkers.values.forEach { marker -> marker.remove() }
        busMarkers.clear()

        // Add new markers
        for (busLocation in busLocations) {
            val ran = Math.random() / 10000
            val bus = findBus(busLocation, scheduleList)
            val station = findStation(busLocation, stationList)

            val busPosition =
                LatLng(
                    (station?.gpsY?.plus(ran) ?: 45.3271),
                    (station?.gpsX?.plus(ran) ?: 14.4422),
                )
            val markerOptions =
                MarkerOptions()
                    .position(busPosition)
                    .title(bus?.variantLineName ?: getString(R.string.departure_failed))
                    .snippet(
                        "aaaa",
                    )
                    .icon(
                        BitmapDescriptorFactory.fromBitmap(
                            createLineMarkerIcon(
                                this,
                                bus?.lineNumber ?: "",
                                bus?.direction ?: "",
                            ),
                        ),
                    )

            val marker = googleMap.addMarker(markerOptions)
            busMarkers[busLocation.busId.toInt()] = marker!!
        }
    }

    private fun findStation(
        busLocation: BusLocation,
        stationList: List<Station>,
    ): Station? {
        val matchingStation = stationList.filter { it.id == busLocation.nextStationId }
        if (matchingStation.isNotEmpty()) {
            return matchingStation.first()
        }
        return null
    }

    private fun findBus(
        busLocation: BusLocation,
        scheduleList: List<Schedule>,
    ): Schedule? {
        val matchingSchedules =
            scheduleList.filter {
                it.startId == busLocation.startId.toString()
            }
        if (matchingSchedules.isNotEmpty()) {
            return matchingSchedules.first()
        }
        return null
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
