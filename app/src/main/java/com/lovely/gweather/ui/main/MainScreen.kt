@file:OptIn(ExperimentalMaterial3Api::class)

package com.lovely.gweather.ui.main

import android.content.Intent
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.lovely.gweather.data.local.WeatherDao
import com.lovely.gweather.data.local.entity.WeatherHistory
import com.lovely.gweather.data.location.LocationManager
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date


@Composable
fun MainScreen(
    weatherDao: WeatherDao,
    onSignOut: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = hour >= 18 || hour < 6
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(weatherDao)
    )
    val weatherHistoryList by mainViewModel.weatherHistory.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // If the app is RESUMED (e.g., coming back from settings)
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-trigger the location fetch
                val locationManager = LocationManager(
                    context = context,
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
                )
                mainViewModel.fetchLocation(context, locationManager)
            }
        }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the composable leaves the screen, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val shouldShowGpsPrompt by mainViewModel.shouldPromptForGps.collectAsState()
    if (shouldShowGpsPrompt) {
        AlertDialog(
            onDismissRequest = {
                // Called when the user clicks outside the dialog or presses the back button
                mainViewModel.onGpsPromptHandled()
            },
            title = { Text("Enable Location Services") },
            text = { Text("To get weather updates for your current location, please enable GPS/Location Services.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Create an Intent to open the device's location settings
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                        // Inform the ViewModel that the prompt has been handled
                        mainViewModel.onGpsPromptHandled()
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // If the user cancels, just dismiss the dialog
                        mainViewModel.onGpsPromptHandled()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    val gradientColors = if (isNight) {
        listOf(
            Color(0xFF312E81), // indigo-900
            Color(0xFF6B21A8), // purple-800
            Color(0xFF1E3A8A)  // blue-900
        )
    } else {
        listOf(
            Color(0xFFFB923C), // orange-400
            Color(0xFFFBBF24), // amber-400
            Color(0xFFFDE047)  // yellow-300
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Weather",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("GWeather", color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.Default.Logout,
                            "Sign Out",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.padding(16.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.WbSunny, "Current") },
                    label = { Text("Current") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.List, "Forecast") },
                    label = { Text("Forecast") }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors)
                )
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> CurrentWeatherTab(isNight, addressLine = mainViewModel.addressLine,
                    currentTemp = mainViewModel.currentTemp,
                    weatherDescription = mainViewModel.weatherDescription,
                    sunrise = mainViewModel.sunrise,
                    sunset = mainViewModel.sunset,
                    weatherIconCode = mainViewModel.weatherIcon)
                1 ->   ForecastTab(history = weatherHistoryList)
            }
        }
    }
}

@Composable
fun CurrentWeatherTab(isNight: Boolean, addressLine: String?,
                      currentTemp: String?,
                      weatherDescription: String?,
                      sunrise: String?,
                      sunset: String?,
                      weatherIconCode: String?) {
    val weatherIcon = when (weatherIconCode) {
        // Day icons
        "01d" -> Icons.Default.WbSunny
        // Night icon (for clear sky)
        "01n" -> Icons.Default.NightsStay
        // Scattered clouds
        "02d", "02n" -> Icons.Default.FilterDrama
        // Cloudy
        "03d", "03n", "04d", "04n" -> Icons.Default.Cloud
        // Rain
        "09d", "09n", "10d", "10n" -> Icons.Default.WaterDrop // Or a rain icon
        // Thunderstorm
        "11d", "11n" -> Icons.Default.Thunderstorm
        // Snow
        "13d", "13n" -> Icons.Default.AcUnit
        // Mist
        "50d", "50n" -> Icons.Default.Dehaze
        // Default to night/day icon if code is unknown
        else -> if (isNight) Icons.Default.NightsStay else Icons.Default.WbSunny
    }

    if (addressLine == null || currentTemp == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Location
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    "Location",
                    tint = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = addressLine ?: "Fetching location...",
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Weather Icon
            Icon(
                imageVector = weatherIcon,
                contentDescription = weatherDescription,
                modifier = Modifier.size(96.dp)
//                tint = if (isNight) Color(0xFFC4B5FD) else Color(0xFFFBBF24)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Temperature
            Text(
                text = currentTemp,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )

            Text(
                text = weatherDescription ?: "Loading...",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Sun Times Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WbTwilight, "Sunrise", tint = Color(0xFFFDE047))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sunrise", color = Color.White.copy(0.7f))
                        Text(sunrise?:"..", color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WbTwilight, "Sunset", tint = Color(0xFFFB923C))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sunset", color = Color.White.copy(0.7f))
                        Text(sunset ?: "..", color = Color.White)
                    }
                }
            }
        }
    }

}

@Composable
fun ForecastTab(history: List<WeatherHistory>) {
    if (history.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No weather history found. Open the app again later to build a history.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(history) { record ->
                WeatherHistoryItem(record = record)
            }
        }
    }
}

@Composable
fun WeatherHistoryItem(record: WeatherHistory) {
    // Format the timestamp for display
    val formattedDate = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())
        .format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.cityName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.temperature,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.weatherDescription,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}