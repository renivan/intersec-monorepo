package com.intersec.androidapp.presentation.screens.flow

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.state.AppSelectionStore
import com.intersec.androidapp.presentation.viewmodel.FlowListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowScreen(
    viewModel: FlowListViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenDetail: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    var protocol by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análise de Fluxos") },
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
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = protocol,
                            onValueChange = { protocol = it },
                            label = { Text("Protocolo") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("Host") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = portText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) portText = it },
                            label = { Text("Porta") },
                            modifier = Modifier.weight(0.4f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            label = { Text("Buscar...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.loadFlows(
                                protocol = protocol.ifBlank { null },
                                host = host.ifBlank { null },
                                port = portText.toIntOrNull(),
                                text = searchText.ifBlank { null }
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Consultar Fluxos")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total de fluxos: ${state.totalItems}",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(state.items) { index, item ->
                    if (index >= state.items.size - 5) {
                        LaunchedEffect(Unit) {
                            viewModel.loadNextPage()
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppSelectionStore.selectedFlow = item
                                onOpenDetail()
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(item.label, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Endpoints: ${item.endpoints}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pacotes: ${item.totalPackets}", style = MaterialTheme.typography.labelSmall)
                                Text("Payload: ${item.totalPayloadBytes} bytes", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                if (state.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }

            state.errorMessage?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}
