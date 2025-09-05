package com.example.rijekabusapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusRatingAdapter : 
    ListAdapter<Map<String, Any>, BusRatingAdapter.RatingViewHolder>(RatingDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RatingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bus_rating, parent, false)
        return RatingViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RatingViewHolder, position: Int) {
        val rating = getItem(position)
        holder.bind(rating)
    }
    
    inner class RatingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.text_rating_username)
        private val timeText: TextView = itemView.findViewById(R.id.text_rating_time)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar_item)
        
        fun bind(rating: Map<String, Any>) {
            usernameText.text = rating["username"] as String
            
            // Set the rating
            ratingBar.rating = (rating["rating"] as Float)
            
            // Format the timestamp
            val timestamp = rating["timestamp"] as Long
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(timestamp)
            timeText.text = dateFormat.format(date)
        }
    }
    
    private class RatingDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem["id"] == newItem["id"]
        }
        
        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem["rating"] == newItem["rating"]
        }
    }
} 