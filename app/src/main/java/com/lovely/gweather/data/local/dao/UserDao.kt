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

        @Query("SELECT * FROM UserEntity WHERE email_address = :email LIMIT 1")
        suspend fun getUserByEmail(email: String): UserEntity?

    @Insert
        suspend fun insertAll(vararg users: UserEntity)

        @Query("UPDATE UserEntity SET password = :passwordHash, password_salt = :passwordSalt WHERE id = :userId")
        suspend fun updateCredentials(userId: Int, passwordHash: String, passwordSalt: String)

    @Delete
    fun delete(user: UserEntity)

}