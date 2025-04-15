package com.example.etatdeslieux.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
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
    
    tertiary = Color(0xFFD32F2F),       // Rouge
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBEE),
    onTertiaryContainer = Color(0xFFD32F2F),
    
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
    
    tertiary = Color(0xFFEF5350),       // Rouge plus clair pour le mode sombre
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF8B0000),
    onTertiaryContainer = Color.White,
    
    surface = Color(0xFF212121),        // Noir
    onSurface = Color.White,
    surfaceVariant = Color(0xFF424242), // Gris foncé
    onSurfaceVariant = Color(0xFF9E9E9E), // Gris
    
    error = Color(0xFFEF5350),          // Rouge
    onError = Color.White
)

data class AppThemeState(
    val darkTheme: Boolean = false,
    val fontScale: Float = 1.0f,
    val highContrast: Boolean = false
)

val LocalAppTheme = compositionLocalOf { AppThemeState() }

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val appThemeState = AppThemeState(
        darkTheme = darkTheme,
        fontScale = fontScale,
        highContrast = highContrast
    )

    val density = Density(
        density = LocalDensity.current.density,
        fontScale = fontScale
    )
    
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Configuration de la barre d'état (status bar)
            window.statusBarColor = if (darkTheme) Color(0xFF121212).toArgb() else colorScheme.surface.toArgb()
            // Configuration de la barre de navigation
            window.navigationBarColor = if (darkTheme) Color(0xFF121212).toArgb() else Color.White.toArgb()
            // Configuration de l'apparence des icônes
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalDensity provides density,
        LocalAppTheme provides appThemeState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
