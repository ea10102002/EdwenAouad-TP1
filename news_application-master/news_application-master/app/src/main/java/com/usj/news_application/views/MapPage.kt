package com.usj.news_application.views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.usj.news_application.models.News
import com.usj.news_application.repositories.NewsRepository
import com.usj.news_application.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.usj.news_application.R

private const val TAG = "MapPage"
private const val DEFAULT_ZOOM = 2f
private const val MARKER_ZOOM = 5f

class MapPage : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("CRASH", "Uncaught exception in thread $thread", throwable)
        }
        MapsInitializer.initialize(this)
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("News Map") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NewsMap()
                    }
                }
            }
        }
    }
}

data class NewsLocation(
    val latLng: LatLng,
    val news: News
)

@Composable
fun NewsMap() {
    var newsLocations by remember { mutableStateOf<List<NewsLocation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedNews by remember { mutableStateOf<News?>(null) }
    val repository = NewsRepository(RetrofitInstance.api)
    val context = LocalContext.current
    val customMarker = remember {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.pin)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, false)
        BitmapDescriptorFactory.fromBitmap(resizedBitmap)
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(0.0, 0.0),
            DEFAULT_ZOOM
        )
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val news = repository.fetchNews()
                Log.d(TAG, "Fetched ${news.size} news items")

                newsLocations = news.mapNotNull { newsItem ->
                    if (newsItem.mapLocation.isBlank()) return@mapNotNull null // Skip empty locations entirely
                    try {
                        val latLng = convertLocationToLatLng(newsItem.mapLocation)
                        if (latLng != null) {
                            NewsLocation(latLng, newsItem)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                Log.d(TAG, "Processed ${newsLocations.size} valid locations")

                newsLocations.firstOrNull()?.let { firstLocation ->
                    withContext(Dispatchers.Main) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(
                            firstLocation.latLng,
                            MARKER_ZOOM
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching news", e)
                errorMessage = "Failed to load news locations: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.Center)
                )
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "",
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.Center)
                )
            }
            else -> {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = false)
                ) {
                    newsLocations.forEach { location ->
                        Marker(
                            state = MarkerState(position = location.latLng),
                            title = location.news.title,
                            snippet = "Click for more info",
                            icon = customMarker,
                            onClick = {
                                selectedNews = location.news
                                true
                            }
                        )
                    }
                }

                // News Detail Dialog
                selectedNews?.let { news ->
                    Dialog(onDismissRequest = { selectedNews = null }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = news.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = formatDateTime(news.datetime),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = news.detailedDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { selectedNews = null },
                                    modifier = Modifier.align(Alignment.End),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateTime(dateTime: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateTime)
        outputFormat.format(date ?: return dateTime)
    } catch (e: Exception) {
        Log.e(TAG, "Error formatting date: $dateTime", e)
        dateTime
    }
}

fun convertLocationToLatLng(location: String): LatLng? {
    return try {
        val parts = location.split(",")
        if (parts.size != 2) {
            Log.w(TAG, "Invalid location format: $location")
            return null
        }

        val lat = parts[0].trim().toDouble()
        val lng = parts[1].trim().toDouble()

        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            Log.w(TAG, "Invalid coordinates: lat=$lat, lng=$lng")
            return null
        }

        LatLng(lat, lng)
    } catch (e: Exception) {
        Log.e(TAG, "Error parsing location: $location", e)
        null
    }
}