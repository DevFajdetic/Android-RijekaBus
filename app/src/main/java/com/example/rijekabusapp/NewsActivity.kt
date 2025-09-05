package com.example.rijekabusapp

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.rijekabusapp.adapters.EXTRA_NEWS
import com.example.rijekabusapp.databinding.ActivityNewsBinding
import com.example.rijekabusapp.helpers.LanguageHelper
import com.example.rijekabusapp.network.models.News

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply language settings
        LanguageHelper.applyLanguage(this)
        
        super.onCreate(savedInstanceState)

        binding = ActivityNewsBinding.inflate(layoutInflater)

        val newsItem = intent.getSerializableExtra(EXTRA_NEWS) as? News

        if (newsItem != null) {
            binding.title.text = newsItem.title
            binding.body.text = newsItem.body
        }
        binding.ivBack.setOnClickListener {
            this.onBackPressed()
        }
        setContentView(binding.root)
    }
    
    // Override attachBaseContext to apply language settings
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageHelper.createConfigurationContext(newBase))
    }
    
    // Apply language when resuming the activity
    override fun onResume() {
        super.onResume()
        LanguageHelper.applyLanguage(this)
    }
}
