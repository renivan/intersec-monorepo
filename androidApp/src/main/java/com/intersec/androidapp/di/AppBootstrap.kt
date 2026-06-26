package com.intersec.androidapp.di

import com.intersec.androidapp.app.MainApplication
import com.intersec.androidapp.domain.repository.CoreAnalysisRepository

/**
 * Delegado para acesso rápido às dependências globais.
 */
object AppBootstrap {

    val coreAnalysisRepository: CoreAnalysisRepository
        get() = MainApplication.appModule.coreAnalysisRepository

    val captureViewModel: com.intersec.androidapp.presentation.viewmodel.CaptureRealtimeViewModel
        get() = MainApplication.appModule.sharedCaptureViewModel
}

