package com.lovely.gweather.ui;

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lovely.gweather.data.local.WeatherDao
import com.lovely.gweather.data.preferences.UserPreferences
import com.lovely.gweather.ui.main.MainScreen
import com.yourapp.weather.screens.AuthenticationScreen
import com.yourapp.weather.screens.RegistrationScreen


@Composable
fun AppRoot(startDestination: String, weatherDao: WeatherDao){
    val navController = rememberNavController()
    val context = LocalContext.current
    val userPreferences = UserPreferences(context.applicationContext)

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("registration") {
            RegistrationScreen(
                onNavigateToSignIn = { navController.navigate("auth") }
            )
        }
        composable("auth") {
            AuthenticationScreen(
                onNavigateToRegistration = { navController.navigate("registration") },
                onSignIn = { email ->
                    userPreferences.saveUserSession(email)
                    navController.navigate("main") }
            )
        }
        composable("main") {
            MainScreen(
                weatherDao = weatherDao,
                onSignOut = {
                    userPreferences.clearUserSession()
                    navController.navigate("auth"){
                        popUpTo("main") {inclusive = true}
                    } }
            )
        }
    }

//    val isLoggedIn = remember { mutableStateOf(false) }
//
//    if (isLoggedIn.value) {
//        // Main app will go here
//    } else {
//        // Auth screens will go here
//    }
}