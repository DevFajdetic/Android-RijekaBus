package com.example.rijekabusapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.adapters.RouteRecyclerAdapter
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.databinding.ActivityRouteDetailBinding
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.network.models.Step
import java.text.SimpleDateFormat
import java.util.Locale

class RouteDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRouteDetailBinding
    private lateinit var adapter: RouteRecyclerAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings
        LanguageHelper.applyLanguage(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityRouteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Get route data from intent
        val route = intent.getSerializableExtra("ROUTE_DATA") as? FavoriteRoute
        
        if (route != null) {
            setupUI(route)
        } else {
            finish()
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
    
    private fun setupUI(route: FavoriteRoute) {
        // Setup toolbar
        binding.tvTitle.text = "Detalji rute"
        binding.ivBack.setOnClickListener { finish() }
        
        // Set route summary information
        binding.tvOrigin.text = route.origin
        binding.tvDestination.text = route.destination
        val croatianLocale = Locale("hr", "HR")
        val inputFormat = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("EEEE, d. MMMM", croatianLocale)

        val parsedDate = inputFormat.parse(route.date)
        val formattedDate = outputFormat.format(parsedDate)
        binding.tvDate.text = formattedDate
        binding.tvDuration.text = route.time
        binding.tvDistance.text = route.distance
        
        // Set route type icon
        when (route.routeType) {
            "BUS" -> binding.ivRouteType.setImageResource(R.drawable.ic_bus)
            "WALK" -> binding.ivRouteType.setImageResource(R.drawable.ic_walk)
            else -> binding.ivRouteType.setImageResource(R.drawable.ic_bus)
        }
        
        // Setup steps recycler view if steps are available
        if (route.steps.isNotEmpty()) {
            setupRecyclerView(route.steps)
        } else {
            // Hide steps section if no steps are available
            binding.tvStepsTitle.text = "No detailed steps available"
            binding.rvSteps.visibility = android.view.View.GONE
        }
    }
    
    private fun setupRecyclerView(steps: List<Step>) {
        adapter = RouteRecyclerAdapter(this, ArrayList(steps))
        binding.rvSteps.layoutManager = LinearLayoutManager(this)
        binding.rvSteps.adapter = adapter
    }
} 