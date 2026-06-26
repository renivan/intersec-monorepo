package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.di.AppBootstrap
import com.intersec.androidapp.core.bridge.NativeFlowQuery
import com.intersec.androidapp.presentation.state.FlowUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlowListViewModel : ViewModel() {

    private val repository = AppBootstrap.coreAnalysisRepository
    private val pageSize = 100 // Mais eficiente com paginação no Native

    private val _uiState = MutableStateFlow(FlowUiState())
    val uiState: StateFlow<FlowUiState> = _uiState.asStateFlow()

    private var currentQuery = NativeFlowQuery()

    init {
        loadFlows()
    }

    fun loadFlows(
        protocol: String? = null,
        host: String? = null,
        port: Int? = null,
        text: String? = null,
    ) {
        currentQuery = NativeFlowQuery(
            protocol = protocol,
            host = host,
            port = port,
            text = text,
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

            // FASE 2: Paginação delegada ao Native
            repository.queryFlows(currentQuery).fold(
                onSuccess = { searchResult ->
                    val updatedItems = if (isNewSearch) {
                        searchResult.items
                    } else {
                        _uiState.value.items + searchResult.items
                    }

                    _uiState.value = FlowUiState(
                        isLoading = false,
                        items = updatedItems,
                        totalItems = searchResult.totalItems,
                        errorMessage = null,
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Falha ao consultar fluxos.",
                    )
                },
            )
        }
    }
}

