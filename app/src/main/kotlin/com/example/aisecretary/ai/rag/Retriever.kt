package com.example.aisecretary.ai.rag
/**
 * Retrieves relevant documents from a [DocumentStore] based on a query string.
 *
 * This is a simple keyword-based retriever used in RAG (Retrieval-Augmented Generation)
 * pipelines to fetch documents that contain the query terms.
 *
 * @property documentStore The document store to search within.
 */
import com.example.aisecretary.ai.rag.DocumentStore


class Retriever(private val documentStore: DocumentStore) {
    /**
     * Retrieves documents from the [documentStore] that contain the query string.
     *
     * @param query The search query used to find relevant documents.
     * @return A list of documents that contain the query (case-insensitive).
     */

    fun retrieveRelevantDocuments(query: String): List<String> {
        // Implement logic to retrieve relevant documents based on the query
        return documentStore.getDocuments().filter { document ->
            document.contains(query, ignoreCase = true)
        }
    }
}