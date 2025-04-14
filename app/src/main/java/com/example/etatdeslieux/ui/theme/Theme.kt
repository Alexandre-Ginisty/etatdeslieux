package com.example.etatdeslieux.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),        // Bleu principal
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF1976D2),
    
    secondary = Color(0xFF42A5F5),      // Bleu accent
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3F2FD),
    onSecondaryContainer = Color(0xFF1976D2),
    
    surface = Color.White,
    onSurface = Color(0xFF212121),      // Noir
    surfaceVariant = Color(0xFFF5F5F5), // Gris clair
    onSurfaceVariant = Color(0xFF9E9E9E), // Gris
    
    error = Color(0xFFD32F2F),          // Rouge
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF42A5F5),        // Bleu accent
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1976D2),
    onPrimaryContainer = Color.White,
    
    secondary = Color(0xFF1976D2),      // Bleu principal
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1565C0),
    onSecondaryContainer = Color.White,
    
    surface = Color(0xFF212121),        // Noir
    onSurface = Color.White,
    surfaceVariant = Color(0xFF424242), // Gris foncé
    onSurfaceVariant = Color(0xFF9E9E9E), // Gris
    
    error = Color(0xFFD32F2F),          // Rouge
    onError = Color.White
)

object AppTheme {
    var isDarkTheme by mutableStateOf(false)
    var fontScale by mutableStateOf(1.0f)

    fun toggleTheme() {
        isDarkTheme = !isDarkTheme
    }

    fun updateFontScale(scale: Float) {
        fontScale = scale
    }
}

@Composable
fun EtatDesLieuxTheme(
    darkTheme: Boolean = AppTheme.isDarkTheme,
    fontScale: Float = AppTheme.fontScale,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
