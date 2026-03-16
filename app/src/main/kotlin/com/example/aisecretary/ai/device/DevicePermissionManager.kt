package com.example.aisecretary.ai.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Manages device control permissions and provides user explanations
 */
class DevicePermissionManager(private val context: Context) {
    
    /**
     * Check if all required permissions are granted
     */
    fun hasAllRequiredPermissions(): Boolean {
        return hasWriteSettingsPermission() &&
               hasBluetoothPermissions() &&
               hasWifiPermissions() &&
               hasAudioPermissions()
    }
    
    /**
     * Check if WRITE_SETTINGS permission is granted
     */
    fun hasWriteSettingsPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true // Pre-Marshmallow, permission is granted at install time
        }
    }
    
    /**
     * Check if Bluetooth permissions are granted
     */
    fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if WiFi permissions are granted
     */
    fun hasWifiPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Check if audio permissions are granted
     */
    fun hasAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get list of missing permissions
     */
    fun getMissingPermissions(): List<PermissionInfo> {
        val missingPermissions = mutableListOf<PermissionInfo>()
        
        if (!hasWriteSettingsPermission()) {
            missingPermissions.add(PermissionInfo.WRITE_SETTINGS)
        }
        
        if (!hasBluetoothPermissions()) {
            missingPermissions.add(PermissionInfo.BLUETOOTH)
        }
        
        if (!hasWifiPermissions()) {
            missingPermissions.add(PermissionInfo.WIFI)
        }
        
        if (!hasAudioPermissions()) {
            missingPermissions.add(PermissionInfo.AUDIO)
        }
        
        return missingPermissions
    }
    
    /**
     * Get intent to request WRITE_SETTINGS permission
     */
    fun getWriteSettingsPermissionIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    
    /**
     * Get intent to request Bluetooth permissions
     */
    fun getBluetoothPermissionIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    
    /**
     * Get intent to request WiFi permissions
     */
    fun getWifiPermissionIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    
    /**
     * Get intent to request audio permissions
     */
    fun getAudioPermissionIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    
    /**
     * Get permission explanation text
     */
    fun getPermissionExplanation(permission: PermissionInfo): String {
        return when (permission) {
            PermissionInfo.WRITE_SETTINGS -> {
                "This permission allows the AI assistant to control your device's screen brightness. " +
                "This is needed for voice commands like 'Set brightness to 50%'."
            }
            PermissionInfo.BLUETOOTH -> {
                "This permission allows the AI assistant to control your device's Bluetooth settings. " +
                "This is needed for voice commands like 'Turn on Bluetooth' or 'Turn off Bluetooth'."
            }
            PermissionInfo.WIFI -> {
                "This permission allows the AI assistant to control your device's WiFi settings. " +
                "This is needed for voice commands like 'Turn on WiFi' or 'Turn off WiFi'."
            }
            PermissionInfo.AUDIO -> {
                "This permission allows the AI assistant to control your device's volume settings. " +
                "This is needed for voice commands like 'Set volume to 50%' or 'Increase volume'."
            }
        }
    }
    
    /**
     * Get permission request instructions
     */
    fun getPermissionRequestInstructions(permission: PermissionInfo): String {
        return when (permission) {
            PermissionInfo.WRITE_SETTINGS -> {
                "To grant this permission:\n" +
                "1. Tap 'Grant Permission'\n" +
                "2. In the system settings, enable 'Allow modify system settings'\n" +
                "3. Return to the app"
            }
            PermissionInfo.BLUETOOTH -> {
                "To grant this permission:\n" +
                "1. Tap 'Grant Permission'\n" +
                "2. In the app settings, enable 'Bluetooth' and 'Bluetooth Admin'\n" +
                "3. Return to the app"
            }
            PermissionInfo.WIFI -> {
                "To grant this permission:\n" +
                "1. Tap 'Grant Permission'\n" +
                "2. In the app settings, enable 'Access WiFi State' and 'Change WiFi State'\n" +
                "3. Return to the app"
            }
            PermissionInfo.AUDIO -> {
                "To grant this permission:\n" +
                "1. Tap 'Grant Permission'\n" +
                "2. In the app settings, enable 'Modify Audio Settings'\n" +
                "3. Return to the app"
            }
        }
    }
    
    /**
     * Get permission intent for a specific permission
     */
    fun getPermissionIntent(permission: PermissionInfo): Intent {
        return when (permission) {
            PermissionInfo.WRITE_SETTINGS -> getWriteSettingsPermissionIntent()
            PermissionInfo.BLUETOOTH -> getBluetoothPermissionIntent()
            PermissionInfo.WIFI -> getWifiPermissionIntent()
            PermissionInfo.AUDIO -> getAudioPermissionIntent()
        }
    }
}

/**
 * Represents different types of permissions needed for device control
 */
enum class PermissionInfo {
    WRITE_SETTINGS,
    BLUETOOTH,
    WIFI,
    AUDIO
} 