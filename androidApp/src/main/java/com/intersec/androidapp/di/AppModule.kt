package com.intersec.androidapp.di

import android.content.Context
import com.intersec.androidapp.core.auth.TierManager
import com.intersec.androidapp.core.bridge.NativeBridgeFacade
import com.intersec.androidapp.core.location.LocationTracker
import com.intersec.androidapp.core.network.geoip.GeoIpRepository
import com.intersec.androidapp.core.neural.NeuralCoreEngine
import com.intersec.androidapp.core.storage.SecuritySettingsManager
import com.intersec.androidapp.data.repository.CoreAnalysisRepositoryImpl
import com.intersec.androidapp.data.repository.FirebaseAuthRepositoryImpl
import com.intersec.androidapp.domain.repository.AuthRepository
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.presentation.viewmodel.CaptureRealtimeViewModel

/**
 * Gerenciador de dependências manuais do projeto (Hardened v3).
 */
class AppModule(private val context: Context) {

    val securitySettingsManager: SecuritySettingsManager by lazy {
        SecuritySettingsManager(context)
    }

    val nativeBridgeFacade: NativeBridgeFacade by lazy {
        NativeBridgeFacade()
    }

    val coreAnalysisRepository: CoreAnalysisRepository by lazy {
        CoreAnalysisRepositoryImpl(nativeBridgeFacade)
    }

    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepositoryImpl()
    }

    val geoIpRepository: GeoIpRepository by lazy {
        GeoIpRepository()
    }

    val neuralCoreEngine: NeuralCoreEngine by lazy {
        NeuralCoreEngine(geoIpRepository)
    }

    val locationTracker: LocationTracker by lazy {
        LocationTracker(context, securitySettingsManager)
    }

    val tierManager: TierManager by lazy {
        TierManager(
            securitySettings = securitySettingsManager,
            nativeBridge = nativeBridgeFacade
        )
    }

    val sharedCaptureViewModel: CaptureRealtimeViewModel by lazy {
        CaptureRealtimeViewModel(coreAnalysisRepository)
    }
}
