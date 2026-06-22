package com.intersec.androidapp.presentation.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intersec.androidapp.core.bridge.RustPacketItem
import com.intersec.androidapp.core.bridge.RustSessionSnapshot
import com.intersec.androidapp.presentation.viewmodel.CaptureActionsViewModel

@Composable
fun HeaderSection(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "📊 Overview & Ações",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(onClick = onBack) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Gerenciamento de captura em tempo real",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun StatusPanel(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isError)
                Color(0xFFEF4444).copy(alpha = 0.2f)
            else
                Color(0xFF22C55E).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isError) Color(0xFFEF4444) else Color(0xFF22C55E),
                modifier = Modifier.size(20.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                message,
                color = Color.White,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun ActionsSection(
    state: com.intersec.androidapp.presentation.state.CaptureActionUiState,
    viewModel: CaptureActionsViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "🎬 Controles de Captura",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Botões principais
        if (!state.isCapturing) {
            ActionButton(
                label = "▶ Iniciar Captura",
                description = "Começar captura em tempo real",
                icon = Icons.Default.PlayArrow,
                isEnabled = !state.isLoading,
                color = Color(0xFF22C55E),
                onClick = { viewModel.startCapture("wlan0") },
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    label = "🔄 Atualizar",
                    description = "Carregar pacotes",
                    icon = Icons.Default.Refresh,
                    isEnabled = !state.isLoading,
                    color = Color(0xFF3B82F6),
                    onClick = { viewModel.refreshPackets() },
                    modifier = Modifier.weight(1f)
                )

                ActionButton(
                    label = "⏹ Parar",
                    description = "Encerrar captura",
                    icon = Icons.Default.Stop,
                    isEnabled = !state.isLoading,
                    color = Color(0xFFEF4444),
                    onClick = { viewModel.stopCapture() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Filtro
        FilterCard(state = state, viewModel = viewModel)

        Spacer(Modifier.height(12.dp))

        // Mais ações
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                label = "💾 Exportar",
                description = "Salvar captura",
                icon = Icons.Default.Download,
                isEnabled = state.lastSnapshot != null,
                color = Color(0xFF8B5CF6),
                onClick = { viewModel.exportCapture() },
                modifier = Modifier.weight(1f)
            )

            ActionButton(
                label = "🔍 Análise",
                description = "Analisar tráfego",
                icon = Icons.Default.Analytics,
                isEnabled = state.currentPackets.isNotEmpty(),
                color = Color(0xFFF59E0B),
                onClick = { /* TODO */ },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FilterCard(
    state: com.intersec.androidapp.presentation.state.CaptureActionUiState,
    viewModel: CaptureActionsViewModel
) {
    var filterText by remember { mutableStateOf(state.captureFilter) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                "🎯 Filtro BPF",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("TCP.port == 443", fontSize = 11.sp) },
                singleLine = true,
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFF3B82F6),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.updateFilter(filterText) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3B82F6)
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Aplicar Filtro", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun ActionButton(
    label: String,
    description: String,
    icon: ImageVector,
    isEnabled: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = Color(0xFF64748B).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SnapshotCard(snapshot: RustSessionSnapshot) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E293B)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "📊 Snapshot Atual",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = "📦",
                    label = "Pacotes",
                    value = snapshot.totalPackets.toString(),
                    color = Color(0xFF3B82F6)
                )

                StatItem(
                    icon = "🌊",
                    label = "Fluxos",
                    value = snapshot.totalFlows.toString(),
                    color = Color(0xFF22C55E)
                )

                StatItem(
                    icon = "📁",
                    label = "Arquivo",
                    value = snapshot.sourceName.take(10),
                    color = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: String,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 20.sp)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun PacketsListSection(packets: List<RustPacketItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "📡 Últimos Pacotes Capturados",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(packets.take(10)) { packet ->
                    PacketRowItem(packet)
                }
            }
        }
    }
}

@Composable
fun PacketRowItem(packet: RustPacketItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "#${packet.packetNumber}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                packet.info.take(50),
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1
            )
        }

        Text(
            packet.highestProtocol ?: "?",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when (packet.highestProtocol) {
                "TCP" -> Color(0xFF3B82F6)
                "UDP" -> Color(0xFF22C55E)
                "TLS" -> Color(0xFF8B5CF6)
                else -> Color.White.copy(alpha = 0.5f)
            }
        )
    }

    Divider(
        color = Color.White.copy(alpha = 0.1f),
        thickness = 1.dp
    )
}


