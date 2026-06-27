package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val StadiumColorScheme = darkColorScheme(
    primary = StadiumNeonGreen,
    onPrimary = Color.White,
    primaryContainer = StadiumCardBackground,
    onPrimaryContainer = StadiumTextPrimary,
    secondary = StadiumNeonGreen,
    onSecondary = Color.White,
    background = StadiumBlack,
    onBackground = StadiumTextPrimary,
    surface = StadiumAnthracite,
    onSurface = StadiumTextPrimary,
    surfaceVariant = StadiumCardBackground,
    onSurfaceVariant = StadiumTextSecondary,
    outline = StadiumTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightSurface,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightPrimary,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBg,
    onSurfaceVariant = LightTextSecondary,
    outline = LightTextSecondary
)

@Composable
fun MyApplicationTheme(
    isStadium: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (isStadium) StadiumColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isStadium
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isStadium
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
