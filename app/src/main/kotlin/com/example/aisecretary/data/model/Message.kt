package com.example.aisecretary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single message in the conversation.
 *
 * This can be a message from the user or the AI assistant.
 *
 * @property id Auto-generated unique ID for the message.
 * @property content The text of the message.
 * @property isFromUser True if the message was sent by the user, false if by the AI.
 * @property timestamp The time the message was created (in milliseconds).
 * @property isImportantInfo True if the message contains important information.
 */
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isImportantInfo: Boolean = false
)