package com.example.rijekabusapp

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import coil.transform.CircleCropTransformation
import com.example.rijekabusapp.adapters.FavoriteRouteRecyclerAdapter
import com.example.rijekabusapp.databinding.ActivityMyRoutesBinding
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.viewmodels.DirectionsViewModel
import com.google.android.material.snackbar.Snackbar

class MyRoutesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyRoutesBinding
    private val viewModel: DirectionsViewModel by viewModels()
    private lateinit var adapter: FavoriteRouteRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings
        LanguageHelper.applyLanguage(this)
        
        super.onCreate(savedInstanceState)
        binding = ActivityMyRoutesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupObservers()
        loadData()
        animateUI()
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

    private fun setupUI() {
        // Set up toolbar with transition
        binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.color_primary))
        binding.toolbarTitle.text = getString(R.string.my_routes)
        
        // Set up back button with ripple effect
        binding.ivBack.setOnClickListener {
            // Apply exit animation
            val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
            binding.statsCard.startAnimation(slideDown)
            
            // Delay finish to allow animation to complete
            binding.statsCard.postDelayed({ finish() }, 200)
        }

        // Load profile picture with circular crop
        binding.profilePic.load(SavedPreference.getPictureUrl(this)) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
            transformations(CircleCropTransformation())
        }

        // Add shadow to profile picture
        binding.profilePic.elevation = 8f

        // Set up recycler view with fancy dividers
        binding.rv.layoutManager = LinearLayoutManager(this)
        binding.rv.addItemDecoration(
            androidx.recyclerview.widget.DividerItemDecoration(
                this, 
                androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
            ).apply {
                ContextCompat.getDrawable(this@MyRoutesActivity, R.drawable.route_divider)?.let {
                    setDrawable(it)
                }
            }
        )
        
        // Add pull-to-refresh functionality
        binding.swipeRefresh.setColorSchemeColors(
            ContextCompat.getColor(this, R.color.color_primary),
            ContextCompat.getColor(this, R.color.color_secondary),
            ContextCompat.getColor(this, R.color.color_tertiary)
        )
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
            binding.swipeRefresh.isRefreshing = false
            Snackbar.make(binding.root, "Routes refreshed!", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        // Observe favorite routes with loading state
        binding.loadingView.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
        
        viewModel.favoriteRoutes.observe(this, Observer { routes ->
            binding.loadingView.visibility = View.GONE
            
            if (routes != null && routes.isNotEmpty()) {
                adapter = FavoriteRouteRecyclerAdapter(this, routes)
                binding.rv.adapter = adapter
                binding.rv.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
                
                // Apply animation to recycler view items
                val controller = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
                binding.rv.startAnimation(controller)
            } else {
                binding.rv.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
                binding.emptyState.setupEmptyStateView(getString(R.string.no_favorite_routes))
            }
        })
        
        // Observe user stats
        viewModel.userStats.observe(this, Observer { stats ->
            if (stats != null) {
                updateUIWithStats(stats)
            }
        })
    }

    private fun loadData() {
        viewModel.getFavoriteRoutes()
        viewModel.getUserStats()
    }
    
    private fun updateUIWithStats(stats: com.example.rijekabusapp.database.models.UserStats) {
        // Update stats card with animations
        val distanceText = String.format("%.2f km", stats.totalDistance)
        animateTextChange(binding.tvWeather, distanceText)
        
        val timeText = "${stats.totalTime} min"
        animateTextChange(binding.tvTemp, timeText)
        
        val tripsText = "${stats.totalTrips} trips"
        animateTextChange(binding.tvHumidity, tripsText)
        
        // Update level information with animation
        binding.levelText.text = "Razina ${stats.level}"
        binding.progressBar.max = 100
        
        // Animate progress bar
        val currentProgress = binding.progressBar.progress
        val targetProgress = stats.experiencePoints % 100
        ObjectAnimator.ofInt(binding.progressBar, "progress", currentProgress, targetProgress).apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        
        // Calculate trips to next level
        val pointsToNextLevel = ((stats.level) * 100) - stats.experiencePoints
        val tripsToNextLevel = (pointsToNextLevel / 20) + 1
        binding.toNext.text = "$tripsToNextLevel putovanja do iduće razine"
        
        // Add special indicator for high-level users
        if (stats.level >= 5) {
            binding.levelBadge.visibility = View.VISIBLE
            binding.levelBadge.setImageResource(R.drawable.ic_star_full)
        } else {
            binding.levelBadge.visibility = View.GONE
        }
    }
    
    private fun animateUI() {
        // Apply entrance animations
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.statsCard.startAnimation(slideUp)
        
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        binding.rvContainer.startAnimation(fadeIn)
    }
    
    private fun animateTextChange(view: View, newText: String) {
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        fadeOut.fillAfter = false
        view.startAnimation(fadeOut)
        
        view.postDelayed({
            if (view is android.widget.TextView) {
                view.text = newText
            }
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            view.startAnimation(fadeIn)
        }, 150)
    }
}
