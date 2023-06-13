package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.StationActivity
import com.example.rijekabusapp.database.models.FavoriteStation
import com.example.rijekabusapp.databinding.BusStationItemViewBinding
import com.example.rijekabusapp.helpers.ItemMoveCallback

class FavoriteStationRecyclerAdapter(
    private val context: Context,
    private val favoritesList: ArrayList<FavoriteStation>,
    // private val imagesList: ArrayList<ArrayList<StationImage>>,
    private var isEditModeEnabled: Boolean,
    private val deleteCallback: (FavoriteStation) -> Unit
) : RecyclerView.Adapter<FavoriteStationRecyclerAdapter.FavoriteStationViewHolder>(),
    ItemMoveCallback.ItemTouchHelperAdapter {

    class FavoriteStationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = BusStationItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteStationViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bus_station_item_view, parent, false)
        return FavoriteStationViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: FavoriteStationViewHolder, position: Int) {
        val station = favoritesList[position]

        if (isEditModeEnabled) {
            holder.binding.ivMove.visibility = View.VISIBLE
        } else {
            holder.binding.ivMove.visibility = View.GONE
        }

        holder.binding.tvStationName.text = station.longName

        holder.binding.btnFavorite.setImageResource(R.drawable.ic_star_full)
        holder.binding.btnFavorite.setOnClickListener {
            if (!isEditModeEnabled) {
                deleteCallback.invoke(station)
                favoritesList.remove(station)
                notifyDataSetChanged()
            }
        }

        holder.binding.root.setOnClickListener {
            if (!isEditModeEnabled) {
                val intent = Intent(context, StationActivity::class.java).apply {
                    putExtra(EXTRA_STATION, station.convertToStation())
                }
                context.startActivity(intent)
            }
        }
        /*imagesList.forEach {
            for (i in 0..it.size) {
                if (it[0].playerId == player.id) {
                    Picasso.get().load(it[0].imageUrl).fit().centerCrop()
                        .into(holder.binding.ivPlayerImage)
                    return
                } else {
                    loadPlayerImagePlaceholder(position, holder.binding.ivPlayerImage)
                }
            }
        } */
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }

    fun setEditModeEnabled(isEditModeEnabled: Boolean) {
        this.isEditModeEnabled = isEditModeEnabled
    }

    override fun onItemMove(sourcePosition: Int, targetPosition: Int) {
        if (isEditModeEnabled) {
            val movedItem = favoritesList.removeAt(sourcePosition)
            favoritesList.add(targetPosition, movedItem)
            notifyItemMoved(sourcePosition, targetPosition)
        }
    }
}
