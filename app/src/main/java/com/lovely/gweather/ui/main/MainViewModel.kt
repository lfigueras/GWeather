package com.lovely.gweather.ui.main

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lovely.gweather.data.location.LocationManager
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Holds the location state for the UI. Compose observes this for changes.
    var location by mutableStateOf<Location?>(null)
        private set // Only the ViewModel can modify this state.

    /**
     *  Starts the process of fetching the device's current location.
     *  It uses viewModelScope to ensure the operation is lifecycle-aware.
     */
    fun fetchLocation(locationManager: LocationManager) {
        viewModelScope.launch {
            // Call the suspend function in your LocationManager
            location = locationManager.getLocation()
        }
    }
}
