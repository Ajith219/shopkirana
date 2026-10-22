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

private val DarkColorScheme =
    darkColorScheme(
        primary = KiranaGreenPrimaryDark,
        onPrimary = KiranaGreenDark,
        primaryContainer = KiranaGreenPrimary,
        onPrimaryContainer = Color(0xFFA5D6A7),
        secondary = KiranaAmberSecondaryDark,
        onSecondary = Color(0xFF3E2723),
        background = KiranaBackgroundDark,
        surface = KiranaSurfaceDark,
        surfaceVariant = KiranaSurfaceVariantDark,
        onBackground = Color(0xFFE2E3DE),
        onSurface = Color(0xFFE2E3DE)
    )

private val LightColorScheme =
    lightColorScheme(
        primary = KiranaGreenPrimary,
        onPrimary = Color.White,
        primaryContainer = KiranaGreenContainer,
        onPrimaryContainer = KiranaOnGreenContainer,
        secondary = KiranaAmberSecondary,
        onSecondary = Color.White,
        secondaryContainer = KiranaAmberContainer,
        onSecondaryContainer = KiranaOnAmberContainer,
        tertiary = KiranaTealTertiary,
        onTertiary = Color.White,
        background = KiranaBackgroundLight,
        surface = KiranaSurfaceLight,
        surfaceVariant = KiranaSurfaceVariantLight,
        onBackground = KiranaTextPrimary,
        onSurface = KiranaTextPrimary,
        onSurfaceVariant = KiranaTextSecondary
    )

@Composable
fun KiranaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded Kirana green & amber palette
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
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

// Backwards-compatible alias if referenced elsewhere
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = KiranaTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
