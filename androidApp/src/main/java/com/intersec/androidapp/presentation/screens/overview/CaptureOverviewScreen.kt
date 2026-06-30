package com.intersec.androidapp.presentation.screens.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.data.model.dto.CommunicationDto
import com.intersec.androidapp.data.model.dto.ProtocolStatDto
import com.intersec.androidapp.presentation.screens.capture.formatVolume
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureOverviewScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenPackets: () -> Unit = {},
    onOpenFlows: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenGeoMap: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.refreshActiveSession()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "VISÃO GERAL", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Default
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshActiveSession() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Atualizar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            SectionHeader("MAPA DE CONECTIVIDADE")
            NeuralMapVisual(
                modifier = Modifier
                    .height(220.dp)
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                nodeCount = (state.overview?.protocolStats?.size ?: 4).coerceIn(4, 12)
            )
            
            Spacer(Modifier.height(24.dp))

            SectionHeader("MÉTRICAS DE TRÁFEGO")
            state.overview?.let { overview ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "PACOTES",
                        value = state.session?.packetCount?.toString() ?: "0",
                        icon = Icons.Default.Inventory2,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "FLUXOS ATIVOS",
                        value = state.session?.flowCount?.toString() ?: "0",
                        icon = Icons.Default.Route,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "VOLUME DE DADOS",
                        value = formatVolume(overview.totalVolumeBytes),
                        icon = Icons.Default.BarChart,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "NÍVEL DE RISCO",
                        value = "${overview.averageRiskScore}%",
                        icon = Icons.Default.Security,
                        color = if (overview.averageRiskScore > 70) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader("CONEXÕES DETECTADAS")
            if (state.overview?.topCommunications?.isEmpty() == true) {
                Text("AGUARDANDO DETECÇÃO...", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Default)
            } else {
                state.overview?.topCommunications?.forEach { comm ->
                    CommunicationItem(comm)
                }
            }

            Spacer(Modifier.height(24.dp))

            SectionHeader("PROTOCOLOS")
            state.overview?.let { overview ->
                ProtocolFlowGrid(overview.protocolStats)
            }

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenFlows, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("FLUXOS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
                }
                OutlinedButton(
                    onClick = onOpenPackets, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("PACOTES", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
                }
            }
            
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onOpenGeoMap,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Public, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("MAPA GEOGRÁFICO", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
            }
            
            Spacer(Modifier.height(8.dp))
            
            Button(
                onClick = onOpenSecurity,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("CENTRAL DE SEGURANÇA", fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Default)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
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
fun CommunicationItem(comm: CommunicationDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${comm.source} -> ${comm.destination}", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
                Row {
                    Text("PKTS: ${comm.packetCount}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontFamily = FontFamily.Default)
                    Spacer(Modifier.width(8.dp))
                    Text("|", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(Modifier.width(8.dp))
                    Text("VOL: ${formatVolume(comm.volumeBytes)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontFamily = FontFamily.Default)
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
                label = { Text(proto.name, fontFamily = FontFamily.Default, fontSize = 10.sp) },
                leadingIcon = {
                    if (proto.isSecure) Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                    else Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                },
                shape = RoundedCornerShape(2.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = if (proto.isPredominant) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    enabled = true,
                    selected = proto.isPredominant,
                    borderWidth = 1.dp
                )
            )
        }
    }
}
