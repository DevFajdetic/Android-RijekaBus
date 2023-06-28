package com.example.rijekabusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.viewpager.ViewPagerAdapter
import com.example.rijekabusapp.databinding.ActivityScheduleBinding
import com.example.rijekabusapp.fragments.LineScheduleFragment
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.network.models.Station
import com.google.android.material.tabs.TabLayoutMediator

class ScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScheduleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        val item1 = intent.getSerializableExtra(EXTRA_STATION) as? Station
        val item2 = intent.getSerializableExtra(EXTRA_LINE) as? Line

        setContentView(binding.root)

        val fragments = listOf(
            LineScheduleFragment("tjedan"),
            LineScheduleFragment("subota"),
            LineScheduleFragment("nedjelja")
        )

        if (item1 != null) {
            binding.viewPager.adapter = ViewPagerAdapter(this, fragments, item1)
        } else if (item2 != null) {
            binding.viewPager.adapter = ViewPagerAdapter(this, fragments, item2)
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Tjedan"
                1 -> tab.text = "Subota"
                2 -> tab.text = "Nedjelja"
            }
        }.attach()
    }
}
