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
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import coil.load
import coil.transform.CircleCropTransformation
import com.example.rijekabusapp.LoginActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.SavedPreference
import com.example.rijekabusapp.databinding.FragmentExploreBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.helpers.showProfileCustomDialog
import com.example.rijekabusapp.helpers.titleCase
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

        isLocationOn()

        binding.userProfilePic.setOnClickListener {
            showProfileCustomDialog(requireContext())
            setupUserProfile()
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
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.ibSettings.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_exploreFragment_to_settingsFragment)
        }

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)

        binding.navView.bringToFront()
        val toggle = ActionBarDrawerToggle(
            activity, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

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

        viewModel = ViewModelProvider(this)[WeatherViewModel::class.java]
        viewModel.currentWeather.observe(viewLifecycleOwner) { weatherResponse ->
            weatherResponse?.let {
                updateWeatherData(weatherResponse)
            }
        }

        if (requireContext().isOnline()) {
            viewModel.getCurrentWeather()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
            binding.weatherDetails.visibility = View.GONE
        }

        return binding.root
    }

    private fun updateWeatherData(weatherResponse: WeatherResponse) {
        binding.tvTemp.text = weatherResponse.main.temp.toString()
        binding.tvWeather.text = titleCase(weatherResponse.weather[0].description)
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
