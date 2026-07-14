package com.example.sonexa.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Changing to AndroidViewModel gives us the Application context we need for DataStore!
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    // 1. Collect the permanent state from DataStore
    val isAmoledGoldEnabled: StateFlow<Boolean> = settingsManager.amoledThemeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val isSmartScanEnabled: StateFlow<Boolean> = settingsManager.smartScanFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, true)


    // 2. Save the new state permanently when the switch is flipped
    fun toggleAmoledTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setAmoledTheme(enabled)
        }
    }

    fun toggleSmartScanner(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.setSmartScan(enabled)
        }
    }

    // NEW
    val hasAcceptedPrivacyPolicy: StateFlow<Boolean> = settingsManager.privacyPolicyFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    // NEW
    fun acceptPrivacyPolicy() {
        viewModelScope.launch {
            settingsManager.setPrivacyPolicyAccepted(true)
        }
    }
}