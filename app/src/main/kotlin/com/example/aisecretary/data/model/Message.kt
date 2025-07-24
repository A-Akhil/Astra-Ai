package com.example.aisecretary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isImportantInfo: Boolean = false,
    // NEW Streaming Properties
    val isStreaming: Boolean = false,
    val streamingComplete: Boolean = false
    val hasError: Boolean = false,
    val errorMessage: String? = null
)
