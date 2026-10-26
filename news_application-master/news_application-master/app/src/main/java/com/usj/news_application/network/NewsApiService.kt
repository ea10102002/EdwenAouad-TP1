package com.usj.news_application.network

import retrofit2.http.GET
import retrofit2.http.Query
import com.usj.news_application.models.NewsResponse

interface NewsApiService {
    @GET("everything")
    suspend fun getNews(
        @Query("q") query: String,
        @Query("language") language: String = "en",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("apiKey") apiKey: String = "4638908b9b954264a7b823393155f9c9"
    ): NewsResponse
}
