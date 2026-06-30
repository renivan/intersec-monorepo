package com.intersec.androidapp.ui.theme

import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- ESCURO (DARK) ---

private val CyberDarkColorScheme = darkColorScheme(
    primary = OliveGreen,
    onPrimary = Color.White,
    secondary = SoftGreen,
    background = CyberBlack,
    surface = CyberGray,
    error = Color(0xFFCF6679)
)

private val TechMasterDarkColorScheme = darkColorScheme(
    primary = MasterSoftCyan,
    onPrimary = Color.Black,
    secondary = MasterNavy,
    background = MasterBackground,
    surface = MasterSurface,
    error = Color.Red
)

private val MinimalDarkColorScheme = darkColorScheme(
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

// --- CLARO (LIGHT) ---

private val CyberLightColorScheme = lightColorScheme(
    primary = OliveGreen,
    onPrimary = Color.White,
    secondary = SoftGreen,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onSurface = Color.Black,
    onBackground = Color.Black
)

private val TechMasterLightColorScheme = lightColorScheme(
    primary = MasterNavy,
    onPrimary = Color.White,
    secondary = MasterSoftCyan,
    background = Color(0xFFF0F4F8),
    surface = Color.White,
    onSurface = Color.Black,
    onBackground = Color.Black
)

private val MinimalLightColorScheme = lightColorScheme(
    primary = Color(0xFFE65100), // Laranja mais escuro para contraste no claro
    onPrimary = Color.White,
    secondary = SoftOrange,
    background = Color.White,
    surface = Color(0xFFFAFAFA),
    onSurface = Color.Black,
    onBackground = Color.Black
)

private val StudioLightColorScheme = lightColorScheme(
    primary = Color(0xFF3574F0),
    onPrimary = Color.White,
    secondary = Color(0xFFDFE1E5),
    background = Color.White,
    surface = Color(0xFFF7F8FA),
    onSurface = Color.Black,
    onBackground = Color.Black
)

@Composable
fun InterSecTheme(
    themeType: AppThemeType = AppThemeType.CYBER_INTERSECURITY,
    isDarkMode: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkMode) {
        when (themeType) {
            AppThemeType.CYBER_INTERSECURITY -> CyberDarkColorScheme
            AppThemeType.TECH_MASTER -> TechMasterDarkColorScheme
            AppThemeType.MINIMAL_ORANGE -> MinimalDarkColorScheme
            AppThemeType.DARK_NIGHT -> StudioDarkColorScheme
        }
    } else {
        when (themeType) {
            AppThemeType.CYBER_INTERSECURITY -> CyberLightColorScheme
            AppThemeType.TECH_MASTER -> TechMasterLightColorScheme
            AppThemeType.MINIMAL_ORANGE -> MinimalLightColorScheme
            AppThemeType.DARK_NIGHT -> StudioLightColorScheme
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
