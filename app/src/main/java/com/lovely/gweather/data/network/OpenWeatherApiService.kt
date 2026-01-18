package com.lovely.gweather.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApiService {

    /**
     * Fetches the current weather for a given latitude and longitude.
     *
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @param apiKey The API key for authentication.
     * @param units The unit system for the temperature (we'll use "metric" for Celsius).
     * @return A [WeatherResponse] object.
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = ApiConstants.API_KEY,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}
