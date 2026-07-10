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
        // No emulador, usamos o DebugProvider ou desativamos temporariamente para evitar bloqueios de attestation
        if (isEmulator()) {
            Log.i("interSec_APP", "Emulador detectado: Usando modo de desenvolvimento para App Check.")
            Firebase.appCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            Firebase.appCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
        }

        appModule = AppModule(this)
    }

    private fun isEmulator(): Boolean {
        val model = android.os.Build.MODEL
        val brand = android.os.Build.BRAND
        val device = android.os.Build.DEVICE
        val product = android.os.Build.PRODUCT
        val hardware = android.os.Build.HARDWARE
        val fingerprint = android.os.Build.FINGERPRINT

        return model.contains("sdk", ignoreCase = true) ||
               model.contains("Emulator", ignoreCase = true) ||
               brand.startsWith("generic") && device.startsWith("generic") ||
               fingerprint.startsWith("generic") ||
               fingerprint.startsWith("unknown") ||
               product == "google_sdk" ||
               product == "sdk_gphone_x86" ||
               hardware == "goldfish" ||
               hardware == "ranchu"
    }
}
