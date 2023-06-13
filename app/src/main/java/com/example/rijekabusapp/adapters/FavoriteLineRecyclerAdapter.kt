package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.LineActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.database.models.FavoriteLine
import com.example.rijekabusapp.databinding.BusLineItemViewBinding
import com.example.rijekabusapp.helpers.ItemMoveCallback

class FavoriteLineRecyclerAdapter(
    private val context: Context,
    private val favoritesList: ArrayList<FavoriteLine>,
    private var isEditModeEnabled: Boolean,
    private val deleteCallback: (FavoriteLine) -> Unit
) : RecyclerView.Adapter<FavoriteLineRecyclerAdapter.FavoriteLineViewHolder>(),
    ItemMoveCallback.ItemTouchHelperAdapter {

    class FavoriteLineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = BusLineItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteLineViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.bus_line_item_view, parent, false)
        return FavoriteLineViewHolder(view)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: FavoriteLineViewHolder, position: Int) {
        val line = favoritesList[position]

        if (isEditModeEnabled) {
            holder.binding.ivMove.visibility = View.VISIBLE
        } else {
            holder.binding.ivMove.visibility = View.GONE
        }

        holder.binding.ivLineNumber.text = line.lineNumber[0].toString()
        holder.binding.tvLineName.text = line.name
        holder.binding.tvDirection.text = line.direction

        holder.binding.btnFavorite.setImageResource(R.drawable.ic_star_full)
        holder.binding.btnFavorite.setOnClickListener {
            if (!isEditModeEnabled) {
                deleteCallback.invoke(line)
                favoritesList.remove(line)
                notifyDataSetChanged()
            }
        }

        holder.binding.root.setOnClickListener {
            if (!isEditModeEnabled) {
                val intent = Intent(context, LineActivity::class.java).apply {
                    putExtra(EXTRA_LINE, line.convertToLine())
                }
                context.startActivity(intent)
            }
        }
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
