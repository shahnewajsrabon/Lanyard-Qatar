package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = LanyardPrimaryDark,
    secondary = LanyardSecondaryDark,
    tertiary = LanyardTertiaryDark,
    background = LanyardBackgroundDark,
    surface = LanyardSurfaceDark,
    error = LanyardErrorDark
)

private val LightColorScheme = lightColorScheme(
    primary = LanyardPrimaryLight,
    secondary = LanyardSecondaryLight,
    tertiary = LanyardTertiaryLight,
    background = LanyardBackgroundLight,
    surface = LanyardSurfaceLight,
    error = LanyardErrorLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prioritize branding colors
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
