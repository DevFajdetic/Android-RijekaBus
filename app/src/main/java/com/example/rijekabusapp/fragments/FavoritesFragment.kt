package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.FavoriteStationRecyclerAdapter
import com.example.rijekabusapp.adapters.LineRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentFavoritesBinding
import com.example.rijekabusapp.viewmodels.FavoritesViewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {

    private lateinit var binding: FragmentFavoritesBinding

    private val viewModel: FavoritesViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        binding.rvFavoriteLines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoriteStations.layoutManager = LinearLayoutManager(requireContext())

        loadFavoriteLines()
        loadFavoriteStations()

        try {
            viewModel.getFavoriteLines()
            viewModel.getFavoriteStationsAndImages()
        } catch (_: Exception) {
        }

        return binding.root
    }

    private fun loadFavoriteStations() {
        viewModel.favoriteStations.observe(viewLifecycleOwner) { stationList ->
            // viewModel.stationImages.observe(viewLifecycleOwner) { imagesList ->
            binding.progressStations.visibility = ProgressBar.VISIBLE

            val adapter = FavoriteStationRecyclerAdapter(
                requireContext(), stationList,
                // imagesList
            ) { player ->
                viewModel.deleteFavoriteStation(player)
            }
            binding.rvFavoriteStations.adapter = adapter

            binding.progressStations.visibility = ProgressBar.GONE
            // }
            setEmptyState(binding.emptyStateStations, stationList.isNotEmpty())
        }
    }

    private fun loadFavoriteLines() {
        viewModel.favoriteLines.observe(viewLifecycleOwner) { linesList ->
            binding.progressLines.visibility = ProgressBar.VISIBLE

            val adapter = LineRecyclerAdapter(
                requireContext(), linesList, null, true,
                null
            ) { line ->
                viewModel.deleteFavoriteLine(line)
            }
            binding.rvFavoriteLines.adapter = adapter
            binding.progressLines.visibility = ProgressBar.GONE
            setEmptyState(binding.emptyStateStations, linesList.isNotEmpty())
        }
    }

    private fun setEmptyState(view: View, isNotEmpty: Boolean) {
        if (isNotEmpty) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
}
