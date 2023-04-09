package com.example.rijekabusapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.rijekabusapp.databinding.ActivityMainBinding
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        val bottomBarBackground = binding.mainAppBar.background as MaterialShapeDrawable
        bottomBarBackground.shapeAppearanceModel = bottomBarBackground.shapeAppearanceModel
            .toBuilder()
            .setTopLeftCorner(CornerFamily.ROUNDED, 100f)
            .setTopRightCorner(CornerFamily.ROUNDED, 100f)
            .build()

        setContentView(binding.root)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = binding.fragmentContainerView.getFragment() as NavHostFragment
        navController = navHostFragment.navController
        binding.mainNavView.setupWithNavController(navController)
        setupActionBarWithNavController(
            navController,
            AppBarConfiguration(
                setOf(
                    R.id.exploreFragment,
                    R.id.favoritesFragment,
                    R.id.linesFragment,
                    R.id.stationsFragment
                )
            )
        )
    }
}
