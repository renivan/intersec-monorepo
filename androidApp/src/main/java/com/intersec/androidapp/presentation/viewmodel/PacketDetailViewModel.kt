package com.intersec.androidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.intersec.androidapp.presentation.state.AppSelectionStore
import com.intersec.androidapp.presentation.state.PacketDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PacketDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PacketDetailUiState())
    val uiState: StateFlow<PacketDetailUiState> = _uiState.asStateFlow()

    fun loadSelectedPacket() {
        val selected = AppSelectionStore.selectedPacket

        _uiState.value = if (selected != null) {
            PacketDetailUiState(
                item = selected,
                errorMessage = null,
            )
        } else {
            PacketDetailUiState(
                item = null,
                errorMessage = "Nenhum pacote selecionado.",
            )
        }
    }
}
