package com.example.etatdeslieux.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF000091),        // Bleu gouvernement
    onPrimary = Color.White,
    secondary = Color(0xFFE1000F),      // Rouge Marianne
    onSecondary = Color.White,
    tertiary = Color(0xFF161616),       // Gris foncé
    surface = Color.White.copy(alpha = 0.95f),
    background = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFFF5F5F5),
    error = Color(0xFFE1000F)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1B1B4B),        // Bleu gouvernement foncé
    onPrimary = Color.White,
    secondary = Color(0xFF8B0000),      // Rouge Marianne foncé
    onSecondary = Color.White,
    tertiary = Color(0xFFE0E0E0),       // Gris clair
    surface = Color(0xFF121212),
    background = Color(0xFF000000),
    surfaceVariant = Color(0xFF1E1E1E),
    error = Color(0xFF8B0000)
)

object AppTheme {
    private var _isDarkTheme by mutableStateOf(false)
    private var _fontScale by mutableStateOf(1f)

    val isDarkTheme: Boolean
        get() = _isDarkTheme

    val fontScale: Float
        get() = _fontScale

    fun toggleTheme() {
        _isDarkTheme = !_isDarkTheme
    }

    fun updateFontScale(scale: Float) {
        _fontScale = scale
    }
}

@Composable
fun EtatDesLieuxTheme(
    darkTheme: Boolean = AppTheme.isDarkTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = LocalTextStyle.current.fontSize * AppTheme.fontScale
        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
