package com.intersec.androidapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

// --- Escuro (DARK) ---

private val CyberDarkColorScheme = darkColorScheme(
    primary = OliveGreen,
    onPrimary = Color.White,
    secondary = SoftGreen,
    background = CyberBlack,
    surface = CyberGray,
    error = Color(0xFFD00E2F)
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
    onSecondary = Color.White,
    onSurface = StudioDarkText,
    onBackground = StudioDarkText
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
            val insetsController = WindowCompat.getInsetsController(window, view)
            
            // Lógica moderna para barras de sistema (Suprimindo avisos de depreciação para manter compatibilidade visual)
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()

            insetsController.isAppearanceLightStatusBars = !isDarkMode
            insetsController.isAppearanceLightNavigationBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

@Preview(name = "Cyber Dark", showBackground = true)
@Composable
fun InterSecThemeCyberDarkPreview() {
    InterSecTheme(themeType = AppThemeType.CYBER_INTERSECURITY, isDarkMode = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            ThemePreviewContent("Cyber Dark")
        }
    }
}

@Preview(name = "Tech Master Light", showBackground = true)
@Composable
fun InterSecThemeTechMasterLightPreview() {
    InterSecTheme(themeType = AppThemeType.TECH_MASTER, isDarkMode = false) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            ThemePreviewContent("Tech Master Light")
        }
    }
}

@Preview(name = "Minimal Orange Dark", showBackground = true)
@Composable
fun InterSecThemeMinimalOrangeDarkPreview() {
    InterSecTheme(themeType = AppThemeType.MINIMAL_ORANGE, isDarkMode = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            ThemePreviewContent("Minimal Orange Dark")
        }
    }
}

@Preview(name = "Dark Night", showBackground = true)
@Composable
fun InterSecThemeDarkNightPreview() {
    InterSecTheme(themeType = AppThemeType.DARK_NIGHT, isDarkMode = true) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            ThemePreviewContent("Dark Night")
        }
    }
}

@Composable
private fun ThemePreviewContent(label: String) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Button(onClick = {}) {
            Text("Primary Button")
        }
        OutlinedTextField(
            value = "Sample Text",
            onValueChange = {},
            label = { Text("Label") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
