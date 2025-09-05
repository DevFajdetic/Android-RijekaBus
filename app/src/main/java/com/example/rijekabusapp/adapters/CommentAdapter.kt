package com.example.rijekabusapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class CommentAdapter(
    private val currentUserId: String,
    private val onLikeClickListener: (Map<String, Any>) -> Unit,
    private val onDeleteClickListener: (Map<String, Any>) -> Unit
) : ListAdapter<Map<String, Any>, CommentAdapter.CommentViewHolder>(CommentDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }
    
    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameText: TextView = itemView.findViewById(R.id.text_comment_username)
        private val commentText: TextView = itemView.findViewById(R.id.text_comment_body)
        private val timeText: TextView = itemView.findViewById(R.id.text_comment_time)
        private val likesText: TextView = itemView.findViewById(R.id.text_comment_likes)
        private val likeButton: ImageButton = itemView.findViewById(R.id.button_like_comment)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.button_delete_comment)
        
        fun bind(comment: Map<String, Any>) {
            usernameText.text = comment["username"] as String
            commentText.text = comment["comment"] as String
            
            // Format the timestamp
            val timestamp = comment["timestamp"] as Long
            timeText.text = getTimeAgo(timestamp)
            
            // Set up likes
            val likes = comment["likes"] as Int
            likesText.text = likes.toString()
            
            // Check if the current user liked this comment
            val likedByMap = comment["likedBy"] as Map<*, *>
            val isLiked = likedByMap.containsKey(currentUserId)
            
            // Update like button appearance
            likeButton.setImageResource(
                if (isLiked) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )
            
            // Set up click listeners
            likeButton.setOnClickListener {
                onLikeClickListener(comment)
            }
            
            // Show delete button only for user's own comments
            val userId = comment["userId"] as String
            if (userId == currentUserId) {
                deleteButton.visibility = View.VISIBLE
                deleteButton.setOnClickListener {
                    onDeleteClickListener(comment)
                }
            } else {
                deleteButton.visibility = View.GONE
            }
        }
        
        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
                diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
                diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
                else -> {
                    val dateFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                    dateFormat.format(Date(timestamp))
                }
            }
        }
    }
    
    private class CommentDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem["id"] == newItem["id"]
        }
        
        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem["comment"] == newItem["comment"] &&
                   oldItem["likes"] == newItem["likes"] &&
                   oldItem["likedBy"] == newItem["likedBy"]
        }
    }
} 