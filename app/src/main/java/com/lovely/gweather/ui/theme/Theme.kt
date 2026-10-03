package com.lovely.gweather.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF7BC6BB),
    secondary = Color(0xFFE5AA50),
    tertiary = Color(0xFFE58B6B),
    background = Color(0xFF13292E),
    surface = Color(0xFF1B353A),
    onSurface = Color(0xFFEAF4F1)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF247A78),
    onPrimary = Color.White,
    secondary = Color(0xFFC9674D),
    tertiary = Color(0xFFD69B32),
    background = Color(0xFFF2F7F5),
    surface = Color.White,
    onSurface = Color(0xFF18333A),
    onSurfaceVariant = Color(0xFF61767A)
)

@Composable
fun GWeatherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}