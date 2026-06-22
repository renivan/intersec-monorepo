package com.intersec.androidapp.presentation.screens.overview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.data.model.dto.*
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Tela de Overview do Tráfego (Fase 2 - Evolução Master)
 * Refatorada para Material 3 Dynamic Colors e Visual "Neural Map" Detalhado.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CaptureOverviewScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenPackets: () -> Unit = {},
    onOpenFlows: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenGeoMap: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.refreshActiveSession()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Network Neural Intelligence") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshActiveSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // ===== NEURAL MAP VISUAL (DETALHAMENTO DINÂMICO) =====
            SectionHeader("Neural Connectivity Map")
            NeuralMapVisual(
                modifier = Modifier.height(220.dp).fillMaxWidth(),
                nodeCount = (state.overview?.protocolStats?.size ?: 4).coerceIn(4, 12)
            )
            
            Spacer(Modifier.height(24.dp))

            // ===== MÉTRICAS OPERACIONAIS GLOBAIS =====
            SectionHeader("Métricas de Operação")
            state.overview?.let { overview ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Pacotes Totais",
                        value = state.session?.packetCount?.toString() ?: "0",
                        icon = Icons.Default.Inventory2,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Fluxos Ativos",
                        value = state.session?.flowCount?.toString() ?: "0",
                        icon = Icons.Default.Route,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Volume de Dados",
                        value = formatVolume(overview.totalVolumeBytes),
                        icon = Icons.Default.BarChart,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Índice de Risco",
                        value = "${overview.averageRiskScore}%",
                        icon = Icons.Default.Security,
                        color = getRiskColor(overview.averageRiskScore)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== TOP COMUNICAÇÕES (DETALHAMENTO DE PACOTES/IP) =====
            SectionHeader("Top Talkers (Comunicações Críticas)")
            if (state.overview?.topCommunications?.isEmpty() == true) {
                Text("Aguardando detecção de fluxos...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                state.overview?.topCommunications?.forEach { comm ->
                    CommunicationItem(comm)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== PROTOCOLOS & SEGURANÇA =====
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeader("Protocolos")
                Text("Segurança", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            state.overview?.let { overview ->
                ProtocolFlowGrid(overview.protocolStats)
            }

            Spacer(Modifier.height(24.dp))

            // ===== EVENTOS DE INTELIGÊNCIA =====
            SectionHeader("Eventos de Inteligência")
            state.overview?.events?.forEach { event ->
                IntelligenceEventRow(event)
            }

            Spacer(Modifier.height(32.dp))

            // ===== AÇÕES TÁTICAS =====
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenFlows, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Fluxos")
                }
                FilledTonalButton(onClick = onOpenPackets, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pacotes")
                }
            }
            
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onOpenGeoMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Public, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Visualizar Mapa Geográfico")
            }
            
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = onOpenSecurity,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Painel de Defesa Ativa")
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun CommunicationItem(comm: CommunicationDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${comm.source} ↔ ${comm.destination}", style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                Row {
                    Text("${comm.packetCount} pacotes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(formatVolume(comm.volumeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProtocolFlowGrid(protocols: List<ProtocolStatDto>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        protocols.forEach { proto ->
            FilterChip(
                selected = proto.isPredominant,
                onClick = {},
                label = { Text(proto.name) },
                leadingIcon = {
                    if (proto.isSecure) Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                    else Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun IntelligenceEventRow(event: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(event, style = MaterialTheme.typography.bodySmall)
    }
}
