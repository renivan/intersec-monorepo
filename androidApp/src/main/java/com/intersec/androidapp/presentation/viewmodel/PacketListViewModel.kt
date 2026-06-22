package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.core.bridge.RustPacketQuery
import com.intersec.androidapp.presentation.state.PacketUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PacketListViewModel : ViewModel() {

    private val repository = AppBootstrap.rustAnalysisRepository
    private val pageSize = 100 // Aumentado agora que Rust é eficiente

    private val _uiState = MutableStateFlow(PacketUiState())
    val uiState: StateFlow<PacketUiState> = _uiState.asStateFlow()

    private var currentQuery = RustPacketQuery()

    init {
        loadPackets()
    }

    fun loadPackets(
        protocol: String? = null,
        host: String? = null,
        text: String? = null,
        packetNumber: Long? = null,
    ) {
        currentQuery = RustPacketQuery(
            protocol = protocol,
            host = host,
            text = text,
            packetNumber = packetNumber,
            offset = 0,
            limit = pageSize
        )
        
        executeSearch(isNewSearch = true)
    }

    fun loadNextPage() {
        if (_uiState.value.isLoading || _uiState.value.items.size >= _uiState.value.totalItems) return

        currentQuery = currentQuery.copy(offset = _uiState.value.items.size.toLong())
        executeSearch(isNewSearch = false)
    }

    private fun executeSearch(isNewSearch: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
            )

            // FASE 2: O motor Rust agora filtra e fatia os dados. 
            // O Android recebe apenas os itens da página solicitada.
            repository.queryPackets(currentQuery).fold(
                onSuccess = { searchResult ->
                    val updatedItems = if (isNewSearch) {
                        searchResult.items
                    } else {
                        _uiState.value.items + searchResult.items
                    }

                    _uiState.value = PacketUiState(
                        isLoading = false,
                        items = updatedItems,
                        totalItems = searchResult.totalItems,
                        errorMessage = null,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Falha ao consultar pacotes.",
                    )
                },
            )
        }
    }
}
