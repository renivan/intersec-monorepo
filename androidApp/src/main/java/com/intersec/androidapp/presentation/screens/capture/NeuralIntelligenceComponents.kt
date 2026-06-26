package com.intersec.androidapp.presentation.screens.capture

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intersec.androidapp.presentation.state.CaptureRealtimeUiState
import com.intersec.androidapp.presentation.state.PacketColorPalette

@Composable
fun NeuralConnectivityMap(state: CaptureRealtimeUiState) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Neural Connectivity Map",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PacketColorPalette.CARD_BACKGROUND.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                
                // Desenha o Core Central (Guardian Engine)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.Cyan.copy(alpha = 0.4f), Color.Transparent),
                        center = center,
                        radius = 60f * pulseScale
                    ),
                    radius = 60f * pulseScale,
                    center = center
                )
                
                drawCircle(
                    color = Color.Cyan,
                    radius = 10f,
                    center = center
                )

                if (state.neuralNodes.isEmpty() && state.isCapturing) {
                    // Efeito de Varredura Circular (Scanning)
                    drawCircle(
                        color = Color.Cyan.copy(alpha = 0.1f),
                        radius = (size.minDimension / 2) * pulseScale,
                        center = center,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }

                // Desenha os Nós Periféricos (Tráfego Real)
                state.neuralNodes.forEach { node ->
                    val nodePos = Offset(node.x * size.width, node.y * size.height)
                    
                    // Desenha a "Sinapse" (Linha de conexão)
                    drawLine(
                        color = Color.White.copy(alpha = 0.1f * node.intensity),
                        start = center,
                        end = nodePos,
                        strokeWidth = 2f
                    )

                    // Brilho do Nó
                    drawCircle(
                        color = Color.Cyan.copy(alpha = 0.3f * node.intensity),
                        radius = 15f * node.intensity,
                        center = nodePos
                    )
                    
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = nodePos
                    )
                }
            }

            Text(
                "CORE ANALYTICS ACTIVE",
                color = Color.Cyan.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}
