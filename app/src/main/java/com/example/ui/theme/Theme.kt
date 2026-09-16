package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RadarTealLight,
    onPrimary = Color.Black,
    primaryContainer = RadarTealDark,
    onPrimaryContainer = Color.White,
    secondary = RadarCyan,
    onSecondary = Color.Black,
    tertiary = RadarMint,
    onTertiary = Color.Black,
    background = RadarDarkBackground,
    onBackground = RadarDarkTextPrimary,
    surface = RadarDarkSurface,
    onSurface = RadarDarkTextPrimary,
    surfaceVariant = RadarDarkSurfaceElevated,
    onSurfaceVariant = RadarDarkTextSecondary,
    outline = RadarDarkCardBorder,
    error = RadarAlert
)

private val LightColorScheme = lightColorScheme(
    primary = RadarTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = RadarCyan,
    onSecondary = Color.White,
    tertiary = RadarMint,
    onTertiary = Color.White,
    background = RadarLightBackground,
    onBackground = RadarLightTextPrimary,
    surface = RadarLightSurface,
    onSurface = RadarLightTextPrimary,
    surfaceVariant = RadarLightSurfaceElevated,
    onSurfaceVariant = RadarLightTextSecondary,
    outline = RadarLightCardBorder,
    error = RadarAlert
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use intentional branded radar palette
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
