package com.example.aisecretary.data.repository

import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.ai.memory.MemoryManager
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.model.ConversationContext
import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Handles chat-related operations like saving messages,
 * getting replies from the AI, and managing memory.
 *
 * @param messageDao Used to access and store messages in the database.
 * @param llamaClient Sends user messages to the AI model and gets responses.
 * @param memoryManager Handles memory detection and storage for user preferences or facts.
 */
class ChatRepository(
    private val messageDao: MessageDao,
    private val llamaClient: LlamaClient,
    private val memoryManager: MemoryManager
) {
    /**
     * Gets all saved messages from the database.
     *
     * @return A flow of message list (updates in real-time).
     */
    fun getAllMessages(): Flow<List<Message>> = messageDao.getAllMessages()

    /**
     * Saves a message to the database.
     *
     * @param message The message to save.
     * @return The ID of the inserted message.
     */

    suspend fun saveMessage(message: Message): Long {
        return messageDao.insertMessage(message)
    }

    /**
     * Sends the user's message to the AI and returns the reply.
     * Also checks for memory-related instructions in the message or response.
     *
     * @param userMessage The user's input message.
     * @param memoryEnabled Whether to use memory (default is true).
     * @return The AI's response or an error.
     */
    suspend fun processUserMessage(userMessage: String, memoryEnabled: Boolean = true): Result<String> {
        // Skip memory processing if disabled
        if (!memoryEnabled) {
            // Just send message to LLM without memory context
            return llamaClient.sendMessage(
                message = userMessage,
                context = ConversationContext(
                    recentMessages = messageDao.getAllMessages().first().takeLast(10),
                    memoryFacts = emptyList()
                )
            )
        }
        
        // Check if the user message contains a memory request
        val memoryDetectionResult = memoryManager.detectAndExtractMemory(userMessage)
        
        if (memoryDetectionResult.wasMemoryDetected) {
            return Result.success("I've remembered that ${memoryDetectionResult.memoryKey} is ${memoryDetectionResult.memoryValue}.")
        }

        // Get recent messages and related memory for better AI context
        val recentMessages = messageDao.getAllMessages().first().takeLast(10)
        val relevantMemory = memoryManager.getRelevantMemory(userMessage)
        
        val context = ConversationContext(
            recentMessages = recentMessages,
            memoryFacts = relevantMemory
        )

        // Send message to LLM
        val llmResult = llamaClient.sendMessage(
            message = userMessage,
            context = context
        )

        // Check if AI replied with memory-saving instructions
        if (llmResult.isSuccess) {
            val response = llmResult.getOrNull() ?: return llmResult
            
            // Analyze the LLM response for memory instructions
            val responseMemoryResult = memoryManager.detectAndExtractMemoryFromResponse(response)
            
            // If memory was detected in the response, we'll still return the original response
            // but the memory has now been saved to the database
            return if (responseMemoryResult.wasMemoryDetected && responseMemoryResult.isFromJson) {
                // For JSON memory detection, we might want to clean up the response 
                // by removing the raw JSON to make it more readable
                val cleanedResponse = cleanJsonFromResponse(response)
                Result.success(cleanedResponse)
            } else {
                // Return the original response
                llmResult
            }
        }
        
        return llmResult
    }

    /**
     * Cleans JSON blocks (used for memory saving) from the AI's response
     * so it looks better when shown to the user.
     *
     * @param response The raw AI response.
     * @return The cleaned response without JSON.
     */
    private fun cleanJsonFromResponse(response: String): String {
        // Improve pattern to find JSON blocks, even with nested structures
        // This finds the outermost JSON object only
        val jsonPattern = Regex("\\{(?:[^{}]|\\{[^{}]*\\})*\\}")
        return response.replace(jsonPattern, "")
            .replace(Regex("\\s+"), " ") // Clean up extra whitespace
            .trim()
    }

    /**
     * Deletes all messages from the database.
     */
    suspend fun clearAllMessages() {
        messageDao.deleteAllMessages()
    }
}