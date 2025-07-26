package com.example.aisecretary.ai.device

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.regex.Pattern

/**
 * Processes voice commands for device control operations
 */
class DeviceVoiceProcessor(private val context: Context) {
    
    private val deviceControlManager = DeviceControlManager(context)
    
    // State flow for processing status
    private val _processingState = MutableStateFlow<DeviceProcessingState>(DeviceProcessingState.Idle)
    val processingState: StateFlow<DeviceProcessingState> = _processingState.asStateFlow()
    
    // Voice command patterns
    private val brightnessPatterns = listOf(
        Pattern.compile("(?i)set brightness to (\\d+)%?"),
        Pattern.compile("(?i)brightness (\\d+)%?"),
        Pattern.compile("(?i)set screen brightness to (\\d+)%?"),
        Pattern.compile("(?i)adjust brightness to (\\d+)%?")
    )
    
    private val volumePatterns = listOf(
        Pattern.compile("(?i)set volume to (\\d+)%?"),
        Pattern.compile("(?i)volume (\\d+)%?"),
        Pattern.compile("(?i)set audio volume to (\\d+)%?"),
        Pattern.compile("(?i)adjust volume to (\\d+)%?")
    )
    
    private val volumeAdjustPatterns = listOf(
        Pattern.compile("(?i)(increase|decrease|turn up|turn down) volume (?:by )?(\\d+)?%?"),
        Pattern.compile("(?i)(raise|lower) volume (?:by )?(\\d+)?%?"),
        Pattern.compile("(?i)volume (up|down)(?: by (\\d+))?%?")
    )
    
    private val wifiPatterns = listOf(
        Pattern.compile("(?i)(turn on|enable|switch on) (?:the )?wifi"),
        Pattern.compile("(?i)(turn off|disable|switch off) (?:the )?wifi"),
        Pattern.compile("(?i)wifi (on|off)")
    )
    
    private val bluetoothPatterns = listOf(
        Pattern.compile("(?i)(turn on|enable|switch on) (?:the )?bluetooth"),
        Pattern.compile("(?i)(turn off|disable|switch off) (?:the )?bluetooth"),
        Pattern.compile("(?i)bluetooth (on|off)")
    )
    
    private val statusPatterns = listOf(
        Pattern.compile("(?i)(what is|check|show) (?:the )?(brightness|volume|wifi|bluetooth) (?:status|level)?"),
        Pattern.compile("(?i)(brightness|volume|wifi|bluetooth) (?:status|level|info)")
    )
    
    /**
     * Process voice command and execute device control operation
     */
    suspend fun processVoiceCommand(command: String): DeviceCommandResult {
        _processingState.value = DeviceProcessingState.Processing
        
        return try {
            // Check for brightness commands
            val brightnessResult = processBrightnessCommand(command)
            if (brightnessResult != null) {
                _processingState.value = DeviceProcessingState.Completed
                return brightnessResult
            }
            
            // Check for volume commands
            val volumeResult = processVolumeCommand(command)
            if (volumeResult != null) {
                _processingState.value = DeviceProcessingState.Completed
                return volumeResult
            }
            
            // Check for WiFi commands
            val wifiResult = processWifiCommand(command)
            if (wifiResult != null) {
                _processingState.value = DeviceProcessingState.Completed
                return wifiResult
            }
            
            // Check for Bluetooth commands
            val bluetoothResult = processBluetoothCommand(command)
            if (bluetoothResult != null) {
                _processingState.value = DeviceProcessingState.Completed
                return bluetoothResult
            }
            
            // Check for status commands
            val statusResult = processStatusCommand(command)
            if (statusResult != null) {
                _processingState.value = DeviceProcessingState.Completed
                return statusResult
            }
            
            _processingState.value = DeviceProcessingState.Completed
            DeviceCommandResult.NoMatch("No device control command recognized")
            
        } catch (e: Exception) {
            _processingState.value = DeviceProcessingState.Error(e.message ?: "Unknown error")
            DeviceCommandResult.Error("Failed to process command: ${e.message}")
        }
    }
    
    private fun processBrightnessCommand(command: String): DeviceCommandResult? {
        for (pattern in brightnessPatterns) {
            val matcher = pattern.matcher(command)
            if (matcher.find()) {
                val level = matcher.group(1)?.toIntOrNull()
                if (level != null && level in 0..100) {
                    val brightnessLevel = (level * 255 / 100)
                    val result = deviceControlManager.setBrightness(brightnessLevel)
                    
                    return when (result) {
                        is DeviceControlResult.Success -> DeviceCommandResult.Success(result.message)
                        is DeviceControlResult.Error -> DeviceCommandResult.Error(result.message)
                        is DeviceControlResult.RequiresUserAction -> DeviceCommandResult.RequiresUserAction(
                            result.message, result.intent
                        )
                    }
                }
            }
        }
        return null
    }
    
