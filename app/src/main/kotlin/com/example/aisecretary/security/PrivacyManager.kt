package com.example.aisecretary.security

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Privacy manager for data anonymization, retention, and user control
 */
class PrivacyManager(
    private val context: Context,
    private val secureStorage: SecureStorage
) {
    
    companion object {
        private const val PRIVACY_SETTINGS_KEY = "privacy_settings"
        private const val DATA_RETENTION_DAYS_KEY = "data_retention_days"
        private const val ANONYMIZATION_ENABLED_KEY = "anonymization_enabled"
        private const val EXPORT_REQUESTS_KEY = "export_requests"
    }

    /**
     * Get current privacy settings
     */
    suspend fun getPrivacySettings(): PrivacySettings = withContext(Dispatchers.IO) {
        val settingsJson = secureStorage.getSecureString(PRIVACY_SETTINGS_KEY)
        if (settingsJson != null) {
            try {
                Json.decodeFromString<PrivacySettings>(settingsJson)
            } catch (e: Exception) {
                getDefaultPrivacySettings()
            }
        } else {
            getDefaultPrivacySettings()
        }
    }

    /**
     * Update privacy settings
     */
    suspend fun updatePrivacySettings(settings: PrivacySettings) = withContext(Dispatchers.IO) {
        val settingsJson = Json.encodeToString(settings)
        secureStorage.storeSecureString(PRIVACY_SETTINGS_KEY, settingsJson)
    }

    /**
     * Anonymize conversation data
     */
    fun anonymizeConversationData(data: String): String {
        var anonymizedData = data
        
        // Remove or replace personally identifiable information
        anonymizedData = anonymizedData.replace(Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), "[EMAIL_REDACTED]")
        anonymizedData = anonymizedData.replace(Regex("\\b\\d{3}-\\d{3}-\\d{4}\\b"), "[PHONE_REDACTED]")
        anonymizedData = anonymizedData.replace(Regex("\\b\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\b"), "[CARD_REDACTED]")
        anonymizedData = anonymizedData.replace(Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"), "[SSN_REDACTED]")
        
        // Replace potential names (simple heuristic)
        anonymizedData = anonymizedData.replace(Regex("\\b[A-Z][a-z]+ [A-Z][a-z]+\\b"), "[NAME_REDACTED]")
        
        return anonymizedData
    }

    /**
     * Export user data for GDPR compliance
     */
    suspend fun exportUserData(): ExportResult = withContext(Dispatchers.IO) {
        try {
            val exportData = UserDataExport(
                exportDate = Date(),
                privacySettings = getPrivacySettings(),
                conversationCount = getConversationCount(),
                memoryFactsCount = getMemoryFactsCount(),
                dataRetentionDays = secureStorage.getInt(DATA_RETENTION_DAYS_KEY, 30)
            )

            val exportJson = Json.encodeToString(exportData)
            val fileName = "astra_ai_data_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
            val file = File(context.filesDir, fileName)
            
            FileWriter(file).use { writer ->
                writer.write(exportJson)
            }

            ExportResult.Success(file.absolutePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    /**
     * Request account deletion
     */
    suspend fun requestAccountDeletion(): DeletionResult = withContext(Dispatchers.IO) {
        try {
            // Clear all stored data
            secureStorage.clearAll()
            
            // Clear app-specific directories
            clearAppData()
            
            DeletionResult.Success
        } catch (e: Exception) {
            DeletionResult.Error(e.message ?: "Failed to delete account data")
        }
    }

    /**
     * Check if data should be automatically deleted based on retention policy
     */
    suspend fun checkDataRetention() = withContext(Dispatchers.IO) {
        val retentionDays = secureStorage.getInt(DATA_RETENTION_DAYS_KEY, 30)
        val cutoffDate = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -retentionDays)
        }.time
        
        // Implementation would check database for old records and delete them
        // This would integrate with your existing database DAOs
    }

    /**
     * Generate privacy report
     */
    suspend fun generatePrivacyReport(): PrivacyReport = withContext(Dispatchers.IO) {
        val settings = getPrivacySettings()
        PrivacyReport(
            dataCollected = listOf(
                "Conversation messages",
                "Memory facts",
                "Voice recordings (if enabled)",
                "App preferences"
            ),
            dataShared = if (settings.allowAnalytics) listOf("Anonymous usage statistics") else emptyList(),
            retentionPeriod = "${secureStorage.getInt(DATA_RETENTION_DAYS_KEY, 30)} days",
            encryptionStatus = "AES-256 encryption enabled",
            lastUpdated = Date()
        )
    }

    private fun getDefaultPrivacySettings(): PrivacySettings {
        return PrivacySettings(
            allowAnalytics = false,
            allowCrashReports = true,
            anonymizeData = true,
            enableBiometric = true,
            dataRetentionDays = 30,
            autoDeleteOldData = true
        )
    }

    private fun clearAppData() {
        // Clear cache directories
        context.cacheDir.deleteRecursively()
        
        // Clear specific app directories
        val appDataDir = File(context.filesDir.parent)
        appDataDir.listFiles()?.forEach { file ->
            if (file.name != "lib") { // Keep native libraries
                file.deleteRecursively()
            }
        }
    }

    private suspend fun getConversationCount(): Int {
        // This would integrate with your message DAO
        return 0 // Placeholder
    }

    private suspend fun getMemoryFactsCount(): Int {
        // This would integrate with your memory DAO
        return 0 // Placeholder
    }

    @Serializable
    data class PrivacySettings(
        val allowAnalytics: Boolean,
        val allowCrashReports: Boolean,
        val anonymizeData: Boolean,
        val enableBiometric: Boolean,
        val dataRetentionDays: Int,
        val autoDeleteOldData: Boolean
    )

    @Serializable
    data class UserDataExport(
        val exportDate: Date,
        val privacySettings: PrivacySettings,
        val conversationCount: Int,
        val memoryFactsCount: Int,
        val dataRetentionDays: Int
    )

    data class PrivacyReport(
        val dataCollected: List<String>,
        val dataShared: List<String>,
        val retentionPeriod: String,
        val encryptionStatus: String,
        val lastUpdated: Date
    )

    sealed class ExportResult {
        data class Success(val filePath: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    sealed class DeletionResult {
        object Success : DeletionResult()
        data class Error(val message: String) : DeletionResult()
    }
}
