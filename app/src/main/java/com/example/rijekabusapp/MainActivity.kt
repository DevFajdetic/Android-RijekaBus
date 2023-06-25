package com.example.rijekabusapp

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.rijekabusapp.databinding.ActivityMainBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.factory.ScheduleViewModelFactory
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    lateinit var scheduleViewModel: ScheduleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
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
            scheduleViewModel.getScheduleList()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), this)
        }
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
            applicationContext: Application
        ): ScheduleViewModel {
            if (scheduleViewModel == null) {
                scheduleViewModel = ViewModelProvider(
                    activity, ScheduleViewModelFactory(applicationContext)
                )[ScheduleViewModel::class.java]
            }
            return scheduleViewModel!!
        }
    }
}
