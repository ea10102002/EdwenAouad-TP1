package com.usj.news_application

import android.app.Application
import com.google.android.libraries.places.api.Places

class NewsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, "AIzaSyCUFM6fYcK7qGm2W91Z6w7C6zKIEDiZ3Xo")
    }
}