    private fun processVolumeCommand(command: String): DeviceCommandResult? {
        // Check for absolute volume setting
        for (pattern in volumePatterns) {
            val matcher = pattern.matcher(command)
            if (matcher.find()) {
                val level = matcher.group(1)?.toIntOrNull()
                if (level != null && level in 0..100) {
                    val result = deviceControlManager.setVolume(level)
                    
                    return when (result) {
                        is DeviceControlResult.Success -> DeviceCommandResult.Success(result.message)
                        is DeviceControlResult.Error -> DeviceCommandResult.Error(result.message)
                        is DeviceControlResult.RequiresUserAction -> DeviceCommandResult.RequiresUserAction(
                            result.message, result.intent
                        )
                    }
                }
            }
        }
        
        // Check for volume adjustment
        for (pattern in volumeAdjustPatterns) {
            val matcher = pattern.matcher(command)
            if (matcher.find()) {
                val action = matcher.group(1)?.lowercase()
                val percentage = matcher.group(2)?.toIntOrNull() ?: 10 // Default 10%
                
                val result = when {
                    action?.contains("increase") == true || action?.contains("up") == true || action?.contains("raise") == true -> {
                        deviceControlManager.increaseVolume(percentage)
                    }
                    action?.contains("decrease") == true || action?.contains("down") == true || action?.contains("lower") == true -> {
                        deviceControlManager.decreaseVolume(percentage)
                    }
                    else -> null
                }
                
                if (result != null) {
                    return when (result) {
                        is DeviceControlResult.Success -> DeviceCommandResult.Success(result.message)
                        is DeviceControlResult.Error -> DeviceCommandResult.Error(result.message)
                        is DeviceControlResult.RequiresUserAction -> DeviceCommandResult.RequiresUserAction(
                            result.message, result.intent
                        )
                    }
                }
            }
        }
        
        return null
    }
    
    private fun processWifiCommand(command: String): DeviceCommandResult? {
        for (pattern in wifiPatterns) {
            val matcher = pattern.matcher(command)
            if (matcher.find()) {
                val action = matcher.group(1)?.lowercase()
                val enabled = when {
                    action?.contains("on") == true || action?.contains("enable") == true -> true
                    action?.contains("off") == true || action?.contains("disable") == true -> false
                    else -> null
                }
                
                if (enabled != null) {
                    val result = deviceControlManager.setWifiEnabled(enabled)
                    
                    return when (result) {
                        is DeviceControlResult.Success -> DeviceCommandResult.Success(result.message)
                        is DeviceControlResult.Error -> DeviceCommandResult.Error(result.message)
                        is DeviceControlResult.RequiresUserAction -> DeviceCommandResult.RequiresUserAction(
                            result.message, result.intent
                        )
                    }
                }
            }
        }
        return null
    }
    
    private fun processBluetoothCommand(command: String): DeviceCommandResult? {
        for (pattern in bluetoothPatterns) {
            val matcher = pattern.matcher(command)
            if (matcher.find()) {
                val action = matcher.group(1)?.lowercase()
                val enabled = when {
                    action?.contains("on") == true || action?.contains("enable") == true -> true
                    action?.contains("off") == true || action?.contains("disable") == true -> false
                    else -> null
                }
                
                if (enabled != null) {
                    val result = deviceControlManager.setBluetoothEnabled(enabled)
                    
                    return when (result) {
                        is DeviceControlResult.Success -> DeviceCommandResult.Success(result.message)
                        is DeviceControlResult.Error -> DeviceCommandResult.Error(result.message)
                        is DeviceControlResult.RequiresUserAction -> DeviceCommandResult.RequiresUserAction(
                            result.message, result.intent
                        )
                    }
                }
            }
        }
        return null
    }
    
    private fun processStatusCommand(command: String): DeviceCommandResult? {
        for (pattern in statusPatterns) {
            val matcher = pattern.matcher(command)
            if (matcher.find()) {
                val device = matcher.group(1)?.lowercase()
                
                val status = when (device) {
                    "brightness" -> {
                        val level = deviceControlManager.getCurrentBrightness()
                        val percentage = (level * 100 / 255)
                        "Brightness is set to $percentage%"
                    }
                    "volume" -> {
                        val level = deviceControlManager.getCurrentVolume()
                        "Volume is set to $level%"
                    }
                    "wifi" -> {
                        val enabled = deviceControlManager.isWifiEnabled()
                        "WiFi is ${if (enabled) "enabled" else "disabled"}"
                    }
                    "bluetooth" -> {
                        val enabled = deviceControlManager.isBluetoothEnabled()
                        "Bluetooth is ${if (enabled) "enabled" else "disabled"}"
                    }
                    else -> null
                }
                
                if (status != null) {
                    return DeviceCommandResult.Success(status)
                }
            }
        }
        return null
    }
    
    /**
     * Get device control manager for direct access
     */
    fun getDeviceControlManager(): DeviceControlManager {
        return deviceControlManager
    }
    
    /**
     * Check if command is a device control command
     */
    fun isDeviceControlCommand(command: String): Boolean {
        val allPatterns = brightnessPatterns + volumePatterns + volumeAdjustPatterns + 
                         wifiPatterns + bluetoothPatterns + statusPatterns
        
        return allPatterns.any { pattern ->
            pattern.matcher(command).find()
        }
    }
}

/**
 * Result of device command processing
 */
sealed class DeviceCommandResult {
    data class Success(val message: String) : DeviceCommandResult()
    data class Error(val message: String) : DeviceCommandResult()
    data class RequiresUserAction(val message: String, val intent: Intent) : DeviceCommandResult()
    data class NoMatch(val message: String) : DeviceCommandResult()
}

/**
 * State of device command processing
 */
sealed class DeviceProcessingState {
    object Idle : DeviceProcessingState()
    object Processing : DeviceProcessingState()
    object Completed : DeviceProcessingState()
    data class Error(val message: String?) : DeviceProcessingState()
} 