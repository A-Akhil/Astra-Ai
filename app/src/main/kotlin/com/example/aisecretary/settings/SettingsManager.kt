package com.example.aisecretary.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user settings like memory, voice output, wake word, and mic activation.
 * Stores settings using SharedPreferences and exposes current values using StateFlow.
 *
 * @param context The context used to access SharedPreferences.
 */
class SettingsManager(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Current value of memory setting
    private val _memoryEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    )
    /** Flow that tells if memory feature is enabled */
    val memoryEnabled: StateFlow<Boolean> = _memoryEnabled.asStateFlow()

    // Current value of voice output setting
    private val _voiceOutputEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    )
    /** Flow that tells if voice output is enabled */
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()

    // Current value of wake word setting
    private val _wakeWordEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    )
    /** Flow that tells if wake word detection is enabled */
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    // Current value of auto mic activation setting
    private val _autoActivateMic = MutableStateFlow(
        preferences.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    )

    /** Flow that tells if mic should auto-activate after speaking */
    val autoActivateMic: StateFlow<Boolean> = _autoActivateMic.asStateFlow()

    /** Returns if memory is currently enabled */
    fun isMemoryEnabled(): Boolean {
        return preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    }

    /** Enables or disables memory and updates the flow */
    fun setMemoryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }

    /** Returns if voice output is currently enabled */
    fun isVoiceOutputEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }

    /** Enables or disables voice output and updates the flow */
    fun setVoiceOutputEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }

    /** Returns if wake word detection is currently enabled */
    fun isWakeWordEnabled(): Boolean {
        return preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    }

    /** Enables or disables wake word detection and updates the flow */
    fun setWakeWordEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    /** Returns if mic auto-activation is currently enabled */
    fun isAutoActivateMicEnabled(): Boolean {
        return preferences.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    }

    /** Enables or disables mic auto-activation and updates the flow */
    fun setAutoActivateMicEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_ACTIVATE_MIC, enabled).apply()
        _autoActivateMic.value = enabled
    }

    companion object {
        private const val PREFS_NAME = "secretary_settings"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_AUTO_ACTIVATE_MIC = "auto_activate_mic"
    }
} 