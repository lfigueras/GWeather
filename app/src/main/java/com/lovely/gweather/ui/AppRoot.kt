package com.lovely.gweather.ui;

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lovely.gweather.ui.main.MainScreen
import com.yourapp.weather.screens.AuthenticationScreen
import com.yourapp.weather.screens.RegistrationScreen


@Composable
fun AppRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "auth"
    ) {
        composable("registration") {
            RegistrationScreen(
                onNavigateToSignIn = { navController.navigate("auth") },
                onRegister = { navController.navigate("main") }
            )
        }
        composable("auth") {
            AuthenticationScreen(
                onNavigateToRegistration = { navController.navigate("registration") },
                onSignIn = { navController.navigate("main") }
            )
        }
        composable("main") {
            MainScreen(
                onSignOut = { navController.navigate("auth") }
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