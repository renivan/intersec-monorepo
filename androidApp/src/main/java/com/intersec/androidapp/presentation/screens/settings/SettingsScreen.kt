package com.intersec.androidapp.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import com.intersec.androidapp.presentation.screens.auth.ProUpgradeDialog
import com.intersec.androidapp.ui.theme.AppThemeType
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showProDialog by remember { mutableStateOf(false) }

    if (showProDialog) {
        ProUpgradeDialog(
            onDismiss = { showProDialog = false },
            onUpgrade = {
                viewModel.upgradeToPro()
                showProDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "CONFIGURAÇÕES", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            SectionHeader("STATUS DO OPERADOR")
            Spacer(Modifier.height(16.dp))
            
            val isPro = state.userTier == 1
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPro) Icons.Default.Star else Icons.Default.Person, 
                        contentDescription = null, 
                        tint = if (isPro) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(if (isPro) "OPERADOR PRO" else "OPERADOR BÁSICO", fontWeight = FontWeight.Bold, color = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        Text(if (isPro) "SISTEMAS OPERACIONAIS" else "ACESSO LIMITADO", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            SectionHeader("AMBIENTE VISUAL")
            Spacer(Modifier.height(16.dp))

            SettingToggleItem(
                title = if (state.isDarkMode) "MODO ESCURO" else "MODO CLARO",
                description = "ALTERNAR LUMINOSIDADE DO TERMINAL.",
                isActive = state.isDarkMode,
                icon = if (state.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                onToggle = { viewModel.toggleDarkMode(it) }
            )

            Spacer(Modifier.height(32.dp))
            SectionHeader("SINCRONIA COM MOTOR")
            Spacer(Modifier.height(16.dp))
            
            SettingToggleItem(
                title = "ESCUDO INTELIGENTE (IPS)",
                description = "SINCRONIZADO COM O NÚCLEO NATIVO.",
                isActive = state.isShieldActive,
                onToggle = { viewModel.toggleSmartShield(it) }
            )
            
            Spacer(Modifier.height(16.dp))
            
            SettingToggleItem(
                title = "KILL-SWITCH GLOBAL",
                description = "TERMINAÇÃO PREVENTIVA DE TRÁFEGO.",
                isActive = state.isKillSwitchOn,
                onToggle = { viewModel.toggleKillSwitch(it) }
            )

            Spacer(Modifier.height(32.dp))
            SectionHeader("🎨 IDENTIDADE VISUAL (PRO)")
            Spacer(Modifier.height(16.dp))
            
            Text(
                "ESCOLHA SUA INTERFACE. TEMAS ALTERNATIVOS REQUEREM ACESSO PRO.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontFamily = FontFamily.Default,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            AppThemeType.entries.forEach { theme ->
                ThemeOptionItem(
                    theme = theme,
                    isSelected = state.themeType == theme,
                    onSelect = {
                        if (state.userTier == 1) {
                            viewModel.updateTheme(theme)
                        } else {
                            showProDialog = true
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ThemeOptionItem(
    theme: AppThemeType,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface,
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                RoundedCornerShape(4.dp)
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = theme.label.uppercase(),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Default
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

@Composable
fun SettingToggleItem(
    title: String,
    description: String,
    isActive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
            Text(description, color = Color.Gray, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Default)
        }
        Switch(
            checked = isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}
