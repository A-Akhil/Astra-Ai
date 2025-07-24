package com.example.aisecretary.data.local.preferences

import android.content.Context
import com.example.aisecretary.security.SecureStorage
import com.example.aisecretary.security.PrivacyManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages application settings and preferences with secure storage
 */
class SettingsManager(context: Context) {
    
    private val secureStorage = SecureStorage(context)
    private val privacyManager = PrivacyManager(context, secureStorage)
    
    // Keys for settings
    companion object {
        private const val WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val VOICE_ENABLED = "voice_enabled"
        private const val THEME_MODE = "theme_mode"
        private const val LANGUAGE = "language"
        private const val BIOMETRIC_ENABLED = "biometric_enabled"
        private const val AUTO_SAVE_CONVERSATIONS = "auto_save_conversations"
        private const val OFFLINE_MODE = "offline_mode"
    }

    // State flows for reactive UI
    private val _isWakeWordEnabled = MutableStateFlow(getWakeWordEnabled())
    val isWakeWordEnabled: Flow<Boolean> = _isWakeWordEnabled.asStateFlow()

    private val _isVoiceEnabled = MutableStateFlow(getVoiceEnabled())
    val isVoiceEnabled: Flow<Boolean> = _isVoiceEnabled.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(getBiometricEnabled())
    val isBiometricEnabled: Flow<Boolean> = _isBiometricEnabled.asStateFlow()

    // Wake word settings
    fun getWakeWordEnabled(): Boolean {
        return secureStorage.getBoolean(WAKE_WORD_ENABLED, false)
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        secureStorage.storeBoolean(WAKE_WORD_ENABLED, enabled)
        _isWakeWordEnabled.value = enabled
    }

    fun isWakeWordEnabled(): Boolean = getWakeWordEnabled()

    // Voice settings
    fun getVoiceEnabled(): Boolean {
        return secureStorage.getBoolean(VOICE_ENABLED, true)
    }

    fun setVoiceEnabled(enabled: Boolean) {
        secureStorage.storeBoolean(VOICE_ENABLED, enabled)
        _isVoiceEnabled.value = enabled
    }

    // Biometric authentication settings
    fun getBiometricEnabled(): Boolean {
        return secureStorage.getBoolean(BIOMETRIC_ENABLED, true)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        secureStorage.storeBoolean(BIOMETRIC_ENABLED, enabled)
        _isBiometricEnabled.value = enabled
    }

    // Theme settings
    fun getThemeMode(): String {
        return secureStorage.getInt(THEME_MODE, 0).toString() // 0 = system, 1 = light, 2 = dark
    }

    fun setThemeMode(mode: String) {
        secureStorage.storeInt(THEME_MODE, mode.toIntOrNull() ?: 0)
    }

    // Language settings
    suspend fun getLanguage(): String {
        return secureStorage.getSecureString(LANGUAGE) ?: "en"
    }

    suspend fun setLanguage(language: String) {
        secureStorage.storeSecureString(LANGUAGE, language)
    }

    // Auto-save conversations
    fun getAutoSaveConversations(): Boolean {
        return secureStorage.getBoolean(AUTO_SAVE_CONVERSATIONS, true)
    }

    fun setAutoSaveConversations(enabled: Boolean) {
        secureStorage.storeBoolean(AUTO_SAVE_CONVERSATIONS, enabled)
    }

    // Offline mode
    fun getOfflineMode(): Boolean {
        return secureStorage.getBoolean(OFFLINE_MODE, false)
    }

    fun setOfflineMode(enabled: Boolean) {
        secureStorage.storeBoolean(OFFLINE_MODE, enabled)
    }

    // Privacy manager access
    fun getPrivacyManager(): PrivacyManager = privacyManager

    // Clear all settings (for account deletion)
    fun clearAllSettings() {
        secureStorage.clearAll()
        _isWakeWordEnabled.value = false
        _isVoiceEnabled.value = true
        _isBiometricEnabled.value = true
    }
}
