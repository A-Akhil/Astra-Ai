package com.example.aisecretary.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aisecretary.settings.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(application)

    private val _voiceInputEnabled = MutableStateFlow(settingsManager.isVoiceOutputEnabled())
    val voiceInputEnabled: StateFlow<Boolean> = _voiceInputEnabled.asStateFlow()
    
    private val _voiceOutputEnabled = MutableStateFlow(settingsManager.isVoiceOutputEnabled())
    val voiceOutputEnabled: StateFlow<Boolean> = _voiceOutputEnabled.asStateFlow()
    
    private val _memoryEnabled = MutableStateFlow(settingsManager.isMemoryEnabled())
    val memoryEnabled: StateFlow<Boolean> = _memoryEnabled.asStateFlow()
    
    private val _wakeWordEnabled = MutableStateFlow(settingsManager.isWakeWordEnabled())
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()
    
    private val _autoActivateMicEnabled = MutableStateFlow(settingsManager.isAutoActivateMicEnabled())
    val autoActivateMicEnabled: StateFlow<Boolean> = _autoActivateMicEnabled.asStateFlow()
    
    private val _darkModeEnabled = MutableStateFlow(settingsManager.isDarkModeEnabled())
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled.asStateFlow()
    
    private val _settingsUpdated = MutableStateFlow(false)
    val settingsUpdated: StateFlow<Boolean> = _settingsUpdated.asStateFlow()
    
    init {
        observeSettings()
    }
    
    private fun observeSettings() {
        viewModelScope.launch {
            settingsManager.voiceOutputEnabled.collectLatest {
                _voiceInputEnabled.value = it
            }
        }
        
        viewModelScope.launch {
            settingsManager.voiceOutputEnabled.collectLatest {
                _voiceOutputEnabled.value = it
            }
        }
        
        viewModelScope.launch {
            settingsManager.memoryEnabled.collectLatest {
                _memoryEnabled.value = it
            }
        }
        
        viewModelScope.launch {
            settingsManager.wakeWordEnabled.collectLatest {
                _wakeWordEnabled.value = it
            }
        }
        
        viewModelScope.launch {
            settingsManager.autoActivateMic.collectLatest {
                _autoActivateMicEnabled.value = it
            }
        }

        viewModelScope.launch {
            settingsManager.darkModeEnabled.collectLatest {
                _darkModeEnabled.value = it
            }
        }
    }
    
    fun setVoiceInputEnabled(enabled: Boolean) {
        _voiceInputEnabled.value = enabled
    }
    
    fun setVoiceOutputEnabled(enabled: Boolean) {
        _voiceOutputEnabled.value = enabled
    }
    
    fun setMemoryEnabled(enabled: Boolean) {
        _memoryEnabled.value = enabled
    }
    
    fun setWakeWordEnabled(enabled: Boolean) {
        _wakeWordEnabled.value = enabled
    }
    
    fun setAutoActivateMicEnabled(enabled: Boolean) {
        _autoActivateMicEnabled.value = enabled
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        _darkModeEnabled.value = enabled
    }
    
    fun saveSettings() {
        settingsManager.setVoiceOutputEnabled(_voiceInputEnabled.value)
        settingsManager.setVoiceOutputEnabled(_voiceOutputEnabled.value)
        settingsManager.setMemoryEnabled(_memoryEnabled.value)
        settingsManager.setWakeWordEnabled(_wakeWordEnabled.value)
        settingsManager.setAutoActivateMicEnabled(_autoActivateMicEnabled.value)
        settingsManager.setDarkModeEnabled(_darkModeEnabled.value)
        _settingsUpdated.value = true
    }
    
    fun resetSettingsUpdatedFlag() {
        _settingsUpdated.value = false
    }
}