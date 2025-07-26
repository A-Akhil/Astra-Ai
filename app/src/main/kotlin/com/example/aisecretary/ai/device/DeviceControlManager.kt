package com.example.aisecretary.ai.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages device control operations including brightness, volume, WiFi, and Bluetooth
 * with proper permission handling and security confirmations.
 */
class DeviceControlManager(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    
    // State flows for device status monitoring
    private val _brightnessLevel = MutableStateFlow(getCurrentBrightness())
    val brightnessLevel: StateFlow<Int> = _brightnessLevel.asStateFlow()
    
    private val _volumeLevel = MutableStateFlow(getCurrentVolume())
    val volumeLevel: StateFlow<Int> = _volumeLevel.asStateFlow()
    
    private val _wifiEnabled = MutableStateFlow(isWifiEnabled())
    val wifiEnabled: StateFlow<Boolean> = _wifiEnabled.asStateFlow()
    
    private val _bluetoothEnabled = MutableStateFlow(isBluetoothEnabled())
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()
    
    // Audit log file
    private val auditLogFile = File(context.filesDir, "device_control_audit.log")
    
    /**
     * Set screen brightness (0-255)
     */
    fun setBrightness(level: Int): DeviceControlResult {
        return try {
            if (!hasWriteSettingsPermission()) {
                return DeviceControlResult.Error("WRITE_SETTINGS permission required")
            }
            
            val clampedLevel = level.coerceIn(0, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, clampedLevel)
            
            _brightnessLevel.value = clampedLevel
            logAuditEvent("BRIGHTNESS", "Set to $clampedLevel")
            
            DeviceControlResult.Success("Brightness set to ${(clampedLevel * 100 / 255)}%")
        } catch (e: Exception) {
            val error = "Failed to set brightness: ${e.message}"
            logAuditEvent("BRIGHTNESS_ERROR", error)
            DeviceControlResult.Error(error)
        }
    }
    
    /**
     * Set volume level (0-100)
     */
    fun setVolume(level: Int): DeviceControlResult {
        return try {
            val clampedLevel = level.coerceIn(0, 100)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumeLevel = (clampedLevel * maxVolume / 100)
            
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0)
            
            _volumeLevel.value = clampedLevel
            logAuditEvent("VOLUME", "Set to $clampedLevel%")
            
            DeviceControlResult.Success("Volume set to $clampedLevel%")
        } catch (e: Exception) {
            val error = "Failed to set volume: ${e.message}"
            logAuditEvent("VOLUME_ERROR", error)
            DeviceControlResult.Error(error)
        }
    }
    
    /**
     * Increase volume by specified percentage
     */
    fun increaseVolume(percentage: Int): DeviceControlResult {
        val currentVolume = getCurrentVolume()
        val newVolume = (currentVolume + percentage).coerceIn(0, 100)
        return setVolume(newVolume)
    }
    
    /**
     * Decrease volume by specified percentage
     */
    fun decreaseVolume(percentage: Int): DeviceControlResult {
        val currentVolume = getCurrentVolume()
        val newVolume = (currentVolume - percentage).coerceIn(0, 100)
        return setVolume(newVolume)
    }
    
    /**
     * Enable or disable WiFi
     */
    fun setWifiEnabled(enabled: Boolean): DeviceControlResult {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10+, we need to guide user to system settings
                return DeviceControlResult.RequiresUserAction(
                    "Please enable/disable WiFi manually in system settings",
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                )
            }
            
            wifiManager.isWifiEnabled = enabled
            _wifiEnabled.value = enabled
            logAuditEvent("WIFI", if (enabled) "Enabled" else "Disabled")
            
            DeviceControlResult.Success("WiFi ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            val error = "Failed to ${if (enabled) "enable" else "disable"} WiFi: ${e.message}"
            logAuditEvent("WIFI_ERROR", error)
            DeviceControlResult.Error(error)
        }
    }
    
    /**
     * Enable or disable Bluetooth
     */
    fun setBluetoothEnabled(enabled: Boolean): DeviceControlResult {
        return try {
            if (bluetoothAdapter == null) {
                return DeviceControlResult.Error("Bluetooth not available on this device")
            }
            
            if (enabled) {
                if (!bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.enable()
                }
            } else {
                if (bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.disable()
                }
            }
            
            _bluetoothEnabled.value = enabled
            logAuditEvent("BLUETOOTH", if (enabled) "Enabled" else "Disabled")
            
            DeviceControlResult.Success("Bluetooth ${if (enabled) "enabled" else "disabled"}")
        } catch (e: Exception) {
            val error = "Failed to ${if (enabled) "enable" else "disable"} Bluetooth: ${e.message}"
            logAuditEvent("BLUETOOTH_ERROR", error)
            DeviceControlResult.Error(error)
        }
    }
    
    /**
     * Get current brightness level
     */
    fun getCurrentBrightness(): Int {
        return try {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            128 // Default brightness
        }
    }
    
    /**
     * Get current volume level (0-100)
     */
    fun getCurrentVolume(): Int {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return (currentVolume * 100 / maxVolume)
    }
    
    /**
     * Check if WiFi is enabled
     */
    fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }
    
    /**
     * Check if app has WRITE_SETTINGS permission
     */
    fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true // Pre-Marshmallow, permission is granted at install time
        }
    }
    
    /**
     * Request WRITE_SETTINGS permission
     */
    fun requestWriteSettingsPermission(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
            Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
        } else {
            null
        }
    }
    
    /**
     * Log audit event for security tracking
     */
    private fun logAuditEvent(action: String, details: String) {
        try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val logEntry = "[$timestamp] $action: $details\n"
            
            FileWriter(auditLogFile, true).use { writer ->
                writer.append(logEntry)
            }
        } catch (e: Exception) {
            Log.e("DeviceControlManager", "Failed to log audit event", e)
        }
    }
    
    /**
     * Get audit log entries
     */
    fun getAuditLog(): String {
        return try {
            if (auditLogFile.exists()) {
                auditLogFile.readText()
            } else {
                "No audit log available"
            }
        } catch (e: Exception) {
            "Error reading audit log: ${e.message}"
        }
    }
    
    /**
     * Clear audit log
     */
    fun clearAuditLog() {
        try {
            if (auditLogFile.exists()) {
                auditLogFile.delete()
            }
        } catch (e: Exception) {
            Log.e("DeviceControlManager", "Failed to clear audit log", e)
        }
    }
    
    /**
     * Refresh device status
     */
    fun refreshDeviceStatus() {
        _brightnessLevel.value = getCurrentBrightness()
        _volumeLevel.value = getCurrentVolume()
        _wifiEnabled.value = isWifiEnabled()
        _bluetoothEnabled.value = isBluetoothEnabled()
    }
}

/**
 * Result of device control operations
 */
sealed class DeviceControlResult {
    data class Success(val message: String) : DeviceControlResult()
    data class Error(val message: String) : DeviceControlResult()
    data class RequiresUserAction(val message: String, val intent: Intent) : DeviceControlResult()
} 