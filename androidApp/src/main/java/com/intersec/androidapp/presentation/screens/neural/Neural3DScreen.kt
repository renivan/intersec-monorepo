package com.intersec.androidapp.presentation.screens.neural

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import io.github.sceneview.SceneView

/**
 * Sentinel 3D HUD: Interface imersiva baseada em Google Filament.
 */
@Composable
fun Neural3DScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    Neural3DContent(state, onBack, viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Neural3DContent(
    state: com.intersec.androidapp.presentation.state.AnalysisUiState,
    onBack: () -> Unit,
    viewModel: AnalysisViewModel? = null
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // --- ÁREA DE CLIQUE PARA DESELECIONAR ---
        Box(modifier = Modifier.fillMaxSize().clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { viewModel?.inspectNeuralLink(null) })

        // --- CAMADA 1: MOTOR 3D FILAMENT (SceneView Nativo) ---
        val isLoadingModel = remember { mutableStateOf(true) }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                SceneView(context).apply {
                    this.modelLoader.loadModelAsync(
                        fileLocation = "models/earth.glb",
                        onResult = { _ ->
                            isLoadingModel.value = false
                        }
                    )
                }
            },
            update = { _ -> }
        )

        if (isLoadingModel.value) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Cyan,
                strokeWidth = 2.dp
            )
        }

        // --- CAMADA 2: HUD TÉCNICO (Painel Esquerdo) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(24.dp)
                .width(180.dp)
        ) {
            TechnicalInfoItem("PLANETA", "TERRA (SOL II)")
            TechnicalInfoItem("SISTEMA", "interSec_SENTINEL")
            TechnicalInfoItem("CORE", "RUST_NEURAL_v3")
            TechnicalInfoItem("NODES", state.neuralLinks.size.toString())
            TechnicalInfoItem("STATUS", if (state.userTier == 1) "PREMIUM_ACTIVE" else "STANDARD_MOD")
        }

        // --- CAMADA 3: GEOMETRIA DE REDE (Painel Direito) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(24.dp)
                .width(150.dp),
            horizontalAlignment = Alignment.End
        ) {
            TechnicalInfoItem("LAT", "%.4f".format(state.lastLatitude ?: 0.0))
            TechnicalInfoItem("LON", "%.4f".format(state.lastLongitude ?: 0.0))
            TechnicalInfoItem("SYNC", "REAL_TIME")
        }

        // --- CAMADA 4: CONTROLES SUPERIORES ---
        CenterAlignedTopAppBar(
            title = { Text("NEURAL SENTINEL 3D", color = Color.Cyan, fontWeight = FontWeight.Black, fontSize = 14.sp) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.Cyan)
                }
            }
        )

        // --- CAMADA 5: MENSAGEM DE ACESSO ---
        if (state.userTier != 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                color = Color.Yellow.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Yellow.copy(alpha = 0.5f))
            ) {
                Text(
                    "(!) UPGRADE REQUERIDO PARA VISUALIZAÇÃO DE INTELIGÊNCIA GLOBAL",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Yellow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- CAMADA 6: PAINEL DE INSPEÇÃO PROFUNDA (DPI) ---
        state.selectedNeuralLink?.let { link ->
            Surface(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(24.dp)
                    .width(280.dp)
                    .animateContentSize(),
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color.Cyan, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("DECODIFICAÇÃO DE FLUXO", color = Color.Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { viewModel?.inspectNeuralLink(null) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    Text("DESTINO: ${link.destIp}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Text("GEO: ${link.city}, ${link.countryCode}", color = Color.LightGray, fontSize = 10.sp)
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.Cyan.copy(alpha = 0.2f))
                    
                    if (state.inspectedPacketPayload == null) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally), strokeWidth = 2.dp, color = Color.Cyan)
                    } else {
                        Text(
                            text = state.inspectedPacketPayload!!,
                            color = Color(0xFF00FF00), // Cor "Matrix"
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 14.sp
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel?.blockIp(link.destIp, "BLOCK FROM 3D HUD") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red)
                    ) {
                        Text("BLOQUEAR NÓ NEURAL", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TechnicalInfoItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            color = Color.Cyan.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
