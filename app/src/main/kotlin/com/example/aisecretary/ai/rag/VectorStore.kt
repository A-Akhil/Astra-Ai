package com.example.aisecretary.ai.rag

/**
 * A simple in-memory vector store that allows adding, retrieving, and searching
 * for vectors based on cosine similarity.
 *
 * This class is used for storing FloatArray vectors associated with unique string IDs.
 * It is particularly useful in applications involving vector embeddings, such as
 * semantic search, memory retrieval, or RAG (Retrieval-Augmented Generation).
 *
 * ### Usage Example:
 * ```
 * val store = VectorStore()
 * store.addVector("doc1", floatArrayOf(0.1f, 0.2f, 0.3f))
 * store.addVector("doc2", floatArrayOf(0.4f, 0.5f, 0.6f))
 * val result = store.search(floatArrayOf(0.1f, 0.2f, 0.3f), topK = 1)
 * println(result) // -> [("doc1", 1.0)]
 * ```
 */
class VectorStore {
    // Stores vectors with their associated IDs
    private val vectors = mutableMapOf<String, FloatArray>()

    /**
     * Adds a vector to the store.
     *
     * @param id A unique identifier for the vector. It should not be reused.
     * @param vector A FloatArray representing the vector embedding.
     *
     * If the same `id` already exists, it will be overwritten.
     */
    fun addVector(id: String, vector: FloatArray) {
        vectors[id] = vector
    }

    /**
     * Retrieves a stored vector by its ID.
     *
     * @param id The identifier associated with the vector.
     * @return The FloatArray vector if found, or null if the ID does not exist.
     */

    fun getVector(id: String): FloatArray? {
        return vectors[id]
    }

    /**
     * Performs a similarity search to find the top [topK] vectors closest to the [queryVector]
     * using cosine similarity.
     *
     * @param queryVector The input vector to compare against stored vectors.
     * @param topK The number of most similar results to return. Must be ≥ 1.
     * @return A list of pairs containing the ID and similarity score of the closest vectors,
     * sorted from highest to lowest similarity.
     *
     * @throws IllegalArgumentException if [topK] is less than 1 or if [queryVector] has zero length.
     */
    fun search(queryVector: FloatArray, topK: Int): List<Pair<String, Float>> {
        return vectors.map { (id, vector) ->
            id to cosineSimilarity(queryVector, vector)
        }.sortedByDescending { it.second }
         .take(topK)
    }

    /**
     * Calculates the cosine similarity between two vectors.
     *
     * Cosine similarity measures how close the directions of the two vectors are.
     * Values range from -1.0 (opposite) to 1.0 (identical).
     *
     * @param vecA The first vector.
     * @param vecB The second vector.
     * @return A float similarity score. Returns 0.0f if either vector has zero magnitude.
     *
     * @throws IllegalArgumentException if vectors have different lengths.
     */

    private fun cosineSimilarity(vecA: FloatArray, vecB: FloatArray): Float {
        val dotProduct = vecA.zip(vecB).map { (a, b) -> a * b }.sum()
        val magnitudeA = Math.sqrt(vecA.map { it * it }.sum().toDouble()).toFloat()
        val magnitudeB = Math.sqrt(vecB.map { it * it }.sum().toDouble()).toFloat()
        return if (magnitudeA == 0f || magnitudeB == 0f) 0f else dotProduct / (magnitudeA * magnitudeB)
    }
}