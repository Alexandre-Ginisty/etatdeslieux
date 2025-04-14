package com.example.etatdeslieux.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val LightColors = lightColorScheme(
    // Vos couleurs personnalisées ici
)

private val DarkColors = darkColorScheme(
    // Vos couleurs personnalisées ici
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

    CompositionLocalProvider(
        LocalDensity provides density,
        LocalAppTheme provides appThemeState
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            content = content
        )
    }
}
