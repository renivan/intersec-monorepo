package com.intersec.androidapp.core.billing

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import com.intersec.androidapp.presentation.viewmodel.AnalysisViewModel

/**
 * BillingManager: Responsável pela ponte financeira com o Google Play.
 * Gerencia assinaturas e validação de perfil PRO.
 */
class BillingManager(
    private val activity: Activity,
    private val viewModel: AnalysisViewModel
) : PurchasesUpdatedListener {

    private val TAG = "interSec_BILLING"
    
    private val billingClient = BillingClient.newBuilder(activity)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Conexão com Google Play Store estabelecida.")
                    checkActiveSubscriptions()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Conexão com Google Play perdida. Tentando reconectar...")
            }
        })
    }

    /**
     * Verifica se o usuário já possui a assinatura ativa.
     */
    fun checkActiveSubscriptions() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val hasPro = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (hasPro) {
                    viewModel.upgradeToPro()
                    Log.i(TAG, "Acesso PRO validado via assinatura existente.")
                }
            }
        }
    }

    /**
     * Inicia o fluxo de compra/assinatura.
     * ID do Produto sugerido: "intersec_elite_monthly"
     */
    fun launchPurchaseFlow(productId: String = "intersec_elite_monthly") {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params, object : ProductDetailsResponseListener {
            override fun onProductDetailsResponse(billingResult: BillingResult, result: QueryProductDetailsResult) {
                val productDetailsList = result.productDetailsList
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    val details = productDetailsList[0]
                    val offers = details.subscriptionOfferDetails
                    var offerToken = ""
                    if (!offers.isNullOrEmpty()) {
                        offerToken = offers[0].offerToken
                    }
                    
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(details)
                            .setOfferToken(offerToken)
                            .build()
                    )

                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient.launchBillingFlow(activity, flowParams)
                } else {
                    Log.e(TAG, "Erro ao buscar detalhes do produto: ${billingResult.debugMessage}")
                }
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.w(TAG, "Operação cancelada pelo operador.")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // TODO: Aqui entra a Verificação via servidor (Firebase)
            // Por enquanto, valida localmente para permitir o teste de UI
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        viewModel.upgradeToPro()
                        Log.i(TAG, "Assinatura confirmada! Patente PRO concedida.")
                    }
                }
            }
        }
    }
}
