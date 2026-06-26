package com.intersec.androidapp.presentation.screens.security

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.core.ads.AdManager
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Security Command Center (Fase 2 - Evolução Master)
 * Centro de Defesa Ativa e Relatórios de SOC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityReportScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "SECURITY COMMAND CENTER", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== MONETIZAÇÃO (PRIORIDADE MVP) =====
            if (state.userTier == 0) {
                item {
                    MonetizationCard(
                        onUpgrade = { viewModel.upgradeToPro() },
                        onWatchAd = {
                            (context as? Activity)?.let { activity ->
                                AdManager.showRewardedAd(
                                    activity = activity,
                                    onRewardEarned = { viewModel.addRewardTime() },
                                    onFailure = { /* Log erro ou Toast */ },
                                )
                            }
                        }
                    )
                }
            }

            // ===== DEFESA ATIVA (CONTROLES) =====
            item {
                SectionHeader("ACTIVE DEFENSE PROTOCOLS")
                Spacer(Modifier.height(8.dp))
                DefenseControlCard(
                    title = "INTELLIGENT SHIELD (IPS)",
                    description = "AUTOMATIC BLOCKING OF MALICIOUS IPs DETECTED BY ENGINE.",
                    icon = Icons.Default.Shield,
                    isActive = state.isShieldActive,
                    onToggle = { viewModel.toggleSmartShield(it) }
                )
                Spacer(Modifier.height(8.dp))
                DefenseControlCard(
                    title = "GLOBAL KILL-SWITCH",
                    description = "CUT ALL NETWORK TRAFFIC IMMEDIATELY ON BREACH DETECTION.",
                    icon = Icons.Default.Dangerous,
                    isActive = state.isKillSwitchOn,
                    color = MaterialTheme.colorScheme.error,
                    onToggle = { viewModel.toggleKillSwitch(it) }
                )
                
                Spacer(Modifier.height(16.dp))
                Text("NEURAL SECURITY LEVEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Default)
                Spacer(Modifier.height(8.dp))
                
                // Seletor de Nível
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecurityLevelButton(
                        label = "LOW",
                        isSelected = state.securityLevel == 0,
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { viewModel.updateSecurityLevel(0) },
                        modifier = Modifier.weight(1f)
                    )
                    SecurityLevelButton(
                        label = "NORMAL",
                        isSelected = state.securityLevel == 1,
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = { viewModel.updateSecurityLevel(1) },
                        modifier = Modifier.weight(1f)
                    )
                    SecurityLevelButton(
                        label = "CRITICAL",
                        isSelected = state.securityLevel == 2,
                        color = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.updateSecurityLevel(2) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ===== SCORE DE RISCO ATUAL =====
            item {
                RiskScoreCard(state.overview?.averageRiskScore ?: 0)
            }

            // ===== LOG DE EVENTOS DE SEGURANÇA (DADOS REAIS) =====
            item {
                SectionHeader("REAL-TIME SECURITY EVENTS")
            }

            val events = state.overview?.events ?: emptyList()
            if (events.isEmpty()) {
                item {
                    Text("NO CRITICAL THREATS DETECTED.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontFamily = FontFamily.Default)
                }
            } else {
                items(events) { event ->
                    SecurityEventItem(
                        event = event.uppercase(),
                        severity = if (event.contains("incompleta") || event.contains("incomum")) "MEDIUM" else "HIGH",
                        onBlockIp = { 
                            val ipMatch = Regex("""\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""").find(event)
                            val target = ipMatch?.value ?: "UNKNOWN_SOURCE"
                            viewModel.blockIp(target, "THREAT DETECTED: $event")
                        }
                    )
                }
            }

            // ===== REGRAS DE FIREWALL ATIVAS =====
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader("FIREWALL QUARANTINE")
                Spacer(Modifier.height(8.dp))
                if (state.firewallRules.isEmpty()) {
                    Text("NO ACTIVE BLOCKS.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = FontFamily.Default)
                }
            }

            items(state.firewallRules) { rule ->
                FirewallRuleItem(
                    rule.target, 
                    rule.reason, 
                    rule.action,
                    onDelete = { viewModel.removeFirewallRule(rule) }
                )
            }

            // ===== LOGS DE OPERAÇÃO =====
            item {
                Spacer(Modifier.height(16.dp))
                SectionHeader("SECURITY OPERATION LOGS")
            }

            val logs = state.importLogs.filter { it.message.contains("Firewall") || it.message.contains("Security") }.takeLast(5).reversed()
            if (logs.isEmpty()) {
                item {
                    Text("NO RECENT OPERATIONS.", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = FontFamily.Default)
                }
            } else {
                items(logs) { log ->
                    Text(
                        "[${log.timestamp}] ${log.message}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when(log.level.name) {
                            "ERROR" -> MaterialTheme.colorScheme.error
                            "WARNING" -> MaterialTheme.colorScheme.tertiary
                            "SUCCESS" -> MaterialTheme.colorScheme.primary
                            else -> Color.Gray
                        },
                        modifier = Modifier.padding(vertical = 2.dp),
                        fontFamily = FontFamily.Default
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun MonetizationCard(onUpgrade: () -> Unit, onWatchAd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("UPGRADE TO PRO PROTECTION", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 14.sp)
            }
            Text(
                "Unlock Advanced AI Threat Detection and Global IP Blacklisting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("GO PRO", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onWatchAd,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("FREE BOOST", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
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
fun SecurityLevelButton(
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color else color.copy(alpha = 0.1f),
            contentColor = if (isSelected) Color.Black else color
        ),
        border = BorderStroke(1.dp, color),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
fun DefenseControlCard(
    title: String,
    description: String,
    icon: ImageVector,
    isActive: Boolean,
    color: Color = MaterialTheme.colorScheme.primary,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isActive) color else Color.Gray)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, color = if (isActive) color else Color.White)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = FontFamily.Default)
            }
            Switch(
                checked = isActive, 
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun RiskScoreCard(score: Int) {
    val color = when {
        score < 30 -> MaterialTheme.colorScheme.primary
        score < 70 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NETWORK THREAT LEVEL", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Default, color = color)
            Text("$score%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Default)
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun SecurityEventItem(event: String, severity: String, onBlockIp: () -> Unit = {}) {
    val color = if (severity == "HIGH") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, color = Color.White)
                Text("SEVERITY: $severity", style = MaterialTheme.typography.labelSmall, color = color, fontFamily = FontFamily.Default)
            }
            IconButton(onClick = onBlockIp) {
                Icon(Icons.Default.Block, contentDescription = "Bloquear", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FirewallRuleItem(target: String, reason: String, action: String, onDelete: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(target, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, color = Color.White)
            Text(reason.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Default)
        }
        Text(
            action,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp)
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Remover", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}
