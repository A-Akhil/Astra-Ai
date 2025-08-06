package com.example.aisecretary.ai.memory

import com.example.aisecretary.data.model.MemoryFact
import com.example.aisecretary.data.model.Message
import java.util.*

/**
 * `MemoryRetriever` is responsible for fetching the most relevant memory facts from a given list
 * based on a user query and optional recent conversation context.
 *
 * It uses a simple scoring algorithm that considers:
 * - Keyword overlap between memory facts and the query
 * - Recent message content for contextual weighting
 * - Exact keyword matches for boosting score
 *
 * This retriever can be used in an AI assistant to simulate memory recall.
 *
 * ### Example Usage:
 * ```
 * val retriever = MemoryRetriever()
 * val relevantFacts = retriever.retrieveRelevantMemory(
 *     query = "favorite food",
 *     allMemoryFacts = storedMemories,
 *     recentMessages = conversationHistory
 * )
 * ```
 */
class MemoryRetriever {

    /**
     * Retrieves memory facts that are most relevant to the given query.
     *
     * @param query The user query to search relevant memories for.
     * @param allMemoryFacts All stored memory facts.
     * @param recentMessages (Optional) Recent messages used to boost contextual relevance.
     * @param maxResults The maximum number of relevant memory facts to return.
     * @return A list of memory facts sorted by relevance.
     * @throws IllegalArgumentException if maxResults is less than 1.
     */
    fun retrieveRelevantMemory(
        query: String,
        allMemoryFacts: List<MemoryFact>,
        recentMessages: List<Message> = emptyList(),
        maxResults: Int = 5
    ): List<MemoryFact> {
        if (allMemoryFacts.isEmpty()) {
            return emptyList()
        }

        // Extract important words from the query
        val queryTerms = extractImportantTerms(query.lowercase(Locale.getDefault()))
        if (queryTerms.isEmpty()) {
            return emptyList()
        }

        // Calculate relevance scores for each memory fact
        val scoredFacts = allMemoryFacts.map { fact ->
            val factText = "${fact.key} ${fact.value}".lowercase(Locale.getDefault())
            val score = calculateRelevanceScore(factText, queryTerms, recentMessages)
            Pair(fact, score)
        }

        // Return top results sorted by score
        return scoredFacts
            .sortedByDescending { it.second }
            .take(maxResults)
            .map { it.first }
    }

    /**
     * Extracts important keywords from the user query to be used in relevance scoring.
     * It removes common stopwords and filters out very short words.
     *
     * @param query The user input to extract keywords from.
     * @return A list of filtered and distinct keywords.
     */
    private fun extractImportantTerms(query: String): List<String> {
        // Remove common stopwords
        val stopwords = setOf(
            "a", "an", "the", "and", "or", "but", "is", "are", "was", "were", 
            "has", "have", "had", "do", "does", "did", "will", "would", "can", 
            "could", "may", "might", "must", "should", "what", "when", "where", 
            "who", "why", "how", "my", "your", "his", "her", "their", "our", "its"
        )
        
        return query.split(Regex("\\W+"))
            .filter { it.length > 2 && it !in stopwords }
            .distinct()
    }

    /**
     * Calculates how relevant a memory fact is to the given query terms and recent context.
     *
     * @param factText The text of the memory fact (key + value).
     * @param queryTerms Important words extracted from the user query.
     * @param recentMessages A list of recent user or system messages.
     * @return A score representing the relevance of the memory fact.
     */
    private fun calculateRelevanceScore(
        factText: String,
        queryTerms: List<String>,
        recentMessages: List<Message>
    ): Double {
        var score = 0.0

        // Match each query term in the fact
        for (term in queryTerms) {
            if (factText.contains(term)) {
                // Count occurrences
                val occurrences = factText.windowed(term.length).count { it == term }
                score += 1.0 * occurrences
            }
        }
        
        // Boost score for exact match queries like "What is my X?"
        if (factText.split(" ").any { term -> 
            queryTerms.any { query -> query == term }
        }) {
            score *= 1.5
        }

        // Boost score if the fact was recently mentioned in conversation
        if (recentMessages.isNotEmpty()) {
            val recentContextScore = recentMessages
                .takeLast(5) // Consider only last 5 messages
                .mapIndexed { index, message ->
                    val messageText = message.content.lowercase(Locale.getDefault())
                    val isRelevant = queryTerms.any { messageText.contains(it) }
                    
                    if (isRelevant) {
                        // Give higher weight to more recent messages
                        val recencyWeight = (5.0 - index) / 5.0
                        recencyWeight * 0.5
                    } else {
                        0.0
                    }
                }
                .sum()
            
            score += recentContextScore
        }
        
        return score
    }
}