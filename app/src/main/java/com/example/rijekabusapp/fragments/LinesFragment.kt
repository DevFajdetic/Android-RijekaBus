package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.LineRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentLinesBinding
import com.example.rijekabusapp.helpers.isOnline
import com.example.rijekabusapp.helpers.showCustomDialog
import com.example.rijekabusapp.network.models.Line
import com.example.rijekabusapp.viewmodels.LinesViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout

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

        setupFilters()
        setupSpinner()

        return binding.root
    }

    private fun setupFilters() {
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
            bsView.findViewById<Button>(R.id.b_reset).setOnClickListener {
                bsDialog.dismiss()
            }
            bsView.findViewById<SwitchMaterial>(R.id.s_location).visibility = View.GONE
            bsView.findViewById<TextView>(R.id.tv_filter_lines).text =
                getString(R.string.filter_stations)
            bsView.findViewById<TextInputLayout>(R.id.til_lines).hint = getString(R.string.stations)

            bsDialog.setContentView(bsView)
            bsDialog.show()
        }
    }

    private fun setupSpinner() {
        val directions = resources.getStringArray(R.array.SpinnerDirectionsItems)
        val spinnerAdapter = ArrayAdapter(
            requireContext(), R.layout.drop_down_toolbar_item, directions
        )

        binding.spinner.adapter = spinnerAdapter

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
                getLinesList(selectedDirection)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private var insertFavoriteLine = fun(it: Line) { viewModel.insertFavoriteLine(it) }
    private var deleteFavoriteLine = fun(it: Line) { viewModel.deleteFavoriteLine(it) }

    fun getLinesList(direction: String) {
        viewModel.getFavoriteLines()
        val favoriteLines = viewModel.favoriteLines.value

        binding.progressBar.visibility = ProgressBar.VISIBLE

        viewModel.linesList.observe(viewLifecycleOwner) { lines ->
            val adapter = LineRecyclerAdapter(
                requireContext(),
                (
                    (
                        if (direction != "") {
                            lines.filter { it.direction == direction }
                        } else lines
                        ) as ArrayList<Line>
                    ),
                favoriteLines,
                false,
                insertFavoriteLine,
                deleteFavoriteLine
            )
            binding.rvLines.adapter = adapter
            binding.progressBar.visibility = ProgressBar.GONE

            setLinesSearchListener(adapter)
        }

        if (requireContext().isOnline()) {
            viewModel.getLinesList()
        } else {
            showCustomDialog(getString(R.string.no_internet_connection), requireContext())
        }
    }

    private fun setLinesSearchListener(adapter: LineRecyclerAdapter) {
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
}
