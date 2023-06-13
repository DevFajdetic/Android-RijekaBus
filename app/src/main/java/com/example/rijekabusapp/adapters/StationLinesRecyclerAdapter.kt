package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.StationLineItemBinding
import com.example.rijekabusapp.network.models.Schedule
import java.util.*

class StationLinesRecyclerAdapter(
    private val context: Context,
    private val stationLinesList: List<Schedule>,
    private val shouldBindTime: Boolean,
) : RecyclerView.Adapter<StationLinesRecyclerAdapter.StationLinesViewHolder>() {

    class StationLinesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = StationLineItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationLinesViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.station_line_item, parent, false)
        return StationLinesViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: StationLinesViewHolder, position: Int) {
        val item = stationLinesList[position]

        holder.binding.tvTime.text = if (shouldBindTime) item.startTime.substring(0, 5) else ""
        holder.binding.tvLineName.text = item.variantLineName
        holder.binding.tvDirection.text = item.direction
        holder.binding.ivLineNumber.text = item.lineNumber
    }

    override fun getItemCount(): Int {
        return stationLinesList.size
    }
}
