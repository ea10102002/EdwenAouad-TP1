package com.usj.news_application.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.tabs.TabLayout
import com.usj.news_application.R
import com.usj.news_application.adapters.NewsAdapter
import com.usj.news_application.viewmodels.NewsPageViewModel
import com.usj.news_application.workers.NewsNotificationWorker
import androidx.core.content.ContextCompat

class NewsPage : Fragment() {
    private val viewModel: NewsPageViewModel by viewModels()
    private lateinit var newsUpdateReceiver: BroadcastReceiver
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_news_page, container, false)

        // Initialize the toolbar
        val toolbar: MaterialToolbar = view.findViewById(R.id.topAppBar)

        // Initialize the Profile Button
        val userInfoButton: ImageView = toolbar.findViewById(R.id.userInfoButton)
        userInfoButton.setOnClickListener {
            val sharedPreferences = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
            val loggedInEmail = sharedPreferences.getString("loggedInEmail", null)
            
            if (loggedInEmail.isNullOrEmpty()) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(requireContext(), AccountInfoActivity::class.java)
                startActivity(intent)
            }
        }
        val mapButton: ImageView = toolbar.findViewById(R.id.mapButton)
        mapButton.setOnClickListener {
            val intent = Intent(requireContext(), MapPage::class.java)
            startActivity(intent)
        }

        // Theme Toggle Button
        val themeButton: ImageView = toolbar.findViewById(R.id.themeButton)
        themeButton.setOnClickListener {
            val isNightMode = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
            if (isNightMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }

        // Language Toggle Button
        val langButton: TextView = toolbar.findViewById(R.id.langButton)
        langButton.setOnClickListener {
            val currentLang = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "en"
            if (currentLang == "en") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ar"))
            } else {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
            }
        }

        tabLayout = view.findViewById(R.id.tabLayout)
        setupTabs()

        // Initialize RecyclerView and ProgressBar
        val recyclerView: RecyclerView = view.findViewById(R.id.news_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        val adapter = NewsAdapter { selectedNews ->
            val intent = Intent(requireContext(), NewsDetailPage::class.java).apply {
                putExtra("NEWS_TITLE", selectedNews.title)
                putExtra("NEWS_CONTENT", selectedNews.content)
                putExtra("NEWS_LOCATION", selectedNews.location)
                putExtra("NEWS_DATETIME", selectedNews.datetime)
                putExtra("NEWS_DETAILED_DESCRIPTION", selectedNews.detailedDescription)
                putExtra("NEWS_IMAGE_URL", selectedNews.imageUrl)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observers for loading and data
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.newsList.observe(viewLifecycleOwner) { news ->
            adapter.submitList(news)
        }

        // Swipe-to-Refresh
        swipeRefreshLayout.setOnRefreshListener {
            val currentTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
            fetchNewsForTab(currentTab)
        }

        // Broadcast Receiver for updates
        newsUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NewsNotificationWorker.ACTION_NEWS_UPDATED) {
                    val currentTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                    fetchNewsForTab(currentTab)
                }
            }
        }
        ContextCompat.registerReceiver(
            requireContext(),
            newsUpdateReceiver,
            IntentFilter(NewsNotificationWorker.ACTION_NEWS_UPDATED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        return view
    }

    private fun setupTabs() {
        val categories = listOf(
            getString(R.string.tab_lebanon),
            getString(R.string.tab_international)
        )
        categories.forEach { category ->
            tabLayout.addTab(tabLayout.newTab().setText(category))
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedCategory = tab?.text.toString()
                fetchNewsForTab(selectedCategory)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun fetchNewsForTab(tabName: String) {
        val query = when (tabName) {
            getString(R.string.tab_lebanon) -> "Lebanon OR Beirut OR \"South Lebanon\""
            getString(R.string.tab_international) -> "World AND NOT Lebanon"
            else -> "Lebanon"
        }
        viewModel.loadNews(query)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(newsUpdateReceiver)
    }
}
