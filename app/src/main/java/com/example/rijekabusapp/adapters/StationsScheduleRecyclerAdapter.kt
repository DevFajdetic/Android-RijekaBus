package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.rijekabusapp.R
import com.example.rijekabusapp.ScheduleActivity
import com.example.rijekabusapp.databinding.BusStationItemViewBinding
import com.example.rijekabusapp.network.models.Schedule
import com.example.rijekabusapp.network.models.Station

class StationsScheduleRecyclerAdapter(
    private val context: Context,
    private val stationScheduleList: List<Schedule>,
    private val stations: List<Station>,
) : RecyclerView.Adapter<StationsScheduleRecyclerAdapter.StationViewHolder>() {

    class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = BusStationItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.bus_station_item_view, parent, false)
        return StationViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val item = stationScheduleList[position]

        holder.binding.btnFavorite.visibility = View.GONE
        holder.binding.tvStationName.text = stations.first { it.id == item.stationId }.longName
        holder.binding.tvDirection.text = item.direction
        holder.binding.ivStationImage.load(R.drawable.ic_bus_stop_two)

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, ScheduleActivity::class.java).apply {
                putExtra(EXTRA_STATION, stations.first { it.id == item.stationId })
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return stationScheduleList.size
    }
}
