package com.example.aisecretary.ai.memory

import com.example.aisecretary.data.local.database.dao.MemoryFactDao
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * Handles memory storage and retrieval for the AI assistant.
 *
 * This class is responsible for:
 * - Detecting memory instructions from user input or AI responses.
 * - Parsing key/value pairs from JSON or text.
 * - Saving memories to the database.
 * - Providing relevant memories based on the current conversation.
 *
 * @param memoryFactDao DAO for persisting memory facts in the database.
 * @param messageDao (Optional) DAO for accessing recent user messages, used for improving memory relevance.
 */
class MemoryManager(
    private val memoryFactDao: MemoryFactDao,
    private val messageDao: MessageDao? = null
) {
    // This helps find related past memories when needed
    private val memoryRetriever = MemoryRetriever()

    // Common sentence patterns used when users ask to remember something
    private val rememberPatterns = listOf(
        Pattern.compile("(?i)remember that (?:my|the) (.+?) (?:is|are) (.+)"),
        Pattern.compile("(?i)save (.+?) as (.+)"),
        Pattern.compile("(?i)store (.+?) as (.+)"),
        Pattern.compile("(?i)note that (?:my|the) (.+?) (?:is|are) (.+)"),
        Pattern.compile("(?i)my (.+?) (?:is|are) (.+)")
    )
    
    // Looks for any JSON object in a message (used when AI sends memories in JSON)
    private val jsonPattern = Pattern.compile("\\{(?:[^{}]|\\{[^{}]*\\})*\\}")

    /**
     * Detects and stores memory from user input using sentence patterns.
     *
     * @param message The user input string to analyze.
     * @return MemoryDetectionResult — contains detection status and parsed data.
     *
     * @sample "Remember that my favorite color is blue"
     * → key: favorite color, value: blue
     */
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
        // If nothing matched, return false
        return MemoryDetectionResult(wasMemoryDetected = false)
    }

    /**
     * This function tries to find memory information from the AI (LLM) response.
     *
     * It first checks if the response contains JSON data like:
     * { "key": "favorite food", "value": "pasta" }
     * If found, it saves the memory and returns it.
     *
     * If not, it checks the response using sentence patterns like:
     * "My birthday is June 5th"
     * and saves that memory too.
     *
     * @param response The full text response from the AI.
     * @return MemoryDetectionResult — tells whether memory was found and saved.
     */
    suspend fun detectAndExtractMemoryFromResponse(response: String): MemoryDetectionResult {
        // Try to extract memory from JSON in the response
        val jsonMemory = extractJsonMemory(response)
        if (jsonMemory != null) {
            val (key, value) = jsonMemory
            
            val memoryFact = MemoryFact(
                key = key,
                value = value
            )

            // Save the memory to the database
            memoryFactDao.insertMemoryFact(memoryFact)
            
            return MemoryDetectionResult(
                wasMemoryDetected = true,
                memoryKey = key,
                memoryValue = value,
                isFromJson = true
            )
        }

        // If no JSON found, try matching memory patterns in plain text
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
        // No memory found
        return MemoryDetectionResult(wasMemoryDetected = false)
    }

    /**
     * Tries to extract a memory from a JSON-like structure in the AI's response.
     *
     * Example valid JSON patterns it detects:
     * - { "memory": { "key": "hobby", "value": "painting" } }
     * - { "key": "birthday", "value": "Jan 1" }
     *
     * @param response The text response from AI that might contain memory in JSON format.
     * @return A Pair of memory key and value if found, or null if not found.
     */
    private fun extractJsonMemory(response: String): Pair<String, String>? {
        try {
            // Try to find JSON objects inside the response string
            val matcher = jsonPattern.matcher(response)
            while (matcher.find()) {
                val jsonString = matcher.group(0) ?: continue
                try {
                    // Convert the found string to a JSON object
                    val jsonObject = JSONObject(jsonString)

                    // Check if the JSON contains memory under "memory", "remember", or "store"
                    if (jsonObject.has("memory") || 
                        jsonObject.has("remember") || 
                        jsonObject.has("store")) {

                        // Get the nested memory object
                        val memoryObj = when {
                            jsonObject.has("memory") -> jsonObject.optJSONObject("memory")
                            jsonObject.has("remember") -> jsonObject.optJSONObject("remember")
                            jsonObject.has("store") -> jsonObject.optJSONObject("store")
                            else -> null
                        }

                        // If key and value are present inside the nested memory
                        if (memoryObj != null && memoryObj.has("key") && memoryObj.has("value")) {
                            val key = memoryObj.optString("key", "")
                            val value = memoryObj.optString("value", "")
                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                return Pair(key, value)
                            }
                        } else if (jsonObject.has("key") && jsonObject.has("value")) {
                            // Handle direct key-value format without "memory" wrapper
                            val key = jsonObject.optString("key", "")
                            val value = jsonObject.optString("value", "")
                            if (key.isNotEmpty() && value.isNotEmpty()) {
                                return Pair(key, value)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // If one JSON block fails, continue checking others
                    continue
                }
            }
        } catch (e: Exception) {
            // Error in JSON parsing, return null
            return null
        }
        // If nothing matched
        return null
    }

    /**
     * Fetches all memory facts stored in the database.
     *
     * @return A list of all MemoryFact items.
     */
    suspend fun getAllMemory(): List<MemoryFact> {
        return memoryFactDao.getAllMemoryFacts().first()
    }

    /**
     * Retrieves memory facts that are most relevant to the user's current query.
     *
     * This method also considers recent messages (if available) to improve context matching.
     *
     * @param query The input text to match memory facts against.
     * @return A list of relevant MemoryFact items based on the query and recent messages.
     */
    suspend fun getRelevantMemory(query: String): List<MemoryFact> {
        // Load all memory facts
        val allMemory = getAllMemory()

        // Try to fetch last 10 recent messages, if messageDao is available
        val recentMessages = messageDao?.let {
            it.getAllMessages().first().takeLast(10)
        } ?: emptyList()

        // Use the memoryRetriever to find matching memory based on query and context
        return memoryRetriever.retrieveRelevantMemory(
            query = query,
            allMemoryFacts = allMemory,
            recentMessages = recentMessages
        )
    }

    /**
     * Searches memory entries that match the given query string.
     *
     * @param query The search term to look for in memory facts.
     * @return A list of matching MemoryFact entries.
     */
    suspend fun searchMemory(query: String): List<MemoryFact> {
        return memoryFactDao.searchMemoryFacts(query).first()
    }

    /**
     * Updates an existing memory fact in the database.
     *
     * @param memoryFact The updated MemoryFact object to save.
     */
    suspend fun updateMemory(memoryFact: MemoryFact) {
        memoryFactDao.updateMemoryFact(memoryFact)
    }

    /**
     * Deletes a memory fact from the database by its ID.
     *
     * @param id The ID of the memory fact to delete.
     */
    suspend fun deleteMemory(id: Long) {
        memoryFactDao.deleteMemoryFact(id)
    }

    /**
     * Clears all memory facts from the database.
     */
    suspend fun clearAllMemory() {
        memoryFactDao.deleteAllMemoryFacts()
    }
}

/**
 * Result object returned after detecting memory from a response.
 *
 * @property wasMemoryDetected True if memory was successfully found and extracted.
 * @property memoryKey The key or title of the memory.
 * @property memoryValue The value or detail of the memory.
 * @property isFromJson True if memory was detected via a JSON structure.
 */
data class MemoryDetectionResult(
    val wasMemoryDetected: Boolean,
    val memoryKey: String = "",
    val memoryValue: String = "",
    val isFromJson: Boolean = false
)