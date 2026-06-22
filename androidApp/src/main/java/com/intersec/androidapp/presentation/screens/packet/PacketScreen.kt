package com.intersec.androidapp.presentation.screens.packet

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
import com.intersec.androidapp.presentation.viewmodel.PacketListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketScreen(
    viewModel: PacketListViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenDetail: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()

    var protocol by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var packetNumberText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Análise de Pacotes") },
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
                    
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Buscar no resumo...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = packetNumberText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) packetNumberText = it },
                            label = { Text("Nº Pacote") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.loadPackets(
                                    protocol = protocol.ifBlank { null },
                                    host = host.ifBlank { null },
                                    text = searchText.ifBlank { null },
                                    packetNumber = packetNumberText.toLongOrNull()
                                )
                            },
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Filtrar")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total encontrado: ${state.totalItems}",
                style = MaterialTheme.typography.labelMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(state.items) { index, item ->
                    // Carrega próxima página quando chegar perto do fim da lista
                    if (index >= state.items.size - 5) {
                        LaunchedEffect(Unit) {
                            viewModel.loadNextPage()
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppSelectionStore.selectedPacket = item
                                onOpenDetail()
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("#${item.packetNumber}", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    item.highestProtocol ?: "N/A",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                text = item.info,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
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
