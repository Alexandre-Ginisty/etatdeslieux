package com.example.etatdeslieux.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000091), // fr_blue
    onPrimary = Color(0xFFFFFFFF), // fr_white
    secondary = Color(0xFFE1000F), // fr_red
    onSecondary = Color(0xFFFFFFFF), // fr_white
    tertiary = Color(0xFFE3E3FD), // fr_blue_light
    onTertiary = Color(0xFF000000), // fr_blue_dark
    background = Color(0xFFFFFFFF), // fr_white
    onBackground = Color(0xFF333333), // fr_grey_dark
    surface = Color(0xFFF7F7F7), // fr_grey_light
    onSurface = Color(0xFF333333), // fr_grey_dark
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE3E3FD), // fr_blue_light
    onPrimary = Color(0xFF000000), // fr_blue_dark
    secondary = Color(0xFFE1000F), // fr_red_light
    onSecondary = Color(0xFF000000), // fr_red_dark
    tertiary = Color(0xFF000091), // fr_blue
    onTertiary = Color(0xFFFFFFFF), // fr_white
    background = Color(0xFF333333), // fr_grey_dark
    onBackground = Color(0xFFFFFFFF), // fr_white
    surface = Color(0xFF666666), // fr_grey_medium
    onSurface = Color(0xFFFFFFFF), // fr_white
)

@Composable
fun EtatDesLieuxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
