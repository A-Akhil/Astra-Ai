package com.example.aisecretary.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "ai_secretary_settings"
        private const val KEY_MEMORY_ENABLED = "memory_enabled"
        private const val KEY_VOICE_OUTPUT_ENABLED = "voice_output_enabled"
        private const val KEY_VOICE_INPUT_ENABLED = "voice_input_enabled"
        private const val KEY_WAKE_WORD_ENABLED = "wake_word_enabled"
        private const val KEY_AUTO_ACTIVATE_MIC = "auto_activate_mic"
        private const val KEY_STREAMING_ENABLED = "streaming_enabled"
        private const val KEY_CONVERSATION_HISTORY_ENABLED = "conversation_history_enabled"
        private const val KEY_BACKGROUND_LISTENING = "background_listening_enabled"
        private const val KEY_TTS_SPEED = "tts_speed"
        private const val KEY_STT_LANGUAGE = "stt_language"
        private const val KEY_AI_MODEL_NAME = "ai_model_name"
        private const val KEY_MAX_RESPONSE_TOKENS = "max_response_tokens"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_AUTO_SAVE_CONVERSATIONS = "auto_save_conversations"
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Memory enabled setting - StateFlow for reactive updates
    private val _memoryEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    )
    val memoryEnabled: StateFlow<Boolean> = _memoryEnabled.asStateFlow()

    // Voice output enabled setting - StateFlow for reactive updates
    private val _voiceOutputEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    )
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()
    
    // Voice input enabled setting - StateFlow for reactive updates
    private val _voiceInputEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_VOICE_INPUT_ENABLED, true)
    )
    val voiceInputEnabled: StateFlow<Boolean> = _voiceInputEnabled.asStateFlow()
    
    // Wake word detection setting - disabled by default for privacy
    private val _wakeWordEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    )
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()
    
    // Auto-activate microphone after speaking - enabled by default
    private val _autoActivateMic = MutableStateFlow(
        preferences.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    )
    val autoActivateMic: StateFlow<Boolean> = _autoActivateMic.asStateFlow()

    // Streaming responses enabled - new feature
    private val _streamingEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_STREAMING_ENABLED, true)
    )
    val streamingEnabled: StateFlow<Boolean> = _streamingEnabled.asStateFlow()

    // Conversation history enabled
    private val _conversationHistoryEnabled = MutableStateFlow(
        preferences.getBoolean(KEY_CONVERSATION_HISTORY_ENABLED, true)
    )
    val conversationHistoryEnabled: StateFlow<Boolean> = _conversationHistoryEnabled.asStateFlow()

    // Background listening capability
    private val _backgroundListening = MutableStateFlow(
        preferences.getBoolean(KEY_BACKGROUND_LISTENING, false)
    )
    val backgroundListening: StateFlow<Boolean> = _backgroundListening.asStateFlow()

    // TTS Speed setting (0.5f to 2.0f)
    private val _ttsSpeed = MutableStateFlow(
        preferences.getFloat(KEY_TTS_SPEED, 1.0f)
    )
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    // STT Language setting
    private val _sttLanguage = MutableStateFlow(
        preferences.getString(KEY_STT_LANGUAGE, "en-US") ?: "en-US"
    )
    val sttLanguage: StateFlow<String> = _sttLanguage.asStateFlow()

    // AI Model configuration
    private val _aiModelName = MutableStateFlow(
        preferences.getString(KEY_AI_MODEL_NAME, "llama3:8b") ?: "llama3:8b"
    )
    val aiModelName: StateFlow<String> = _aiModelName.asStateFlow()

    // Memory Management Functions
    fun isMemoryEnabled(): Boolean {
        return preferences.getBoolean(KEY_MEMORY_ENABLED, true)
    }

    fun setMemoryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_MEMORY_ENABLED, enabled).apply()
        _memoryEnabled.value = enabled
    }

    // Voice Output Functions
    fun isVoiceOutputEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOICE_OUTPUT_ENABLED, true)
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_VOICE_OUTPUT_ENABLED, enabled).apply()
        _voiceOutputEnabled.value = enabled
    }

    // Voice Input Functions
    fun isVoiceInputEnabled(): Boolean {
        return preferences.getBoolean(KEY_VOICE_INPUT_ENABLED, true)
    }

    fun setVoiceInputEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_VOICE_INPUT_ENABLED, enabled).apply()
        _voiceInputEnabled.value = enabled
    }

    // Wake Word Functions
    fun isWakeWordEnabled(): Boolean {
        return preferences.getBoolean(KEY_WAKE_WORD_ENABLED, false)
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_WAKE_WORD_ENABLED, enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    // Auto Activate Microphone Functions
    fun isAutoActivateMicEnabled(): Boolean {
        return preferences.getBoolean(KEY_AUTO_ACTIVATE_MIC, true)
    }

    fun setAutoActivateMicEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_ACTIVATE_MIC, enabled).apply()
        _autoActivateMic.value = enabled
    }

    // Streaming Functions - NEW
    fun isStreamingEnabled(): Boolean {
        return preferences.getBoolean(KEY_STREAMING_ENABLED, true)
    }

    fun setStreamingEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_STREAMING_ENABLED, enabled).apply()
        _streamingEnabled.value = enabled
    }

    // Conversation History Functions
    fun isConversationHistoryEnabled(): Boolean {
        return preferences.getBoolean(KEY_CONVERSATION_HISTORY_ENABLED, true)
    }

    fun setConversationHistoryEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_CONVERSATION_HISTORY_ENABLED, enabled).apply()
        _conversationHistoryEnabled.value = enabled
    }

    // Background Listening Functions
    fun isBackgroundListening(): Boolean {
        return preferences.getBoolean(KEY_BACKGROUND_LISTENING, false)
    }

    fun setBackgroundListening(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_BACKGROUND_LISTENING, enabled).apply()
        _backgroundListening.value = enabled
    }

    // TTS Speed Functions
    fun getTtsSpeed(): Float {
        return preferences.getFloat(KEY_TTS_SPEED, 1.0f)
    }

    fun setTtsSpeed(speed: Float) {
        val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
        preferences.edit().putFloat(KEY_TTS_SPEED, clampedSpeed).apply()
        _ttsSpeed.value = clampedSpeed
    }

    // STT Language Functions
    fun getSttLanguage(): String {
        return preferences.getString(KEY_STT_LANGUAGE, "en-US") ?: "en-US"
    }

    fun setSttLanguage(language: String) {
        preferences.edit().putString(KEY_STT_LANGUAGE, language).apply()
        _sttLanguage.value = language
    }

    // AI Model Configuration Functions
    fun getAiModelName(): String {
        return preferences.getString(KEY_AI_MODEL_NAME, "llama3:8b") ?: "llama3:8b"
    }

    fun setAiModelName(modelName: String) {
        preferences.edit().putString(KEY_AI_MODEL_NAME, modelName).apply()
        _aiModelName.value = modelName
    }

    // Advanced AI Settings
    fun getMaxResponseTokens(): Int {
        return preferences.getInt(KEY_MAX_RESPONSE_TOKENS, 2048)
    }

    fun setMaxResponseTokens(tokens: Int) {
        val clampedTokens = tokens.coerceIn(100, 4096)
        preferences.edit().putInt(KEY_MAX_RESPONSE_TOKENS, clampedTokens).apply()
    }

    fun getTemperature(): Float {
        return preferences.getFloat(KEY_TEMPERATURE, 0.7f)
    }

    fun setTemperature(temperature: Float) {
        val clampedTemp = temperature.coerceIn(0.1f, 2.0f)
        preferences.edit().putFloat(KEY_TEMPERATURE, clampedTemp).apply()
    }

    // UI Theme Functions
    fun getThemeMode(): String {
        return preferences.getString(KEY_THEME_MODE, "system") ?: "system"
    }

    fun setThemeMode(mode: String) {
        preferences.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    // Notification Functions
    fun isNotificationEnabled(): Boolean {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    fun setNotificationEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    // Auto Save Conversations
    fun isAutoSaveConversations(): Boolean {
        return preferences.getBoolean(KEY_AUTO_SAVE_CONVERSATIONS, true)
    }

    fun setAutoSaveConversations(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_AUTO_SAVE_CONVERSATIONS, enabled).apply()
    }

    // Utility Functions
    fun resetToDefaults() {
        preferences.edit().clear().apply()
        
        // Reset all StateFlow values to defaults
        _memoryEnabled.value = true
        _voiceOutputEnabled.value = true
        _voiceInputEnabled.value = true
        _wakeWordEnabled.value = false
        _autoActivateMic.value = true
        _streamingEnabled.value = true
        _conversationHistoryEnabled.value = true
        _backgroundListening.value = false
        _ttsSpeed.value = 1.0f
        _sttLanguage.value = "en-US"
        _aiModelName.value = "llama3:8b"
    }

    fun exportSettings(): Map<String, Any> {
        return mapOf(
            "memoryEnabled" to isMemoryEnabled(),
            "voiceOutputEnabled" to isVoiceOutputEnabled(),
            "voiceInputEnabled" to isVoiceInputEnabled(),
            "wakeWordEnabled" to isWakeWordEnabled(),
            "autoActivateMicEnabled" to isAutoActivateMicEnabled(),
            "streamingEnabled" to isStreamingEnabled(),
            "conversationHistoryEnabled" to isConversationHistoryEnabled(),
            "backgroundListening" to isBackgroundListening(),
            "ttsSpeed" to getTtsSpeed(),
            "sttLanguage" to getSttLanguage(),
            "aiModelName" to getAiModelName(),
            "maxResponseTokens" to getMaxResponseTokens(),
            "temperature" to getTemperature(),
            "themeMode" to getThemeMode(),
            "notificationEnabled" to isNotificationEnabled(),
            "autoSaveConversations" to isAutoSaveConversations()
        )
    }
}
