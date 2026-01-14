package com.fadhilmanfa.pingo.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GroqRepository(private val apiKey: String) {
    private val apiService: GroqApiService

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(GroqApiService.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        apiService = retrofit.create(GroqApiService::class.java)
    }

    suspend fun getAiResponse(prompt: String, model: String = "openai/gpt-oss-120b"): String {
        return try {
            val request = ChatRequest(
                model = model,
                messages = listOf(
                    Message(role = "system", content = "You are Pingo AI, a helpful web browsing assistant."),
                    Message(role = "user", content = prompt)
                )
            )
            val response = apiService.getChatCompletion("Bearer $apiKey", request)
            response.choices.firstOrNull()?.message?.content ?: "Maaf, saya tidak mendapatkan respon."
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }
    }
}
