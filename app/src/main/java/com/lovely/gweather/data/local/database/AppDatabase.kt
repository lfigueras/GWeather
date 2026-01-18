package com.lovely.gweather.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lovely.gweather.data.local.WeatherDao
import com.lovely.gweather.data.local.dao.UserDao
import com.lovely.gweather.data.local.entity.UserEntity
import com.lovely.gweather.data.local.entity.WeatherHistory

@Database(entities = [UserEntity::class, WeatherHistory::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao
    abstract fun weatherDao(): WeatherDao
}