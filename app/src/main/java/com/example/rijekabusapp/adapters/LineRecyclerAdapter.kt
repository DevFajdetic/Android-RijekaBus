package com.example.rijekabusapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.LineActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.BusLineItemViewBinding
import com.example.rijekabusapp.helpers.ItemMoveCallback
import com.example.rijekabusapp.helpers.generateUniqueColor
import com.example.rijekabusapp.network.models.Line
import java.util.Collections

const val EXTRA_LINE = "com.example.rijekabusapp.extraLine"

class LineRecyclerAdapter(
    private val context: Context,
    private var linesList: ArrayList<Line>,
    private val favoriteLines: ArrayList<Line>?,
    private val usedForFavorites: Boolean,
    private val insertCallback: ((Line) -> Unit)?,
    private val deleteCallback: ((Line) -> Unit)?,
) : RecyclerView.Adapter<LineRecyclerAdapter.LineViewHolder>(),
    ItemMoveCallback.ItemTouchHelperAdapter,
    Filterable {
    private var filterList = linesList
    private var savedPositions: List<Int> = emptyList()

    class LineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = BusLineItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LineViewHolder {
        val view =
            LayoutInflater
                .from(context)
                .inflate(R.layout.bus_line_item_view, parent, false)
        return LineViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: LineViewHolder,
        position: Int,
    ) {
        val line = filterList[position]

        holder.binding.ivLineNumber.text = line.lineNumber
        holder.binding.ivLineNumber
            .setBackgroundColor(Color.parseColor(generateUniqueColor(line.lineNumber)))
        holder.binding.tvLineName.text = line.name
        holder.binding.tvDirection.text = line.direction

        val exists = isLineFavorite(line.name)
        if (usedForFavorites) {
            holder.binding.btnFavorite.isActivated = true
            holder.binding.btnFavorite.setOnClickListener {
                onFavoriteLineButtonClick(line)
            }
        } else {
            holder.binding.btnFavorite.isActivated = exists
            holder.binding.btnFavorite.setOnClickListener {
                onRegularLineButtonClick(exists, holder, line)
            }
        }

        holder.binding.root.setOnClickListener {
            val intent =
                Intent(context, LineActivity::class.java).apply {
                    putExtra(EXTRA_LINE, line)
                }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    private fun isLineFavorite(name: String): Boolean {
        var exists = false
        favoriteLines?.forEach {
            if (it.name == name) exists = true
        }
        return exists
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onFavoriteLineButtonClick(line: Line) {
        deleteCallback?.invoke(line)
        filterList.remove(line)
        notifyDataSetChanged()
    }

    private fun onRegularLineButtonClick(
        exists: Boolean,
        holder: LineViewHolder,
        line: Line,
    ) {
        if (!exists) {
            holder.binding.btnFavorite.isActivated = true
            insertCallback?.invoke(line)
        } else {
            holder.binding.btnFavorite.isActivated = false
            deleteCallback?.invoke(line)
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(text: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (text.isNullOrEmpty()) {
                    // Return all items when query is empty or less than 3 characters
                    Log.d("filter", "Empty or less than 3 chars: ${text?.length ?: 0}")
                    val allLines = ArrayList<Line>(linesList)
                    filterResults.values = allLines
                    filterResults.count = allLines.size
                } else {
                    val searchChar = text.toString().lowercase()
                    val searchItems = ArrayList<Line>()

                    // Filter when we have at least 3 characters
                    Log.d("filter", "Filtering with: $searchChar")
                    for (it in linesList) {
                        if (it.name.lowercase().contains(searchChar) ||
                            it.lineNumber.lowercase().contains(searchChar)
                        ) {
                            android.util.Log.d("Filter", "Match: ${it.name}")
                            searchItems.add(it)
                        }
                    }

                    filterResults.values = searchItems
                    filterResults.count = searchItems.size
                    Log.d("filter", "Found ${searchItems.size} matches")
                }
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(
                p0: CharSequence?,
                filterResults: FilterResults?,
            ) {
                filterList = filterResults?.values as ArrayList<Line>
                Log.d("filter", "Publishing results: ${filterList.size} items")
                notifyDataSetChanged()
            }
        }
    }

    fun getSavedPositions(): List<Int> {
        return savedPositions
    }

    fun setSavedPositions(positions: List<Int>) {
        savedPositions = positions
    }

    override fun onItemMove(
        sourcePosition: Int,
        targetPosition: Int,
    ) {
        // Update the positions in the adapter, but don't save them yet
        if (sourcePosition < targetPosition) {
            for (i in sourcePosition until targetPosition) {
                Collections.swap(linesList, i, i + 1)
            }
        } else {
            for (i in sourcePosition downTo targetPosition + 1) {
                Collections.swap(linesList, i, i - 1)
            }
        }
        notifyItemMoved(sourcePosition, targetPosition)
    }
}
