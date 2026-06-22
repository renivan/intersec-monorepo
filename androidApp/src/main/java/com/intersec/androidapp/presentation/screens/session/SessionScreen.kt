package com.intersec.androidapp.presentation.screens.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.viewmodel.SessionListViewModel

@Composable
fun SessionScreen(
    viewModel: SessionListViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    val tagsCsv = remember { mutableStateOf("") }
    val notes = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            text = "Sessões salvas",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = tagsCsv.value,
            onValueChange = { tagsCsv.value = it },
            label = { Text("Tags CSV") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes.value,
            onValueChange = { notes.value = it },
            label = { Text("Notas") },
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.persistActiveSession(
                    tagsCsv = tagsCsv.value,
                    notes = notes.value.ifBlank { null },
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Salvar sessão ativa")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { viewModel.loadSessions() }, modifier = Modifier.fillMaxWidth()) {
            Text("Atualizar lista")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Voltar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isLoading || state.isSaving) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
        }

        state.infoMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text("Total de sessões: ${state.items.size}")

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(state.items) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Sessão: ${item.sessionId}")
                        Text("Arquivo: ${item.sourceName}")
                        Text("Pacotes: ${item.totalPackets}")
                        Text("Fluxos: ${item.totalFlows}")
                        Text("Tags: ${item.tagsCsv}")
                        Text("Notas: ${item.notes ?: "-"}")
                    }
                }
            }
        }
    }
}
