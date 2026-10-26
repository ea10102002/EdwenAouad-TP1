package com.usj.news_application.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.usj.news_application.models.News
import com.usj.news_application.network.RetrofitInstance
import com.usj.news_application.repositories.NewsRepository
import kotlinx.coroutines.launch

class NewsPageViewModel : ViewModel() {
    private val repository = NewsRepository(RetrofitInstance.api)

    private val _newsList = MutableLiveData<List<News>>()
    val newsList: LiveData<List<News>> get() = _newsList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    init {
        loadNews()
    }

    fun loadNews(query: String = "lebanon OR beirut OR \"middle east\"") {
        _isLoading.value = true
        _error.value = null // Reset any previous error
        viewModelScope.launch {
            try {
                val fetchedNews = repository.fetchNews(query)
                Log.d("NewsPageViewModel", "Loaded ${fetchedNews.size} news items")
                _newsList.value = fetchedNews

                if (fetchedNews.isEmpty()) {
                    _error.value = "No news items found"
                    Log.w("NewsPageViewModel", "No news items found")
                }
            } catch (e: Exception) {
                val errorMessage = "Failed to load news: ${e.message}"
                _error.value = errorMessage
                Log.e("NewsPageViewModel", errorMessage, e)
            }
            finally {
                _isLoading.value = false
            }
        }
    }

    fun forceRefresh() {
        loadNews()
    }
}
