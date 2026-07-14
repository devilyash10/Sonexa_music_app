package com.example.sonexa.feature.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// This initializes the actual storage file on the phone
private val Context.dataStore by preferencesDataStore(name = "sonexa_settings")

class SettingsManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        // These are the exact "keys" used to look up your saved data
        val AMOLED_GOLD_KEY = booleanPreferencesKey("amoled_gold_enabled")
        val SMART_SCAN_KEY = booleanPreferencesKey("smart_scan_enabled")
        val PRIVACY_POLICY_KEY = booleanPreferencesKey("privacy_policy_accepted")
    }

    // 1. READ THE DATA
    val amoledThemeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AMOLED_GOLD_KEY] ?: false // Default is false
    }

    val smartScanFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SMART_SCAN_KEY] ?: true // Default is true
    }

    // 2. WRITE THE DATA
    suspend fun setAmoledTheme(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AMOLED_GOLD_KEY] = enabled
        }
    }

    suspend fun setSmartScan(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SMART_SCAN_KEY] = enabled
        }
    }
    // Read State
    val privacyPolicyFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PRIVACY_POLICY_KEY] ?: false
    }

    // Write State
    suspend fun setPrivacyPolicyAccepted(accepted: Boolean) {
        dataStore.edit { preferences ->
            preferences[PRIVACY_POLICY_KEY] = accepted
        }
    }
}