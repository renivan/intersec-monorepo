package com.intersec.androidapp.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdManager: Gerenciador tático de publicidade premiada.
 * Implementa estratégia híbrida de Preload e On-Demand.
 */
object AdManager {
    private const val TAG = "interSec_ADS"
    // ID de Teste do Google (Seguro para desenvolvimento)
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    /**
     * Inicia o pré-carregamento silencioso.
     */
    fun preloadRewardedAd(context: Context) {
        if (rewardedAd != null || isLoading) return
        
        isLoading = true
        Log.d(TAG, "Iniciando Preload de Anúncio Premiado...")
        
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Falha no Preload: ${adError.message}")
                rewardedAd = null
                isLoading = false
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.i(TAG, "Anúncio Premiado carregado e pronto para exibição.")
                rewardedAd = ad
                isLoading = false
            }
        })
    }

    /**
     * Exibe o anúncio se estiver pronto, ou tenta carregar On-Demand.
     */
    fun showRewardedAd(activity: Activity, onRewardEarned: (Int) -> Unit, onFailure: () -> Unit) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                Log.i(TAG, "Recompensa concedida: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(rewardItem.amount)
                rewardedAd = null
                preloadRewardedAd(activity) // Recarrega para a próxima vez
            }
        } else {
            Log.w(TAG, "Anúncio não disponível no cache. Tentando carga On-Demand...")
            loadAndShowOnDemand(activity, onRewardEarned, onFailure)
        }
    }

    private fun loadAndShowOnDemand(activity: Activity, onRewardEarned: (Int) -> Unit, onFailure: () -> Unit) {
        val adRequest = AdRequest.Builder().build()
        isLoading = true
        
        RewardedAd.load(activity, REWARDED_AD_UNIT_ID, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Falha na carga On-Demand: ${adError.message}")
                isLoading = false
                onFailure()
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.i(TAG, "Carga On-Demand concluída. Exibindo...")
                isLoading = false
                rewardedAd = ad
                showRewardedAd(activity, onRewardEarned, onFailure)
            }
        })
    }
}
