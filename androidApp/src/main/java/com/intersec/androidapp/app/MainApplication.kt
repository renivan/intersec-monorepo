package com.intersec.androidapp.app

import android.app.Application
import com.google.android.gms.security.ProviderInstaller
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.intersec.androidapp.di.AppModule
import android.util.Log

class MainApplication : Application() {
    
    companion object {
        lateinit var instance: MainApplication
            private set
        lateinit var appModule: AppModule
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 1. Instala o ProviderInstaller para resolver avisos de módulos GMS/Dynamite antigos
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: Exception) {
            Log.w("interSec_APP", "GMS ProviderInstaller falhou (Comum em emuladores): ${e.message}")
        }

        // 2. Inicializa o Firebase
        Firebase.initialize(context = this)

        // 3. Instala a factory do App Check
        // No emulador, usamos o DebugProvider para evitar erros de integridade (-17)
        val providerFactory = if (isEmulator()) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        
        Firebase.appCheck.installAppCheckProviderFactory(providerFactory)

        appModule = AppModule(this)
    }

    private fun isEmulator(): Boolean {
        val model = android.os.Build.MODEL
        return model.contains("sdk", ignoreCase = true) || 
               model.contains("Emulator", ignoreCase = true) || 
               android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic") ||
               "google_sdk" == android.os.Build.PRODUCT
    }
}
