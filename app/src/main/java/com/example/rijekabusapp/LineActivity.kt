package com.example.rijekabusapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.adapters.EXTRA_LINE
import com.example.rijekabusapp.adapters.LineStationsRecyclerAdapter
import com.example.rijekabusapp.databinding.ActivityLineBinding
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.viewmodels.BusLocationViewModel
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import java.text.SimpleDateFormat
import java.util.*

class LineActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLineBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var busLocationViewModel: BusLocationViewModel
    private lateinit var sdf: SimpleDateFormat
    private lateinit var currentTime: Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineBinding.inflate(layoutInflater)
        val lineItem = intent.getSerializableExtra(EXTRA_LINE) as? Line

        setContentView(binding.root)

        scheduleViewModel = MainActivity.ViewModelHolder.getScheduleViewModel(this, application)
        binding.rvLineStations.layoutManager = LinearLayoutManager(this)

        busLocationViewModel = ViewModelProvider(this)[BusLocationViewModel::class.java]

        // Access the scheduleList and observe changes as needed
        scheduleViewModel.scheduleList.observe(this) { scheduleList ->
            busLocationViewModel.busLocationsLiveData.observe(this) { busLocations ->
                val filteredSchedules = scheduleList.filter { schedule ->
                    schedule.lineNumber == lineItem?.lineNumber &&
                        schedule.variantLineName == lineItem.name &&
                        schedule.linVarId == lineItem.linVarId &&
                        busLocations.any { it.startId.toString() == schedule.startId }
                }

                val adapter = LineStationsRecyclerAdapter(this, filteredSchedules)
                binding.rvLineStations.adapter = adapter

                if (filteredSchedules.isEmpty()) {
                    binding.emptyState.setupEmptyStateView(getString(R.string.no_buses_error_desc))
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvLineStations.visibility = View.GONE
                }
                binding.progressBar.visibility = ProgressBar.GONE
            }
            busLocationViewModel.getBusLocations()
        }
    }
}
