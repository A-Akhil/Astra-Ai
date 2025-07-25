package com.example.aisecretary.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Memory enabled setting
    private val _memoryEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    )
    val memoryEnabled: StateFlow<Boolean> = _memoryEnabled.asStateFlow()

    // Voice output enabled setting
    private val _voiceOutputEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    )
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()
    
    // Wake word detection setting - disabled by default
    private val _wakeWordEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    )
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()
    
    // Auto-activate microphone after speaking - enabled by default
    private val _autoActivateMic = MutableStateFlow(
        preferences.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    )
    val autoActivateMic: StateFlow<Boolean> = _autoActivateMic.asStateFlow()

    // Dark mode setting
    private val _darkModeEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_DARK_MODE_ENABLED, false)
    )
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()

    fun isMemoryEnabled(): Boolean {
        return preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    }

    fun setMemoryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }

    fun isVoiceOutputEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }
    
    fun isWakeWordEnabled(): Boolean {
        return preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    }
    
    fun setWakeWordEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _wakeWordEnabled.value = enabled
    }
    
    fun isAutoActivateMicEnabled(): Boolean {
        return preferences.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    }
    
    fun setAutoActivateMicEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_ACTIVATE_MIC, enabled).apply()
        _autoActivateMic.value = enabled
    }

    fun isDarkModeEnabled(): Boolean {
        return preferences.getBoolean(KEY_DARK_MODE_ENABLED, false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_DARK_MODE_ENABLED, enabled).apply()
        _darkModeEnabled.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "secretary_settings"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_AUTO_ACTIVATE_MIC = "auto_activate_mic"
        private const val KEY_DARK_MODE_ENABLED = "dark_mode_enabled"
    }
} 