package com.usj.news_application.repositories

import android.util.Log
import com.usj.news_application.models.News
import com.usj.news_application.network.NewsApiService

class NewsRepository(private val apiService: NewsApiService) {
    suspend fun fetchNews(query: String = "Lebanon"): List<News> {
        return try {
            val response = apiService.getNews(query = query)
            val articles = response.articles ?: emptyList()
            val newsList = articles.map { it.toNewsItem() }
            Log.d("NewsRepository", "Fetched ${newsList.size} news items from API")
            newsList
        } catch (e: Exception) {
            Log.e("NewsRepository", "Error fetching news", e)
            emptyList()
        }
    }
}