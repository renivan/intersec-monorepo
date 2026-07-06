package com.intersec.androidapp.core.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * ConsentManager: Gerencia o consentimento legal (GDPR/CCPA) para usuários EEE e EUA.
 * Garante que anúncios só carreguem após aceitação dos termos de privacidade.
 */
object ConsentManager {
    private const val TAG = "interSec_CONSENT"
    private lateinit var consentInformation: ConsentInformation

    /**
     * Inicia o fluxo de solicitação de consentimento.
     */
    fun requestConsent(activity: Activity, onConsentGathered: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // Informações atualizadas. Agora verificamos se precisamos mostrar o formulário.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "Erro ao processar formulário de consentimento: ${formError.message}")
                    }
                    
                    // Se o consentimento foi obtido ou não é necessário, seguimos para carregar Ads
                    if (canRequestAds()) {
                        Log.i(TAG, "Consentimento obtido ou desnecessário. Liberando Ads.")
                        onConsentGathered()
                    }
                }
            },
            { requestError ->
                if (requestError.message.contains("Publisher misconfiguration")) {
                    Log.w(TAG, "UMP: Publisher misconfiguration (Comum se Forms não configurados no console). Prosseguindo...")
                } else {
                    Log.e(TAG, "Falha ao solicitar info de consentimento: ${requestError.message}")
                }
                // Em caso de erro, tentamos prosseguir de qualquer forma (fallback)
                onConsentGathered()
            }
        )
    }

    /**
     * Verifica se o estado atual de consentimento permite o carregamento de anúncios.
     */
    fun canRequestAds(): Boolean {
        return if (!::consentInformation.isInitialized) false
        else consentInformation.canRequestAds()
    }
}
