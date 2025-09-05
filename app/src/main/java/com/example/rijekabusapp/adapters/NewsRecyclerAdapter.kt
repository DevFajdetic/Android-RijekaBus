package com.example.rijekabusapp.adapters

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rijekabusapp.NewsActivity
import com.example.rijekabusapp.R
import com.example.rijekabusapp.databinding.NewsItemViewBinding
import com.example.rijekabusapp.network.models.News

const val EXTRA_NEWS = "com.example.rijekabusapp.extraNews"

class NewsRecyclerAdapter(
    private val context: Context,
    private val newsList: List<News>,
) : RecyclerView.Adapter<NewsRecyclerAdapter.NewsViewHolder>() {
    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = NewsItemViewBinding.bind(view)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NewsViewHolder {
        val view =
            LayoutInflater
                .from(context)
                .inflate(R.layout.news_item_view, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: NewsViewHolder,
        position: Int,
    ) {
        val newsItem = newsList[position]

        holder.binding.tvDate.text = newsItem.date
        holder.binding.tvTitle.text = newsItem.title
        when (newsItem.category) {
            "Promjene i preregulacija" -> {
                holder.binding.tvIcon.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.context, R.color.color_traffic_changes),
                    )
            }
            "Vozni red i linije" -> {
                holder.binding.tvIcon.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.context, R.color.color_drive_schedule_and_lines),
                    )
            }
            "Obavijesti" -> {
                holder.binding.tvIcon.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.context, R.color.color_warning_sign),
                    )
            }
        }

        holder.binding.root.setOnClickListener {
            val intent =
                Intent(context, NewsActivity::class.java).apply {
                    putExtra(EXTRA_NEWS, newsItem)
                }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return newsList.size
    }
}
