package com.example.aisecretary.data.repository

import com.example.aisecretary.ai.llm.LlamaClient
import com.example.aisecretary.ai.memory.MemoryManager
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.model.ConversationContext
import com.example.aisecretary.data.model.Message
import com.example.aisecretary.data.model.StreamingChunk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class ChatRepository(
    private val messageDao: MessageDao,
    private val llamaClient: LlamaClient,
    private val memoryManager: MemoryManager
) {
    // Get all messages from the database
    fun getAllMessages(): Flow<List<Message>> = messageDao.getAllMessages()

    // Save a message to the database
    suspend fun saveMessage(message: Message): Long {
        return messageDao.insertMessage(message)
    }

    // Process a user message and get AI response
    suspend fun processUserMessage(message: String, memoryEnabled: Boolean): Flow<ResponseResult> = flow {
        try {
            // Save user message first
            val userMessage = Message(
                content = message,
                isFromUser = true,
                timestamp = System.currentTimeMillis()
            )
            messageDao.insertMessage(userMessage)

            // Get conversation context if memory is enabled
            val context = if (memoryEnabled) {
                getRecentConversationContext()
            } else null

            // Start streaming response with sentence buffering
            var fullResponse = ""
            var sentenceBuffer = ""
            var lastSentenceEnd = 0

            llamaClient.sendStreamingMessage(message, context).collect { chunk ->
                fullResponse += chunk.content
                sentenceBuffer += chunk.content

                if (chunk.isComplete) {
                    // Save the complete AI response
                    val aiMessage = Message(
                        content = fullResponse,
                        isFromUser = false,
                        timestamp = System.currentTimeMillis()
                    )
                    messageDao.insertMessage(aiMessage)

                    // Extract and save memory if enabled and response contains important info
                    if (memoryEnabled && fullResponse.length > 50) {
                        extractAndSaveMemory(message, fullResponse)
                    }

                    // Emit final sentence if any remaining
                    if (sentenceBuffer.trim().isNotEmpty()) {
                        emit(ResponseResult.CompleteSentence(sentenceBuffer.trim()))
                        sentenceBuffer = ""
                    }

                    emit(ResponseResult.Content(fullResponse, isComplete = true))
                } else {
                    // Check for sentence boundaries
                    val sentences = detectCompleteSentences(sentenceBuffer)
                    if (sentences.isNotEmpty()) {
                        sentences.forEach { sentence ->
                            emit(ResponseResult.CompleteSentence(sentence))
                        }
                        // Keep remaining incomplete sentence in buffer
                        sentenceBuffer = getIncompleteSentence(sentenceBuffer)
                    }

                    emit(ResponseResult.Content(chunk.content, isComplete = false))
                }
            }
        } catch (e: Exception) {
            emit(ResponseResult.Error("Failed to process message: ${e.message}"))
        }
    }

    private fun detectCompleteSentences(text: String): List<String> {
        val sentences = mutableListOf<String>()
        val sentenceEnders = listOf('.', '!', '?')
        var lastEnd = 0

        for (i in text.indices) {
            if (sentenceEnders.contains(text[i])) {
                // Look ahead to see if this is really the end
                val nextChar = text.getOrNull(i + 1)
                if (nextChar == null || nextChar == ' ' || nextChar == '\n') {
                    val sentence = text.substring(lastEnd, i + 1).trim()
                    if (sentence.isNotEmpty() && sentence.length > 3) { // Avoid very short fragments
                        sentences.add(sentence)
                        lastEnd = i + 1
                    }
                }
            }
        }

        return sentences
    }

    private fun getIncompleteSentence(text: String): String {
        val sentenceEnders = listOf('.', '!', '?')
        var lastEnd = 0

        for (i in text.indices) {
            if (sentenceEnders.contains(text[i])) {
                val nextChar = text.getOrNull(i + 1)
                if (nextChar == null || nextChar == ' ' || nextChar == '\n') {
                    lastEnd = i + 1
                }
            }
        }

        return text.substring(lastEnd).trim()
    }

    // Remove JSON blocks from the response to make it more readable
    private fun cleanJsonFromResponse(response: String): String {
        // Improve pattern to find JSON blocks, even with nested structures
        // This finds the outermost JSON object only
        val jsonPattern = Regex("\\{(?:[^{}]|\\{[^{}]*\\})*\\}")
        return response.replace(jsonPattern, "")
            .replace(Regex("\\s+"), " ") // Clean up extra whitespace
            .trim()
    }

    // Clear all messages
    suspend fun clearAllMessages() {
        messageDao.deleteAllMessages()
    }

    sealed class ResponseResult {
        data class Success(val response: String) : ResponseResult()
        data class Error(val message: String) : ResponseResult()
        data class StreamingChunk(val chunk: String, val fullResponseSoFar: String) : ResponseResult()
        data class StreamingSentence(val sentence: String, val fullResponseSoFar: String, val isComplete: Boolean) : ResponseResult()
        data class StreamingComplete(val fullResponse: String, val sentences: List<String>) : ResponseResult()
        data class MemoryWarning(val message: String) : ResponseResult()
    }

    private suspend fun getRecentConversationContext(): ConversationContext {
        val recentMessages = messageDao.getAllMessages().first().takeLast(10)
        val relevantMemory = memoryManager.getRelevantMemory(recentMessages.joinToString(" ") { it.content })

        return ConversationContext(
            recentMessages = recentMessages,
            memoryFacts = relevantMemory
        )
    }

    private suspend fun extractAndSaveMemory(userMessage: String, aiResponse: String) {
        val responseMemoryResult = memoryManager.detectAndExtractMemoryFromResponse(aiResponse)

        if (responseMemoryResult.wasMemoryDetected && responseMemoryResult.isFromJson) {
            val cleanedResponse = cleanJsonFromResponse(aiResponse)
            // Here, you might want to save the cleaned response to the database instead of the original.
            val memoryKey = responseMemoryResult.memoryKey
            val memoryValue = responseMemoryResult.memoryValue
        }
    }
}
