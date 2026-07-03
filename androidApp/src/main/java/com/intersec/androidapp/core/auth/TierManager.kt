package com.intersec.androidapp.core.auth

import com.google.firebase.auth.FirebaseAuth
import com.intersec.androidapp.core.bridge.NativeBridgeFacade
import com.intersec.androidapp.core.storage.SecuritySettingsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Gerencia a validacão de Tier (Standard/Premium) e ativação do motor nativo.
 */
class TierManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val securitySettings: SecuritySettingsManager,
    private val nativeBridge: NativeBridgeFacade
) {
    fun syncAndValidate() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = auth.currentUser ?: auth.signInAnonymously().await().user
                val currentTier = securitySettings.userTier.first()
                
                if (currentTier == 1) {
                    val token = user?.getIdToken(false)?.await()?.token ?: "AUTO_VALIDATED_TOKEN"
                    nativeBridge.activatePremiumFeatures(token)
                }
            } catch (_: Exception) {}
        }
    }
    
    suspend fun performUpgrade() {
        securitySettings.setUserTier(1)
        syncAndValidate()
    }
}
