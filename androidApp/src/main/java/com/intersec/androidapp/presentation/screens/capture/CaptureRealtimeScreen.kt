package com.intersec.androidapp.presentation.screens.capture

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.SettingsInputHdmi
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.core.ads.AdManager
import com.intersec.androidapp.core.vpn.InterSecVpnService
import com.intersec.androidapp.presentation.state.CaptureRealtimeUiState
import com.intersec.androidapp.presentation.state.PacketColorPalette
import com.intersec.androidapp.presentation.state.RealtimeFlowModel
import com.intersec.androidapp.presentation.state.RealtimePacketModel
import com.intersec.androidapp.presentation.viewmodel.CaptureRealtimeViewModel
import java.util.Locale

/**
 * Função de utilidade compartilhada para formatação de volume de dados.
 */
fun formatVolume(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 * 1024 -> String.format(Locale.US, "%.1f GB", bytes.toFloat() / (1024 * 1024 * 1024))
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes.toFloat() / (1024 * 1024))
        bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes.toFloat() / 1024)
        else -> "$bytes bytes"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureRealtimeScreen(
    viewModel: CaptureRealtimeViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val scrollState = rememberScrollState()
    
    val vpnLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            context.startService(Intent(context, InterSecVpnService::class.java).apply { action = "START" })
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
                    context.startService(Intent(context, InterSecVpnService::class.java).apply { action = "START" })
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
            text = { Text("Não foi possível iniciar a captura direta na interface ${state.networkInterface}. Deseja tentar o modo VPN segura (Não-Root)?") },
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
                    Text("CANCELAR OPERAÇÃO")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(12.dp)
        ) {
            CaptureHeader(state, viewModel, onBack)
            Spacer(Modifier.height(8.dp))
            InterfaceNetworkCard(state, viewModel)
            
            Spacer(Modifier.height(12.dp))
            NeuralConnectivityMap(state)

            Spacer(Modifier.height(12.dp))
            CaptureFilterSection(state, viewModel)
            
            Spacer(Modifier.height(12.dp))
            CaptureControlsSection(state, viewModel, activity, context)
            
            Spacer(Modifier.height(12.dp))
            CaptureStatusSection(state)
            Spacer(Modifier.height(12.dp))
            PacketsListSection(state)
        }
    }

    if (state.showSummaryModal) {
        CaptureSummaryModal(
            state = state,
            onDiscard = { 
                context.startService(Intent(context, InterSecVpnService::class.java).apply { action = "STOP" })
                viewModel.discardCapture() 
            },
            onContinue = { viewModel.hideSummary() },
            onAnalyze = { 
                if (state.userTier == 0) {
                    activity?.let {
                        AdManager.showRewardedAd(it, {
                            viewModel.hideSummary()
                        }, {
                            // Erro
                        })
                    }
                } else {
                    viewModel.hideSummary() 
                }
            }
        )
    }
}

@Composable
fun CaptureHeader(state: CaptureRealtimeUiState, viewModel: CaptureRealtimeViewModel, onBack: () -> Unit) {
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Captura Real-time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (state.userTier == 1) "PRO" else "FREE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = if (state.userTier == 1) Color(0xFFEAB308) else Color.Gray,
                modifier = Modifier.padding(end = 4.dp)
            )
            Switch(
                checked = state.userTier == 1,
                onCheckedChange = { viewModel.toggleTier() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFEAB308),
                    checkedTrackColor = Color(0xFFEAB308).copy(alpha = 0.5f),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                ),
                modifier = Modifier.size(32.dp).padding(end = 8.dp)
            )

            if (state.isCapturing) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red, shape = CircleShape)
                )
            }
        }
    }

    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
        thickness = 1.dp
    )
}

