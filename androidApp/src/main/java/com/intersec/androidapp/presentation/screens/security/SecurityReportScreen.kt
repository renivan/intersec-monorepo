package com.intersec.androidapp.presentation.screens.security

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Security Command Center (Fase 2 - Evolução Master)
 * Centro de Defesa Ativa e Relatórios de SOC.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityReportScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    var isShieldActive by remember { mutableStateOf(true) }
    var isKillSwitchOn by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Security Command Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
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
                Text("Controles de Defesa Ativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                DefenseControlCard(
                    title = "Escudo Inteligente (IPS)",
                    description = "Bloqueio automático de IPs maliciosos detectados pelo Rust.",
                    icon = Icons.Default.Shield,
                    isActive = isShieldActive,
                    onToggle = { isShieldActive = it }
                )
                Spacer(Modifier.height(8.dp))
                DefenseControlCard(
                    title = "Kill-Switch Global",
                    description = "Corta todo o tráfego de rede imediatamente em caso de invasão.",
                    icon = Icons.Default.Dangerous,
                    isActive = isKillSwitchOn,
                    color = MaterialTheme.colorScheme.error,
                    onToggle = { isKillSwitchOn = it }
                )
            }

            // ===== SCORE DE RISCO ATUAL =====
            item {
                RiskScoreCard(state.overview?.averageRiskScore ?: 0)
            }

            // ===== LOG DE EVENTOS DE SEGURANÇA (DADOS REAIS) =====
            item {
                Text("Eventos de Segurança em Tempo Real", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            val events = state.overview?.events ?: emptyList()
            if (events.isEmpty()) {
                item {
                    Text("Nenhum evento crítico detectado no momento.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(events) { event ->
                    SecurityEventItem(
                        event = event,
                        severity = if (event.contains("incompleta") || event.contains("incomum")) "Média" else "Alta"
                    )
                }
            }

            // ===== REGRAS DE FIREWALL ATIVAS =====
            item {
                Spacer(Modifier.height(8.dp))
                Text("Regras de Firewall (Quarentena)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                FirewallRuleItem("192.168.1.50", "Bloqueado (Scanning)", "DROP")
                FirewallRuleItem("malicious-cnc.ru", "Bloqueado (Exfiltração)", "REJECT")
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isActive) color else MaterialTheme.colorScheme.outline)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = isActive, onCheckedChange = onToggle)
        }
    }
}

@Composable
fun RiskScoreCard(score: Int) {
    val color = when {
        score < 30 -> Color(0xFF22C55E)
        score < 70 -> Color(0xFFEAB308)
        else -> Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nível de Risco da Rede", style = MaterialTheme.typography.labelLarge)
            Text("$score%", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black, color = color)
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SecurityEventItem(event: String, severity: String) {
    val color = if (severity == "Alta") Color(0xFFEF4444) else Color(0xFFEAB308)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(color))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("Severidade: $severity", style = MaterialTheme.typography.labelSmall, color = color)
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
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(target, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            action,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp).background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
        )
    }
}
