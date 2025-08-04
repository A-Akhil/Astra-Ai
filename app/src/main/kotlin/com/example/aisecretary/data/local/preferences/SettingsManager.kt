package com.example.aisecretary.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages application settings and preferences with reactive streams
 */
class SettingsManager(context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    // Memory feature settings
    private val _memoryEnabled = MutableStateFlow(isMemoryEnabled())
    val memoryEnabled: StateFlow<Boolean> = _memoryEnabled.asStateFlow()
    
    // Voice feature settings
    private val _voiceInputEnabled = MutableStateFlow(isVoiceInputEnabled())
    val voiceInputEnabled: StateFlow<Boolean> = _voiceInputEnabled.asStateFlow()
    
    private val _voiceOutputEnabled = MutableStateFlow(isVoiceOutputEnabled())
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()
    
    // Wake word settings
    private val _wakeWordEnabled = MutableStateFlow(isWakeWordEnabled())
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()
    
    // Auto-activate microphone settings  
    private val _autoActivateMic = MutableStateFlow(isAutoActivateMicEnabled())
    val autoActivateMic: StateFlow<Boolean> = _autoActivateMic.asStateFlow()
    
    // Streaming settings
    private val _streamingEnabled = MutableStateFlow(isStreamingEnabled())
    val streamingEnabled: StateFlow<Boolean> = _streamingEnabled.asStateFlow()
    
    // Background listening
    private val _backgroundListening = MutableStateFlow(isBackgroundListening())
    val backgroundListening: StateFlow<Boolean> = _backgroundListening.asStateFlow()
    
    // TTS Speed
    private val _ttsSpeed = MutableStateFlow(getTtsSpeed())
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()
    
    // Memory settings
    fun isMemoryEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_MEMORY_ENABLED, true)
    }
    
    fun setMemoryEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }
    
    // Voice input settings
    fun isVoiceInputEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VOICE_INPUT_ENABLED, true)
    }
    
    fun setVoiceInputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VOICE_INPUT_ENABLED, enabled).apply()
        _voiceInputEnabled.value = enabled
    }
    
    // Voice output settings
    fun isVoiceOutputEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }
    
    fun setVoiceOutputEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }
    
    // Wake word settings
    fun isWakeWordEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    }
    
    fun setWakeWordEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _wakeWordEnabled.value = enabled
    }
    
    // Auto-activate microphone settings
    fun isAutoActivateMicEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    }
    
    fun setAutoActivateMicEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_AUTO_ACTIVATE_MIC, enabled).apply()
        _autoActivateMic.value = enabled
    }
    
    // Streaming settings
    fun isStreamingEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_STREAMING_ENABLED, true)
    }
    
    fun setStreamingEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_STREAMING_ENABLED, enabled).apply()
        _streamingEnabled.value = enabled
    }
    
    // Background listening
    fun isBackgroundListening(): Boolean {
        return sharedPrefs.getBoolean(KEY_BACKGROUND_LISTENING, false)
    }
    
    fun setBackgroundListening(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_BACKGROUND_LISTENING, enabled).apply()
        _backgroundListening.value = enabled
    }
    
    // TTS Speed
    fun getTtsSpeed(): Float {
        return sharedPrefs.getFloat(KEY_TTS_SPEED, 1.0f)
    }
    
    fun setTtsSpeed(speed: Float) {
        sharedPrefs.edit().putFloat(KEY_TTS_SPEED, speed.coerceIn(0.5f, 2.0f)).apply()
        _ttsSpeed.value = speed
    }
    
    companion object {
        private const val PREFS_NAME = "ai_secretary_prefs"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_INPUT_ENABLED = "voice_input_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_AUTO_ACTIVATE_MIC = "auto_activate_mic"
        private const val KEY_STREAMING_ENABLED = "streaming_enabled"
        private const val KEY_BACKGROUND_LISTENING = "background_listening"
        private const val KEY_TTS_SPEED = "tts_speed"
    }
}
