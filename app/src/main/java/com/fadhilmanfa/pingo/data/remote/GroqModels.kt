package com.fadhilmanfa.pingo.data.remote

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens") val maxTokens: Int = 1024,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: Message,
    @SerializedName("finish_reason") val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)
