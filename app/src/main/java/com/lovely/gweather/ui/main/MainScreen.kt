@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)

package com.lovely.gweather.ui.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.lovely.gweather.data.local.WeatherDao
import com.lovely.gweather.data.local.entity.WeatherHistory
import com.lovely.gweather.data.location.LocationManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val WeatherInk = Color(0xFF18333A)
private val WeatherMuted = Color(0xFF61767A)
private val WeatherTeal = Color(0xFF247A78)
private val WeatherCoral = Color(0xFFC9674D)
private val WeatherGold = Color(0xFFD69B32)
private val WeatherCanvas = Color(0xFFF2F7F5)
private val WeatherWhite = Color(0xFFFFFFFF)

@Composable
fun MainScreen(
    weatherDao: WeatherDao,
    userEmail: String,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = hour >= 18 || hour < 6
    val context = LocalContext.current
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    )
    val hasLocationPermission = locationPermissionsState.permissions.any { it.status.isGranted }
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(weatherDao, userEmail)
    )
    val weatherHistoryList by mainViewModel.weatherHistory.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    val locationManager = remember(context) {
        LocationManager(
            context = context,
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        )
    }
    var hasFetchedLocation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            mainViewModel.fetchLocation(context, locationManager)
            hasFetchedLocation = true
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, hasLocationPermission) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && hasLocationPermission && hasFetchedLocation) {
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
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign out?") },
            text = { Text("You will need to sign in again to view your weather history.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    }
                ) {
                    Text("Sign out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("GWeather", color = WeatherInk, fontWeight = FontWeight.SemiBold)
                        Text("LOCAL WEATHER", color = WeatherMuted, style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { mainViewModel.fetchLocation(context, locationManager) }) {
                        Icon(Icons.Default.Refresh, "Refresh weather", tint = WeatherInk)
                    }
                    IconButton(onClick = { showSignOutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            "Sign Out",
                            tint = WeatherInk
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = WeatherInk
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = WeatherWhite,
                contentColor = WeatherInk,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.WbSunny, null) },
                    label = { Text("Current") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = WeatherTeal,
                        selectedTextColor = WeatherTeal,
                        indicatorColor = Color(0xFFDCEDEA),
                        unselectedIconColor = WeatherMuted,
                        unselectedTextColor = WeatherMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, null) },
                    label = { Text("History") },
                    colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                        selectedIconColor = WeatherTeal,
                        selectedTextColor = WeatherTeal,
                        indicatorColor = Color(0xFFDCEDEA),
                        unselectedIconColor = WeatherMuted,
                        unselectedTextColor = WeatherMuted
                    )
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE8F2F0), WeatherCanvas, Color(0xFFF8F6F0))
                    )
                )
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> if (hasLocationPermission) {
                    CurrentWeatherTab(
                        isNight,
                        addressLine = mainViewModel.addressLine,
                        currentTemp = mainViewModel.currentTemp,
                        weatherDescription = mainViewModel.weatherDescription,
                        sunrise = mainViewModel.sunrise,
                        sunset = mainViewModel.sunset,
                        weatherIconCode = mainViewModel.weatherIcon,
                        isLoading = mainViewModel.isWeatherLoading,
                        errorMessage = mainViewModel.weatherError,
                        onRetry = { mainViewModel.fetchLocation(context, locationManager) }
                    )
                } else {
                    LocationPermissionPrompt(
                        onGrantPermission = { locationPermissionsState.launchMultiplePermissionRequest() },
                        onOpenSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }
                1 ->   ForecastTab(history = weatherHistoryList)
            }
        }
    }
}

@Composable
private fun LocationPermissionPrompt(
    onGrantPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(color = WeatherWhite, shape = RoundedCornerShape(12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.LocationOn, null, tint = WeatherTeal, modifier = Modifier.size(34.dp))
                Spacer(Modifier.height(12.dp))
                Text("Location access", color = WeatherInk, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Allow location to see weather for your area. Your saved history is still available without it.",
                    color = WeatherMuted,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onGrantPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = WeatherTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Allow location")
                }
                TextButton(onClick = onOpenSettings) {
                    Text("App settings", color = WeatherTeal)
                }
            }
        }
    }
}

