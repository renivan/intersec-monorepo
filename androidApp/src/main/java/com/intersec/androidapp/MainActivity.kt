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

        // Inicia o fluxo de consentimento ANTES de qualquer coisa de Ads
        ConsentManager.requestConsent(this) {
            MobileAds.initialize(this) {
                // Preload de anúncio após inicialização bem sucedida
                com.intersec.androidapp.core.ads.AdManager.preloadRewardedAd(this)
            }
        }

        setContent {
            val analysisViewModel: AnalysisViewModel = viewModel()
            val state by analysisViewModel.uiState.collectAsState()

            // Inicializa o gestor financeiro
            remember { 
                billingManager = BillingManager(this, analysisViewModel)
                true
            }

            InterSecTheme(
                themeType = state.themeType,
                dynamicColor = false
            ) {
                InterSecApp(analysisViewModel)
            }
        }
    }

    fun startBillingFlow() {
        billingManager.launchPurchaseFlow()
    }
}
