package com.lovely.gweather.data.model

/**
 * Data class representing the credentials sent to the API during login or sign-up.
 */
data class UserCredentials(
    val email: String,
    val password: String
)

/**
 * Data class representing the successful response received from the API
 * after a user successfully logs in or signs up.
 */
data class AuthToken(
    val token: String,
    val userId: String
)