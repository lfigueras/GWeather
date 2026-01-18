package com.lovely.gweather.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponse(
    @Json(name = "weather")
    val weather: List<WeatherDescription>,

    @Json(name = "main")
    val main: Main,

    @Json(name = "sys")
    val sys: Sys,

    @Json(name = "name")
    val cityName: String
)

@JsonClass(generateAdapter = true)
data class WeatherDescription(
    @Json(name = "main")
    val main: String,

    @Json(name = "icon")
    val icon: String
)


@JsonClass(generateAdapter = true)
data class Main(
    @Json(name = "temp")
    val temp: Double,
)


@JsonClass(generateAdapter = true)
data class Sys(
    @Json(name = "sunrise")
    val sunrise: Long,

    @Json(name = "sunset")
    val sunset: Long
)

