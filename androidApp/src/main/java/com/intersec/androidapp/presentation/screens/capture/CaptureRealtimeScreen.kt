package com.intersec.androidapp.presentation.screens.capture

import android.annotation.SuppressLint
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.state.CaptureRealtimeUiState
import com.intersec.androidapp.presentation.state.PacketColorPalette
import com.intersec.androidapp.presentation.state.RealtimePacketModel
import com.intersec.androidapp.presentation.viewmodel.CaptureRealtimeViewModel

@Composable
fun CaptureRealtimeScreen(
    viewModel: CaptureRealtimeViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.onVpnAuthorized()
        }
    }

    if (state.showVpnTerms) {
        VpnTermsDialog(
            onAccept = {
                viewModel.acceptVpnTerms()
                val intent = VpnService.prepare(context)
                if (intent != null) {
                    vpnLauncher.launch(intent)
                } else {
                    viewModel.onVpnAuthorized()
                }
            },
            onDismiss = { viewModel.clearError() }
        )
    }

    if (state.errorMessage == "ERRO_SEM_ROOT") {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Acesso Negado (Root necessário)") },
            text = { Text("Não foi possível iniciar a captura direta na interface ${state.networkInterface}. Deseja tentar o modo VPN Sentinel (Não-Root)?") },
            confirmButton = {
                Button(onClick = { 
                    val intent = VpnService.prepare(context)
                    if (intent != null) {
                        vpnLauncher.launch(intent)
                    } else {
                        viewModel.onVpnAuthorized()
                    }
                    viewModel.clearError()
                }) {
                    Text("USAR MODO VPN")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PacketColorPalette.BACKGROUND_DARK)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            CaptureHeader(state, onBack)
            Spacer(Modifier.height(8.dp))
            InterfaceNetworkCard(state)
            
            Spacer(Modifier.height(12.dp))
            NeuralConnectivityMap(state)

            Spacer(Modifier.height(12.dp))
            CaptureFilterSection(state, viewModel)
            
            Spacer(Modifier.height(12.dp))
            CaptureControlsSection(state, viewModel)
            
            Spacer(Modifier.height(12.dp))
            CaptureStatusSection(state)
            Spacer(Modifier.height(12.dp))
            PacketsListSection(state)
        }
    }
}

@Composable
fun CaptureHeader(state: CaptureRealtimeUiState, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Captura Real-time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (state.isCapturing) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(Color.Red, shape = CircleShape)
            )
        }
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = Color.White.copy(alpha = 0.1f),
        thickness = 1.dp
    )
}

@Composable
fun InterfaceNetworkCard(state: CaptureRealtimeUiState) {
    var showInterfaceDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showInterfaceDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = PacketColorPalette.CARD_BACKGROUND
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("INTERFACE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text(state.networkInterface, color = Color.Cyan, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("TIPO DE REDE", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text(state.networkName, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
        }
    }

    if (showInterfaceDialog) {
        AlertDialog(
            onDismissRequest = { showInterfaceDialog = false },
            title = { Text("Selecionar Interface de Monitoramento") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.availableInterfaces) { info ->
                        ListItem(
                            headlineContent = { Text(info.interfaceName, fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${info.typeName} - ${info.details}") },
                            leadingContent = { 
                                val icon = when(info.typeName) {
                                    "Wi-Fi" -> Icons.Default.Wifi
                                    "Ethernet" -> Icons.Default.SettingsInputHdmi
                                    else -> Icons.Default.SignalCellularAlt
                                }
                                Icon(icon, contentDescription = null)
                            },
                            trailingContent = {
                                if (state.networkInterface == info.interfaceName) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Cyan)
                                }
                            },
                            modifier = Modifier.clickable { 
                                // viewModel.onInterfaceSelected(info.interfaceName, info.typeName)
                                showInterfaceDialog = false 
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInterfaceDialog = false }) { Text("FECHAR") }
            }
        )
    }
}

