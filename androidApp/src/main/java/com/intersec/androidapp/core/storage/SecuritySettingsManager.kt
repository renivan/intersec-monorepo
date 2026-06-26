package com.intersec.androidapp.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

/**
 * Gerenciador persistente para configurações de segurança do interSec.
 * Sincroniza as decisões do usuário com o motor Native e mantém o estado entre reinicializações.
 */
class SecuritySettingsManager(private val context: Context) {

    companion object {
        private val KEY_SMART_SHIELD = booleanPreferencesKey("smart_shield_active")
        private val KEY_KILL_SWITCH = booleanPreferencesKey("kill_switch_active")
        private val KEY_SECURITY_LEVEL = intPreferencesKey("security_level") // 0=Baixo, 1=Normal, 2=Alto
        private val KEY_USER_TIER = intPreferencesKey("user_tier") // 0=FREE, 1=PRO
        private val KEY_THEME_TYPE = intPreferencesKey("app_theme_type")
        private val KEY_REWARDED_MINUTES_MONTH = intPreferencesKey("rewarded_minutes_month")
        private val KEY_LAST_REWARD_MONTH = intPreferencesKey("last_reward_month")
    }

    val smartShieldActive: Flow<Boolean> = context.dataStore.data.map { it[KEY_SMART_SHIELD] ?: true }
    val killSwitchActive: Flow<Boolean> = context.dataStore.data.map { it[KEY_KILL_SWITCH] ?: false }
    val securityLevel: Flow<Int> = context.dataStore.data.map { it[KEY_SECURITY_LEVEL] ?: 1 }
    val userTier: Flow<Int> = context.dataStore.data.map { it[KEY_USER_TIER] ?: 0 }
    val themeType: Flow<Int> = context.dataStore.data.map { it[KEY_THEME_TYPE] ?: 0 }
    val rewardedMinutesMonth: Flow<Int> = context.dataStore.data.map { it[KEY_REWARDED_MINUTES_MONTH] ?: 0 }
    val lastRewardMonth: Flow<Int> = context.dataStore.data.map { it[KEY_LAST_REWARD_MONTH] ?: -1 }

    suspend fun setSmartShield(active: Boolean) {
        context.dataStore.edit { it[KEY_SMART_SHIELD] = active }
        // TODO: Notificar motor Native via JNI
    }

    suspend fun setKillSwitch(active: Boolean) {
        context.dataStore.edit { it[KEY_KILL_SWITCH] = active }
        // TODO: Executar bloqueio imediato se active for true
    }

    suspend fun setSecurityLevel(level: Int) {
        context.dataStore.edit { it[KEY_SECURITY_LEVEL] = level }
        // TODO: Atualizar nível na Válvula de Tráfego Native
    }

    suspend fun setUserTier(tier: Int) {
        context.dataStore.edit { it[KEY_USER_TIER] = tier }
    }

    suspend fun setThemeType(typeId: Int) {
        context.dataStore.edit { it[KEY_THEME_TYPE] = typeId }
    }

    suspend fun addRewardedMinute(currentMonth: Int) {
        context.dataStore.edit { prefs ->
            val lastMonth = prefs[KEY_LAST_REWARD_MONTH] ?: -1
            var minutes = prefs[KEY_REWARDED_MINUTES_MONTH] ?: 0
            
            if (lastMonth != currentMonth) {
                minutes = 1
                prefs[KEY_LAST_REWARD_MONTH] = currentMonth
            } else {
                minutes += 1
            }
            prefs[KEY_REWARDED_MINUTES_MONTH] = minutes
        }
    }
}

