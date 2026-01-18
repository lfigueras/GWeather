package com.lovely.gweather.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    @ColumnInfo(name = "full_name") var fullName: String?,
    @ColumnInfo(name = "email_address") var emailAddress: String?,
    @ColumnInfo(name = "password") var password: String?
)
