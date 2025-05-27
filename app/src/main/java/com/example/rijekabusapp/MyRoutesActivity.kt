package com.example.rijekabusapp

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.rijekabusapp.adapters.FavoriteRouteRecyclerAdapter
import com.example.rijekabusapp.databinding.ActivityMyRoutesBinding
import com.example.rijekabusapp.viewmodels.DirectionsViewModel

class MyRoutesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyRoutesBinding
    private val viewModel: DirectionsViewModel by viewModels()
    private lateinit var adapter: FavoriteRouteRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyRoutesBinding.inflate(layoutInflater)

        viewModel.getFavoriteRoutes()
        val favoriteRoutes = viewModel.favoriteRoutes.value
        if (favoriteRoutes != null) {
            adapter = FavoriteRouteRecyclerAdapter(this, favoriteRoutes)
            binding.rv.layoutManager = LinearLayoutManager(this)
            binding.rv.adapter = adapter
        } else {
        }

        binding.profilePic.load(
            SavedPreference.getPictureUrl(this),
        ) {
            crossfade(true)
            placeholder(R.drawable.ic_person)
        }

        setContentView(binding.root)
    }
}
