package com.example.rijekabusapp.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R

class LineFilterAdapter(
    private val context: Context,
    private val lineNumbers: List<String>,
    private val lineDirections: List<String>
) : RecyclerView.Adapter<LineFilterAdapter.LineFilterViewHolder>() {

    // Keep track of selected lines
    private val selectedLines = mutableMapOf<String, Boolean>()
    
    init {
        // Initialize all lines as unselected
        for (i in lineNumbers.indices) {
            val key = "${lineNumbers[i]}_${lineDirections[i]}"
            selectedLines[key] = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineFilterViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_line_filter, parent, false)
        return LineFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: LineFilterViewHolder, position: Int) {
        val lineNumber = lineNumbers[position]
        val lineDirection = lineDirections[position]
        val key = "${lineNumber}_${lineDirection}"
        
        // Set the line number and direction
        holder.tvLineNumber.text = lineNumber
        holder.tvLineDirection.text = lineDirection
        
        // Set the line color indicator
        holder.cardLineColor.setCardBackgroundColor(getColorForLine(lineNumber))
        
        // Set checkbox state
        holder.cbLineSelected.isChecked = selectedLines[key] == true
        
        // Set click listener for the whole item
        holder.itemView.setOnClickListener {
            // Toggle selection
            val isSelected = !(selectedLines[key] ?: false)
            selectedLines[key] = isSelected
            holder.cbLineSelected.isChecked = isSelected
        }
        
        // Set click listener for checkbox
        holder.cbLineSelected.setOnClickListener {
            selectedLines[key] = holder.cbLineSelected.isChecked
        }
    }

    override fun getItemCount(): Int = lineNumbers.size

    // Get all selected line numbers and directions
    fun getSelectedLines(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        for (i in lineNumbers.indices) {
            val lineNumber = lineNumbers[i]
            val lineDirection = lineDirections[i]
            val key = "${lineNumber}_${lineDirection}"
            if (selectedLines[key] == true) {
                result.add(Pair(lineNumber, lineDirection))
            }
        }
        return result
    }
    
    // Clear all selections
    fun clearSelections() {
        for (key in selectedLines.keys) {
            selectedLines[key] = false
        }
        notifyDataSetChanged()
    }
    
    // Select specific lines
    fun selectLines(lines: List<Pair<String, String>>) {
        clearSelections()
        for (line in lines) {
            val key = "${line.first}_${line.second}"
            selectedLines[key] = true
        }
        notifyDataSetChanged()
    }
    
    // Get a consistent color for a line number
    private fun getColorForLine(lineNumber: String): Int {
        return when (lineNumber) {
            "1" -> Color.parseColor("#FF5722") // Deep Orange
            "2" -> Color.parseColor("#2196F3") // Blue
            "3" -> Color.parseColor("#4CAF50") // Green
            "4" -> Color.parseColor("#9C27B0") // Purple
            "5" -> Color.parseColor("#FFC107") // Amber
            "6" -> Color.parseColor("#795548") // Brown
            "7" -> Color.parseColor("#607D8B") // Blue Grey
            "8" -> Color.parseColor("#E91E63") // Pink
            "9" -> Color.parseColor("#00BCD4") // Cyan
            else -> {
                // Generate a color based on hash of line number
                val hash = lineNumber.hashCode()
                val hue = (hash % 360).toFloat()
                val hsv = floatArrayOf(hue, 0.8f, 0.8f)
                Color.HSVToColor(hsv)
            }
        }
    }

    class LineFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLineNumber: TextView = itemView.findViewById(R.id.tvLineNumber)
        val tvLineDirection: TextView = itemView.findViewById(R.id.tvLineDirection)
        val cbLineSelected: CheckBox = itemView.findViewById(R.id.cbLineSelected)
        val cardLineColor: CardView = itemView.findViewById(R.id.cardLineColor)
    }
} 