package com.example.rijekabusapp.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.rijekabusapp.databinding.EmptyStateViewBinding

class EmptyStateView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    private val binding = EmptyStateViewBinding
        .inflate(LayoutInflater.from(context), this, true)

    fun setupEmptyStateView(description: String) {
        binding.tvDescription.text = description
    }
}
