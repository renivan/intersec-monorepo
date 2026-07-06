package com.intersec.androidapp.presentation.screens.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeuralMapVisual(modifier: Modifier, nodeCount: Int = 6) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val secondaryColor = MaterialTheme.colorScheme.onSurface
        
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Central Node (Local Device)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, primaryColor.copy(alpha = 0.2f)),
                    center = center,
                    radius = 30.dp.toPx()
                ),
                radius = 24.dp.toPx(),
                center = center
            )
            drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = center)

            // Dynamic Nodes (Detected IPs/Flows)
            for (i in 0 until nodeCount) {
                val angle = (i * (360f / nodeCount)) * (Math.PI / 180f).toFloat()
                val radius = size.minDimension * 0.35f
                val nodePos = Offset(
                    center.x + radius * kotlin.math.cos(angle),
                    center.y + radius * kotlin.math.sin(angle)
                )

                // Neural Connection Line
                drawLine(
                    color = primaryColor.copy(alpha = 0.2f),
                    start = center,
                    end = nodePos,
                    strokeWidth = 1.dp.toPx()
                )

                // Data Pulse (Moving point on-line)
                val pulsePos = Offset(
                    center.x + (radius * 0.6f) * kotlin.math.cos(angle),
                    center.y + (radius * 0.6f) * kotlin.math.sin(angle)
                )
                drawCircle(color = primaryColor.copy(alpha = 0.5f), radius = 3.dp.toPx(), center = pulsePos)

                // External Node
                drawCircle(color = secondaryColor.copy(alpha = 0.8f), radius = 5.dp.toPx(), center = nodePos)
            }
        }
        
        // Sem rótulo SCAN ATIVO
    }
}

@Composable
fun OverviewMiniCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, fontFamily = FontFamily.Default, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontFamily = FontFamily.Default)
        }
    }
}

fun getRiskColor(score: Int): Color = when {
    score < 30 -> Color(0xFF22C55E)
    score < 70 -> Color(0xFFEAB308)
    else -> Color(0xFFEF4444)
}
