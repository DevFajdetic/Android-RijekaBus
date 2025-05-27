package com.example.rijekabusapp.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.FavoriteLineRecyclerAdapter
import com.example.rijekabusapp.adapters.FavoriteStationRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentFavoritesBinding
import com.example.rijekabusapp.helpers.ItemMoveCallback
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.viewmodels.FavoritesViewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {
    private lateinit var binding: FragmentFavoritesBinding

    private lateinit var favoriteStationAdapter: FavoriteStationRecyclerAdapter
    private lateinit var favoriteLineAdapter: FavoriteLineRecyclerAdapter

    private val viewModel: FavoritesViewModel by activityViewModels()

    private var isEditModeEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        binding.rvFavoriteLines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoriteStations.layoutManager = LinearLayoutManager(requireContext())

        binding.ibEdit.setOnClickListener {
            if (isEditModeEnabled) { // On Save
                binding.ibEdit.setImageResource(R.drawable.ic_edit)
                saveFavoritesPositions()
            } else { // On Edit
                binding.ibEdit.setImageResource(R.drawable.ic_check)
            }
            isEditModeEnabled = !isEditModeEnabled
            toggleEditMode(isEditModeEnabled)
        }
        loadFavoriteLines()
        loadFavoriteStations()

        try {
            viewModel.getFavoriteStationsAndImages()
        } catch (_: Exception) {
        }

        return binding.root
    }

    private fun saveFavoritesPositions() {
        viewModel.favoriteLinesUpdate()
        viewModel.favoriteStationsUpdate()
    }

    private fun loadFavoriteStations() {
        viewModel.favoriteStations.observe(viewLifecycleOwner) { stationList ->
            viewModel.stationImages.observe(viewLifecycleOwner) { imagesList ->
                favoriteStationAdapter =
                    FavoriteStationRecyclerAdapter(
                        requireContext(), stationList, imagesList, isEditModeEnabled,
                    ) { station ->
                        viewModel.deleteFavoriteStation(station.convertToStation())
                    }
                val itemMoveCallback =
                    ItemMoveCallback(favoriteStationAdapter) { isEditModeEnabled }
                val itemTouchHelper = ItemTouchHelper(itemMoveCallback)
                itemTouchHelper.attachToRecyclerView(binding.rvFavoriteStations)
                binding.rvFavoriteStations.adapter = favoriteStationAdapter
            }
            binding.progressStations.visibility = ProgressBar.GONE
            binding.emptyStateStations
                .setupEmptyStateView(getString(R.string.favorites_error_desc))
            setEmptyState(binding.emptyStateStations, stationList.isNotEmpty())
        }

        if (requireContext().isOnline()) {
            binding.progressStations.visibility = ProgressBar.VISIBLE
            viewModel.getFavoriteStationsAndImages()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    private fun loadFavoriteLines() {
        viewModel.favoriteLines.observe(viewLifecycleOwner) { linesList ->
            favoriteLineAdapter =
                FavoriteLineRecyclerAdapter(
                    requireContext(), linesList, isEditModeEnabled,
                ) { line ->
                    viewModel.deleteFavoriteLine(line.convertToLine())
                }
            val itemMoveCallback = ItemMoveCallback(favoriteLineAdapter) { isEditModeEnabled }
            val itemTouchHelper = ItemTouchHelper(itemMoveCallback)
            itemTouchHelper.attachToRecyclerView(binding.rvFavoriteLines)
            binding.rvFavoriteLines.adapter = favoriteLineAdapter
            binding.emptyStateLines.setupEmptyStateView(getString(R.string.favorites_error_desc))
            setEmptyState(binding.emptyStateLines, linesList.isNotEmpty())
            binding.progressLines.visibility = ProgressBar.GONE
        }

        if (requireContext().isOnline()) {
            binding.progressLines.visibility = ProgressBar.VISIBLE
            viewModel.getFavoriteLines()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    private fun setEmptyState(
        view: View,
        isNotEmpty: Boolean,
    ) {
        if (isNotEmpty) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun toggleEditMode(isEditModeEnabled: Boolean) {
        if (::favoriteStationAdapter.isInitialized) {
            favoriteStationAdapter.setEditModeEnabled(isEditModeEnabled)
            favoriteStationAdapter.notifyDataSetChanged()
        }
        if (::favoriteLineAdapter.isInitialized) {
            favoriteLineAdapter.setEditModeEnabled(isEditModeEnabled)
            favoriteLineAdapter.notifyDataSetChanged()
        }
    }
}
