package com.example.rijekabusapp.network.paging.line

import androidx.recyclerview.widget.DiffUtil
import com.example.rijekabusapp.network.models.Line

object LineDiff : DiffUtil.ItemCallback<Line>() {
    override fun areItemsTheSame(
        oldItem: Line,
        newItem: Line,
    ): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(
        oldItem: Line,
        newItem: Line,
    ): Boolean {
        return oldItem == newItem
    }
}
