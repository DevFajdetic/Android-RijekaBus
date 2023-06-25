package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.MainActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.StationRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentStationsBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip

class StationsFragment : Fragment() {

    private val viewModel: StationsViewModel by activityViewModels()
    private lateinit var scheduleViewModel: ScheduleViewModel
    private lateinit var binding: FragmentStationsBinding
    private var selectedDirection: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentStationsBinding.inflate(inflater, container, false)
        binding.rvStations.layoutManager = LinearLayoutManager(requireContext())

        binding.filter.setOnClickListener {
            val bsDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
            val bsView = LayoutInflater.from(binding.root.context)
                .inflate(
                    R.layout.bottom_sheet_filter,
                    binding.root.findViewById(R.id.bs_container)
                )

            bsView.findViewById<Button>(R.id.b_apply).setOnClickListener {
                bsDialog.dismiss()
            }
            bsDialog.setContentView(bsView)
            bsDialog.show()
        }

        setupSpinner()

        return binding.root
    }

    private fun setupSpinner() {
        val directions = resources.getStringArray(R.array.SpinnerDirectionsItems)
        val spinnerAdapter = ArrayAdapter(
            requireContext(), R.layout.drop_down_toolbar_item, directions
        )

        binding.spinner.adapter = spinnerAdapter
        scheduleViewModel = (requireActivity() as MainActivity).scheduleViewModel

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> selectedDirection = "" // All lines
                    1 -> selectedDirection = "B"
                    2 -> selectedDirection = "A"
                }
                getStationsList(selectedDirection)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private var insertFavoriteStation = fun(it: Station) { viewModel.insertFavoriteStation(it) }
    private var deleteFavoriteStation = fun(it: Station) { viewModel.deleteFavoriteStation(it) }

    fun getStationsList(direction: String) {
        viewModel.getFavoriteStations()
        val favoriteStations = viewModel.favoriteStations.value

        binding.progressBar.visibility = ProgressBar.VISIBLE

        viewModel.stationsList.observe(viewLifecycleOwner) { stations ->
            scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
                val adapter = StationRecyclerAdapter(
                    requireContext(),
                    (
                        (
                            if (direction != "") {
                                stations.filter { st ->
                                    scheduleList.any { sch ->
                                        sch.stationId == st.id && sch.direction == direction
                                    }
                                }
                            } else stations
                            ) as ArrayList<Station>
                        ),
                    favoriteStations,
                    false,
                    insertFavoriteStation,
                    deleteFavoriteStation
                )
                binding.rvStations.adapter = adapter
                binding.progressBar.visibility = ProgressBar.GONE

                setTeamSearchListener(adapter)
            }
        }

        if (requireContext().isOnline()) {
            viewModel.getStationsList()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    private fun setTeamSearchListener(adapter: StationRecyclerAdapter) {
        binding.SVLines.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(text: String?): Boolean {
                adapter.filter.filter(text)
                return false
            }
        })
    }

    fun applyFilter(filter: String) {
        val chip = Chip(requireContext())
        chip.text = filter
        chip.isCloseIconVisible = true
        chip.setOnCloseIconClickListener {
            // Remove the chip when the close icon is clicked
            binding.cgFilters.removeView(chip)
            // Perform any necessary action related to removing the filter
        }
        binding.cgFilters.addView(chip)
    }
}
