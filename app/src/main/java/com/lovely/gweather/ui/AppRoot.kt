package com.lovely.gweather.ui;

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.lovely.gweather.ui.auth.AuthenticationScreen

@Composable
fun AppRoot() {
    AuthenticationScreen();
//    val isLoggedIn = remember { mutableStateOf(false) }
//
//    if (isLoggedIn.value) {
//        // Main app will go here
//    } else {
//        // Auth screens will go here
//    }
}