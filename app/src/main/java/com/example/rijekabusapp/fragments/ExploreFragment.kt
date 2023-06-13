package com.example.rijekabusapp.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import coil.load
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.FragmentExploreBinding
import com.example.rijekabusapp.network.response.WeatherResponse
import com.example.rijekabusapp.viewmodels.WeatherViewModel

class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var viewModel: WeatherViewModel

    @SuppressLint("QueryPermissionsNeeded")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentExploreBinding.inflate(inflater, container, false)

        binding.news.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_newsFragment)
        }

        binding.cityCard.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_cityCardFragment)
        }

        binding.telefon.setOnClickListener {
            val phoneNumber = "051311400"
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No phone app found", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.mail.setOnClickListener {
            val receiverEmail = "autotrolej@autotrolej.hr"
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$receiverEmail")
            }

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.ibMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.ibSettings.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_settingsFragment)
        }

        binding.navView.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        viewModel.currentWeather.observe(viewLifecycleOwner) { weatherResponse ->
            weatherResponse?.let {
                updateWeatherData(weatherResponse)
            }
        }

        viewModel.getCurrentWeather()

        return binding.root
    }

    private fun updateWeatherData(weatherResponse: WeatherResponse) {
        binding.tvTemp.text = weatherResponse.main.temp.toString()
        binding.tvWeather.text = weatherResponse.weather[0].description
        binding.tvHumidity.text = weatherResponse.main.humidity.toString()
        binding.tvWind.text = weatherResponse.wind.speed.toString()
        binding.tvPressure.text = weatherResponse.main.pressure.toString()
        binding.tvSight.text = weatherResponse.visibility.toString()
        val iconCode = weatherResponse.weather[0].icon
        Log.d("tag", "https://openweathermap.org/img/w/$iconCode.png")
        binding.ivWeather.load("https://openweathermap.org/img/w/$iconCode.png") {
            crossfade(true)
            placeholder(R.drawable.ic_weather_placeholder)
        }
    }

    /*
private fun setupUserProfile() {
    binding.userProfilePic.load(SavedPreference.getPictureUrl(this)) {
        crossfade(true)
        placeholder(R.drawable.ic_person)
        transformations(CircleCropTransformation())
    }

    binding.userProfileName.text = SavedPreference.getGivenName(this)
    binding.userProfileSurname.text = SavedPreference.getFamilyName(this)
}*/
}
