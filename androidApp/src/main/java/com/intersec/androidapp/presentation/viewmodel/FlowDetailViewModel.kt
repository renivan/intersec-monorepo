package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.intersec.androidapp.presentation.state.AppSelectionStore
import com.intersec.androidapp.presentation.state.FlowDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlowDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FlowDetailUiState())
    val uiState: StateFlow<FlowDetailUiState> = _uiState.asStateFlow()

    fun loadSelectedFlow() {
        val selected = AppSelectionStore.selectedFlow

        _uiState.value = if (selected != null) {
            FlowDetailUiState(
                item = selected,
                errorMessage = null,
            )
        } else {
            FlowDetailUiState(
                item = null,
                errorMessage = "Nenhum fluxo selecionado.",
            )
        }
    }
}
