package com.example.aisecretary.ui.privacy

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisecretary.data.local.preferences.SettingsManager
import com.example.aisecretary.security.PrivacyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for privacy settings management
 */
class PrivacySettingsViewModel(application: Application) : AndroidViewModel(application) {
    
    private val settingsManager = SettingsManager(application)
    private val privacyManager = settingsManager.getPrivacyManager()
    
    private val _privacySettings = MutableStateFlow(PrivacyManager.PrivacySettings(
        allowAnalytics = false,
        allowCrashReports = true,
        anonymizeData = true,
        enableBiometric = true,
        dataRetentionDays = 30,
        autoDeleteOldData = true
    ))
    val privacySettings: StateFlow<PrivacyManager.PrivacySettings> = _privacySettings.asStateFlow()
    
    private val _uiState = MutableStateFlow<PrivacyUiState>(PrivacyUiState.Idle)
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        loadPrivacySettings()
    }

    private fun loadPrivacySettings() {
        viewModelScope.launch {
            try {
                val settings = privacyManager.getPrivacySettings()
                _privacySettings.value = settings
            } catch (e: Exception) {
                _uiState.value = PrivacyUiState.Error("Failed to load privacy settings")
            }
        }
    }

    fun updateAnalyticsPermission(allowed: Boolean) {
        updateSettings { it.copy(allowAnalytics = allowed) }
    }

    fun updateCrashReportsPermission(allowed: Boolean) {
        updateSettings { it.copy(allowCrashReports = allowed) }
    }

    fun updateDataAnonymization(enabled: Boolean) {
        updateSettings { it.copy(anonymizeData = enabled) }
    }

    fun updateBiometricAuthentication(enabled: Boolean) {
        updateSettings { it.copy(enableBiometric = enabled) }
        settingsManager.setBiometricEnabled(enabled)
    }

    fun updateAutoDataDeletion(enabled: Boolean) {
        updateSettings { it.copy(autoDeleteOldData = enabled) }
    }

    fun updateDataRetentionDays(days: Int) {
        updateSettings { it.copy(dataRetentionDays = days) }
    }

    private fun updateSettings(updateFunction: (PrivacyManager.PrivacySettings) -> PrivacyManager.PrivacySettings) {
        viewModelScope.launch {
            try {
                val currentSettings = _privacySettings.value
                val newSettings = updateFunction(currentSettings)
                privacyManager.updatePrivacySettings(newSettings)
                _privacySettings.value = newSettings
                _uiState.value = PrivacyUiState.Success("Settings updated")
            } catch (e: Exception) {
                _uiState.value = PrivacyUiState.Error("Failed to update settings")
            }
        }
    }

    fun exportUserData() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Loading
            try {
                val result = privacyManager.exportUserData()
                when (result) {
                    is PrivacyManager.ExportResult.Success -> {
                        _uiState.value = PrivacyUiState.Success("Data exported to: ${result.filePath}")
                    }
                    is PrivacyManager.ExportResult.Error -> {
                        _uiState.value = PrivacyUiState.Error("Export failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PrivacyUiState.Error("Export failed: ${e.message}")
            }
        }
    }

    fun generatePrivacyReport() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Loading
            try {
                val report = privacyManager.generatePrivacyReport()
                _uiState.value = PrivacyUiState.Success("Privacy report generated")
                // Here you could navigate to a report viewing screen
            } catch (e: Exception) {
                _uiState.value = PrivacyUiState.Error("Failed to generate report")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Loading
            try {
                val result = privacyManager.requestAccountDeletion()
                when (result) {
                    is PrivacyManager.DeletionResult.Success -> {
                        _uiState.value = PrivacyUiState.Success("Account deleted successfully")
                        // Here you could navigate to a goodbye screen or exit the app
                    }
                    is PrivacyManager.DeletionResult.Error -> {
                        _uiState.value = PrivacyUiState.Error("Deletion failed: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PrivacyUiState.Error("Deletion failed: ${e.message}")
            }
        }
    }
}

sealed class PrivacyUiState {
    object Idle : PrivacyUiState()
    object Loading : PrivacyUiState()
    data class Success(val message: String) : PrivacyUiState()
    data class Error(val message: String) : PrivacyUiState()
}
