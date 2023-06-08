package com.example.rijekabusapp

import android.os.Bundle
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.rijekabusapp.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)

        val webView = binding.map
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Load your JavaScript code
        webView.loadUrl("https://busri.alwaysdata.net/")

        // Set a WebViewClient to handle page navigation
        webView.webViewClient = WebViewClient()
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    }
}
