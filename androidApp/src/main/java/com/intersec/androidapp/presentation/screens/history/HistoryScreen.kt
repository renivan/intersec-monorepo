package com.intersec.androidapp.presentation.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.SessionListViewModel

/**
 * Proposta do usuário adaptada: Lista o histórico de sessões salvas.
 */
@Composable
fun HistoryScreen(
    viewModel: SessionListViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Column(Modifier.padding(16.dp)) {
        Text("Histórico de Capturas", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            CircularProgressIndicator()
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.items) { session ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(text = session.sourceName, style = MaterialTheme.typography.titleMedium)
                        Text(text = "Pacotes: ${session.totalPackets}")
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }
    }
}
