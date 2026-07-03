package com.intersec.androidapp.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_settings")

/**
 * Gerenciador persistente para configurações de segurança do interSec.
 */
class SecuritySettingsManager(private val context: Context) {

    companion object {
        private val KEY_SMART_SHIELD = booleanPreferencesKey("smart_shield_active")
        private val KEY_KILL_SWITCH = booleanPreferencesKey("kill_switch_active")
        private val KEY_SECURITY_LEVEL = intPreferencesKey("security_level") 
        private val KEY_USER_TIER = intPreferencesKey("user_tier") 
        private val KEY_THEME_TYPE = intPreferencesKey("app_theme_type")
        private val KEY_REWARDED_MINUTES_MONTH = intPreferencesKey("rewarded_minutes_month")
        private val KEY_LAST_REWARD_MONTH = intPreferencesKey("last_reward_month")
        private val KEY_IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        private val KEY_FIREWALL_RULES = stringSetPreferencesKey("firewall_rules")
        private val KEY_LAST_LATITUDE = doublePreferencesKey("last_latitude")
        private val KEY_LAST_LONGITUDE = doublePreferencesKey("last_longitude")
    }

    val smartShieldActive: Flow<Boolean> = context.dataStore.data.map { it[KEY_SMART_SHIELD] ?: true }
    val killSwitchActive: Flow<Boolean> = context.dataStore.data.map { it[KEY_KILL_SWITCH] ?: false }
    val securityLevel: Flow<Int> = context.dataStore.data.map { it[KEY_SECURITY_LEVEL] ?: 1 }
    val userTier: Flow<Int> = context.dataStore.data.map { it[KEY_USER_TIER] ?: 0 }
    val themeType: Flow<Int> = context.dataStore.data.map { it[KEY_THEME_TYPE] ?: 0 }
    val rewardedMinutesMonth: Flow<Int> = context.dataStore.data.map { it[KEY_REWARDED_MINUTES_MONTH] ?: 0 }
    val lastRewardMonth: Flow<Int> = context.dataStore.data.map { it[KEY_LAST_REWARD_MONTH] ?: -1 }
    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_DARK_MODE] ?: true }
    val firewallRules: Flow<Set<String>> = context.dataStore.data.map { it[KEY_FIREWALL_RULES] ?: emptySet() }
    val lastLocation: Flow<Pair<Double, Double>?> = context.dataStore.data.map { prefs ->
        val lat = prefs[KEY_LAST_LATITUDE]
        val lon = prefs[KEY_LAST_LONGITUDE]
        if (lat != null && lon != null) Pair(lat, lon) else null
    }

    suspend fun setSmartShield(active: Boolean) {
        context.dataStore.edit { it[KEY_SMART_SHIELD] = active }
    }

    suspend fun setKillSwitch(active: Boolean) {
        context.dataStore.edit { it[KEY_KILL_SWITCH] = active }
    }

    suspend fun setSecurityLevel(level: Int) {
        context.dataStore.edit { it[KEY_SECURITY_LEVEL] = level }
    }

    suspend fun setUserTier(tier: Int) {
        context.dataStore.edit { it[KEY_USER_TIER] = tier }
    }

    suspend fun setThemeType(typeId: Int) {
        context.dataStore.edit { it[KEY_THEME_TYPE] = typeId }
    }

    suspend fun setDarkMode(active: Boolean) {
        context.dataStore.edit { it[KEY_IS_DARK_MODE] = active }
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

    suspend fun saveLocation(latitude: Double, longitude: Double) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_LATITUDE] = latitude
            prefs[KEY_LAST_LONGITUDE] = longitude
        }
    }

    suspend fun addFirewallRule(ip: String, reason: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FIREWALL_RULES] ?: emptySet()
            prefs[KEY_FIREWALL_RULES] = current + "$ip|$reason"
        }
    }

    suspend fun removeFirewallRule(ruleStr: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_FIREWALL_RULES] ?: emptySet()
            prefs[KEY_FIREWALL_RULES] = current - ruleStr
        }
    }
}
