package com.usj.news_application.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.usj.news_application.models.News
import com.usj.news_application.network.RetrofitInstance
import com.usj.news_application.repositories.NewsRepository
import com.usj.news_application.utils.NewsPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class NewsNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    companion object {
        const val ACTION_NEWS_UPDATED = "com.usj.news_application.NEWS_UPDATED"
        private const val TAG = "NewsNotificationWorker"

        fun schedulePeriodicWork(context: Context) {
            Log.d(TAG, "Scheduling periodic work...")
            val newsWorkRequest = PeriodicWorkRequestBuilder<NewsNotificationWorker>(
                15, // Interval
                TimeUnit.SECONDS,
                5, // Flex interval
                TimeUnit.SECONDS
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "CheckNewNews",
                ExistingPeriodicWorkPolicy.KEEP,
                newsWorkRequest
            ).also {
                Log.d(TAG, "Work scheduled with ID: ${newsWorkRequest.id}")
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val preferencesManager = NewsPreferencesManager(applicationContext)
        val repository = NewsRepository(RetrofitInstance.api)
        try {
            Log.d(TAG, "Starting work execution...")

            // Fetch the latest news from the API
            val latestNews = repository.fetchNews()
            Log.d(TAG, "Fetched ${latestNews.size} news items: ${latestNews.map { it.id }}")

            // Get the last known news
            val lastKnownNews = preferencesManager.getLastKnownNews()
            Log.d(TAG, "Last known news (${lastKnownNews.size} items): ${lastKnownNews.map { it.id }}")

            // Find new news items
            val newNewsItems = findNewNewsItems(lastKnownNews, latestNews)
            Log.d(TAG, "Found ${newNewsItems.size} new items: ${newNewsItems.map { it.id }}")

            // If there are new news items, send notifications
            if (newNewsItems.isNotEmpty()) {
                Log.d(TAG, "Preparing to send notifications...")

                // Broadcast an intent to signal news update
                val intent = Intent(ACTION_NEWS_UPDATED)
                applicationContext.sendBroadcast(intent)
                Log.d(TAG, "Broadcast sent")

                newNewsItems.forEach { newsItem ->
                    try {
                        sendNewsNotification(newsItem)
                        Log.d(TAG, "Notification sent for news ID: ${newsItem.id}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to send notification for news ID: ${newsItem.id}", e)
                    }
                }

                preferencesManager.saveLastKnownNews(latestNews)
                Log.d(TAG, "Updated last known news in preferences")
            } else {
                Log.d(TAG, "No new news items found")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Work failed with exception", e)
            Result.failure()
        }
    }

    private fun findNewNewsItems(oldNews: List<News>, newNews: List<News>): List<News> {
        // Add debug logging for comparison
        Log.d(TAG, "Comparing old news IDs: ${oldNews.map { it.id }}")
        Log.d(TAG, "With new news IDs: ${newNews.map { it.id }}")

        return newNews.filter { newItem ->
            val isNew = oldNews.none { it.id == newItem.id }
            Log.d(TAG, "News item ${newItem.id} is new: $isNew")
            isNew
        }
    }

    private fun sendNewsNotification(news: News) {
        Log.d(TAG, "Creating notification for news ID: ${news.id}")

        val notificationManager = applicationContext.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "NEW_NEWS_CHANNEL",
                "New News Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for news update notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }

        // Build the notification
        val notification = NotificationCompat.Builder(applicationContext, "NEW_NEWS_CHANNEL")
            .setContentTitle("Breaking News: ${news.title}")
            .setContentText(news.content)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(news.content)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Add a default icon
            .build()

        // Show the notification with a unique ID for each news item
        val notificationId = news.id.hashCode()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notification sent with ID: $notificationId")
    }
}