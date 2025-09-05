package com.example.rijekabusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.viewpager.ViewPagerAdapter
import com.example.rijekabusapp.databinding.ActivityScheduleBinding
import com.example.rijekabusapp.fragments.LineScheduleFragment
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.google.android.material.tabs.TabLayoutMediator

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        LanguageHelper.applyLanguage(this)

        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val stationItem = intent.getSerializableExtra(EXTRA_STATION) as? Station
        val lineItem = intent.getSerializableExtra(EXTRA_LINE) as? Line

        // Set activity title based on the item type
        if (stationItem != null) {
            binding.toolbarTitle.text = getString(R.string.station_schedule_for, stationItem.shortName)
        } else if (lineItem != null) {
            binding.toolbarTitle.text = getString(R.string.bus_schedule_for, lineItem.lineNumber, lineItem.name)
        }

        // Setup back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Create fragments for the different day types
        val fragments = listOf(
            LineScheduleFragment("tjedan"),
            LineScheduleFragment("subota"),
            LineScheduleFragment("nedjelja"),
        )

        // Set up ViewPager with fragments
        if (stationItem != null) {
            binding.viewPager.adapter = ViewPagerAdapter(this, fragments, stationItem)
        } else if (lineItem != null) {
            binding.viewPager.adapter = ViewPagerAdapter(this, fragments, lineItem)
        }

        // Set up TabLayout with ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.weekday)
                1 -> tab.text = getString(R.string.saturday)
                2 -> tab.text = getString(R.string.sunday)
            }
        }.attach()
    }
}
