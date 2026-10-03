package com.lovely.gweather

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.lovely.gweather.data.local.database.AppDatabase
import com.lovely.gweather.data.preferences.UserPreferences
import com.lovely.gweather.ui.AppRoot
import com.lovely.gweather.ui.theme.GWeatherTheme

class MainActivity : ComponentActivity() {
    //Database
    object Database {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gweather-db"
                )
                    .addMigrations(AppDatabase.MIGRATION_2_3)
                    .build()
                    .also { instance = it }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userPreferences = UserPreferences(applicationContext)
        // 3. DETERMINE the starting screen based on login state
        val startDestination = if (userPreferences.isLoggedIn && userPreferences.loggedInUserIdentifier != null) {
            "main" // If logged in, go straight to MainScreen
        } else {
            "auth" // Otherwise, start at AuthScreen
        }
        val weatherDao = Database.getInstance(applicationContext).weatherDao()
        setContent {
            GWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot(startDestination, weatherDao)
                }
            }
        }
    }
}

