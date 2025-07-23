package com.example.aisecretary.data.model

data class StreamingResponse(
    val response: String,
    val done: Boolean,
    val context: List<Int>? = null,
    val model: String? = null,
    val created_at: String? = null
)

data class StreamingChunk(
    val content: String,
    val isComplete: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class StreamingState(
    val isStreaming: Boolean = false,
    val currentContent: String = "",
    val messageId: Long? = null
)
