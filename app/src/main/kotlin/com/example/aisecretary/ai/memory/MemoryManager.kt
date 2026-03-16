package com.example.aisecretary.ai.memory

import com.example.aisecretary.data.local.database.dao.MemoryFactDao
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.util.regex.Pattern

class MemoryManager(
    private val memoryFactDao: MemoryFactDao,
    private val messageDao: MessageDao? = null
) {
    // Enhanced retriever
    private val memoryRetriever = MemoryRetriever()
    
    // Patterns to detect memory statements
    private val rememberPatterns = listOf(
        Pattern.compile("(?i)remember that (?:my|the) (.+?) (?:is|are) (.+)"),
        Pattern.compile("(?i)save (.+?) as (.+)"),
        Pattern.compile("(?i)store (.+?) as (.+)"),
        Pattern.compile("(?i)note that (?:my|the) (.+?) (?:is|are) (.+)"),
        Pattern.compile("(?i)my (.+?) (?:is|are) (.+)")
    )
    
    // Device control patterns for voice recognition
    private val deviceControlPatterns = listOf(
        // Brightness patterns
        Pattern.compile("(?i)set brightness to (\\d+)%?"),
        Pattern.compile("(?i)brightness (\\d+)%?"),
        Pattern.compile("(?i)set screen brightness to (\\d+)%?"),
        Pattern.compile("(?i)adjust brightness to (\\d+)%?"),
        
        // Volume patterns
        Pattern.compile("(?i)set volume to (\\d+)%?"),
        Pattern.compile("(?i)volume (\\d+)%?"),
        Pattern.compile("(?i)set audio volume to (\\d+)%?"),
        Pattern.compile("(?i)adjust volume to (\\d+)%?"),
        Pattern.compile("(?i)(increase|decrease|turn up|turn down) volume (?:by )?(\\d+)?%?"),
        Pattern.compile("(?i)(raise|lower) volume (?:by )?(\\d+)?%?"),
        Pattern.compile("(?i)volume (up|down)(?: by (\\d+))?%?"),
        
        // WiFi patterns
        Pattern.compile("(?i)(turn on|enable|switch on) (?:the )?wifi"),
        Pattern.compile("(?i)(turn off|disable|switch off) (?:the )?wifi"),
        Pattern.compile("(?i)wifi (on|off)"),
        
        // Bluetooth patterns
        Pattern.compile("(?i)(turn on|enable|switch on) (?:the )?bluetooth"),
        Pattern.compile("(?i)(turn off|disable|switch off) (?:the )?bluetooth"),
        Pattern.compile("(?i)bluetooth (on|off)"),
        
        // Status patterns
        Pattern.compile("(?i)(what is|check|show) (?:the )?(brightness|volume|wifi|bluetooth) (?:status|level)?"),
        Pattern.compile("(?i)(brightness|volume|wifi|bluetooth) (?:status|level|info)")
    )
    
    // Pattern to detect JSON objects in text
    private val jsonPattern = Pattern.compile("\\{(?:[^{}]|\\{[^{}]*\\})*\\}")

    // Detect and extract memory from user message
    suspend fun detectAndExtractMemory(message: String): MemoryDetectionResult {
        for (pattern in rememberPatterns) {
            val matcher = pattern.matcher(message)
            if (matcher.find()) {
                val key = matcher.group(1)?.trim() ?: continue
                val value = matcher.group(2)?.trim() ?: continue
                
                val memoryFact = MemoryFact(
                    key = key,
                    value = value
                )
                
                // Save to database
                memoryFactDao.insertMemoryFact(memoryFact)
                
                return MemoryDetectionResult(
                    wasMemoryDetected = true,
                    memoryKey = key,
                    memoryValue = value
                )
            }
        }
        
        return MemoryDetectionResult(wasMemoryDetected = false)
    }
    
    // Detect and extract memory from LLM response
    suspend fun detectAndExtractMemoryFromResponse(response: String): MemoryDetectionResult {
        // First try to extract JSON content
        val jsonMemory = extractJsonMemory(response)
        if (jsonMemory != null) {
            val (key, value) = jsonMemory
            
            val memoryFact = MemoryFact(
                key = key,
                value = value
            )
            
            // Save to database
            memoryFactDao.insertMemoryFact(memoryFact)
            
            return MemoryDetectionResult(
                wasMemoryDetected = true,
                memoryKey = key,
                memoryValue = value,
                isFromJson = true
            )
        }
        
        // Fallback to regex patterns if no JSON is found
        for (pattern in rememberPatterns) {
            val matcher = pattern.matcher(response)
            if (matcher.find()) {
                val key = matcher.group(1)?.trim() ?: continue
                val value = matcher.group(2)?.trim() ?: continue
                
                val memoryFact = MemoryFact(
                    key = key,
                    value = value
                )
                
                // Save to database
                memoryFactDao.insertMemoryFact(memoryFact)
                
                return MemoryDetectionResult(
                    wasMemoryDetected = true,
                    memoryKey = key,
                    memoryValue = value
                )
            }
        }
        
        return MemoryDetectionResult(wasMemoryDetected = false)
    }
    
    // Extract memory from JSON in the response
    private fun extractJsonMemory(response: String): Pair<String, String>? {
        try {
            // Find potential JSON objects
            val matcher = jsonPattern.matcher(response)
            while (matcher.find()) {
                val jsonString = matcher.group(0) ?: continue
                try {
                    val jsonObject = JSONObject(jsonString)
                    
                    // Check if this JSON is a memory entry
                    if (jsonObject.has("memory") || 
                        jsonObject.has("remember") || 
                        jsonObject.has("store")) {
                        
                        // Extract memory data
                        val memoryObj = when {
                            jsonObject.has("memory") -> jsonObject.optJSONObject("memory")
                            jsonObject.has("remember") -> jsonObject.optJSONObject("remember")
                            jsonObject.has("store") -> jsonObject.optJSONObject("store")
                            else -> null
                        }
                        
                        if (memoryObj != null && memoryObj.has("key") && memoryObj.has("value")) {
                            val key = memoryObj.optString("key", "")
                            val value = memoryObj.optString("value", "")
                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                return Pair(key, value)
                            }
                        } else if (jsonObject.has("key") && jsonObject.has("value")) {
                            // Direct key-value format
                            val key = jsonObject.optString("key", "")
                            val value = jsonObject.optString("value", "")
                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                return Pair(key, value)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Invalid JSON, continue searching
                    continue
                }
            }
        } catch (e: Exception) {
            // Error in JSON parsing, return null
            return null
        }
        return null
    }

    // Get all memory facts
    suspend fun getAllMemory(): List<MemoryFact> {
        return memoryFactDao.getAllMemoryFacts().first()
    }

    // Get memory facts relevant to a query using enhanced retrieval
    suspend fun getRelevantMemory(query: String): List<MemoryFact> {
        val allMemory = getAllMemory()
        
        // Get recent messages for context if messageDao is available
        val recentMessages = messageDao?.let {
            it.getAllMessages().first().takeLast(10)
        } ?: emptyList()
        
        return memoryRetriever.retrieveRelevantMemory(
            query = query,
            allMemoryFacts = allMemory,
            recentMessages = recentMessages
        )
    }

    // Search memory with a specific query
    suspend fun searchMemory(query: String): List<MemoryFact> {
        return memoryFactDao.searchMemoryFacts(query).first()
    }

    // Update an existing memory fact
    suspend fun updateMemory(memoryFact: MemoryFact) {
        memoryFactDao.updateMemoryFact(memoryFact)
    }

    // Delete a specific memory fact
    suspend fun deleteMemory(id: Long) {
        memoryFactDao.deleteMemoryFact(id)
    }

    // Clear all memory
    suspend fun clearAllMemory() {
        memoryFactDao.deleteAllMemoryFacts()
    }
    
    /**
     * Detect if a message contains device control commands
     */
    fun isDeviceControlCommand(message: String): Boolean {
        return deviceControlPatterns.any { pattern ->
            pattern.matcher(message).find()
        }
    }
    
    /**
     * Get device control command type from message
     */
    fun getDeviceControlCommandType(message: String): DeviceControlType? {
        val lowerMessage = message.lowercase()
        
        return when {
            lowerMessage.contains("brightness") -> DeviceControlType.BRIGHTNESS
            lowerMessage.contains("volume") -> DeviceControlType.VOLUME
            lowerMessage.contains("wifi") -> DeviceControlType.WIFI
            lowerMessage.contains("bluetooth") -> DeviceControlType.BLUETOOTH
            else -> null
        }
    }
}

/**
 * Types of device control commands
 */
enum class DeviceControlType {
    BRIGHTNESS,
    VOLUME,
    WIFI,
    BLUETOOTH
}

data class MemoryDetectionResult(
    val wasMemoryDetected: Boolean,
    val memoryKey: String = "",
    val memoryValue: String = "",
    val isFromJson: Boolean = false
)