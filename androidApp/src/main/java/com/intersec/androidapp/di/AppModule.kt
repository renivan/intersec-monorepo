package com.intersec.androidapp.di

import com.intersec.androidapp.core.bridge.NativeBridgeFacade
import com.intersec.androidapp.core.storage.SecuritySettingsManager
import com.intersec.androidapp.data.repository.CoreAnalysisRepositoryImpl
import com.intersec.androidapp.data.repository.FirebaseAuthRepositoryImpl
import com.intersec.androidapp.domain.repository.AuthRepository
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository
import com.intersec.androidapp.presentation.viewmodel.CaptureRealtimeViewModel

/**
 * Gerenciador de dependências manuais do projeto.
 */
class AppModule(private val context: android.content.Context) {

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

    val sharedCaptureViewModel: CaptureRealtimeViewModel by lazy {
        CaptureRealtimeViewModel(coreAnalysisRepository)
    }

}

