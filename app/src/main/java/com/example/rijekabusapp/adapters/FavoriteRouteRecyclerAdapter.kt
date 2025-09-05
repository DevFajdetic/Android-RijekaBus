package com.example.rijekabusapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.RouteDetailActivity
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.databinding.RouteItemViewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class FavoriteRouteRecyclerAdapter(
    private val context: Context,
    private val favoritesList: ArrayList<FavoriteRoute>,
) : RecyclerView.Adapter<FavoriteRouteRecyclerAdapter.FavoriteRouteViewHolder>() {
    class FavoriteRouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = RouteItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): FavoriteRouteViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.route_item_view, parent, false)
        return FavoriteRouteViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: FavoriteRouteViewHolder,
        position: Int,
    ) {
        val item = favoritesList[position]

        // Set date
        val croatianLocale = Locale("hr", "HR")
        val inputFormat = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("EEEE, d. MMMM", croatianLocale)

        val parsedDate = inputFormat.parse(item.date)
        val formattedDate = outputFormat.format(parsedDate)
        holder.binding.activityDate.text = formattedDate
        
        // Set times
        holder.binding.tvStartStationTime.text = item.depTime
        holder.binding.tvEndStationTime.text = item.arrTime
        
        // Set station names
        holder.binding.tvStartStationName.text = item.origin
        holder.binding.tvEndStationName.text = item.destination
        
        // Set duration and distance
        holder.binding.duration.text = item.time
        holder.binding.distance.text = item.distance
        
        // Get routeType with fallback for older entries
        val routeType = item.routeType.ifEmpty { 
            // Determine type based on busUsed field for backward compatibility
            if (item.busUsed != "0") "BUS" else "WALK" 
        }
        
        // Set appropriate icons and colors based on route type
        when (routeType) {
            "BUS" -> {
                holder.binding.activityIcon.setImageResource(R.drawable.ic_bus)
                setupBusColors(holder, item.busUsed)
            }
            "WALK" -> {
                holder.binding.activityIcon.setImageResource(R.drawable.ic_walk)
                setupWalkColors(holder)
            }
            else -> { // MIXED
                // For mixed routes, check if there's a bus line used
                if (item.busUsed != "0") {
                    holder.binding.activityIcon.setImageResource(R.drawable.ic_bus)
                    setupBusColors(holder, item.busUsed)
                } else {
                    // Fallback to walk icon if no bus line info
                    holder.binding.activityIcon.setImageResource(R.drawable.ic_walk)
                    setupMixedColors(holder)
                }
            }
        }
        
        // Set click listener to open route details
        holder.itemView.setOnClickListener {
            try {
                val intent = Intent(context, RouteDetailActivity::class.java).apply {
                    putExtra("ROUTE_DATA", item)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Handle serialization errors gracefully
                e.printStackTrace()
            }
        }
    }
    
    private fun setupBusColors(holder: FavoriteRouteViewHolder, busLine: String) {
        val lineNumber = try {
            busLine.toInt()
        } catch (e: NumberFormatException) {
            0
        }
        
        // Select different colors based on line number
        when {
            lineNumber <= 5 -> {
                holder.binding.startStationImageContainer.setCardBackgroundColor(context.getColor(R.color.color_tertiary))
                holder.binding.endStationImageContainer.setCardBackgroundColor(context.getColor(R.color.color_tertiary))
            }
            lineNumber <= 10 -> {
                holder.binding.startStationImageContainer.setCardBackgroundColor(context.getColor(R.color.color_secondary))
                holder.binding.endStationImageContainer.setCardBackgroundColor(context.getColor(R.color.color_secondary))
            }
            else -> {
                holder.binding.startStationImageContainer.setCardBackgroundColor(context.getColor(R.color.color_primary))
                holder.binding.endStationImageContainer.setCardBackgroundColor(context.getColor(R.color.color_primary))
            }
        }
    }
    
    private fun setupWalkColors(holder: FavoriteRouteViewHolder) {
        holder.binding.startStationImageContainer.setCardBackgroundColor(context.getColor(R.color.greenish))
        holder.binding.endStationImageContainer.setCardBackgroundColor(context.getColor(R.color.greenish))
    }
    
    private fun setupMixedColors(holder: FavoriteRouteViewHolder) {
        holder.binding.startStationImageContainer.setCardBackgroundColor(context.getColor(R.color.quantum_yellow800))
        holder.binding.endStationImageContainer.setCardBackgroundColor(context.getColor(R.color.quantum_yellow800))
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }
}
