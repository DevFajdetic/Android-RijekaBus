package com.example.rijekabusapp.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.LineStationsScheduleItemBinding
import com.example.rijekabusapp.network.models.Schedule
import java.text.SimpleDateFormat
import java.util.*

class LineStationsRecyclerAdapter(
    private val context: Context,
    private val lineStationsItems: List<Schedule>
) : RecyclerView.Adapter<LineStationsRecyclerAdapter.LineStationsViewHolder>() {

    class LineStationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineStationsScheduleItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineStationsViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.line_stations_schedule_item, parent, false)
        return LineStationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineStationsViewHolder, position: Int) {
        val item = lineStationsItems[position]

        holder.binding.tvArriveTime.text = item.startTime.substring(0, 5)
        holder.binding.tvStationName.text = item.stationId.toString()

        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"))
        val sdf = SimpleDateFormat("HH:mm:ss.SSSSSSS", Locale("hr", "HR"))
        val startTime: Date = sdf.parse(item.startTime) as Date
        val startCalendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"))
        startCalendar.time = startTime

        Log.d("tag", startCalendar.time.toString())
        Log.d("tag", currentTime.time.toString())
        if (startCalendar.compareTo(currentTime) > 0) {
            holder.binding.ivDone.setImageResource(R.drawable.ic_hourglass)
            holder.binding.vTimeline.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.wave)
            )
        } else {
            holder.binding.ivDone.setImageResource(R.drawable.ic_check)
        }
    }

    override fun getItemCount(): Int {
        return lineStationsItems.size
    }
}
