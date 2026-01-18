package com.lovely.gweather.ui.auth

import android.util.Patterns

object AuthValidator {

    fun validateName(name: String): String? {
        if (name.isBlank()) {
            return "Name cannot be empty"        }
        return null // null means no error
    }

    fun validateEmail(email: String): String? {
        if (email.isBlank()) {
            return "Email cannot be empty"
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email"
        }
        return null // null means no error
    }

    fun validatePassword(password: String): String? {
        if (password.isBlank()) {
            return "Password cannot be empty"
        }
        if (password.length < 6) {
            return "Password must be at least 6 characters"
        }
        return null // null means no error
    }

    fun validateConfirmPassword(password: String, confirm: String): String? {
        if (password != confirm) {
            return "Passwords do not match"
        }
        return null // null means no error
    }
}
