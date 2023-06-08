package com.example.rijekabusapp.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rijekabusapp.R
import com.example.rijekabusapp.adapters.NewsRecyclerAdapter
import com.example.rijekabusapp.databinding.FragmentNewsBinding
import com.example.rijekabusapp.network.models.News
import com.example.rijekabusapp.network.webscraping.NewsScraper
import kotlinx.coroutines.*

class NewsFragment : Fragment() {
    private lateinit var binding: FragmentNewsBinding
    private lateinit var adapter: NewsRecyclerAdapter
    private val newsItems = mutableListOf<News>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewsBinding.inflate(inflater, container, false)
        binding.rvNews.layoutManager = LinearLayoutManager(requireContext())

        binding.ivBack.setOnClickListener {
            Navigation.findNavController(binding.root)
                .navigate(R.id.action_newsFragment_to_exploreFragment)
        }

        adapter = NewsRecyclerAdapter(requireContext(), newsItems)
        binding.rvNews.adapter = adapter
        scrapeNews()

        return binding.root
    }

    private fun scrapeNews() {
        Log.d("uso", "uso")
        binding.progressBar.visibility = View.VISIBLE

        MainScope().launch {
            Log.d("uso", "startam rutine")
            val newNewsItems = withContext(Dispatchers.IO) {
                NewsScraper.scrapeNews()
            }
            Log.d("uso", "szavrsio rutine")
            newsItems.clear()
            newsItems.addAll(newNewsItems)
            adapter.notifyItemRangeInserted(0, newNewsItems.size)

            binding.progressBar.visibility = View.GONE
        }
    }
}
