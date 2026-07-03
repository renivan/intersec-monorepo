package com.intersec.androidapp.presentation.screens.neural

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.state.AnalysisUiState
import com.intersec.androidapp.presentation.state.NeuralLink3D
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import kotlinx.coroutines.delay

/**
 * Interface 3D Sentinel: Visualização da Camada de Transporte em tempo real.
 */
@Composable
fun Neural3DScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    Neural3DContent(state, onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Neural3DContent(
    state: AnalysisUiState,
    onBack: () -> Unit = {}
) {
    var rotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            rotation += 0.5f
            delay(16)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("NEURAL 3D TRANSPORT", color = Color.Cyan, fontWeight = FontWeight.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.Cyan)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                val center = Offset(size.width / 2, size.height / 2)
                val globeRadius = size.width.coerceAtMost(size.height) * 0.35f
                
                drawCircle(
                    color = Color.Cyan.copy(alpha = 0.1f),
                    radius = globeRadius,
                    center = center,
                    style = Stroke(width = 1.dp.toPx())
                )

                state.neuralLinks.forEach { link ->
                    val rad = Math.toRadians(rotation.toDouble() + link.longitude).toFloat()
                    val cosRotation = kotlin.math.cos(rad)
                    val sinRotation = kotlin.math.sin(rad)
                    
                    if (cosRotation > -0.2f) {
                        val projectedX = center.x + (globeRadius * sinRotation)
                        val projectedY = center.y + (globeRadius * kotlin.math.sin(Math.toRadians(link.latitude).toFloat()))
                        
                        drawCircle(
                            color = link.color.copy(alpha = link.intensity),
                            radius = (4.dp.toPx() * link.intensity).coerceAtLeast(2f),
                            center = Offset(projectedX, projectedY)
                        )
                        
                        drawLine(
                            color = link.color.copy(alpha = 0.3f * link.intensity),
                            start = center,
                            end = Offset(projectedX, projectedY),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(24.dp)
            ) {
                Text("NEURAL_ENGINE: ACTIVE", color = Color.Green, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                Text("NODES_SYNCED: ${state.neuralLinks.size}", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                if (state.userTier != 1) {
                    Spacer(Modifier.height(8.dp))
                    Text("(!) ACESSO STANDARD: DADOS LIMITADOS", color = Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun Neural3DScreenPreview() {
    val mockLinks = listOf(
        NeuralLink3D("1", "DEVICE", "8.8.8.8", "TCP", 0.8f, Color.Cyan, 37.0, -122.0, "US", "Mountain View"),
        NeuralLink3D("2", "DEVICE", "1.1.1.1", "UDP", 0.5f, Color.Magenta, 48.0, 2.0, "FR", "Paris"),
        NeuralLink3D("3", "DEVICE", "157.240.22.35", "TLS", 0.9f, Color.Cyan, -23.5, -46.6, "BR", "Sao Paulo")
    )
    Neural3DContent(
        state = AnalysisUiState(neuralLinks = mockLinks, userTier = 0)
    )
}
