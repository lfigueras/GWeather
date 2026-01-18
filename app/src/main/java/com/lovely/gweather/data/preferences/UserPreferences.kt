package com.lovely.gweather.data.preferences

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages storing and retrieving user session data using SharedPreferences.
 * This provides a fast way to check login state without hitting the database.
 */
class UserPreferences(context: Context) {

    private val prefsName = "user_session"
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_USER_EMAIL = "user_email" // Or user ID
    }

    /**
     * Saves the user's session data after a successful login.
     * @param identifier The email or ID of the user who logged in.
     */
    fun saveUserSession(identifier: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_EMAIL, identifier)
        editor.apply() // Use apply() for asynchronous saving.
    }

    /**
     * Clears the user's session data on logout.
     */
    fun clearUserSession() {
        val editor = sharedPreferences.edit()
        editor.remove(KEY_IS_LOGGED_IN)
        editor.remove(KEY_USER_EMAIL)
        editor.apply()
    }

    /**
     * Checks if a user is currently logged in.
     */
    val isLoggedIn: Boolean
        get() = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)

    /**
     * Retrieves the identifier (email/ID) of the currently logged-in user.
     */
    val loggedInUserIdentifier: String?
        get() = sharedPreferences.getString(KEY_USER_EMAIL, null)
}
