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
import com.lovely.gweather.data.local.WeatherDao
import com.lovely.gweather.data.local.entity.WeatherHistory
import com.lovely.gweather.data.location.LocationManager
import com.lovely.gweather.data.location.exceptions.GpsNotEnabledException
import com.lovely.gweather.data.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class MainViewModel(private val weatherDao: WeatherDao) : ViewModel() {

    // Holds the location state for the UI. Compose observes this for changes.
    var location by mutableStateOf<Location?>(null)
        private set // Only the ViewModel can modify this state.
    var addressLine by mutableStateOf<String?>(null)
        private set

    var currentTemp by mutableStateOf<String?>(null)
        private set
    var weatherDescription by mutableStateOf<String?>(null)
        private set
    var weatherIcon by mutableStateOf<String?>(null)
        private set
    var sunrise by mutableStateOf<String?>(null)
        private set
    var sunset by mutableStateOf<String?>(null)
        private set

    val weatherHistory = weatherDao.getAllWeatherHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Start collecting when the UI is on screen
            initialValue = emptyList()
        )


    private val _shouldPromptForGps = MutableStateFlow(false)
    val shouldPromptForGps = _shouldPromptForGps.asStateFlow()

    fun fetchLocation(context: Context, locationManager: LocationManager) {
        viewModelScope.launch {
            try {
                val newLocation = locationManager.getLocation()
                location = newLocation

                if (newLocation != null) {
                    getAddressLine(context, newLocation)
                    fetchWeather(newLocation.latitude, newLocation.longitude)
                } else {
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

    private fun fetchWeather(latitude: Double, longitude: Double) {
        // Use the viewModelScope to launch a coroutine on the IO dispatcher (best for network calls)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Make the network call using our singleton RetrofitInstance
                val response = RetrofitInstance.api.getCurrentWeather(latitude, longitude)

                // Format the sunrise/sunset times (API provides them in seconds, Date needs milliseconds)
                val sunriseTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(response.sys.sunrise * 1000))
                val sunsetTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(response.sys.sunset * 1000))
                val newHistoryRecord = WeatherHistory(
                    cityName = response.cityName,
                    temperature = "${response.main.temp.toInt()}°C",
                    weatherDescription = response.weather.firstOrNull()?.main ?: "Unknown"
                )
                weatherDao.insertWeatherHistory(newHistoryRecord)


                launch(Dispatchers.Main) {
                    currentTemp = "${response.main.temp.toInt()}°C"
                    weatherDescription = response.weather.firstOrNull()?.main ?: "Unknown"
                    weatherIcon = response.weather.firstOrNull()?.icon
                    sunrise = sunriseTime
                    sunset = sunsetTime
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching weather data", e)
                // Handle network errors by updating the UI
                launch(Dispatchers.Main) {
                    currentTemp = "N/A"
                    weatherDescription = "Error"
                }
            }
        }
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


