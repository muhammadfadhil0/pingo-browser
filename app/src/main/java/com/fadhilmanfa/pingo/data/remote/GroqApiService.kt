package com.fadhilmanfa.pingo.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApiService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") apiKey: String,
        @Body request: ChatRequest
    ): ChatResponse

    companion object {
        const val BASE_URL = "https://api.groq.com/openai/"
    }
}
