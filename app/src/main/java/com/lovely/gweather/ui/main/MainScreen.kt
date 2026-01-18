@file:OptIn(ExperimentalMaterial3Api::class)

package com.lovely.gweather.ui.main

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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.lovely.gweather.data.location.LocationManager
import java.util.Calendar


@Composable
fun MainScreen(onSignOut: () -> Unit,  mainViewModel: MainViewModel = viewModel()) {
    var selectedTab by remember { mutableStateOf(0) }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = hour >= 18 || hour < 6
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val locationManager = LocationManager(
            context = context,
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        )
        mainViewModel.fetchLocation(locationManager)
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
                0 -> CurrentWeatherTab(isNight, location = mainViewModel.location)
                1 -> WeatherListTab()
            }
        }
    }
}

@Composable
fun CurrentWeatherTab(isNight: Boolean, location: Location?) {
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
            if (location != null) {
                Text(
                    "Lat: ${location.latitude}, Lon: ${location.longitude}",
                    color = Color.White.copy(alpha = 0.9f)
                )
            } else {
                Text(
                    "Fetching location...",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Weather Icon
        Icon(
            imageVector = if (isNight) Icons.Default.NightsStay else Icons.Default.WbSunny,
            contentDescription = "Weather",
            modifier = Modifier.size(96.dp),
            tint = if (isNight) Color(0xFFC4B5FD) else Color(0xFFFBBF24)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Temperature
        Text(
            text = "24°",
            style = MaterialTheme.typography.displayLarge,
            color = Color.White
        )

        Text(
            text = if (isNight) "Clear Night" else "Sunny",
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
                    Text("6:24 AM", color = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WbTwilight, "Sunset", tint = Color(0xFFFB923C))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sunset", color = Color.White.copy(0.7f))
                    Text("5:47 PM", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WeatherListTab() {
    // Weather list implementation
    Text("Weather List", color = Color.White)
}