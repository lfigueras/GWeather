package com.lovely.gweather.ui.auth


import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for the AuthValidator.
 * We need Robolectric because AuthValidator uses Android's `Patterns` class.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
class AuthValidatorTest {


    @Test
    fun `validateEmail returns error for empty string`() {
        val result = AuthValidator.validateEmail("")
        assertThat(result).isEqualTo("Email cannot be empty")
    }

    @Test
    fun `validateEmail returns error for invalid format`() {
        val result = AuthValidator.validateEmail("test@domain") // missing .com
        assertThat(result).isEqualTo("Please enter a valid email")
    }

    @Test
    fun `validateEmail returns null for valid email`() {
        val result = AuthValidator.validateEmail("test@domain.com")
        assertThat(result).isNull()
    }


    @Test
    fun `validatePassword returns error for empty string`() {
        val result = AuthValidator.validatePassword("")
        assertThat(result).isEqualTo("Password cannot be empty")
    }

    @Test
    fun `validatePassword returns error for password too short`() {
        val result = AuthValidator.validatePassword("12345") // 5 chars
        assertThat(result).isEqualTo("Password must be at least 6 characters")
    }

    @Test
    fun `validatePassword returns null for valid password`() {
        val result = AuthValidator.validatePassword("123456")
        assertThat(result).isNull()
    }


    @Test
    fun `validateConfirmPassword returns error for non-matching passwords`() {
        val result = AuthValidator.validateConfirmPassword("password123", "password456")
        assertThat(result).isEqualTo("Passwords do not match")
    }

    @Test
    fun `validateConfirmPassword returns null for matching passwords`() {
        val result = AuthValidator.validateConfirmPassword("password123", "password123")
        assertThat(result).isNull()
    }
}