@Composable
fun VpnTermsDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🛡️ Autorização do Túnel Sentinel") },
        text = {
            Column {
                Text(
                    "Para realizar a análise em tempo real, o interSec precisa criar um túnel VPN local.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "• Seus dados NÃO saem do dispositivo.\n" +
                    "• O motor Rust processa tudo localmente.\n" +
                    "• Isso permite detectar invasões e vazamentos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ao aceitar, você verá um ícone de chave na barra de status.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Cyan, contentColor = Color.Black)
            ) {
                Text("ACEITAR E ATIVAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR")
            }
        }
    )
}

@Composable
fun CaptureFilterSection(state: CaptureRealtimeUiState, viewModel: CaptureRealtimeViewModel) {
    Column {
        Text(
            "🎯 Filtro BPF (Opcional)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = state.filterInput,
            onValueChange = { viewModel.updateFilterInput(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ex: tcp port 443", color = Color.Gray, fontSize = 14.sp) },
            enabled = !state.isCapturing,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                focusedContainerColor = PacketColorPalette.CARD_BACKGROUND,
                unfocusedContainerColor = PacketColorPalette.CARD_BACKGROUND
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun CaptureControlsSection(
    state: CaptureRealtimeUiState,
    viewModel: CaptureRealtimeViewModel
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!state.isCapturing) {
            Button(
                onClick = { 
                    if (!state.isVpnAuthorized) {
                        viewModel.requestVpnAuthorization()
                    } else {
                        viewModel.startCapture()
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("INICIAR CAPTURA")
            }
        } else {
            val controlColor = if (state.isPaused) Color(0xFF22C55E) else Color(0xFFEAB308)
            val controlText = if (state.isPaused) "RETOMAR" else "PAUSAR"
            val controlIcon = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause

            Button(
                onClick = { if (state.isPaused) viewModel.resumeCapture() else viewModel.pauseCapture() },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = controlColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(controlIcon, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(controlText)
            }

            Button(
                onClick = { viewModel.stopCapture() },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("PARAR")
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun CaptureStatusSection(state: CaptureRealtimeUiState) {
    if (!state.isCapturing) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PacketColorPalette.CARD_BACKGROUND)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("DURAÇÃO", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text(
                    String.format("%02d:%02d:%02d", 
                        state.elapsedSeconds / 3600, 
                        (state.elapsedSeconds % 3600) / 60, 
                        state.elapsedSeconds % 60),
                    color = Color.White, fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("PACOTES", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text(state.totalPackets.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PacketsListSection(state: CaptureRealtimeUiState) {
    var showFlows by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (showFlows) 1 else 0,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = !showFlows, onClick = { showFlows = false }) {
                Text("PACOTES", modifier = Modifier.padding(12.dp), color = Color.White)
            }
            Tab(selected = showFlows, onClick = { showFlows = true }) {
                Text("FLUXOS", modifier = Modifier.padding(12.dp), color = Color.White)
            }
        }

        Spacer(Modifier.height(8.dp))

        if (showFlows) {
            FlowsList(state)
        } else {
            if (state.packets.isEmpty() && state.isCapturing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(state.packets) { packet ->
                        PacketRow(packet)
                    }
                }
            }
        }
    }
}

@Composable
fun FlowsList(state: CaptureRealtimeUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(state.flows) { flow ->
            FlowRow(flow)
        }
    }
}

@Composable
fun FlowRow(flow: com.intersec.androidapp.presentation.state.RealtimeFlowModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PacketColorPalette.CARD_BACKGROUND),
        border = BorderStroke(1.dp, if (flow.isInsecure) Color.Red.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (flow.isInsecure) Icons.Default.Warning else Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = if (flow.isInsecure) Color.Red else Color.Cyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(flow.label, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text(flow.endpoints, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${flow.packetCount} pkts", style = MaterialTheme.typography.labelSmall, color = Color.Green)
                Text("${flow.payloadBytes} bytes", style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
            }
        }
    }
}

@Composable
fun PacketRow(packet: RealtimePacketModel) {
    val color = PacketColorPalette.getColorForType(packet.colorType)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("#${packet.number}", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
                Text("${String.format("%.3f", packet.timestampSeconds)}s", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                Text(packet.protocol, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(packet.info, style = MaterialTheme.typography.bodySmall, color = Color.White, fontFamily = FontFamily.Monospace)
        }
    }
}
