package com.example.rijekabusapp.fragments.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.adapters.RouteRecyclerAdapter
import com.example.rijekabusapp.databinding.BottomsheetTripBinding
import com.example.rijekabusapp.network.models.Step
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RoutesListBottomSheet(
    private val stepList: ArrayList<Step>,
) : BottomSheetDialogFragment() {
    private var _binding: BottomsheetTripBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { RouteRecyclerAdapter(requireContext(), arrayListOf()) }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BottomsheetTripBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.rvRoutes.layoutManager = LinearLayoutManager(requireContext())
        adapter.setStepList(stepList)
        binding.rvRoutes.adapter = adapter

        // TODO: Implement click listeners for go and addToRoutes buttons
        return view
    }
}
