package com.intersec.androidapp.presentation.screens.capture

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.R
import com.intersec.androidapp.core.file.AndroidCaptureImporter
import com.intersec.androidapp.presentation.screens.overview.NeuralMapVisual
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import com.intersec.androidapp.presentation.viewmodel.AuthViewModel

@Composable
fun MissionControlScreen(
    viewModel: AnalysisViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onOpenPackets: () -> Unit = {},
    onOpenFlows: () -> Unit = {},
    onOpenOverview: () -> Unit = {},
    onOpenSecurity: () -> Unit = {},
    onOpenCaptureRealtime: () -> Unit = {},
    onOpenImportLog: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var registrationPassword by remember { mutableStateOf("") }

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
                "INTERSEC: MISSION CONTROL",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
        
        Spacer(Modifier.height(16.dp))

        // ===== OPERATOR AUTHORIZATION (REGISTRATION) =====
        if (!authState.isSuccess) {
            SectionHeader("OPERATOR REGISTRATION")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("IDENTIFY OPERATOR TO INITIALIZE CORE", style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Monospace)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = authState.email,
                        onValueChange = { authViewModel.onEmailChange(it) },
                        label = { Text("EMAIL", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = registrationPassword,
                        onValueChange = { registrationPassword = it },
                        label = { Text("SECURITY KEY", fontSize = 10.sp) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(2.dp),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { authViewModel.register(registrationPassword) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        enabled = !authState.isLoading
                    ) {
                        if (authState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        else Text("AUTHORIZE OPERATOR", fontWeight = FontWeight.Bold)
                    }
                    if (authState.error != null) {
                        Text(authState.error!!, color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ===== IDENTIFICADOR DE REDE & ENTROPIA =====
        NetworkStatusSection(state.session != null, state.overview?.averageRiskScore ?: 0)

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

        SectionHeader("TACTICAL OPERATIONS")
        
        QuickModeButton(
            icon = Icons.Default.Videocam, 
            color = MaterialTheme.colorScheme.primary, 
            modifier = Modifier.fillMaxWidth(),
            onClick = onOpenCaptureRealtime
        )

        Spacer(Modifier.height(24.dp))

        // ===== GRID TÁTICO DE ANÁLISE =====
        SectionHeader("INTELLIGENCE CHANNELS")

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(360.dp) 
        ) {
            item { TacticalCard("FLOWS", "ACTIVE CHANNELS", Icons.Default.SwapHoriz, MaterialTheme.colorScheme.primary, onOpenFlows) }
            item { TacticalCard("PACKETS", "X-RAY DETAIL", Icons.AutoMirrored.Filled.List, Color.White, onOpenPackets) }
            item { TacticalCard("OVERVIEW", "NEURAL MAP", Icons.Default.Assessment, MaterialTheme.colorScheme.primary, onOpenOverview) }
            item { TacticalCard("SECURITY", "ACTIVE DEFENSE", Icons.Default.Security, MaterialTheme.colorScheme.error, onOpenSecurity) }
            item { TacticalCard("THEMES", "VISUAL IDENTITY", Icons.Default.Palette, MaterialTheme.colorScheme.tertiary, onOpenSettings) }
            item { TacticalCard("SETTINGS", "SYSTEM CONFIG", Icons.Default.Settings, MaterialTheme.colorScheme.primary, onOpenSettings) }
        }

        Spacer(Modifier.height(24.dp))

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
            Text("IMPORT PCAP DATASET", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
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

        // ===== BEHAVIORAL ALERTS (DADOS REAIS DO MOTOR Native) =====
        SectionHeader("BEHAVIORAL ENGINE ALERTS")
        
        val events = state.overview?.events ?: emptyList()
        if (events.isEmpty()) {
            Text(
                "NO SUSPICIOUS BEHAVIOR DETECTED.", 
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
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
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), thickness = 1.dp)
    }
}

@Composable
fun NetworkStatusSection(isCapturing: Boolean, riskScore: Int = 0) {
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
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { if (isCapturing) (100 - riskScore) / 100f else 0f },
                    modifier = Modifier.size(50.dp),
                    color = if (riskScore > 50) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
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
                    text = if (isCapturing) "INTERFACE: WLAN0 (ACTIVE)" else "IDLE: WAITING INTERFACE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isCapturing) "NETWORK INTEGRITY: ${100 - riskScore}%" else "ENTROPY: NULL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Monospace
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
        Text(label, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
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
            Text(title, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Monospace)
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
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(sourceName.uppercase(), color = Color.White, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("PKTS: $packetCount", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
            }
            IconButton(onClick = onLogs) { Icon(Icons.Default.Terminal, contentDescription = "Logs", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onClear) { Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
fun BehaviorAlertItem(
    target: String = "ANOMALY DETECTED", 
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
        Text("!", color = color, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(target, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Text(alert, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontFamily = FontFamily.Monospace)
        }
    }
}
