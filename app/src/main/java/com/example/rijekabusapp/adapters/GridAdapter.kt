package com.example.rijekabusapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.GridItemViewBinding
import com.example.rijekabusapp.network.models.Schedule

class GridAdapter(
    private val context: Context,
    private val itemList: List<Schedule>,
    private val activeList: List<Schedule>
) : RecyclerView.Adapter<GridAdapter.GridViewHolder>() {

    class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = GridItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val view = LayoutInflater
            .from(context)
            .inflate(R.layout.grid_item_view, parent, false)
        return GridViewHolder(view)
    }

    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        val item = itemList[position]

        if (activeList.any { it.startId == item.startId }) {
        } else {
            holder.binding.ivIsActive.visibility = View.GONE
            holder.binding.clContainer.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.surface_surface_2)
            )
        }

        holder.binding.tvTime.text = item.startTime.subSequence(0, 5)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
