@file:OptIn(ExperimentalPermissionsApi::class)
package com.lovely.gweather

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.lovely.gweather.data.local.database.AppDatabase
import com.lovely.gweather.data.preferences.UserPreferences
import com.lovely.gweather.ui.AppRoot
import com.lovely.gweather.ui.permissions.PermanentlyDeniedScreen
import com.lovely.gweather.ui.permissions.PermissionRationaleScreen
import com.lovely.gweather.ui.theme.GWeatherTheme

class MainActivity : ComponentActivity() {
    //Database
    object Database {
        fun getInstance(context: Context): AppDatabase{
            return Room.databaseBuilder(context,AppDatabase::class.java, "gweather-db").build()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userPreferences = UserPreferences(applicationContext)
        // 3. DETERMINE the starting screen based on login state
        val startDestination = if (userPreferences.isLoggedIn) {
            "main" // If logged in, go straight to MainScreen
        } else {
            "auth" // Otherwise, start at AuthScreen
        }
        setContent {
            GWeatherTheme {
                val locationPermissionsState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    )
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LaunchedEffect(Unit) {
                        if (!locationPermissionsState.allPermissionsGranted && !locationPermissionsState.shouldShowRationale) {
                            locationPermissionsState.launchMultiplePermissionRequest()
                        }
                    }

                    when {
                        // If all permissions are granted, show the main app content.
                        locationPermissionsState.allPermissionsGranted -> {
                            AppRoot(startDestination)
                        }

                        // If user has denied permissions, show a rationale and a button to ask again.
                        locationPermissionsState.shouldShowRationale -> {
                            PermissionRationaleScreen {
                                locationPermissionsState.launchMultiplePermissionRequest()
                            }
                        }

                        // If we're still here, it means permissions were denied.
                        // This state is hit after the initial request (from LaunchedEffect) is denied.
                        else -> {
                            PermanentlyDeniedScreen()
                        }
                    }
                }
            }
        }
    }
}

