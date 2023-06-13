package com.example.rijekabusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.viewpager.ViewPagerAdapter
import com.example.rijekabusapp.databinding.ActivityLineBinding
import com.example.rijekabusapp.fragments.StationLinesFragment
import com.example.rijekabusapp.fragments.StationLinesListFragment
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.google.android.material.tabs.TabLayout

class StationActivity : AppCompatActivity() {

    lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var binding: ActivityLineBinding
    private lateinit var stationItem: Station

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineBinding.inflate(layoutInflater)
        stationItem = (intent.getSerializableExtra(EXTRA_STATION) as? Station)!!

        scheduleViewModel = MainActivity.ViewModelHolder.getScheduleViewModel(this, application)
        setupViewPagerAndTabs()

        setContentView(binding.root)
    }

    private fun setupViewPagerAndTabs() {
        binding.tvIcon.text = stationItem.shortName[0].toString()
        binding.tvLineName.text = stationItem.shortName
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
