package com.example.aisecretary.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Secure storage implementation using Android's EncryptedSharedPreferences
 * and additional AES encryption for sensitive data
 */
class SecureStorage(context: Context) {
    
    private val securityManager = SecurityManager(context)
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Store encrypted string data
     */
    suspend fun storeSecureString(key: String, value: String) = withContext(Dispatchers.IO) {
        val encryptedData = securityManager.encryptData(value)
        val serializedData = Json.encodeToString(
            SerializableEncryptedData(
                Base64.encodeToString(encryptedData.data, Base64.DEFAULT),
                Base64.encodeToString(encryptedData.iv, Base64.DEFAULT)
            )
        )
        encryptedPrefs.edit().putString(key, serializedData).apply()
    }

    /**
     * Retrieve and decrypt string data
     */
    suspend fun getSecureString(key: String): String? = withContext(Dispatchers.IO) {
        val serializedData = encryptedPrefs.getString(key, null) ?: return@withContext null
        try {
            val deserializedData = Json.decodeFromString<SerializableEncryptedData>(serializedData)
            val encryptedData = EncryptedData(
                Base64.decode(deserializedData.data, Base64.DEFAULT),
                Base64.decode(deserializedData.iv, Base64.DEFAULT)
            )
            securityManager.decryptData(encryptedData)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Store boolean preference
     */
    fun storeBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit().putBoolean(key, value).apply()
    }

    /**
     * Retrieve boolean preference
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }

    /**
     * Store integer preference
     */
    fun storeInt(key: String, value: Int) {
        encryptedPrefs.edit().putInt(key, value).apply()
    }

    /**
     * Retrieve integer preference
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return encryptedPrefs.getInt(key, defaultValue)
    }

    /**
     * Remove a key from secure storage
     */
    fun remove(key: String) {
        encryptedPrefs.edit().remove(key).apply()
    }

    /**
     * Clear all secure storage
     */
    fun clearAll() {
        encryptedPrefs.edit().clear().apply()
    }

    /**
     * Check if key exists
     */
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }

    @kotlinx.serialization.Serializable
    private data class SerializableEncryptedData(
        val data: String,
        val iv: String
    )
}
