package com.intersec.androidapp.presentation.screens.capture

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.R
import com.intersec.androidapp.core.ads.AdManager
import com.intersec.androidapp.core.file.AndroidCaptureImporter
import com.intersec.androidapp.presentation.screens.overview.NeuralMapVisual
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

@Composable
fun MissionControlScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onOpenPackets: () -> Unit = {},
    onOpenFlows: () -> Unit = {},
    onOpenOverview: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenCaptureRealtime: () -> Unit = {},
    onOpenImportLog: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
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
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // ===== BRANDING & STATUS =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_neural_core),
                contentDescription = "Logo",
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "INTERSEC: SEGURANÇA",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Default
            )
        }
        
        Spacer(Modifier.height(16.dp))

        // ===== IDENTIFICADOR DE REDE & ENTROPIA =====
        NetworkStatusSection(
            isCapturing = state.session != null, 
            riskScore = state.overview?.averageRiskScore ?: 0,
            networkState = state.networkState
        )

        Spacer(Modifier.height(16.dp))
        
        // Pulso Neural (Mini Visualização do Core Native)
        NeuralMapVisual(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
            nodeCount = if (state.session != null) (state.overview?.protocolStats?.size ?: 4).coerceIn(2, 10) else 0
        )

        Spacer(Modifier.height(24.dp))

        SectionHeader("MONITORAMENTO ATIVO")
        
        QuickModeButton(
            label = "INICIAR CAPTURA EM TEMPO REAL",
            icon = Icons.Default.Videocam, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenCaptureRealtime
        )

        Spacer(Modifier.height(24.dp))

        // ===== GRID DE ANÁLISE =====
        SectionHeader("CANAIS DE ANÁLISE")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(280.dp) 
        ) {
            item { TacticalCard("FLUXOS", "CANAIS ATIVOS", Icons.Default.SwapHoriz, MaterialTheme.colorScheme.primary, onOpenFlows) }
            item { TacticalCard("PACOTES", "DETALHAMENTO", Icons.AutoMirrored.Filled.List, Color.White, onOpenPackets) }
            item { TacticalCard("VISÃO GERAL", "MAPA DE REDE", Icons.Default.Assessment, MaterialTheme.colorScheme.primary, onOpenOverview) }
            item { TacticalCard("PROTEÇÃO", "DEFESA ATIVA", Icons.Default.Security, MaterialTheme.colorScheme.error, onOpenSecurity) }
        }

        Spacer(Modifier.height(24.dp))

        // Botão de Upgrade Estilo Banner
        if (state.userTier == 0) {
            UpgradeBanner(onClick = { viewModel.setShowAdRewardDialog(true) })
            Spacer(Modifier.height(24.dp))
        }

        // ===== IMPORTAÇÃO & SESSÃO =====
        OutlinedButton(
            onClick = { filePicker.launch(arrayOf("*/*")) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("IMPORT PCAP DATASET", fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold)
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

        // ===== ALERTAS DE COMPORTAMENTO (DADOS REAIS DO MOTOR Native) =====
        SectionHeader("ALERTAS DE COMPORTAMENTO")
        
        val events = state.overview?.events ?: emptyList()
        if (events.isEmpty()) {
            Text(
                "NENHUM COMPORTAMENTO SUSPEITO DETECTADO.", 
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontFamily = FontFamily.Default,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            events.forEach { event ->
                val color = if (event.contains("incompleta") || event.contains("incomum")) 
                    MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                BehaviorAlertItem(alert = event.uppercase(), color = color)
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), color = MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(40.dp))
    }

    if (state.showAdRewardDialog) {
        SubscriptionAdModal(
            onDismiss = { viewModel.setShowAdRewardDialog(false) },
            onUpgrade = {
                val activity = context as? com.intersec.androidapp.MainActivity
                activity?.startBillingFlow()
                viewModel.setShowAdRewardDialog(false)
            }
        )
    }
}

@Composable
fun UpgradeBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("LIBERAR ACESSO PRO", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Remova limites de tempo e habilite temas exclusivos.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SubscriptionAdModal(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Fechar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }

                Icon(
                    Icons.Default.VerifiedUser,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "INTERSEC PRO",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                
                Text(
                    "NÍVEL ANALISTA EXPERIENTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    "Acesse o monitoramento ilimitado, todos os temas visuais e suporte a atualizações prioritárias de inteligência.",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("INICIAR TESTE POR R$ 1,00", fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss) {
                    Text("CONTINUAR NO MODO LIMITADO", color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
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
fun NetworkStatusSection(
    isCapturing: Boolean, 
    riskScore: Int = 0,
    networkState: com.intersec.androidapp.presentation.state.NetworkState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = if (!networkState.isConnected) Color.Gray 
                             else if (riskScore > 50) MaterialTheme.colorScheme.error 
                             else MaterialTheme.colorScheme.primary

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (networkState.isConnected) (100 - riskScore) / 100f else 0f },
                    modifier = Modifier.size(50.dp),
                    color = statusColor,
                    strokeWidth = 2.dp,
                    trackColor = statusColor.copy(alpha = 0.1f)
                )
                Icon(
                    if (networkState.isConnected) {
                        when (networkState.typeName) {
                            "Wi-Fi" -> Icons.Default.Wifi
                            "Ethernet" -> Icons.Default.SettingsEthernet
                            else -> Icons.Default.SignalCellularAlt
                        }
                    } else Icons.Default.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = statusColor
                )
            }

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = if (networkState.isConnected) 
                        "INTERFACE: ${networkState.interfaceName.uppercase()} (${networkState.typeName.uppercase()})" 
                        else "IDLE: AGUARDANDO REDE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Default
                )
                Text(
                    text = if (networkState.isConnected) 
                        "STATUS: ${networkState.details.uppercase()} | INTEGRIDADE: ${100 - riskScore}%" 
                        else "ENTROPIA: NULL",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontFamily = FontFamily.Default
                )
            }
        }
    }
}

@Composable
fun QuickModeButton(
    label: String = "INIT REAL-TIME CAPTURE", 
    icon: ImageVector, 
    color: Color, 
    modifier: Modifier, 
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = color
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Icon(icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(12.dp))
        Text(label, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Default)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TacticalCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Default)
        }
    }
}

@Composable
fun ActiveSessionPanel(sourceName: String, packetCount: Long, onClear: () -> Unit, onLogs: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Adjust, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sourceName.uppercase(), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
                Text("PKTS: $packetCount", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Default)
            }
            IconButton(onClick = onLogs) { Icon(Icons.Default.Terminal, contentDescription = "Logs", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onClear) { Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun BehaviorAlertItem(
    target: String = "ANOMALIA DETECTADA",
    alert: String, 
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .border(1.dp, color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("!", color = color, fontWeight = FontWeight.Black, fontFamily = FontFamily.Default, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(target, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default)
            Text(alert, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Default)
        }
    }
}
