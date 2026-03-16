package com.example.aisecretary.ai.device

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine

/**
 * Monitors device status and provides real-time feedback
 */
class DeviceStatusMonitor(private val context: Context) {
    
    private val deviceControlManager = DeviceControlManager(context)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    // State flows for device status
    private val _brightnessStatus = MutableStateFlow(DeviceStatus.Brightness(getCurrentBrightnessPercentage()))
    private val _volumeStatus = MutableStateFlow(DeviceStatus.Volume(getCurrentVolumePercentage()))
    private val _wifiStatus = MutableStateFlow(DeviceStatus.Wifi(isWifiEnabled()))
    private val _bluetoothStatus = MutableStateFlow(DeviceStatus.Bluetooth(isBluetoothEnabled()))
    
    // Combined status flow
    val deviceStatus: StateFlow<DeviceStatusSummary> = combine(
        _brightnessStatus,
        _volumeStatus,
        _wifiStatus,
        _bluetoothStatus
    ) { brightness, volume, wifi, bluetooth ->
        DeviceStatusSummary(
            brightness = brightness,
            volume = volume,
            wifi = wifi,
            bluetooth = bluetooth
        )
    }.asStateFlow()
    
    /**
     * Refresh all device status
     */
    fun refreshAllStatus() {
        _brightnessStatus.value = DeviceStatus.Brightness(getCurrentBrightnessPercentage())
        _volumeStatus.value = DeviceStatus.Volume(getCurrentVolumePercentage())
        _wifiStatus.value = DeviceStatus.Wifi(isWifiEnabled())
        _bluetoothStatus.value = DeviceStatus.Bluetooth(isBluetoothEnabled())
    }
    
    /**
     * Get current brightness percentage
     */
    private fun getCurrentBrightnessPercentage(): Int {
        return try {
            val brightness = Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
            (brightness * 100 / 255)
        } catch (e: Exception) {
            50 // Default 50%
        }
    }
    
    /**
     * Get current volume percentage
     */
    private fun getCurrentVolumePercentage(): Int {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return (currentVolume * 100 / maxVolume)
    }
    
    /**
     * Check if WiFi is enabled
     */
    private fun isWifiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }
    
    /**
     * Check if Bluetooth is enabled
     */
    private fun isBluetoothEnabled(): Boolean {
        return deviceControlManager.isBluetoothEnabled()
    }
    
    /**
     * Get status description for a specific device
     */
    fun getStatusDescription(deviceType: DeviceControlType): String {
        return when (deviceType) {
            DeviceControlType.BRIGHTNESS -> {
                val percentage = getCurrentBrightnessPercentage()
                "Screen brightness is set to $percentage%"
            }
            DeviceControlType.VOLUME -> {
                val percentage = getCurrentVolumePercentage()
                "Audio volume is set to $percentage%"
            }
            DeviceControlType.WIFI -> {
                val enabled = isWifiEnabled()
                "WiFi is ${if (enabled) "enabled" else "disabled"}"
            }
            DeviceControlType.BLUETOOTH -> {
                val enabled = isBluetoothEnabled()
                "Bluetooth is ${if (enabled) "enabled" else "disabled"}"
            }
        }
    }
    
    /**
     * Get comprehensive device status report
     */
    fun getDeviceStatusReport(): String {
        val brightness = getCurrentBrightnessPercentage()
        val volume = getCurrentVolumePercentage()
        val wifi = isWifiEnabled()
        val bluetooth = isBluetoothEnabled()
        
        return """
            Device Status Report:
            • Screen Brightness: $brightness%
            • Audio Volume: $volume%
            • WiFi: ${if (wifi) "Enabled" else "Disabled"}
            • Bluetooth: ${if (bluetooth) "Enabled" else "Disabled"}
        """.trimIndent()
    }
    
    /**
     * Check if device settings are optimal
     */
    fun getOptimalSettingsRecommendations(): List<String> {
        val recommendations = mutableListOf<String>()
        
        val brightness = getCurrentBrightnessPercentage()
        if (brightness > 80) {
            recommendations.add("Consider reducing brightness to save battery")
        } else if (brightness < 20) {
            recommendations.add("Consider increasing brightness for better visibility")
        }
        
        val volume = getCurrentVolumePercentage()
        if (volume > 90) {
            recommendations.add("High volume detected - consider lowering to protect hearing")
        }
        
        if (!isWifiEnabled()) {
            recommendations.add("WiFi is disabled - enable for better connectivity")
        }
        
        return recommendations
    }
}

/**
 * Represents different types of device status
 */
sealed class DeviceStatus {
    data class Brightness(val percentage: Int) : DeviceStatus()
    data class Volume(val percentage: Int) : DeviceStatus()
    data class Wifi(val enabled: Boolean) : DeviceStatus()
    data class Bluetooth(val enabled: Boolean) : DeviceStatus()
}

/**
 * Summary of all device status
 */
data class DeviceStatusSummary(
    val brightness: DeviceStatus.Brightness,
    val volume: DeviceStatus.Volume,
    val wifi: DeviceStatus.Wifi,
    val bluetooth: DeviceStatus.Bluetooth
) 