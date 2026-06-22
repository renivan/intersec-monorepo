package com.intersec.androidapp.presentation.screens.capture

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.core.file.AndroidCaptureImporter
import com.intersec.androidapp.presentation.screens.overview.NeuralMapVisual
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Dashboard Mission Control (Fase 2 - Evolução Master)
 * Centro de Operações Táticas Unificado.
 */
@Composable
fun MissionControlScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onOpenPackets: () -> Unit = {},
    onOpenFlows: () -> Unit = {},
    onOpenOverview: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenCaptureRealtime: () -> Unit = {},
    onOpenImportLog: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = AndroidCaptureImporter.importToCache(context, uri)
            viewModel.openCapture(localPath)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ===== IDENTIFICADOR DE REDE & ENTROPIA =====
        NetworkStatusSection(state.session != null, state.overview?.averageRiskScore ?: 0)

        Spacer(Modifier.height(16.dp))
        
        // Pulso Neural (Mini Visualização do Core Rust)
        NeuralMapVisual(modifier = Modifier.height(100.dp).fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        Text("Ações de Captura", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        QuickModeButton(
            "Captura Real-time", 
            Icons.Default.Videocam, 
            MaterialTheme.colorScheme.primary, 
            Modifier.fillMaxWidth(),
            onOpenCaptureRealtime
        )

        Spacer(Modifier.height(24.dp))

        // ===== GRID TÁTICO DE ANÁLISE =====
        Text("Análise & Inteligência", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(240.dp) 
        ) {
            item { TacticalCard("Fluxos", "Canais Ativos", Icons.Default.SwapHoriz, MaterialTheme.colorScheme.tertiary, onOpenFlows) }
            item { TacticalCard("Pacotes", "X-Ray Detail", Icons.Default.List, MaterialTheme.colorScheme.secondary, onOpenPackets) }
            item { TacticalCard("Overview", "Neural Map", Icons.Default.Assessment, MaterialTheme.colorScheme.primary, onOpenOverview) }
            item { TacticalCard("Segurança", "Relatório SOC", Icons.Default.Shield, MaterialTheme.colorScheme.error, onOpenSecurity) }
        }

        Spacer(Modifier.height(24.dp))

        // ===== IMPORTAÇÃO & SESSÃO =====
        Button(
            onClick = { filePicker.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh, contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Importar arquivo .pcap(ng)")
        }

        if (state.session != null) {
            Spacer(Modifier.height(16.dp))
            ActiveSessionPanel(
                sourceName = state.session?.sourceName ?: "",
                packetCount = state.session?.packetCount ?: 0,
                onClear = { viewModel.clearSession() },
                onLogs = onOpenImportLog
            )
        }

        Spacer(Modifier.height(24.dp))

        // ===== BEHAVIORAL ALERTS (DADOS REAIS DO MOTOR RUST) =====
        Text("Inteligência de Comportamento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        
        val events = state.overview?.events ?: emptyList()
        if (events.isEmpty()) {
            Text(
                "Nenhum comportamento suspeito detectado.", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            events.forEach { event ->
                val color = if (event.contains("incompleta") || event.contains("incomum")) 
                    Color(0xFFEAB308) else Color(0xFFEF4444)
                BehaviorAlertItem("Anomalia Detectada", event, color)
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp))
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
fun NetworkStatusSection(isCapturing: Boolean, riskScore: Int = 0) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (isCapturing) (100 - riskScore) / 100f else 0f },
                    modifier = Modifier.size(50.dp),
                    color = if (riskScore > 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    strokeWidth = 5.dp
                )
                Icon(
                    if (isCapturing) Icons.Default.Wifi else Icons.Default.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (riskScore > 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = if (isCapturing) "wlan0 (Wi-Fi Conectado)" else "Aguardando Interface",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isCapturing) "Integridade da Rede: ${100 - riskScore}%" else "Entropia: --",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun QuickModeButton(label: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ActiveSessionPanel(sourceName: String, packetCount: Long, onClear: () -> Unit, onLogs: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Adjust, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sourceName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("$packetCount pacotes", style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onLogs) { Icon(Icons.Default.Terminal, contentDescription = "Logs", modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onClear) { Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun BehaviorAlertItem(target: String, alert: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(target, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(alert, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
