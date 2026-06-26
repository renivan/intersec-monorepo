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
import androidx.compose.material.icons.filled.Palette
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
                        "SYSTEM SETTINGS", 
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
            SectionHeader("USER TIER STATUS")
            Spacer(Modifier.height(16.dp))
            
            val isPro = state.userTier == 1
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPro) Color.Cyan.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, if (isPro) Color.Cyan else Color.Gray.copy(alpha = 0.2f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPro) Icons.Default.Star else Icons.Default.Person, 
                        contentDescription = null, 
                        tint = if (isPro) Color.Cyan else Color.Gray
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(if (isPro) "PRO OPERATOR" else "BASIC OPERATOR", fontWeight = FontWeight.Bold, color = if (isPro) Color.Cyan else Color.White)
                        Text(if (isPro) "ALL SYSTEMS OPERATIONAL" else "LIMITED ACCESS MODE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            SectionHeader("GLOBAL PREFERENCES")
            Spacer(Modifier.height(16.dp))

            SettingToggleItem(
                title = "REAL-TIME ALERTS",
                description = "RECEIVE CRITICAL THREAT ALERTS IN STATUS BAR.",
                isActive = true,
                onToggle = { }
            )

            Spacer(Modifier.height(32.dp))
            SectionHeader("CORE ENGINE SYNC")
            Spacer(Modifier.height(16.dp))
            
            SettingToggleItem(
                title = "INTELLIGENT SHIELD (IPS)",
                description = "SYNCED WITH NATIVE CORE ENGINE.",
                isActive = state.isShieldActive,
                onToggle = { viewModel.toggleSmartShield(it) }
            )
            
            Spacer(Modifier.height(16.dp))
            
            SettingToggleItem(
                title = "GLOBAL KILL-SWITCH",
                description = "PREVENTIVE TRAFFIC TERMINATION.",
                isActive = state.isKillSwitchOn,
                onToggle = { viewModel.toggleKillSwitch(it) }
            )

            Spacer(Modifier.height(32.dp))
            SectionHeader("🎨 VISUAL IDENTITY & THEMES (PRO)")
            Spacer(Modifier.height(16.dp))
            
            Text(
                "CHOOSE YOUR OPERATIONAL SKIN. ALTERNATIVE THEMES REQUIRE PRO CLEARANCE.",
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
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.1f),
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
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
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
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
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
