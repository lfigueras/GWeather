package com.lovely.gweather.data.repository

import com.lovely.gweather.data.model.AuthToken

// This object acts as a simple in-memory local database
object SimpleAuthStorage {
    private var authToken: AuthToken? = null

    fun saveToken(token: AuthToken) {
        authToken = token
    }

    fun getToken(): AuthToken? {
        return authToken
    }

    fun clearToken() {
        authToken = null
    }
}