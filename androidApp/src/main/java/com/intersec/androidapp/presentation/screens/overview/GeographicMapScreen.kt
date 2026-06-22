package com.intersec.androidapp.presentation.screens.overview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * Tela de Mapa Geográfico (Fase 2 - Evolução Master)
 * Visualização tática da localização dos servidores acessados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeographicMapScreen(
    viewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Geographic Intel") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // ===== WORLD MAP CANVAS (SIMULADO) =====
            SectionHeader("Mapa de Conexões Externas")
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Desenha Grade do Mapa
                    val gridColor = Color.White.copy(alpha = 0.05f)
                    for (i in 1..10) {
                        val x = size.width * (i / 10f)
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height))
                        val y = size.height * (i / 10f)
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y))
                    }

                    // Desenha Pontos de Conexão Reais vindos do Rust
                    state.overview?.geoPoints?.forEach { point ->
                        // Mapeamento simples Lat/Lon para X/Y
                        val x = ((point.longitude + 180) / 360f) * size.width
                        val y = ((90 - point.latitude) / 180f) * size.height
                        
                        drawCircle(
                            color = Color(0xFFEF4444).copy(alpha = 0.4f),
                            radius = (10.dp + (point.flowCount / 10).dp).toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 4.dp.toPx(),
                            center = Offset(x.toFloat(), y.toFloat())
                        )
                    }
                }

                Text(
                    "LIVE GEO-TRACKING",
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(24.dp))

            // ===== LISTA DE PAÍSES IDENTIFICADOS =====
            SectionHeader("Destinos Detectados")
            
            if (state.overview?.geoPoints?.isEmpty() == true) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma conexão externa detectada ainda.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                state.overview?.geoPoints?.forEach { point ->
                    CountryItem(point.countryName, point.countryCode, point.flowCount)
                }
            }
        }
    }
}

@Composable
fun CountryItem(name: String, code: String, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(getFlagEmoji(code), fontSize = 24.sp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold)
                Text("$count conexões ativas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🌐"
    val firstLetter = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondLetter = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
    return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
}
