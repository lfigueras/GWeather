package com.lovely.gweather.ui.main

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lovely.gweather.data.location.LocationManager
import com.lovely.gweather.data.location.exceptions.GpsNotEnabledException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Holds the location state for the UI. Compose observes this for changes.
    var location by mutableStateOf<Location?>(null)
        private set // Only the ViewModel can modify this state.
    var addressLine by mutableStateOf<String?>(null)
        private set

    private val _shouldPromptForGps = MutableStateFlow(false)
    val shouldPromptForGps = _shouldPromptForGps.asStateFlow()

    fun fetchLocation(context: Context, locationManager: LocationManager) {
        viewModelScope.launch {
            try {
                val newLocation = locationManager.getLocation()
                location = newLocation // Update location state

                if (newLocation != null) {
                    // If we got a location, perform reverse geocoding
                    getAddressLine(context, newLocation)
                } else {
                    // If location is null, set a placeholder address text
                    addressLine = "Location not found"
                }
            } catch (e: GpsNotEnabledException) {
                Log.w("MainViewModel", "Caught GpsNotEnabledException. Emitting event to UI.")
                addressLine = "Please enable GPS"
                _shouldPromptForGps.value = true
            } catch (e: Exception){
                Log.e("MainViewModel", "Error fetching location", e)
                addressLine = "Error fetching location"
            }
        }
    }
    fun onGpsPromptHandled() {
        _shouldPromptForGps.value = false
    }
    private fun getAddressLine(context: Context, location: Location) {
        // Geocoding can be slow, so run it on a background I/O thread
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())

                // getFromLocation is deprecated on API 33+, but this handles both legacy and new calls.
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

                val finalAddress = if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    // Combine city (locality) and country for a clean display
                    val city = address.locality
                    val country = address.countryName
                    if (city != null && country != null) {
                        "$city, $country"
                    } else {
                        city ?: country ?: "Unknown Location"
                    }
                } else {
                    "Location name not found"
                }

                // Switch back to the Main thread to update the UI state
                launch(Dispatchers.Main) {
                    addressLine = finalAddress
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Could not get address line.", e)
                launch(Dispatchers.Main) {
                    addressLine = "Could not find address"
                }
            }
        }
    }
}


