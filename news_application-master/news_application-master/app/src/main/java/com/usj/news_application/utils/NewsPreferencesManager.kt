package com.usj.news_application.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.usj.news_application.models.News

class NewsPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NewsPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveLastKnownNews(newsList: List<News>) {
        val newsJson = gson.toJson(newsList)
        prefs.edit().putString("LAST_KNOWN_NEWS", newsJson).apply()
    }

    fun getLastKnownNews(): List<News> {
        val newsJson = prefs.getString("LAST_KNOWN_NEWS", null)
        Log.d("NewsPrefs", "Retrieved JSON: $newsJson")
        return if (newsJson != null) {
            val type = object : TypeToken<List<News>>() {}.type
            gson.fromJson<List<News>>(newsJson, type).also {
                Log.d("NewsPrefs", "Parsed ${it.size} items: ${it.map { news -> news.id }}")
            }
        } else {
            emptyList()
        }
    }

    fun clearLastKnownNews() {
        prefs.edit().remove("LAST_KNOWN_NEWS").apply()
    }
}