package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.paging.LinePagingAdapter
import com.example.rijekabusapp.databinding.FragmentLinesBinding
import com.example.rijekabusapp.network.paging.line.LineDiff
import com.example.rijekabusapp.viewmodels.LinesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LinesFragment : Fragment() {

    private val viewModel: LinesViewModel by activityViewModels()
    private lateinit var binding: FragmentLinesBinding
    private var selectedDirection: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLinesBinding.inflate(inflater, container, false)
        binding.rvLines.layoutManager = LinearLayoutManager(requireContext())

        setupSpinner()

        return binding.root
    }

    private fun setupSpinner() {
        val directions = resources.getStringArray(R.array.SpinnerItemsLines)
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            directions
        )

        binding.spinner.adapter = spinnerAdapter

        binding.spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> selectedDirection = "" // All lines
                    1 -> selectedDirection = "A"
                    2 -> selectedDirection = "B"
                }
                getAllLines(selectedDirection)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun getAllLines(direction: String) {
        // Load favorites - All Directions
        viewModel.getFavoriteLines()
        val favoriteLinesList = viewModel.favoriteLines

        val pagingAdapter = LinePagingAdapter(requireContext(), favoriteLinesList.value, LineDiff, {
            viewModel.insertFavoriteLine(it)
        }, {
            viewModel.deleteFavoriteLine(it)
        })
        binding.rvLines.adapter = pagingAdapter

        pagingAdapter.addLoadStateListener {
            if (it.refresh is LoadState.Loading) {
                binding.progressBar.visibility = ProgressBar.VISIBLE
            } else {
                binding.progressBar.visibility = ProgressBar.GONE
            }
        }

        viewModel.linePagingSource.updateDirection(direction)

        lifecycleScope.launch {
            viewModel.flow.collectLatest {
                pagingAdapter.submitData(it)
            }
        }
    }
}
