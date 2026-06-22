package com.intersec.androidapp.presentation.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onBack: () -> Unit = {}) {
    Column(Modifier.padding(16.dp)) {
        Text("Configurações", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        
        Text("Idioma: Português")
        Text("Tema: Sistema")
        
        Spacer(Modifier.weight(1f))
        
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}
