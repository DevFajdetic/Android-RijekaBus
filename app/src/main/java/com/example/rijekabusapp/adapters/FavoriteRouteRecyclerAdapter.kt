package com.example.rijekabusapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.database.models.FavoriteRoute
import com.example.rijekabusapp.databinding.RouteItemViewBinding

class FavoriteRouteRecyclerAdapter(
    private val context: Context,
    private val favoritesList: ArrayList<FavoriteRoute>,
) : RecyclerView.Adapter<FavoriteRouteRecyclerAdapter.FavoriteRouteViewHolder>() {

    class FavoriteRouteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = RouteItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteRouteViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.route_item_view, parent, false)
        return FavoriteRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteRouteViewHolder, position: Int) {
        val item = favoritesList[position]

        holder.binding.tvMatchDate.text = item.date
        holder.binding.tvHomeTeamPoints.text = item.depTime
        holder.binding.tvAwayTeamPoints.text = item.arrTime
        holder.binding.tvHomeTeamAbbreviation.text = item.origin
        holder.binding.tvAwayTeamAbbreviation.text = item.destination
        holder.binding.duration.text = item.time
        holder.binding.distance.text = item.distance
    }

    override fun getItemCount(): Int {
        return favoritesList.size
    }
}
