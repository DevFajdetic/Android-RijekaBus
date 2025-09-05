package com.example.rijekabusapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.GridItemViewBinding
import com.example.rijekabusapp.databinding.ItemStationTimelineBinding
import com.example.rijekabusapp.databinding.LineStationsScheduleItemBinding
import com.example.rijekabusapp.network.models.Schedule

class TimelineAdapter(
    private val context: Context,
    private val departures: List<Schedule>,
    private val activeList: List<Schedule>,
    private val stationMode: Boolean = false,
    private val stationId: Int? = null,
    private val allSchedules: List<Schedule> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var expandedPosition = -1
    
    companion object {
        private const val VIEW_TYPE_BUS = 0
        private const val VIEW_TYPE_STATION = 1
    }

    class BusTimelineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = GridItemViewBinding.bind(view)
    }
    
    class StationScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LineStationsScheduleItemBinding.bind(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (stationMode) VIEW_TYPE_STATION else VIEW_TYPE_BUS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BUS -> {
                val view = LayoutInflater.from(context).inflate(R.layout.grid_item_view, parent, false)
                BusTimelineViewHolder(view)
            }
            VIEW_TYPE_STATION -> {
                val view = LayoutInflater.from(context).inflate(R.layout.line_stations_schedule_item, parent, false)
                StationScheduleViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val schedule = departures[position]
        
        when (holder.itemViewType) {
            VIEW_TYPE_BUS -> bindBusTimelineView(holder as BusTimelineViewHolder, schedule, position)
            VIEW_TYPE_STATION -> bindStationScheduleView(holder as StationScheduleViewHolder, schedule)
        }
    }
    
    private fun bindBusTimelineView(holder: BusTimelineViewHolder, departure: Schedule, position: Int) {
        val isActive = activeList.any { it.startId == departure.startId }
        val isExpanded = position == expandedPosition

        // Set up the main item view
        holder.binding.tvTime.text = departure.startTime.subSequence(0, 5)

        // Handle active state visual indicator
        if (isActive) {
            holder.binding.ivIsActive.visibility = View.VISIBLE
            holder.binding.clContainer.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary_color_transparent)
            )
        } else {
            holder.binding.ivIsActive.visibility = View.GONE
            holder.binding.clContainer.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.surface_surface_2)
            )
        }

        // Update expand icon based on expanded state
        holder.binding.ivExpand.setImageResource(
            if (isExpanded) R.drawable.ic_expand_less else R.drawable.ic_expand_more
        )

        // Handle expanded state - show station timeline or bus stops
        if (isExpanded) {
            // Show expanded content - either station timeline or bus station arrivals
            holder.binding.expandedContent.visibility = View.VISIBLE

            if (allSchedules.isNotEmpty()) {
                // Show all stations for this bus departure
                val stationSchedules = allSchedules.filter {
                    it.startId == departure.startId
                }.sortedBy {
                    it.stationOrdial
                }

                val stationsAdapter = StationTimelineAdapter(context, stationSchedules)
                holder.binding.rvStationTimeline.layoutManager = LinearLayoutManager(context)
                holder.binding.rvStationTimeline.adapter = stationsAdapter
                holder.binding.rvStationTimeline.visibility = View.VISIBLE
            }
        } else {
            // Collapse the view
            holder.binding.expandedContent.visibility = View.GONE
            holder.binding.rvStationTimeline.visibility = View.GONE
        }

        // Set click listener to expand/collapse
        holder.itemView.setOnClickListener {
            val previousExpandedPosition = expandedPosition
            expandedPosition = if (isExpanded) -1 else position

            // Notify changes
            if (previousExpandedPosition >= 0) {
                notifyItemChanged(previousExpandedPosition)
            }
            notifyItemChanged(position)
        }
    }
    
    private fun bindStationScheduleView(holder: StationScheduleViewHolder, schedule: Schedule) {
        // Set arrival time
        holder.binding.tvArriveTime.text = schedule.startTime.substring(0, 5)
        
        // Set station name to show bus line info instead
        holder.binding.tvStationName.text = "${schedule.lineNumber} - ${schedule.variantLineName}"
        
        // Set timeline indicators
        holder.binding.ivSpace.visibility = View.GONE
        
        // Configure notification icon visibility - set to GONE for simplicity
        holder.binding.ivNotification.visibility = View.GONE
        
        // Configure early arrival and distance - hide them for simplicity
        holder.binding.tvEarlyArrival.visibility = View.GONE
        holder.binding.tvDistance.visibility = View.GONE
        
        // Show bus icon for the timeline indicator
        holder.binding.ivDone.setImageResource(R.drawable.ic_bus)
        holder.binding.ivDone.setImageTintList(ContextCompat.getColorStateList(context, R.color.white))
        
        // Highlight active buses
        if (activeList.any { it.startId == schedule.startId }) {
            holder.binding.ivDone.backgroundTintList = 
                ContextCompat.getColorStateList(context, R.color.sub_green)
            // Highlight the background for active buses
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, R.color.primary_color_transparent)
            )
        } else {
            holder.binding.ivDone.backgroundTintList = 
                ContextCompat.getColorStateList(context, R.color.color_tertiary)
            holder.itemView.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.transparent)
            )
        }
        
        // Set the vertical timeline color
        holder.binding.vTimeline.setBackgroundColor(
            ContextCompat.getColor(context, R.color.color_secondary)
        )
    }

    override fun getItemCount(): Int {
        return departures.size
    }
}

/**
 * Adapter for showing station arrival times in the expanded view
 */
class StationTimelineAdapter(
    private val context: Context,
    private val stationSchedules: List<Schedule>
) : RecyclerView.Adapter<StationTimelineAdapter.StationViewHolder>() {

    class StationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ItemStationTimelineBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_station_timeline, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stationSchedules[position]

        holder.binding.tvStationName.text = station.name
        holder.binding.tvArrivalTime.text = station.startTime.substring(0, 5)

        // Configure timeline line visibility
        // Hide the line for the last item
        if (position == stationSchedules.size - 1) {
            holder.binding.timelineLine.visibility = View.INVISIBLE
        } else {
            holder.binding.timelineLine.visibility = View.VISIBLE
        }

        holder.binding.timelineDot.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.sub_green)
        holder.binding.tvStationName.setTypeface(null, android.graphics.Typeface.BOLD)
    }

    override fun getItemCount(): Int {
        return stationSchedules.size
    }
}

