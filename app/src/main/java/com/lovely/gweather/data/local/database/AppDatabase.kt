package com.lovely.gweather.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lovely.gweather.data.auth.CredentialHasher
import com.lovely.gweather.data.local.WeatherDao
import com.lovely.gweather.data.local.dao.UserDao
import com.lovely.gweather.data.local.entity.UserEntity
import com.lovely.gweather.data.local.entity.WeatherHistory

@Database(entities = [UserEntity::class, WeatherHistory::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao() : UserDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE UserEntity ADD COLUMN password_salt TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE weather_history ADD COLUMN userEmail TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE UserEntity SET email_address = lower(trim(email_address))")

                val legacyPasswords = mutableListOf<Pair<Int, String>>()
                db.query("SELECT id, password FROM UserEntity WHERE password_salt = ''").use { cursor ->
                    while (cursor.moveToNext()) {
                        if (!cursor.isNull(1)) {
                            legacyPasswords += cursor.getInt(0) to cursor.getString(1)
                        }
                    }
                }
                legacyPasswords.forEach { (id, password) ->
                    val credential = CredentialHasher.hash(password)
                    db.execSQL(
                        "UPDATE UserEntity SET password = ?, password_salt = ? WHERE id = ?",
                        arrayOf(credential.hash, credential.salt, id)
                    )
                }

                db.query("SELECT COUNT(*) FROM UserEntity").use { cursor ->
                    if (cursor.moveToFirst() && cursor.getInt(0) == 1) {
                        db.query("SELECT email_address FROM UserEntity LIMIT 1").use { userCursor ->
                            if (userCursor.moveToFirst() && !userCursor.isNull(0)) {
                                db.execSQL(
                                    "UPDATE weather_history SET userEmail = ? WHERE userEmail = ''",
                                    arrayOf(userCursor.getString(0))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}