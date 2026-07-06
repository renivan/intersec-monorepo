package com.intersec.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.core.billing.BillingManager
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import com.intersec.androidapp.ui.InterSecApp
import com.intersec.androidapp.ui.theme.InterSecTheme

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissões concedidas ou negadas
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            val analysisViewModel: AnalysisViewModel = viewModel()
            val state by analysisViewModel.uiState.collectAsState()

            // A inicialização agora é feita pela InitializationScreen/ViewModel
            // para garantir um feedback profissional ao usuário.

            // Inicializa o gestor financeiro
            remember { 
                billingManager = BillingManager(this, analysisViewModel)
                true
            }

            InterSecTheme(
                themeType = state.themeType,
                isDarkMode = state.isDarkMode
            ) {
                InterSecApp(analysisViewModel)
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()

        // Localização (para detalhes da rede)
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        // Bluetooth (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }

        // Notificações (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }

    fun startBillingFlow() {
        billingManager.launchPurchaseFlow()
    }
}
