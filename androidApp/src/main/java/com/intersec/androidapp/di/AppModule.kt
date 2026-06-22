package com.intersec.androidapp.di

import com.intersec.androidapp.core.bridge.RustBridgeFacade
import com.intersec.androidapp.data.repository.RustAnalysisRepositoryImpl
import com.intersec.androidapp.domain.repository.RustAnalysisRepository

/**
 * Gerenciador de dependências manual do projeto.
 */
class AppModule() {

    val rustBridgeFacade: RustBridgeFacade by lazy {
        RustBridgeFacade()
    }

    val rustAnalysisRepository: RustAnalysisRepository by lazy {
        RustAnalysisRepositoryImpl(rustBridgeFacade)
    }

}
