package com.intersec.androidapp.presentation.screens.flow

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.screens.overview.NeuralMapVisual
import com.intersec.androidapp.presentation.screens.overview.OverviewMiniCard
import com.intersec.androidapp.presentation.screens.overview.formatVolume
import com.intersec.androidapp.presentation.screens.overview.getRiskColor
import com.intersec.androidapp.presentation.viewmodel.FlowDetailViewModel

/**
 * Tela de Detalhe de Fluxo (Fase 2 - Evolução Master)
 * Refatorada para ser um "Mini-Overview" específico da comunicação selecionada.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDetailScreen(
    viewModel: FlowDetailViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadSelectedFlow()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Flow Intelligence") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Ações de exportação */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar")
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
            if (state.item != null) {
                val item = state.item!!

                // ===== VISUAL NEURAL DO FLUXO =====
                Text("Conectividade Tática", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                NeuralMapVisual(modifier = Modifier.height(180.dp).fillMaxWidth(), nodeCount = 2) // Representação direta Source -> Dest
                
                Spacer(Modifier.height(24.dp))

                // ===== CARD PRINCIPAL DE IDENTIFICAÇÃO =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(item.label, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("ENDPOINTS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(item.endpoints, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ===== MÉTRICAS ESPECÍFICAS DO FLUXO =====
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Pacotes",
                        value = item.totalPackets.toString(),
                        icon = Icons.Default.Inventory2,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    OverviewMiniCard(
                        modifier = Modifier.weight(1f),
                        label = "Volume",
                        value = formatVolume(item.totalPayloadBytes),
                        icon = Icons.Default.BarChart,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(Modifier.height(24.dp))

                // ===== STATUS DE SEGURANÇA DO FLUXO =====
                Text("Análise de Risco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                val flowRisk = if (item.isInsecure) 85 else 10
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = getRiskColor(flowRisk).copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, getRiskColor(flowRisk).copy(alpha = 0.3f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (item.isInsecure) Icons.Default.Warning else Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = getRiskColor(flowRisk)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                if (item.isInsecure) "Esta conexão não é criptografada" else "Conexão segura detectada",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = getRiskColor(flowRisk)
                            )
                            Text(
                                if (item.isInsecure) "Dados sensíveis podem estar expostos." else "Tráfego via protocolo TLS/SSL.",
                                style = MaterialTheme.typography.bodySmall,
                                color = getRiskColor(flowRisk).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ===== AÇÕES DE DEFESA (KILL-SWITCH POR FLUXO) =====
                Text("Defesa Ativa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                Button(
                    onClick = { /* Bloquear destino */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("BLOQUEAR ENDEREÇO DE DESTINO")
                }
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { /* Ver pacotes deste fluxo */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("INSPECIONAR PACOTES DO FLUXO")
                }

                Spacer(Modifier.height(40.dp))
            }

            state.errorMessage?.let { error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
