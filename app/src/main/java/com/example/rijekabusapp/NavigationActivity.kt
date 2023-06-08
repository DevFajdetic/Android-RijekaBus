package com.example.rijekabusapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.rijekabusapp.databinding.ActivityNavigationBinding
import com.example.rijekabusapp.network.models.BusLocation
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

// https://winter-star-9de5.kombajn.workers.dev/?autotrolej - Svaki put kad se dogodi promjena
// UZET STANICA ID

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var busLocationViewModel: BusLocationViewModel
    private lateinit var mMap: GoogleMap
    private val busMarkers = mutableMapOf<Int, Marker>()
    private lateinit var binding: ActivityNavigationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        busLocationViewModel = ViewModelProvider(this)[BusLocationViewModel::class.java]

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val rijeka = LatLng(45.3271, 14.4422)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rijeka, 12f))

        busLocationViewModel.busLocationsLiveData.observe(this) { busLocations ->
            updateMarkers(busLocations)
        }

        busLocationViewModel.getBusLocations()
    }

    private fun updateMarkers(busLocations: List<BusLocation>) {
        busMarkers.values.forEach { marker -> marker.remove() }
        busMarkers.clear()

        // Add new markers
        for (busLocation in busLocations) {
            val busPosition = LatLng(busLocation.gpsX, busLocation.gpsY)
            val markerOptions = MarkerOptions()
                .position(busPosition)
                .title(busLocation.busId)
                .snippet(busLocation.provideTime)
                .icon(BitmapDescriptorFactory.fromBitmap(createMarkerIcon(busLocation.busId)))

            val marker = mMap.addMarker(markerOptions)
            busMarkers[busLocation.nextStationId] = marker!!
        }
    }

    private fun createMarkerIcon(lineNumber: String): Bitmap {
        val markerSize = resources.getDimensionPixelSize(R.dimen.marker_size)
        val bitmap = Bitmap.createBitmap(markerSize, markerSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw circle background
        val paint = Paint()
        paint.color = ContextCompat.getColor(this, R.color.bluish)
        canvas.drawCircle(markerSize / 2f, markerSize / 2f, markerSize / 2f, paint)

        // Draw line number
        paint.color = ContextCompat.getColor(this, R.color.black)
        paint.textSize = resources.getDimensionPixelSize(R.dimen.marker_text_size).toFloat()
        paint.textAlign = Paint.Align.CENTER
        val x = markerSize / 2f
        val y = markerSize / 2f - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(lineNumber, x, y, paint)

        return bitmap
    }
}
