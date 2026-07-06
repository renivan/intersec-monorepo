package com.intersec.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intersec.androidapp.core.billing.BillingManager
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel
import com.intersec.androidapp.ui.InterSecApp
import com.intersec.androidapp.ui.theme.InterSecTheme

import com.intersec.androidapp.core.ads.ConsentManager
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    fun startBillingFlow() {
        billingManager.launchPurchaseFlow()
    }
}
