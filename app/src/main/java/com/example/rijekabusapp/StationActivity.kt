package com.example.rijekabusapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.viewpager.ViewPagerAdapter
import com.example.rijekabusapp.databinding.ActivityStationBinding
import com.example.rijekabusapp.fragments.StationLinesFragment
import com.example.rijekabusapp.fragments.StationLinesListFragment
import com.example.rijekabusapp.helpers.NOTIFICATION_CHANNEL_ID
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.google.android.material.tabs.TabLayout

class StationActivity : AppCompatActivity() {

    lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var binding: ActivityStationBinding
    private lateinit var stationItem: Station

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStationBinding.inflate(layoutInflater)
        stationItem = (intent.getSerializableExtra(EXTRA_STATION) as? Station)!!

        scheduleViewModel = MainActivity.ViewModelHolder.getScheduleViewModel(this, application)
        setupViewPagerAndTabs()
        createNotificationChannel()
        setContentView(binding.root)
    }
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "My Notification Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    private fun setupViewPagerAndTabs() {
        binding.tvIcon.text = stationItem.shortName[0].toString()
        binding.tvStationName.text = stationItem.shortName
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        val tabs = listOf("Active", "Lines")
        tabs.forEach { tabTitle ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitle))
        }

        val fragments = listOf(StationLinesFragment(), StationLinesListFragment())
        val pagerAdapter = ViewPagerAdapter(this, fragments, stationItem)
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
            }
        })

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.viewPager.setCurrentItem(tab.position, true)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // No action needed
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // No action needed
            }
        })
    }
}