@Composable
fun InterfaceNetworkCard(state: CaptureRealtimeUiState, viewModel: CaptureRealtimeViewModel) {
    var showInterfaceDialog by remember { mutableStateOf(false) }
    val isPro = state.userTier == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isPro) { showInterfaceDialog = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isPro) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isPro) "INTERFACE ATIVA" else "CONEXÃO AUTOMÁTICA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(state.networkInterface, color = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("TIPO DE CONEXÃO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) )
                Text(state.networkName.uppercase(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            if (isPro) {
                Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
            }
        }
    }

    if (showInterfaceDialog) {
        AlertDialog(
            onDismissRequest = { showInterfaceDialog = false },
            title = { 
                Text(
                    "SELECIONAR INTERFACE DE MONITORAMENTO", 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default
                ) 
            },
            containerColor = MaterialTheme.colorScheme.surface,
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(state.availableInterfaces) { info ->
                        val isSelected = state.networkInterface == info.interfaceName
                        val icon = when(info.typeName) {
                            "Wi-Fi" -> Icons.Default.Wifi
                            "Ethernet" -> Icons.Default.SettingsInputHdmi
                            else -> Icons.Default.SignalCellularAlt
                        }
                        ListItem(
                            headlineContent = { 
                                Text(
                                    info.interfaceName, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            supportingContent = { 
                                Text(
                                    "${info.typeName} - ${info.details}",
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else Color.Gray
                                ) 
                            },
                            leadingContent = { 
                                Icon(icon, contentDescription = null, tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
                            },
                            trailingContent = {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                headlineColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                leadingIconColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            ),
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable { 
                                    viewModel.onInterfaceSelected(info.interfaceName, info.typeName)
                                    showInterfaceDialog = false 
                                }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showInterfaceDialog = false }) { 
                    Text("FECHAR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) 
                }
            }
        )
    }
}

@Composable
fun VpnTermsDialog(onAccept: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Autorização do Túnel Sentinel") },
        text = {
            Column {
                Text(
                    "Para realizar a análise em tempo real, o interSec precisa criar um túnel VPN local.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "• Seus dados NÃO saem do dispositivo.\n" +
                    "• O motor Native processa tudo localmente.\n" +
                    "• Isso permite detectar invasões e vazamentos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text("ACEITAR E ATIVAR PROTEÇÃO")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
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
            color = MaterialTheme.colorScheme.onBackground,
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
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun CaptureControlsSection(
    state: CaptureRealtimeUiState,
    viewModel: CaptureRealtimeViewModel,
    activity: Activity?,
    context: android.content.Context
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!state.isCapturing) {
            Button(
                onClick = { 
                    val onAction = {
                        if (!state.isVpnAuthorized) {
                            viewModel.requestVpnAuthorization()
                        } else {
                            viewModel.startCapture()
                        }
                    }

                    if (state.userTier == 0) {
                        activity?.let {
                            AdManager.showRewardedAd(it, {
                                onAction()
                            }, {
                                // Fallback: se o ad falhar, inicia assim mesmo para não travar o usuário
                                onAction()
                            })
                        } ?: onAction()
                    } else {
                        onAction()
                    }
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFF22C55E).copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.userTier == 0) "INICIAR (AD REQ)" else "INICIAR CAPTURA")
            }
        } else {
            val controlColor = if (state.isPaused) Color(0xFF22C55E) else Color(0xFFEAB308)
            val controlText = if (state.isPaused) "RETOMAR" else "PAUSAR"
            val controlIcon = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause

            Button(
                onClick = { if (state.isPaused) viewModel.resumeCapture() else viewModel.pauseCapture() },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = controlColor),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, controlColor.copy(alpha = 0.5f))
            ) {
                Icon(controlIcon, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(controlText)
            }

            Button(
                onClick = { 
                    context.startService(Intent(context, InterSecVpnService::class.java).apply { action = "STOP" })
                    viewModel.stopCapture() 
                },
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("DURAÇÃO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(
                    String.format("%02d:%02d:%02d", 
                        state.elapsedSeconds / 3600, 
                        (state.elapsedSeconds % 3600) / 60, 
                        state.elapsedSeconds % 60),
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("PACOTES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(state.totalPackets.toString(), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CaptureSummaryModal(
    state: CaptureRealtimeUiState,
    onDiscard: () -> Unit,
    onContinue: () -> Unit,
    onAnalyze: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onContinue,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { 
            Text(
                "RESUMO DA OPERAÇÃO", 
                color = MaterialTheme.colorScheme.primary, 
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Default,
                fontSize = 16.sp
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryRow("PACOTES CAPTURADOS", state.totalPackets.toString())
                SummaryRow("DURAÇÃO DA MISSÃO", String.format(java.util.Locale.US, "%02d:%02d:%02d", 
                    state.elapsedSeconds / 3600, 
                    (state.elapsedSeconds % 3600) / 60, 
                    state.elapsedSeconds % 60))
                SummaryRow("VOLUME TOTAL", formatVolume(state.totalBytes))

                val avgSize = if (state.totalPackets > 0) state.totalBytes / state.totalPackets else 0L
                SummaryRow("TAMANHO MÉDIO PKT", "$avgSize bytes")
                
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                
                Text(
                    "DESEJA DESCARTAR OS DADOS OU PROSSEGUIR COM A ANÁLISE DETALHADA?",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontFamily = FontFamily.Default
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAnalyze,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Text(if (state.userTier == 0) "ASSISTIR AD E ANALISAR" else "ANALISAR DADOS", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscard) {
                    Text("DESCARTAR", color = Color.Red.copy(alpha = 0.7f))
                }
                TextButton(onClick = onContinue) {
                    Text("MANTER PAUSADO", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    )
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Default)
        Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketsListSection(state: CaptureRealtimeUiState) {
    var showFlows by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp) 
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        PrimaryTabRow(
            selectedTabIndex = if (showFlows) 1 else 0,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(selected = !showFlows, onClick = { showFlows = false }) {
                Text("PACOTES", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
            }
            Tab(selected = showFlows, onClick = { showFlows = true }) {
                Text("FLUXOS", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onSurface)
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

@OptIn(ExperimentalMaterial3Api::class)
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
fun FlowRow(flow: RealtimeFlowModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (flow.isInsecure) Color.Red.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (flow.isInsecure) Icons.Default.Warning else Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = if (flow.isInsecure) Color.Red else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(flow.label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                Text(flow.endpoints, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${flow.packetCount} pkts", style = MaterialTheme.typography.labelSmall, color = Color(0xFF22C55E))
                Text("${flow.payloadBytes} bytes", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
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
                Text(
                    "${String.format(Locale.US, "%.3f", packet.timestampSeconds)}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(packet.protocol, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(packet.info, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, fontFamily = FontFamily.Default)
        }
    }
}
