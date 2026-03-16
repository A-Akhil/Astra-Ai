package com.example.aisecretary.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Handles app settings like memory and voice features.
 *
 * Stores and updates user preferences using SharedPreferences.
 * Also provides live updates using Flow.
 */
class SettingsManager(context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    // Flow to keep track of memory setting
    private val _memoryEnabled = MutableStateFlow(isMemoryEnabled())
    val memoryEnabled: Flow<Boolean> = _memoryEnabled.asStateFlow()

    // Flow to keep track of voice input setting
    private val _voiceInputEnabled = MutableStateFlow(isVoiceInputEnabled())
    val voiceInputEnabled: Flow<Boolean> = _voiceInputEnabled.asStateFlow()

    // Flow to keep track of voice output setting
    private val _voiceOutputEnabled = MutableStateFlow(isVoiceOutputEnabled())
    val voiceOutputEnabled: Flow<Boolean> = _voiceOutputEnabled.asStateFlow()

    /**
     * Checks if memory feature is turned on.
     */
    fun isMemoryEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_MEMORY_ENABLED, true)
    }

    /**
     * Turn memory feature on or off.
     */
    fun setMemoryEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }

    /**
     * Checks if voice input is turned on.
     */
    fun isVoiceInputEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VOICE_INPUT_ENABLED, true)
    }

    /**
     * Turn voice input on or off.
     */
    fun setVoiceInputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VOICE_INPUT_ENABLED, enabled).apply()
        _voiceInputEnabled.value = enabled
    }

    /**
     * Checks if voice output is turned on.
     */
    fun isVoiceOutputEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }

    /**
     * Turn voice output on or off.
     */
    fun setVoiceOutputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }
    
    companion object {
        private const val PREFS_NAME = "ai_secretary_prefs"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_INPUT_ENABLED = "voice_input_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
    }
}