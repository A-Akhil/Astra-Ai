package com.example.aisecretary.data.local.database.dao

import com.example.aisecretary.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Extension functions for MessageDao to support streaming operations
 */

/**
 * Save a streaming message and emit the updated message list
 */
suspend fun MessageDao.saveStreamingMessage(
    content: String,
    isFromUser: Boolean,
    isStreaming: Boolean = false
): Flow<Message> = flow {
    val message = Message(
        content = content,
        isFromUser = isFromUser,
        timestamp = System.currentTimeMillis(),
        isStreaming = isStreaming
    )
    
    val messageId = insertMessage(message)
    val savedMessage = message.copy(id = messageId)
    emit(savedMessage)
}

/**
 * Update a streaming message with new content
 */
suspend fun MessageDao.updateStreamingMessage(
    messageId: Long,
    newContent: String,
    isComplete: Boolean = false
): Flow<Message> = flow {
    val existingMessage = getMessageById(messageId)
    if (existingMessage != null) {
        val updatedMessage = existingMessage.copy(
            content = newContent,
            isStreaming = !isComplete,
            timestamp = if (isComplete) System.currentTimeMillis() else existingMessage.timestamp
        )
        insertMessage(updatedMessage)
        emit(updatedMessage)
    }
}

/**
 * Get all messages with streaming status
 */
fun MessageDao.getAllMessagesWithStreamingStatus(): Flow<List<Message>> {
    return getAllMessages()
}

/**
 * Mark a message as complete (no longer streaming)
 */
suspend fun MessageDao.markMessageComplete(messageId: Long) {
    val message = getMessageById(messageId)
    if (message != null) {
        val updatedMessage = message.copy(
            isStreaming = false,
            timestamp = System.currentTimeMillis()
        )
        updateMessage(updatedMessage)
    }
}
