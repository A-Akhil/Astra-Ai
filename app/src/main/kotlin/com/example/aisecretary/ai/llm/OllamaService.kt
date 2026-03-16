package com.example.aisecretary.ai.llm

import com.example.aisecretary.data.model.StreamingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface OllamaService {
    @POST("/api/generate")
    @Headers("Content-Type: application/json")
    suspend fun generateResponse(@Body request: Map<String, Any>): Response<Map<String, Any>>
    
    @POST("/api/generate")
    @Headers("Content-Type: application/json")
    @Streaming
    suspend fun generateStreamingResponse(@Body request: Map<String, Any>): Response<okhttp3.ResponseBody>
    
    @POST("/api/chat")
    @Headers("Content-Type: application/json") 
    @Streaming
    suspend fun chatStreamingResponse(@Body request: Map<String, Any>): Response<okhttp3.ResponseBody>
}
