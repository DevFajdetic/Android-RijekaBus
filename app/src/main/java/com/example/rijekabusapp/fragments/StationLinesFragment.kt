package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.example.rijekabusapp.R
import com.example.rijekabusapp.StationActivity
import com.example.rijekabusapp.adapters.EXTRA_STATION
import com.example.rijekabusapp.adapters.StationLinesRecyclerAdapter
import com.example.rijekabusapp.adapters.image.ImageSliderAdapter
import com.example.rijekabusapp.databinding.FragmentStationLinesBinding
import com.example.rijekabusapp.fragments.bottomsheet.AddPhotoBottomSheet
import com.example.rijekabusapp.fragments.bottomsheet.EditPhotosBottomSheet
import com.example.rijekabusapp.helpers.*
import com.example.rijekabusapp.network.models.Station
import com.example.rijekabusapp.network.models.StationImage
import com.example.rijekabusapp.viewmodels.ScheduleViewModel
import com.example.rijekabusapp.viewmodels.StationDetailsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class StationLinesFragment : Fragment(), OnMapReadyCallback {

    private lateinit var binding: FragmentStationLinesBinding
    private lateinit var scheduleViewModel: ScheduleViewModel
    private val viewModel: StationDetailsViewModel by activityViewModels()
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private lateinit var stationItem: Station
    private val imageSliderAdapter by lazy { ImageSliderAdapter(requireContext(), arrayListOf()) }
    private var playerImageList = ArrayList<StationImage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStationLinesBinding.inflate(inflater, container, false)
        stationItem = (arguments?.getSerializable(EXTRA_STATION) as? Station)!!

        binding.googleMap.onCreate(savedInstanceState)
        latitude = stationItem.gpsY
        longitude = stationItem.gpsX
        binding.googleMap.getMapAsync(this)
        setupCurrentSchedule(stationItem, savedInstanceState)

        return binding.root
    }

    private fun setupCurrentSchedule(stationItem: Station?, savedInstanceState: Bundle?) {
        binding.progressBar.visibility = ProgressBar.VISIBLE
        binding.imageProgressBar.visibility = View.VISIBLE

        scheduleViewModel = (requireActivity() as StationActivity).scheduleViewModel
        binding.rvStationLines.layoutManager = LinearLayoutManager(requireContext())

        binding.imageSliderViewPager.setParentInterceptingTouch(false)
        if (imageSliderAdapter.count == 0) {
            binding.ivPlaceholder.load(R.drawable.ic_bus_stop_one)
            binding.imageProgressBar.visibility = ProgressBar.GONE
        }
        binding.imageSliderViewPager.adapter = imageSliderAdapter

        viewModel.imagesList.observe(viewLifecycleOwner) { imageList ->
            binding.imageProgressBar.visibility = View.VISIBLE

            playerImageList.addAll(imageList)

            binding.ivPlaceholder.visibility = View.GONE
            imageSliderAdapter.setImageList(playerImageList)
            binding.indicator.setViewPager(binding.imageSliderViewPager)

            binding.imageProgressBar.visibility = View.GONE

            binding.btnEditImage.setOnClickListener {
                openEditPhotoBottomSheet(imageList)
            }
        }

        binding.btnAddImage.setOnClickListener {
            openAddPhotoBottomSheet()
        }

        scheduleViewModel.scheduleList.observe(viewLifecycleOwner) { scheduleList ->
            val filteredStations = scheduleList.filter {
                it.stationId == stationItem?.id &&
                    compareWithCurrentTime(stringToTime(it.startTime)) >= 0
            }.sortedBy { it.startTime }

            val adapter = StationLinesRecyclerAdapter(
                requireContext(),
                filteredStations,
                true
            )
            binding.rvStationLines.adapter = adapter
            binding.progressBar.visibility = ProgressBar.GONE

            if (filteredStations.isEmpty()) {
                binding.emptyState.setupEmptyStateView(getString(R.string.no_stations_error_desc))
                binding.ivMissing.visibility = View.VISIBLE
                binding.emptyState.visibility = View.VISIBLE
                binding.rvStationLines.visibility = View.GONE
            }
        }

        if (!requireContext().isOnline()) {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        } else {
            if (stationItem != null) {
                viewModel.getStationImages(stationItem.id)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.googleMap.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.googleMap.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.googleMap.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.googleMap.onLowMemory()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (latitude.toString()[0].toString() == "0") {
            return
        }

        val markerOptions = MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title(stationItem.longName)
        googleMap.addMarker(markerOptions)

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(latitude, longitude))
            .zoom(16f)
            .build()
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private fun openAddPhotoBottomSheet() {
        val bottomSheet = AddPhotoBottomSheet(stationItem, {
            viewModel.addImageForStation(it)
        }, {
            imageSliderAdapter.updateImageList(it)
            playerImageList.add(it)
            binding.indicator.setViewPager(binding.imageSliderViewPager)
        }, {
            binding.ivPlaceholder.visibility = View.GONE
            binding.imageProgressBar.visibility = View.GONE
        })
        bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        bottomSheet.show(requireActivity().supportFragmentManager, "AddPhoto")
    }

    private fun openEditPhotoBottomSheet(imageList: ArrayList<StationImage>) {
        val bottomSheet = EditPhotosBottomSheet(
            imageList, {
                viewModel.deleteStationImage(it.id!!)
                playerImageList.remove(it)
            }, {
            imageSliderAdapter.setImageList(it)
            playerImageList.clear()
            playerImageList.addAll(it)
            binding.indicator.setViewPager(binding.imageSliderViewPager)
        },
            {
                viewModel.deleteAllStationImages(it)
                playerImageList.removeAll(it)
                imageSliderAdapter.clearImageList(it)
                if (playerImageList.isEmpty()) binding.ivPlaceholder.visibility = View.VISIBLE
                binding.indicator.setViewPager(binding.imageSliderViewPager)
            }, {
            showCustomSnackbar(requireContext(), binding.root, it)
        }
        )
        bottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        bottomSheet.show(requireActivity().supportFragmentManager, "EditPhoto")
    }
}
