package com.example.aisecretary.ai.rag

/**
* [DocumentStore] is a simple in-memory storage system for handling text-based documents.
*
* It is primarily used in Retrieval-Augmented Generation (RAG) systems, allowing developers
* to add, retrieve, and clear documents that may be used for reference or context retrieval.
*
* This class is not persistent—data is lost when the app is closed or the instance is cleared.
*
* ### Example Usage:
* ```
* val store = DocumentStore()
* store.addDocument("AI is transforming the world.")
* val allDocs = store.getDocuments()
* store.clearDocuments()
* ```
*/
class DocumentStore {
    /** Internal list that holds all stored documents. */
    private val documents = mutableListOf<String>()

    /**
     * Adds a new document to the store.
     *
     * @param document The text content of the document to be added.
     *  @throws IllegalArgumentException If the document is blank.
     */
    fun addDocument(document: String) {
        documents.add(document)
    }

    /**
     * Retrieves all documents currently in the store.
     *
     * @return A list of all stored documents.
     */
    fun getDocuments(): List<String> {
        return documents
    }

    /**
     * Removes all documents from the store.
     */
    fun clearDocuments() {
        documents.clear()
    }
}