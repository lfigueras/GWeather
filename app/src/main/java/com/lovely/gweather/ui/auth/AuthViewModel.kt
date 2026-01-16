package com.lovely.gweather.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import com.lovely.gweather.data.model.AuthToken
import com.lovely.gweather.data.model.UserCredentials
import com.lovely.gweather.data.repository.SimpleAuthStorage

class AuthViewModel : ViewModel() {

    fun login(credentials: UserCredentials): Boolean {
        // Simulate a successful login if email and password match our test values
        if (credentials.email == "test@app.com" && credentials.password == "password") {
            val fakeToken = AuthToken(token = "valid_token_123", userId = "user_abc")
            SimpleAuthStorage.saveToken(fakeToken)
            Log.d("AuthViewModel", "Login successful, token saved.");
            return true // Login successful
        }
        Log.d("AuthViewModel", "Login failed");
        return false // Login failed
    }

    fun isLoggedIn(): Boolean {
        return SimpleAuthStorage.getToken() != null
    }

    fun logout() {
        SimpleAuthStorage.clearToken()
    }
}