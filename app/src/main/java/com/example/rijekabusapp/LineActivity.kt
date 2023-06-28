package com.example.rijekabusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.viewpager.ViewPagerAdapter
import com.example.rijekabusapp.databinding.ActivityLineBinding
import com.example.rijekabusapp.fragments.LineScheduleFragment
import com.example.rijekabusapp.fragments.LineStationsFragment
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.google.android.material.tabs.TabLayout

class LineActivity : AppCompatActivity() {

    lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var binding: ActivityLineBinding
    private lateinit var lineItem: Line

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineBinding.inflate(layoutInflater)
        lineItem = (intent.getSerializableExtra(EXTRA_LINE) as? Line)!!

        scheduleViewModel = MainActivity.ViewModelHolder.getScheduleViewModel(this, application)
        setupViewPagerAndTabs()

        setContentView(binding.root)
    }

    private fun setupViewPagerAndTabs() {
        binding.tvIcon.text = lineItem.lineNumber
        binding.tvLineName.text = lineItem.name
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        // Initialize TabLayout
        val tabs = listOf("Active", "Schedule")
        tabs.forEach { tabTitle ->
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(tabTitle))
        }

        // Initialize ViewPager2
        val fragments = listOf(LineStationsFragment(), LineScheduleFragment("TJEDAN"))
        val pagerAdapter = ViewPagerAdapter(this, fragments, lineItem)
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
