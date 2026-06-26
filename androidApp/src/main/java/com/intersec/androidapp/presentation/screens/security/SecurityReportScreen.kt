package com.intersec.androidapp.presentation.screens.security

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Security Command Center (Fase 2 - EvoluÃ§Ã£o Master)
 * Centro de Defesa Ativa e RelatÃ³rios de SOC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityReportScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "SECURITY COMMAND CENTER", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
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
                Text("NEURAL SECURITY LEVEL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
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
                    Text("NO CRITICAL THREATS DETECTED.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontFamily = FontFamily.Monospace)
                }
            } else {
                items(events) { event ->
                    SecurityEventItem(
                        event = event.uppercase(),
                        severity = if (event.contains("incompleta") || event.contains("incomum")) "MEDIUM" else "HIGH"
                    )
                }
            }

            // ===== REGRAS DE FIREWALL ATIVAS =====
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader("FIREWALL QUARANTINE")
                Spacer(Modifier.height(8.dp))
                FirewallRuleItem("192.168.1.50", "THREAT: PORT SCANNING", "DROP")
                FirewallRuleItem("malicious-cnc.ru", "THREAT: DATA EXFILTRATION", "REJECT")
            }

            item { Spacer(Modifier.height(40.dp)) }
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
            fontFamily = FontFamily.Monospace
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
                Text(title, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (isActive) color else Color.White)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontFamily = FontFamily.Monospace)
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
            Text("NETWORK THREAT LEVEL", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = color)
            Text("$score%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = color, fontFamily = FontFamily.Monospace)
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
fun SecurityEventItem(event: String, severity: String) {
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
                Text(event, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
                Text("SEVERITY: $severity", style = MaterialTheme.typography.labelSmall, color = color, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = { /* Bloquear IP */ }) {
                Icon(Icons.Default.Block, contentDescription = "Bloquear", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FirewallRuleItem(target: String, reason: String, action: String) {
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
            Text(target, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.White)
            Text(reason.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Monospace)
        }
        Text(
            action,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp)
        )
    }
}