@Composable
fun CurrentWeatherTab(
    isNight: Boolean,
    addressLine: String?,
    currentTemp: String?,
    weatherDescription: String?,
    sunrise: String?,
    sunset: String?,
    weatherIconCode: String?,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    val weatherIcon = when (weatherIconCode) {
        "01d" -> Icons.Default.WbSunny
        "01n" -> Icons.Default.NightsStay
        "02d", "02n" -> Icons.Default.FilterDrama
        "03d", "03n", "04d", "04n" -> Icons.Default.Cloud
        "09d", "09n", "10d", "10n" -> Icons.Default.WaterDrop
        "11d", "11n" -> Icons.Default.Thunderstorm
        "13d", "13n" -> Icons.Default.AcUnit
        "50d", "50n" -> Icons.Default.Dehaze
        else -> if (isNight) Icons.Default.NightsStay else Icons.Default.WbSunny
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = WeatherWhite.copy(alpha = 0.88f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, null, tint = WeatherTeal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(7.dp))
                Text(
                    text = addressLine ?: "Finding your location",
                    color = WeatherInk,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        when {
            errorMessage != null && currentTemp == null -> WeatherErrorState(errorMessage, onRetry)
            isLoading || currentTemp == null -> WeatherLoadingState()
            else -> {
                Icon(
                    imageVector = weatherIcon,
                    contentDescription = weatherDescription ?: "Current weather",
                    modifier = Modifier.size(88.dp),
                    tint = WeatherTeal
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = currentTemp,
                    color = WeatherInk,
                    fontSize = 68.sp,
                    lineHeight = 76.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = weatherDescription ?: "Current conditions",
                    color = WeatherMuted,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(30.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = WeatherWhite
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WeatherTime("Sunrise", sunrise, WeatherGold)
                        Surface(modifier = Modifier.width(1.dp).height(48.dp), color = Color(0xFFE4EBE8)) {}
                        WeatherTime("Sunset", sunset, WeatherCoral)
                    }
                }

                if (isLoading) {
                    Spacer(Modifier.height(14.dp))
                    Text("Updating weather...", color = WeatherMuted, style = MaterialTheme.typography.bodySmall)
                } else if (errorMessage != null) {
                    Spacer(Modifier.height(14.dp))
                    Text(errorMessage, color = WeatherCoral, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun WeatherTime(label: String, time: String?, tint: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.WbTwilight, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(7.dp))
        Text(label, color = WeatherMuted, style = MaterialTheme.typography.labelMedium)
        Text(time ?: "--:--", color = WeatherInk, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun WeatherLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 62.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = WeatherTeal, strokeWidth = 3.dp)
        Spacer(Modifier.height(18.dp))
        Text("Getting the latest weather", color = WeatherInk, style = MaterialTheme.typography.titleMedium)
        Text("This usually takes a moment", color = WeatherMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun WeatherErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = WeatherWhite,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CloudOff, null, tint = WeatherCoral, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(12.dp))
                Text("Weather unavailable", color = WeatherInk, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Text(message, color = WeatherMuted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = WeatherTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Try again")
                }
            }
        }
    }
}

@Composable
fun ForecastTab(history: List<WeatherHistory>) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text("Weather history", color = WeatherInk, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        Text("Recent observations", color = WeatherMuted, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(18.dp))

        if (history.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 56.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(color = Color(0xFFDCEDEA), shape = RoundedCornerShape(18.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = WeatherTeal,
                        modifier = Modifier.padding(15.dp).size(28.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("No observations yet", color = WeatherInk, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Weather checks will appear here.",
                    color = WeatherMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { record ->
                    WeatherHistoryItem(record = record)
                }
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = WeatherWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    color = WeatherInk,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.temperature,
                    style = MaterialTheme.typography.titleMedium,
                    color = WeatherInk
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = WeatherMuted
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = WeatherMuted
                )
            }
        }
    }
}