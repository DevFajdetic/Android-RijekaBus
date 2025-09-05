package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.LineStationsScheduleItemBinding
import com.example.rijekabusapp.helpers.convertTimeTo12HourFormat
import com.example.rijekabusapp.helpers.showTimePickerDialog
import com.example.rijekabusapp.network.models.Schedule
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.TimeZone
import java.time.temporal.ChronoUnit

class LineStationsRecyclerAdapter(
    private val context: Context,
    private val lineStationsItems: List<Schedule>,
    private val nextStation: Schedule?,
    private val hourFormat: Boolean,
    private val distanceToNext: String? = null,
) : RecyclerView.Adapter<LineStationsRecyclerAdapter.LineStationsViewHolder>() {
    class LineStationsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineStationsScheduleItemBinding.bind(view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LineStationsViewHolder {
        val view =
            LayoutInflater
                .from(context)
                .inflate(R.layout.line_stations_schedule_item, parent, false)
        return LineStationsViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(
        holder: LineStationsViewHolder,
        position: Int,
    ) {
        val item = lineStationsItems[position]

        holder.binding.ivNotification.setOnClickListener {
            showTimePickerDialog(context, item)
        }

        val formattedTime = if (hourFormat) {
            item.startTime.substring(0, 5)
        } else {
            convertTimeTo12HourFormat(item.startTime.substring(0, 5))
        }
        holder.binding.tvArriveTime.text = formattedTime
        holder.binding.tvStationName.text = item.name

        // Reset early arrival text
        holder.binding.tvEarlyArrival.visibility = View.GONE

        if (nextStation?.stationId == item.stationId) {
            holder.binding.ivDone.setImageResource(R.drawable.ic_in_progress)
            holder.binding.vTimeline.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.wave),
            )
            holder.binding.ivDone.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(holder.itemView.context, R.color.wave))
            // Show distance to next station if available
            if (distanceToNext != null) {
                val distanceText = "$distanceToNext m"
                holder.binding.tvDistance.visibility = View.VISIBLE
                holder.binding.tvDistance.text = distanceText
            } else {
                holder.binding.tvDistance.visibility = View.GONE
            }
            
            // Check if bus is ahead of schedule
            val zoneId = ZoneId.of("Europe/Zagreb")
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS")
            val scheduledTime = LocalTime.parse(item.startTime, formatter)
            val currentTime = LocalTime.now(zoneId)
            
            if (currentTime.isBefore(scheduledTime)) {
                val minutesEarly = ChronoUnit.MINUTES.between(currentTime, scheduledTime)
                if (minutesEarly > 0) {
                    holder.binding.tvEarlyArrival.visibility = View.VISIBLE
                    holder.binding.tvEarlyArrival.text = "Earlier: ~${minutesEarly+2} min"
                }
            }
        } else {
            holder.binding.tvDistance.visibility = View.GONE
            
            if (nextStation != null) {
                if (nextStation.stationOrdial > item.stationOrdial) {
                    holder.binding.ivDone.setImageResource(R.drawable.ic_check)
                    holder.binding.vTimeline.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.greenish),
                    )
                    holder.binding.ivDone.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.context, R.color.greenish))
                } else {
                    holder.binding.ivDone.setImageResource(R.drawable.ic_hourglass)
                    holder.binding.vTimeline.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.waveALPHA),
                    )
                    holder.binding.ivDone.backgroundTintList = ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.context, R.color.waveALPHA))
                }
            } else {
                val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zagreb"))
                val zoneId = ZoneId.of("Europe/Zagreb")
                val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS")
                val startTime = LocalTime.parse(item.startTime, formatter)
                val arriveTime = startTime.atDate(LocalDate.now()).atZone(zoneId).toInstant()
                if (currentTime.toInstant().compareTo(arriveTime) < 0) {
                    holder.binding.ivDone.setImageResource(R.drawable.ic_hourglass)
                    holder.binding.vTimeline.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.wave),
                    )
                } else {
                    holder.binding.ivDone.setImageResource(R.drawable.ic_check)
                    holder.binding.vTimeline.setBackgroundColor(
                        ContextCompat.getColor(holder.itemView.context, R.color.grey),
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return lineStationsItems.size
    }
    
    /**
     * Returns the ID of the next station, or null if there is no next station
     */
    fun getNextStationId(): Int? {
        return nextStation?.stationId
    }
}
