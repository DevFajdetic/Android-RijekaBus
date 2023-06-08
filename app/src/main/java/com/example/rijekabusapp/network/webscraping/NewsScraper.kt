package com.example.rijekabusapp.network.webscraping

import com.example.rijekabusapp.network.models.News
import kotlinx.coroutines.*
import org.jsoup.Jsoup

object NewsScraper {
    private const val url = "https://www.autotrolej.hr/novosti-i-obavijesti/"

    suspend fun scrapeNews(): List<News> = coroutineScope {
        val doc = withContext(Dispatchers.IO) { Jsoup.connect(url).get() }
        val articles = doc.select("article")

        val newsItems = articles.map { article ->
            async(Dispatchers.IO) {
                val titleElement = article.selectFirst("h3.entry-title a")
                val title = titleElement?.text()
                val link = titleElement?.absUrl("href")

                val linkedDoc = withContext(Dispatchers.IO) { Jsoup.connect(link).get() }
                val bodyElements = linkedDoc.select("div.entry-content p")
                val body = bodyElements.joinToString(separator = "\n") { it.text() }

                val categoryElements = article.select("div.news-meta span")
                val category = categoryElements.firstOrNull()?.text()

                val dateElement = article.selectFirst("div.newscat-subtitle")
                val date = dateElement?.text()

                News(title, body, category, date)
            }
        }.awaitAll()

        return@coroutineScope newsItems
    }
}
