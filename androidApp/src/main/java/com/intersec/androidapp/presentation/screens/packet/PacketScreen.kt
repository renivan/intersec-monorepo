package com.intersec.androidapp.presentation.screens.packet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.presentation.screens.auth.ProUpgradeDialog
import com.intersec.androidapp.presentation.state.AppSelectionStore
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import com.intersec.androidapp.presentation.viewmodel.PacketListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PacketScreen(
    viewModel: PacketListViewModel = viewModel(),
    analysisViewModel: AnalysisViewModel = viewModel(),
    onBack: () -> Unit = {},
    onOpenDetail: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val analysisState by analysisViewModel.uiState.collectAsState()

    var protocol by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }
    var packetNumberText by remember { mutableStateOf("") }
    var showProDialog by remember { mutableStateOf(false) }

    if (showProDialog) {
        ProUpgradeDialog(
            onDismiss = { showProDialog = false },
            onUpgrade = { 
                analysisViewModel.upgradeToPro()
                showProDialog = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "PACKET X-RAY", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Default
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = protocol,
                            onValueChange = { protocol = it },
                            label = { Text("PROTO", fontFamily = FontFamily.Default, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(2.dp),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Default, fontSize = 12.sp)
                        )
                        OutlinedTextField(
                            value = host,
                            onValueChange = { host = it },
                            label = { Text("HOST/IP", fontFamily = FontFamily.Default, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(2.dp),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Default, fontSize = 12.sp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("SEARCH PAYLOAD...", fontFamily = FontFamily.Default, fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Default, fontSize = 12.sp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = packetNumberText,
                            onValueChange = { if (it.all { char -> char.isDigit() }) packetNumberText = it },
                            label = { Text("PKT #", fontFamily = FontFamily.Default, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(2.dp),
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Default, fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (analysisState.userTier == 0 && (protocol.isNotBlank() || host.isNotBlank() || searchText.isNotBlank())) {
                                    showProDialog = true
                                } else {
                                    viewModel.loadPackets(
                                        protocol = protocol.ifBlank { null },
                                        host = host.ifBlank { null },
                                        text = searchText.ifBlank { null },
                                        packetNumber = packetNumberText.toLongOrNull()
                                    )
                                }
                            },
                            modifier = Modifier.height(52.dp),
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("SCAN", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Default, color = Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "OPERATIONAL READOUT: ${state.totalItems} PACKETS FOUND",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Default,
                color = MaterialTheme.colorScheme.primary
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("#${item.packetNumber}", style = MaterialTheme.typography.titleSmall, fontFamily = FontFamily.Default, color = Color.White)
                                Text(
                                    item.highestProtocol ?: "N/A",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = item.info,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                color = Color.Gray,
                                fontFamily = FontFamily.Default,
                                fontSize = 12.sp
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
