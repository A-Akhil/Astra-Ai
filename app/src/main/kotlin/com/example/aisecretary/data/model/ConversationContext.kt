package com.example.aisecretary.data.model

/**
 * Holds the current context of a conversation.
 *
 * @property recentMessages List of recent messages exchanged in the conversation.
 * @property memoryFacts List of important facts remembered during the conversation.
 */
data class ConversationContext(
    val recentMessages: List<Message> = emptyList(),
    val memoryFacts: List<MemoryFact> = emptyList()
)