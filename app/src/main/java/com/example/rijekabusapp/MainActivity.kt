package com.example.rijekabusapp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.rijekabusapp.databinding.ActivityMainBinding
import com.example.rijekabusapp.firebase.FirebaseAuthHelper
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.helpers.PREF_THEME_MODE
import com.example.rijekabusapp.helpers.getBoolFromPreferences
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.factory.ScheduleViewModelFactory
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    lateinit var scheduleViewModel: ScheduleViewModel
    private val firebaseAuthHelper by lazy { (application as RijekaBusApplication).firebaseAuthHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings before super.onCreate
        LanguageHelper.applyLanguage(this)
        
        // Apply theme before setting content view
        applyThemeFromPreferences()
        
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        // Add rounded corners to bottom navigation
        val bottomBarBackground = binding.mainAppBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel =
            bottomBarBackground.shapeAppearanceModel.toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, 100f)
                .setTopRightCorner(CornerFamily.ROUNDED, 100f).build()

        setContentView(binding.root)

        setupNavigation()

        scheduleViewModel = ViewModelHolder.getScheduleViewModel(this, application)
        if (this.isOnline()) {
            scheduleViewModel.getScheduleList(null)
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), this)
        }
    }
    
    // Override attachBaseContext to apply language settings
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.createConfigurationContext(newBase))
    }
    
    // Apply language when resuming the activity
    override fun onResume() {
        super.onResume()
        LanguageHelper.applyLanguage(this)
    }

    private fun applyThemeFromPreferences() {
        val isDarkMode = getBoolFromPreferences(PREF_THEME_MODE, false, this)
        val mode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun setupNavigation() {
        val navHostFragment = binding.fragmentContainerView.getFragment() as NavHostFragment
        navController = navHostFragment.navController
        binding.mainNavView.setupWithNavController(navController)

        binding.mainFAB.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    object ViewModelHolder {
        private var scheduleViewModel: ScheduleViewModel? = null

        fun getScheduleViewModel(
            activity: FragmentActivity,
            applicationContext: Application,
        ): ScheduleViewModel {
            if (scheduleViewModel == null) {
                scheduleViewModel =
                    ViewModelProvider(
                        activity, ScheduleViewModelFactory(applicationContext),
                    )[ScheduleViewModel::class.java]
            }
            return scheduleViewModel!!
        }
    }
}
