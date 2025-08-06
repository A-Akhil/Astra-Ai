package com.example.aisecretary.ai.llm

import com.example.aisecretary.ai.llm.model.OllamaRequest
import com.example.aisecretary.ai.llm.model.OllamaResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * This interface helps talk to the Ollama AI using Retrofit.
 * It sends messages to the AI and gets replies back.
 */
interface OllamaService {
    /**
     * Sends a message to the AI and gets its response.
     *
     * @param request The message you want to send to the AI.
     * @return The AI's reply wrapped in a response.
     */
    @POST("/api/generate")
    suspend fun generateCompletion(
        @Body request: OllamaRequest
    ): Response<OllamaResponse>
}