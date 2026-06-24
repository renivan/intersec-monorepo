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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.data.model.dto.*
import com.intersec.androidapp.presentation.state.PacketColorPalette
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Tela de Overview do Tráfego (Fase 2 - Evolução Master)
 * Refatorada para o Tema Master Navy/Cyan.
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
        containerColor = PacketColorPalette.BACKGROUND_DARK,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Network Neural Intelligence", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PacketColorPalette.BACKGROUND_DARK
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshActiveSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PacketColorPalette.BACKGROUND_DARK)
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
                        color = Color.Cyan
                    )
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Fluxos Ativos",
                        value = state.session?.flowCount?.toString() ?: "0",
                        icon = Icons.Default.Route,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Volume de Dados",
                        value = formatVolume(overview.totalVolumeBytes),
                        icon = Icons.Default.BarChart,
                        color = Color.Cyan
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
                Text("Aguardando detecção de fluxos...", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.5f))
            } else {
                state.overview?.topCommunications?.forEach { comm ->
                    CommunicationItem(comm)
                }
            }

            Spacer(Modifier.height(24.dp))

            // ===== PROTOCOLOS & SEGURANÇA =====
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                SectionHeader("Protocolos")
                Text("Segurança", style = MaterialTheme.typography.labelSmall, color = Color.Cyan)
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
                Button(
                    onClick = onOpenFlows, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PacketColorPalette.CARD_BACKGROUND)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.Cyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Fluxos", color = Color.White)
                }
                FilledTonalButton(
                    onClick = onOpenPackets, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = PacketColorPalette.CARD_BACKGROUND)
                ) {
                    Icon(Icons.Default.List, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Pacotes", color = Color.White)
                }
            }
            
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onOpenGeoMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = Color.Cyan)
                Spacer(Modifier.width(8.dp))
                Text("Visualizar Mapa Geográfico", color = Color.White)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = onOpenSecurity,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
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
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun CommunicationItem(comm: CommunicationDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = PacketColorPalette.CARD_BACKGROUND)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Cyan.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Cyan)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${comm.source} ↔ ${comm.destination}", color = Color.White, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
                Row {
                    Text("${comm.packetCount} pacotes", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.width(8.dp))
                    Text("•", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Spacer(Modifier.width(8.dp))
                    Text(formatVolume(comm.volumeBytes), style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
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
                label = { Text(proto.name, color = Color.White) },
                leadingIcon = {
                    if (proto.isSecure) Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Cyan)
                    else Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.5f))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Cyan.copy(alpha = 0.2f),
                    containerColor = PacketColorPalette.CARD_BACKGROUND,
                    labelColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = Color.White.copy(alpha = 0.1f),
                    enabled = true,
                    selected = proto.isPredominant
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
        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Cyan)
        Spacer(Modifier.width(12.dp))
        Text(event, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall)
    }
}
