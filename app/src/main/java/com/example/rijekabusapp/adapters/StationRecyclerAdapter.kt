package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.StationActivity
import com.example.rijekabusapp.databinding.BusStationItemViewBinding
import com.example.rijekabusapp.network.models.Station

const val EXTRA_STATION = "com.example.rijekabusapp.extraStation"

class StationRecyclerAdapter(
    private val context: Context,
    private var stationsList: ArrayList<Station>,
    private val favoriteStations: ArrayList<Station>?,
    private val usedForFavorites: Boolean,
    private val insertCallback: ((Station) -> Unit)?,
    private val deleteCallback: ((Station) -> Unit)?
) : RecyclerView.Adapter<StationRecyclerAdapter.StationViewHolder>(), Filterable {
    private var filterList = stationsList

    class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = BusStationItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.bus_station_item_view, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = filterList[position]
        Log.d("rap", station.gpsX.toString())
        holder.binding.tvStationName.text = station.longName
        val exists = isStationFavorite(station.id)

        if (usedForFavorites) {
            holder.binding.btnFavorite.isActivated = true
            holder.binding.btnFavorite.setOnClickListener {
                onFavoriteStationButtonClick(station)
            }
        } else {
            holder.binding.btnFavorite.isActivated = exists
            holder.binding.btnFavorite.setOnClickListener {
                onRegularStationButtonClick(exists, holder, station)
            }
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, StationActivity::class.java).apply {
                putExtra(EXTRA_STATION, station)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    private fun isStationFavorite(stationId: Int): Boolean {
        var exists = false
        favoriteStations?.forEach {
            if (it.id == stationId) exists = true
        }
        return exists
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onFavoriteStationButtonClick(station: Station) {
        deleteCallback?.invoke(station)
        filterList.remove(station)
        notifyDataSetChanged()
    }

    private fun onRegularStationButtonClick(
        exists: Boolean,
        holder: StationViewHolder,
        station: Station
    ) {
        if (!exists) {
            holder.binding.btnFavorite.isActivated = true
            insertCallback?.invoke(station)
        } else {
            holder.binding.btnFavorite.isActivated = false
            deleteCallback?.invoke(station)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(text: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (text.isNullOrEmpty()) {
                    filterResults.values = ArrayList<Station>(stationsList)
                    filterList = stationsList
                } else {
                    val searchChar = text.toString().lowercase()
                    val searchItems = ArrayList<Station>()
                    for (it in filterList) {
                        if (it.longName.lowercase().contains(searchChar) ||
                            it.shortName.lowercase().contains(searchChar)
                        ) {
                            searchItems.add(it)
                        }
                    }

                    filterResults.values = searchItems
                }
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(p0: CharSequence?, filterResults: FilterResults?) {
                if (p0.isNullOrEmpty()) filterList = stationsList
                filterList = filterResults!!.values as ArrayList<Station>
                notifyDataSetChanged()
            }
        }
    }
}
