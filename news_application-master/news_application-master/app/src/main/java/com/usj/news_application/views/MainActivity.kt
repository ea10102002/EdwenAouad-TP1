package com.usj.news_application.views

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.usj.news_application.R
import com.usj.news_application.workers.NewsNotificationWorker

class MainActivity : AppCompatActivity() {
    // In MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Schedule periodic work for notifications
        NewsNotificationWorker.schedulePeriodicWork(this)

        // Check worker status
        WorkManager.getInstance(applicationContext)
            .getWorkInfosForUniqueWorkLiveData("CheckNewNews")
            .observe(this) { workInfos ->
                Log.d("NewsDebug", "Work info status: ${workInfos?.firstOrNull()?.state}")
            }

        // Load the NewsPage fragment into the fragment container
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, NewsPage())
            .commit()

        // for google maps api
        Places.initialize(applicationContext, "AIzaSyCUFM6fYcK7qGm2W91Z6w7C6zKIEDiZ3Xo")
    }
}
