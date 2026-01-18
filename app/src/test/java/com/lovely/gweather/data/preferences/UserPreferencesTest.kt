package com.lovely.gweather.data.preferences

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserPreferencesTest {

    @Mock
    private lateinit var mockContext: Context
    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences
    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var userPreferences: UserPreferences

    @Before
    fun setup() {

        MockitoAnnotations.openMocks(this)
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.remove(any())).thenReturn(mockEditor)


        userPreferences = UserPreferences(mockContext)
    }

    @Test
    fun `saveUserSession should save correct email and logged-in status`() {
        val testEmail = "test@example.com"
        val emailCaptor = argumentCaptor<String>()
        val isLoggedInCaptor = argumentCaptor<Boolean>()

        userPreferences.saveUserSession(testEmail)

        verify(mockEditor).putString(eq(UserPreferences.KEY_USER_EMAIL), emailCaptor.capture())
        verify(mockEditor).putBoolean(eq(UserPreferences.KEY_IS_LOGGED_IN), isLoggedInCaptor.capture())
        verify(mockEditor).apply()

        assertEquals(testEmail, emailCaptor.firstValue)
        assertEquals(true, isLoggedInCaptor.firstValue)
    }

    @Test
    fun `clearUserSession should remove session keys`() {

        userPreferences.clearUserSession()


        verify(mockEditor).remove(eq(UserPreferences.KEY_IS_LOGGED_IN))
        verify(mockEditor).remove(eq(UserPreferences.KEY_USER_EMAIL))

        verify(mockEditor).apply()
    }
}
