package com.intersec.androidapp.presentation.screens.capture

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeuralConnectivityMap(state: com.intersec.androidapp.presentation.state.CaptureRealtimeUiState) {
    Box(
        modifier = Modifier
            .height(180.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Central Hub
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                    center = center,
                    radius = 60.dp.toPx()
                ),
                radius = 50.dp.toPx(),
                center = center
            )
            drawCircle(color = primaryColor, radius = 4.dp.toPx(), center = center)

            // Dynamic Neural Nodes
            state.neuralNodes.forEach { node ->
                val nodePos = Offset(node.x * size.width, node.y * size.height)
                
                // Connection line
                drawLine(
                    color = primaryColor.copy(alpha = 0.15f * node.intensity),
                    start = center,
                    end = nodePos,
                    strokeWidth = 1.dp.toPx()
                )
                
                // Outer glow
                drawCircle(
                    color = primaryColor.copy(alpha = 0.2f * node.intensity),
                    radius = 8.dp.toPx(),
                    center = nodePos
                )
                
                // Node point
                drawCircle(
                    color = onSurfaceColor.copy(alpha = 0.6f * node.intensity),
                    radius = 3.dp.toPx(),
                    center = nodePos
                )
            }
        }

        // Sem rótulo SCAN ATIVO
    }
}
