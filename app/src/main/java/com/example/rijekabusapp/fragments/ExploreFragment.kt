package com.example.rijekabusapp.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import coil.load
import coil.transform.CircleCropTransformation
import com.example.rijekabusapp.*
import com.example.rijekabusapp.databinding.FragmentExploreBinding
import com.example.rijekabusapp.helpers.*
import com.example.rijekabusapp.network.response.WeatherResponse
import com.example.rijekabusapp.viewmodels.WeatherViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class ExploreFragment : Fragment() {

    private lateinit var binding: FragmentExploreBinding
    private lateinit var viewModel: WeatherViewModel
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)

        setupUIBasedOnLogin()
        isLocationOn()
        dashboardOnClicks()

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        viewModel.currentWeather.observe(viewLifecycleOwner) { weatherResponse ->
            weatherResponse?.let {
                updateWeatherData(weatherResponse)
            }
        }

        if (requireContext().isOnline()) {
            var lang = "hr"
            var unit = "metric"
            if (getPreferantLanguage(requireContext()) == "English") {
                lang = "en"
            }
            if (!getPreferantUnit(requireContext())) {
                unit = "imperial"
            }
            viewModel.getCurrentWeather(unit, lang)
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
            binding.weatherDetails.visibility = View.GONE
        }

        return binding.root
    }

    private fun dashboardOnClicks() {
        binding.ibMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.bringToFront()
        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.settings -> Navigation.findNavController(binding.root)
                    .navigate(R.id.action_exploreFragment_to_settingsFragment)
                R.id.about -> Navigation.findNavController(binding.root)
                    .navigate(R.id.action_exploreFragment_to_aboutActivity)
                R.id.rate -> Toast.makeText(
                    context,
                    getString(R.string.not_implemented), Toast.LENGTH_SHORT
                ).show()
                R.id.logout -> signOut()
                R.id.login -> {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                }
                R.id.profile -> showProfileCustomDialog(requireContext())
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        binding.ibSettings.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_settingsFragment)
        }

        binding.news.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_newsFragment)
        }

        binding.cityCard.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_cityCardFragment)
        }

        binding.telefon.setOnClickListener {
            val phoneNumber = "+38551311400"
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
                Toast.makeText(requireContext(), R.string.no_mail_app, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.schedules.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_schedulerItemPickerFragment)
        }

        binding.myRoutes.setOnClickListener {
            val intent = Intent(requireContext(), MyRoutesActivity::class.java)
            startActivity(intent)
        }

        binding.gradska.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.autotrolej.hr/wp-content/" +
                            "uploads/2020/02/autotrolej_mreza_prigradskih_linija.pdf"
                    )
                )
            )
        }

        binding.prigradska.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(
                        "https://www.autotrolej.hr/wp-content" +
                            "/uploads/2023/01/autotrolej-mreza-linija-2022.png"
                    )
                )
            )
        }
    }

    private fun setupUIBasedOnLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        if (SavedPreference.getEmail(requireContext()) == "") {
            binding.profileDetails.visibility = View.GONE
        } else {
            binding.navView.menu.findItem(R.id.logout).isVisible = true
            binding.navView.menu.findItem(R.id.profile).isVisible = true
            binding.navView.menu.findItem(R.id.login).isVisible = false
            setupUserProfile()
        }

        binding.userProfilePic.setOnClickListener {
            showProfileCustomDialog(requireContext())
            setupUserProfile()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeatherData(weatherResponse: WeatherResponse) {
        binding.tvTemp.text =
            weatherResponse.main.temp.toString() + appendTemperatureMetric(requireContext())
        binding.tvWeather.text =
            titleCase(weatherResponse.weather[0].description)
        binding.tvHumidity.text = weatherResponse.main.humidity.toString() + " %"
        binding.tvWind.text =
            weatherResponse.wind.speed.toString() + appendWindMetric(requireContext())
        binding.tvPressure.text = weatherResponse.main.pressure.toString() + " hPa"
        binding.tvSight.text =
            if (getPreferantUnit(requireContext())) weatherResponse.visibility.toString() + " m"
            else convertDistance(
                weatherResponse.visibility,
                "meters",
                "miles"
            ).toString() + " mi"
        val iconCode = weatherResponse.weather[0].icon
        Log.d("tag", "https://openweathermap.org/img/w/$iconCode.png")
        binding.ivWeather.load("https://openweathermap.org/img/w/$iconCode.png") {
            crossfade(true)
            placeholder(R.drawable.ic_weather_placeholder)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUserProfile() {
        binding.userProfilePic.load(SavedPreference.getPictureUrl(requireContext())) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        if (SavedPreference.getUsername(requireContext()) == "") {
            binding.userProfileName.text = SavedPreference.getGivenName(requireContext()) +
                " " + SavedPreference.getFamilyName(requireContext())
        } else {
            binding.userProfileName.text = SavedPreference.getUsername(requireContext())
        }
    }

    private fun isLocationOn() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE)
            as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (isLocationEnabled) {
            binding.location.append(" " + getString(R.string.on))
            binding.location.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.greenish)
            )
        } else {
            binding.location.append(" " + getString(R.string.off))
            binding.location.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.redish)
            )
        }
    }

    private fun signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            SavedPreference.clearPreferences(requireContext())
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }
}
