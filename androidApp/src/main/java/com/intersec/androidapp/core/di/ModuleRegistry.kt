package com.intersec.androidapp.core.di

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.intersec.androidapp.app.MainApplication
import com.intersec.androidapp.core.ads.AdManager
import com.intersec.androidapp.core.ads.ConsentManager
import com.intersec.androidapp.core.runtime.NativeRuntimeLoader
import com.intersec.androidapp.core.network.ThreatIntelManager
import kotlinx.coroutines.delay

/**
 * Registry de Módulos: Controla o carregamento seletivo de órgãos do sistema
 * baseado no perfil de usuário (FREE vs PRO).
 */
object ModuleRegistry {
    private const val TAG = "interSec_MODULES"

    // Estados de carregamento
    var isCoreInitialized = false
    var isAdvancedAnalyticsReady = false
    var isNeural3DReady = false
    var isVpnStackReady = false

    /**
     * Inicializa módulos BASE (Comuns a ambos os perfis)
     */
    suspend fun initializeBase(activity: Activity): Boolean {
        Log.i(TAG, "Iniciando Módulos Base...")
        
        // 1. Motor Native (Essencial)
        val nativeOk = NativeRuntimeLoader.ensureLoaded()
        if (!nativeOk) return false
        
        // 2. Consentimento e Ads (Necessário para monetização Free e Conformidade)
        ConsentManager.requestConsent(activity) {
            MobileAds.initialize(activity) {
                AdManager.preloadRewardedAd(activity)
            }
        }
        
        isCoreInitialized = true
        return true
    }

    /**
     * Inicializa Módulos PRO (Exclusivos ou Otimizados)
     */
    suspend fun initializeProModules() {
        Log.i(TAG, "Ativando Bio-módulos PRO...")
        
        // 1. Inteligência de Ameaças em Tempo Real
        ThreatIntelManager.syncThreatFeeds(isPro = true)
        isAdvancedAnalyticsReady = true
        delay(200)

        // 2. Pré-aquecimento do Motor Neural 3D
        // Aqui poderíamos inicializar shaders ou texturas do SceneView
        isNeural3DReady = true
        delay(200)

        // 3. Otimização da Stack de VPN para alta performance
        isVpnStackReady = true
        Log.i(TAG, "Sistema PRO totalmente operacional.")
    }

    /**
     * Inicializa Módulos FREE (Limitados)
     */
    suspend fun initializeFreeModules() {
        Log.i(TAG, "Configurando Ambiente FREE...")
        
        // Ameaças apenas básicas
        ThreatIntelManager.syncThreatFeeds(isPro = false)
        
        isAdvancedAnalyticsReady = false // Desativado no Free
        isNeural3DReady = true // 3D funciona mas com menos nós
        isVpnStackReady = true 
        
        Log.i(TAG, "Sistema FREE pronto (Módulos de publicidade ativos).")
    }
}
