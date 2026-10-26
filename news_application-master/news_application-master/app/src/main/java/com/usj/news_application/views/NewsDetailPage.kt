package com.usj.news_application.views

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.usj.news_application.R

class NewsDetailPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_details_page)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener { onBackPressed() }

        val title = intent.getStringExtra("NEWS_TITLE")
        val content = intent.getStringExtra("NEWS_CONTENT")
        val location = intent.getStringExtra("NEWS_LOCATION")
        val datetime = intent.getStringExtra("NEWS_DATETIME")
        val detailedDescription = intent.getStringExtra("NEWS_DETAILED_DESCRIPTION")
        val imageUrl = intent.getStringExtra("NEWS_IMAGE_URL")

        supportActionBar?.title = title ?: "News Details"

        findViewById<TextView>(R.id.news_content).text = content
        findViewById<TextView>(R.id.news_location).text = location
        
        val cleanDateTime = datetime?.replace('T', ' ')?.substringBefore("Z")
        findViewById<TextView>(R.id.news_datetime).text = cleanDateTime
        
        findViewById<TextView>(R.id.news_detailed_description).text = detailedDescription
        
        val imageView = findViewById<ImageView>(R.id.news_image)
        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl)
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .into(imageView)
        } else {
            imageView.setImageResource(android.R.color.darker_gray)
        }
    }
}