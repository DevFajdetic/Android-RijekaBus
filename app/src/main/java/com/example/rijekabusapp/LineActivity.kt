package com.example.rijekabusapp

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.viewpager.ViewPagerAdapter
import com.example.rijekabusapp.databinding.ActivityLineBinding
import com.example.rijekabusapp.fragments.LineScheduleFragment
import com.example.rijekabusapp.fragments.LineStationsFragment
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.google.android.material.tabs.TabLayout

class LineActivity : AppCompatActivity() {
    lateinit var scheduleViewModel: ScheduleViewModel
    private val busLocationViewModel: BusLocationViewModel by viewModels()

    private lateinit var binding: ActivityLineBinding
    private lateinit var lineItem: Line

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings
        LanguageHelper.applyLanguage(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityLineBinding.inflate(layoutInflater)
        lineItem = (intent.getSerializableExtra(EXTRA_LINE) as? Line)!!

        scheduleViewModel = MainActivity.ViewModelHolder.getScheduleViewModel(this, application)
        // Initialize WebSocket connection for real-time bus location updates
        if (!busLocationViewModel.isConnected()) {
            busLocationViewModel.connectToWebSocket()
        }
        
        setupViewPagerAndTabs()
        
        // Clean up old ratings, comments, and chat messages not needed anymore as Firebase handles this

        setContentView(binding.root)
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

    private fun setupViewPagerAndTabs() {
        binding.tvIcon.text = lineItem.lineNumber
        binding.tvLineName.text = lineItem.name
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Initialize TabLayout
        val tabs = listOf(R.string.Active, R.string.Schedule)
        tabs.forEach { tabTitle ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitle))
        }

        // Initialize ViewPager2
        val fragments = listOf(LineStationsFragment(), LineScheduleFragment("tjedan"))
        val pagerAdapter = ViewPagerAdapter(this, fragments, lineItem)
        binding.viewPager.adapter = pagerAdapter

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
                }
            },
        )

        binding.tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    binding.viewPager.setCurrentItem(tab.position, true)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    // No action needed
                }

                override fun onTabReselected(tab: TabLayout.Tab) {
                    // No action needed
                }
            },
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Disconnect WebSocket when activity is destroyed
        busLocationViewModel.disconnectWebSocket()
    }
}
