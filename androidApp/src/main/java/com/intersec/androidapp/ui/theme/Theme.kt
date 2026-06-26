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

private val MilitaryColorScheme = darkColorScheme(
    primary = TacticalGreen,
    onPrimary = Color.Black,
    secondary = RadarGray,
    onSecondary = TacticalGreen,
    tertiary = WarningOrange,
    background = StealthGray,
    surface = RadarGray,
    error = AlertRed
)

private val TechMasterColorScheme = darkColorScheme(
    primary = MasterPrimary,
    onPrimary = Color.White,
    secondary = MasterCyan,
    background = MasterBackground,
    surface = MasterSurface,
    error = Color.Red
)

private val MinimalColorScheme = darkColorScheme(
    primary = MinimalOrange,
    onPrimary = Color.White,
    secondary = MinimalWhite,
    background = MinimalBlack,
    surface = MinimalGray,
    error = Color.Red
)

private val MaterialLightColorScheme = lightColorScheme(
    primary = MaterialPrimary,
    background = MaterialBackground,
    surface = MaterialSurface,
    error = MaterialError
)

@Composable
fun InterSecTheme(
    themeType: AppThemeType = AppThemeType.TACTICAL_MILITARY,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        AppThemeType.TACTICAL_MILITARY -> MilitaryColorScheme
        AppThemeType.TECH_MASTER -> TechMasterColorScheme
        AppThemeType.MINIMAL -> MinimalColorScheme
        AppThemeType.MATERIAL_STANDARD -> {
            val context = LocalContext.current
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) darkColorScheme(primary = MaterialPrimary) else MaterialLightColorScheme
            }
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
