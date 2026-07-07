package com.intersec.androidapp.presentation.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.intersec.androidapp.app.MainApplication
import com.intersec.androidapp.core.bridge.NativeBridgeClient
import com.intersec.androidapp.core.diagnostic.DeepIntegrityScanner
import com.intersec.androidapp.core.di.ModuleRegistry
import com.intersec.androidapp.presentation.state.InitializationUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class InitializationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InitializationUiState())
    val uiState: StateFlow<InitializationUiState> = _uiState.asStateFlow()
    private val bridge = NativeBridgeClient()

    fun startHealthCheck(activity: Activity) {
        viewModelScope.launch {
            try {
                val context = activity.applicationContext

                // Executa o trabalho pesado em background para não travar a UI (evita frame skips)
                withContext(Dispatchers.IO) {
                    // 1. Detectar Perfil do Usuário
                    _uiState.update { it.copy(status = "Autenticando Perfil...", progress = 0.05f) }
                    val userTier = MainApplication.appModule.securitySettingsManager.userTier.first()
                    val isPro = userTier == 1

                    // 2. Inicialização BASE e Teste de Resposta do Motor
                    _uiState.update { it.copy(status = "Validando Motor Native...", progress = 0.15f) }
                    val baseOk = ModuleRegistry.initializeBase(activity)
                    if (!baseOk) {
                        _uiState.update { it.copy(error = "ERRO: Biblioteca nativa não encontrada!") }
                        return@withContext
                    }

                    // Verificação de Integridade Real do Binário
                    if (!DeepIntegrityScanner.verifyBinaryIntegrity(context)) {
                        _uiState.update { it.copy(error = "ERRO: Integridade do binário nativo violada.") }
                        return@withContext
                    }

                    // TESTE REAL: O Motor está vivo?
                    val response = bridge.ping()
                    if (response == "ERROR: NATIVE_NOT_LOADED" || response.isEmpty()) {
                        _uiState.update { it.copy(error = "ERRO: Motor nativo não responde.") }
                        return@withContext
                    }
                    _uiState.update { it.copy(isNativeLoaded = true, progress = 0.25f) }

                    // 3. Varredura Profunda de Integridade (REAIS)
                    _uiState.update { it.copy(status = "Verificando permissões de sistema...", progress = 0.35f) }
                    if (!DeepIntegrityScanner.validatePermissions(context)) {
                        _uiState.update { it.copy(error = "ERRO: Permissões críticas ausentes.") }
                        return@withContext
                    }
                    
                    _uiState.update { it.copy(status = "Limpando artefatos de sessões antigas...", progress = 0.55f) }
                    val artifactsFound = DeepIntegrityScanner.scanLegacyArtifacts(context)
                    if (artifactsFound > 0) {
                        delay(500.milliseconds)
                    }

                    _uiState.update { it.copy(status = "Validando barramento de eventos neurais...", progress = 0.75f) }
                    DeepIntegrityScanner.testNeuralBus()

                    // 4. Inicialização Modular
                    if (isPro) {
                        _uiState.update { it.copy(status = "Ativando Bio-módulos PRO...", progress = 0.85f) }
                        ModuleRegistry.initializeProModules()
                    } else {
                        _uiState.update { it.copy(status = "Configurando Ambiente FREE...", progress = 0.85f) }
                        ModuleRegistry.initializeFreeModules()
                    }

                    // 5. Finalização
                    _uiState.update { it.copy(
                        status = if (isPro) "SISTEMA PRO OPERACIONAL" else "SISTEMA FREE PRONTO", 
                        progress = 1.0f, 
                        isComplete = true 
                    ) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Falha no motor: ${e.message}") }
            }
        }
    }
}
