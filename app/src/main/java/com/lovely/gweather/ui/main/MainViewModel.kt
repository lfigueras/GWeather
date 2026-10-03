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
import com.lovely.gweather.data.network.ApiConstants
import com.lovely.gweather.data.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.io.IOException
import retrofit2.HttpException

class MainViewModel(
    private val weatherDao: WeatherDao,
    private val userEmail: String
) : ViewModel() {

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
    var isWeatherLoading by mutableStateOf(false)
        private set
    var weatherError by mutableStateOf<String?>(null)
        private set

    val weatherHistory = weatherDao.getWeatherHistory(userEmail)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // Start collecting when the UI is on screen
            initialValue = emptyList()
        )


    private val _shouldPromptForGps = MutableStateFlow(false)
    val shouldPromptForGps = _shouldPromptForGps.asStateFlow()
    private var isFetchingLocation = false

    fun fetchLocation(context: Context, locationManager: LocationManager) {
        if (isFetchingLocation) return
        isFetchingLocation = true
        weatherError = null
        viewModelScope.launch {
            try {
                val newLocation = locationManager.getLocation()
                location = newLocation

                if (newLocation != null) {
                    getAddressLine(context, newLocation)
                    fetchWeather(newLocation.latitude, newLocation.longitude)
                } else {
                    addressLine = "Location not found"
                    weatherError = "Could not determine your location. Check location services and try again."
                }
            } catch (e: GpsNotEnabledException) {
                Log.w("MainViewModel", "Caught GpsNotEnabledException. Emitting event to UI.")
                addressLine = "Please enable GPS"
                weatherError = "Location services are turned off."
                _shouldPromptForGps.value = true
            } catch (e: Exception){
                Log.e("MainViewModel", "Error fetching location", e)
                addressLine = "Error fetching location"
                weatherError = "Could not get your location. Please try again."
            } finally {
                isFetchingLocation = false
            }
        }
    }
    fun onGpsPromptHandled() {
        _shouldPromptForGps.value = false
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        // Use the viewModelScope to launch a coroutine on the IO dispatcher (best for network calls)
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                isWeatherLoading = true
                weatherError = null
            }

            if (ApiConstants.API_KEY.isBlank()) {
                withContext(Dispatchers.Main) {
                    weatherError = "OpenWeather API key missing"
                    isWeatherLoading = false
                }
                return@launch
            }

            try {
                // Make the network call using our singleton RetrofitInstance
                val response = RetrofitInstance.api.getCurrentWeather(latitude, longitude)

                // Format the sunrise/sunset times (API provides them in seconds, Date needs milliseconds)
                val sunriseTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(response.sys.sunrise * 1000))
                val sunsetTime = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(response.sys.sunset * 1000))
                val temperature = "${response.main.temp.toInt()}°C"
                val description = response.weather.firstOrNull()?.main ?: "Unknown"
                val newHistoryRecord = WeatherHistory(
                    cityName = response.cityName,
                    temperature = temperature,
                    weatherDescription = description,
                    userEmail = userEmail
                )
                val latestRecord = weatherDao.getLatestWeatherHistory(userEmail)
                val isRecentDuplicate = latestRecord != null &&
                    latestRecord.cityName == newHistoryRecord.cityName &&
                    latestRecord.temperature == temperature &&
                    latestRecord.weatherDescription == description &&
                    System.currentTimeMillis() - latestRecord.timestamp < 15 * 60 * 1000
                if (!isRecentDuplicate) {
                    weatherDao.insertWeatherHistory(newHistoryRecord)
                }


                withContext(Dispatchers.Main) {
                    currentTemp = temperature
                    weatherDescription = description
                    weatherIcon = response.weather.firstOrNull()?.icon
                    sunrise = sunriseTime
                    sunset = sunsetTime
                    weatherError = null
                    isWeatherLoading = false
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching weather data", e)
                val message = when (e) {
                    is HttpException -> when (e.code()) {
                        401 -> "Invalid OpenWeather API key"
                        429 -> "Weather API rate limit reached"
                        else -> "Weather service unavailable (${e.code()})"
                    }
                    is IOException -> "No internet connection"
                    else -> "Unable to fetch weather"
                }
                withContext(Dispatchers.Main) {
                    weatherError = message
                    isWeatherLoading = false
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


