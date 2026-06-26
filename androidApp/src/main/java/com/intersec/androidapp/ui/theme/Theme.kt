package com.intersec.androidapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberColorScheme = darkColorScheme(
    primary = OliveGreen,
    onPrimary = Color.White,
    secondary = SoftGreen,
    background = CyberBlack,
    surface = CyberGray,
    error = Color(0xFFCF6679)
)

private val TechMasterColorScheme = darkColorScheme(
    primary = MasterSoftCyan,
    onPrimary = Color.Black,
    secondary = MasterNavy,
    background = MasterBackground,
    surface = MasterSurface,
    error = Color.Red
)

private val MinimalColorScheme = darkColorScheme(
    primary = SoftOrange,
    onPrimary = Color.Black,
    secondary = MinimalWhite,
    background = MinimalBlack,
    surface = MinimalSurface,
    error = Color.Red
)

private val StudioDarkColorScheme = darkColorScheme(
    primary = StudioDarkPrimary,
    onPrimary = Color.White,
    secondary = StudioDarkSelection,
    background = StudioDarkBackground,
    surface = StudioDarkSurface,
    error = StudioDarkError,
    onSurface = StudioDarkText
)

@Composable
fun InterSecTheme(
    themeType: AppThemeType = AppThemeType.CYBER_INTERSECURITY,
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        AppThemeType.CYBER_INTERSECURITY -> CyberColorScheme
        AppThemeType.TECH_MASTER -> TechMasterColorScheme
        AppThemeType.MINIMAL_ORANGE -> MinimalColorScheme
        AppThemeType.DARK_NIGHT -> StudioDarkColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
