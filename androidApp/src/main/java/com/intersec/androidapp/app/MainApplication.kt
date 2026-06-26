package com.intersec.androidapp.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.intersec.androidapp.core.ads.AdManager
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize
import com.intersec.androidapp.di.AppModule

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
        
        // Inicializa o Firebase
        Firebase.initialize(context = this)

        // Instala a factory do Play Integrity
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

        // Inicializa o Mobile Ads SDK
        MobileAds.initialize(this) {
            AdManager.preloadRewardedAd(this)
        }

        appModule = AppModule(this)
    }
}
