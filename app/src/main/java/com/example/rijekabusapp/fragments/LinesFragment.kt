package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.FragmentLinesBinding

class LinesFragment : Fragment() {

    private lateinit var binding: FragmentLinesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLinesBinding.inflate(inflater, container, false)

        val directions = arrayOf("Istok -> Zapad", "Zapad -> Istok")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.drop_down_item,
            directions
        )

        binding.filledExposed.setAdapter(spinnerAdapter)

        return binding.root
    }
}
