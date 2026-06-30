package com.intersec.androidapp.presentation.screens.flow

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.screens.overview.OverviewMiniCard
import com.intersec.androidapp.presentation.screens.overview.getRiskColor
import com.intersec.androidapp.presentation.viewmodel.FlowDetailViewModel
import com.intersec.androidapp.presentation.screens.capture.formatVolume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowDetailScreen(
    viewModel: FlowDetailViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("DETALHES DO FLUXO", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            state.item?.let { item ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    // ===== HEADER DO FLUXO =====
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("IDENTIFICADOR", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(item.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("ENDPOINTS", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(item.endpoints, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // ===== MÉTRICAS RÁPIDAS =====
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

                    // ===== SITUAÇÃO DE SEGURANÇA DO FLUXO =====
                    Text("Análise de Risco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    
                    val flowRisk = if (item.isInsecure) 85 else 10
                    val riskColor = getRiskColor(flowRisk)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (item.isInsecure) Icons.Default.Warning else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = riskColor
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    if (item.isInsecure) "Esta conexão não é criptografada" else "Conexão segura detectada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColor
                                )
                                Text(
                                    if (item.isInsecure) "Dados sensíveis podem estar expostos." else "Tráfego via protocolo TLS/SSL.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = riskColor.copy(alpha = 0.8f)
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
            }

            state.errorMessage?.let { error ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
