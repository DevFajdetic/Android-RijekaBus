package com.example.rijekabusapp.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.FragmentCityCardBinding

class CityCardFragment : Fragment() {
    private lateinit var binding: FragmentCityCardBinding

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentCityCardBinding.inflate(inflater, container, false)

        binding.ivBack.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_cityCardFragment_to_exploreFragment)
        }

        val webView = binding.wvCityCard
        webView.settings.apply {
            cacheMode = WebSettings.LOAD_DEFAULT
            layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
            userAgentString = "Mozilla/5.0 (Linux; Android 11; Pixel 4) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/88.0.4324.181 Mobile Safari/537.36"
        }
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(
            "https://www.rijekacitycard.hr/hr/kupi-rcc-usluge/autotrolej",
        )

        return binding.root
    }
}
