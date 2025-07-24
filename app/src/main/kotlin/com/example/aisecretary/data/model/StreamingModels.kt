package com.example.aisecretary.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamingResponse(
    val response: String,
    val done: Boolean,
    val context: List<Int>? = null,
    val model: String? = null,
    val created_at: String? = null,
    val total_duration: Long? = null,
    val load_duration: Long? = null,
    val prompt_eval_count: Int? = null,
    val prompt_eval_duration: Long? = null,
    val eval_count: Int? = null,
    val eval_duration: Long? = null
)

@Serializable
data class StreamingChunk(
    val content: String,
    val isComplete: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int = 0,
    val sentenceComplete: Boolean = false
)

@Serializable
data class StreamingState(
    val isStreaming: Boolean = false,
    val currentContent: String = "",
    val messageId: Long? = null,
    val bufferContent: String = "",
    val lastSentenceIndex: Int = 0,
    val isInterrupted: Boolean = false
)

@Serializable
data class SentenceBuffer(
    val content: String,
    val isComplete: Boolean,
    val sentences: List<String> = emptyList(),
    val remainder: String = ""
)

@Serializable
data class StreamingError(
    val error: String,
    val recoverable: Boolean = true,
    val retryCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class VoiceStreamingState(
    val isSpeaking: Boolean = false,
    val speechQueue: List<String> = emptyList(),
    val currentSentence: String = "",
    val speechInterrupted: Boolean = false,
    val autoMicScheduled: Boolean = false
)
