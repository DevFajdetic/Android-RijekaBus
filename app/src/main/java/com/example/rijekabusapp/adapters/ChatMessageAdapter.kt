package com.example.rijekabusapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatMessageAdapter(private val currentUserId: String) : 
    ListAdapter<Map<String, Any>, ChatMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            VIEW_TYPE_MY_MESSAGE -> inflater.inflate(R.layout.item_message_sent, parent, false)
            else -> inflater.inflate(R.layout.item_message_received, parent, false)
        }
        return MessageViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }
    
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        val userId = message["userId"].toString()
        
        return if (userId == currentUserId) {
            VIEW_TYPE_MY_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }
    
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.text_message_body)
        private val timeText: TextView = itemView.findViewById(R.id.text_message_time)
        private val nameText: TextView? = itemView.findViewById(R.id.text_message_name)
        
        fun bind(message: Map<String, Any>) {
            messageText.text = message["message"] as String
            
            // Format the timestamp
            val timestamp = message["timestamp"] as Long
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = Date(timestamp)
            timeText.text = dateFormat.format(date)
            
            // Set the name for received messages
            nameText?.text = message["username"] as String
        }
    }
    
    private class MessageDiffCallback : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem["id"] == newItem["id"]
        }
        
        override fun areContentsTheSame(oldItem: Map<String, Any>, newItem: Map<String, Any>): Boolean {
            return oldItem == newItem
        }
    }
    
    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
    }
} 