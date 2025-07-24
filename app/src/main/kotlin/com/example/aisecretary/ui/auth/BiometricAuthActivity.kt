package com.example.aisecretary.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aisecretary.MainActivity
import com.example.aisecretary.R
import com.example.aisecretary.security.SecurityManager
import com.example.aisecretary.security.BiometricAuthStatus
import com.example.aisecretary.data.local.preferences.SettingsManager
import kotlinx.coroutines.launch

/**
 * Activity for biometric authentication before app access
 */
class BiometricAuthActivity : AppCompatActivity() {
    
    private lateinit var securityManager: SecurityManager
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric_auth)
        
        securityManager = SecurityManager(this)
        settingsManager = SettingsManager(this)
        
        // Check if biometric authentication is enabled in settings
        if (!settingsManager.getBiometricEnabled()) {
            // If biometric is disabled, proceed to main activity
            proceedToMainActivity()
            return
        }
        
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        when (securityManager.isBiometricAvailable()) {
            BiometricAuthStatus.AVAILABLE -> {
                promptBiometricAuth()
            }
            BiometricAuthStatus.NO_HARDWARE -> {
                Toast.makeText(this, "Biometric hardware not available", Toast.LENGTH_LONG).show()
                proceedToMainActivity()
            }
            BiometricAuthStatus.HARDWARE_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware temporarily unavailable", Toast.LENGTH_LONG).show()
                proceedToMainActivity()
            }
            BiometricAuthStatus.NONE_ENROLLED -> {
                Toast.makeText(this, "No biometric credentials enrolled", Toast.LENGTH_LONG).show()
                proceedToMainActivity()
            }
            BiometricAuthStatus.UNKNOWN_ERROR -> {
                Toast.makeText(this, "Biometric authentication error", Toast.LENGTH_LONG).show()
                proceedToMainActivity()
            }
        }
    }

    private fun promptBiometricAuth() {
        securityManager.authenticateWithBiometrics(
            activity = this,
            onSuccess = {
                lifecycleScope.launch {
                    proceedToMainActivity()
                }
            },
            onError = { errorMessage ->
                Toast.makeText(this, "Authentication failed: $errorMessage", Toast.LENGTH_LONG).show()
                finish() // Close app if authentication fails
            }
        )
    }

    private fun proceedToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Prevent going back from biometric auth
        finish()
    }
}
