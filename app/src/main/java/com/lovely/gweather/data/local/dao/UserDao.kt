package com.lovely.gweather.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.lovely.gweather.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<UserEntity>

    @Query("SELECT * FROM UserEntity WHERE email_address = :first AND " +
            "password = :last LIMIT 1")
    fun findByName(first: String, last: String): UserEntity

    @Insert
    fun insertAll(vararg users: UserEntity)

    @Delete
    fun delete(user: UserEntity)
}