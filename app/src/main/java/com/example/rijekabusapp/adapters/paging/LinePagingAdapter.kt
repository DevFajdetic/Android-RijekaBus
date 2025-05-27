package com.example.rijekabusapp.adapters.paging

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.LineActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.BusLineItemViewBinding
import com.example.rijekabusapp.network.models.Line

class LinePagingAdapter(
    private val context: Context,
    private val favouriteLines: ArrayList<Line>?,
    diffCallback: DiffUtil.ItemCallback<Line>,
    private val insertCallback: (Line) -> Unit,
    private val deleteCallback: (Line) -> Unit,
) :
    PagingDataAdapter<Line, LinePagingAdapter.LineViewHolder>(diffCallback) {
    class LineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = BusLineItemViewBinding.bind(itemView)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LineViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.bus_line_item_view, parent, false)
        return LineViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LineViewHolder,
        position: Int,
    ) {
        val line = getItem(position)

        holder.binding.ivLineNumber.text = line?.lineNumber?.get(0).toString()
        holder.binding.tvLineName.text = line?.name
        holder.binding.tvDirection.text = line?.direction

        val exists = isLineFavorite(line!!.name)

        holder.binding.btnFavorite.isActivated = exists
        holder.binding.btnFavorite.setOnClickListener {
            if (!exists) {
                holder.binding.btnFavorite.isActivated = true
                insertCallback.invoke(line)
            } else {
                holder.binding.btnFavorite.isActivated = false
                deleteCallback.invoke(line)
            }
        }

        holder.binding.root.setOnClickListener {
            val intent = Intent(context, LineActivity::class.java)
            context.startActivity(intent)
        }
    }

    private fun isLineFavorite(name: String): Boolean {
        var exists = false
        favouriteLines?.forEach {
            if (it.name == name) exists = true
        }
        return exists
    }
}
