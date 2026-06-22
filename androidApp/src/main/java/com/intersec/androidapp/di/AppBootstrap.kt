package com.intersec.androidapp.di

import com.intersec.androidapp.app.MainApplication
import com.intersec.androidapp.domain.repository.RustAnalysisRepository

/**
 * Delegado para acesso rápido às dependências globais.
 */
object AppBootstrap {

    val rustAnalysisRepository: RustAnalysisRepository
        get() = MainApplication.appModule.rustAnalysisRepository
}
