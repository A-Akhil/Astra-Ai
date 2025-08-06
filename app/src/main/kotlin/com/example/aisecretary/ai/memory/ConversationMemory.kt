package com.example.aisecretary.ai.memory

/**
 * Stores key parts of a conversation to help the AI recall important context later.
 *
 * This class acts as a simple memory manager that stores user-AI interactions
 * which may be useful for generating more context-aware responses.
 *
 * ### Example:
 * ```
 * val memory = ConversationMemory()
 * memory.addMemory("User likes sci-fi books.")
 * val past = memory.getMemories()
 * memory.clearMemories()
 * ```
 */
class ConversationMemory {
    // List that holds memory strings
    private val memoryStore: MutableList<String> = mutableListOf()

    /**
     * Adds a new memory to the list.
     *
     * @param memory The text to remember.
     */
    fun addMemory(memory: String) {
        memoryStore.add(memory)
    }

    /**
     * Returns all stored memories.
     *
     * @return A list of memory strings.
     */
    fun getMemories(): List<String> {
        return memoryStore
    }

    /**
     * Clears all saved memories.
     */
    fun clearMemories() {
        memoryStore.clear()
    }
}