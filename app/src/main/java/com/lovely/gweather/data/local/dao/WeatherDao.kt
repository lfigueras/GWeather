package com.lovely.gweather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lovely.gweather.data.local.entity.WeatherHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherHistory(weatherHistory: WeatherHistory)

    @Query("SELECT * FROM weather_history WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getWeatherHistory(userEmail: String): Flow<List<WeatherHistory>>

    @Query("SELECT * FROM weather_history WHERE userEmail = :userEmail ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestWeatherHistory(userEmail: String): WeatherHistory?
}
