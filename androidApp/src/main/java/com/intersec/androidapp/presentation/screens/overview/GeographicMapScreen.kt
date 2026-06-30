package com.intersec.androidapp.presentation.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeographicMapScreen(
    onBack: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "MAPA GEOGRÁFICO", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null, 
                    modifier = Modifier.size(80.dp), 
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "LOCALIZANDO ORIGENS DE TRÁFEGO...", 
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Default
                )
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.width(200.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
        }
    }
}
