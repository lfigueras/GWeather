package com.lovely.gweather.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lovely.gweather.data.local.WeatherDao

class MainViewModelFactory(private val weatherDao: WeatherDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(weatherDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
