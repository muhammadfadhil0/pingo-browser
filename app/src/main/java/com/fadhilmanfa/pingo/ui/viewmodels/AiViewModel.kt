package com.fadhilmanfa.pingo.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fadhilmanfa.pingo.data.remote.GroqRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AiViewModel(private val repository: GroqRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<AiUiState>(AiUiState.Idle)
    val uiState: StateFlow<AiUiState> = _uiState.asStateFlow()
    
    // Stores the current page content in Markdown format
    private var currentPageContext: String? = null

    /**
     * Set the current page context (Markdown content from web page)
     */
    fun setPageContext(markdownContent: String) {
        currentPageContext = markdownContent
    }

    /**
     * Clear the current page context
     */
    fun clearPageContext() {
        currentPageContext = null
    }

    /**
     * Ask AI with optional page context
     * @param prompt User's question
     * @param includePageContext Whether to include the current page as context
     */
    fun askAi(prompt: String, includePageContext: Boolean = true) {
        if (prompt.isBlank()) return

        val fullPrompt = if (includePageContext && !currentPageContext.isNullOrBlank()) {
            """
            |Berikut adalah konten halaman web yang sedang dilihat user:
            |
            |$currentPageContext
            |
            |---
            |
            |Pertanyaan user: $prompt
            |
            |Berikan jawaban yang relevan dengan konten halaman di atas jika pertanyaan berkaitan dengan halaman tersebut. Jika tidak, jawab pertanyaan secara umum.
            """.trimMargin()
        } else {
            prompt
        }

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            val response = repository.getAiResponse(fullPrompt)
            _uiState.value = AiUiState.Success(response)
        }
    }

    fun reset() {
        _uiState.value = AiUiState.Idle
    }
}

sealed class AiUiState {
    object Idle : AiUiState()
    object Loading : AiUiState()
    data class Success(val response: String) : AiUiState()
    data class Error(val message: String) : AiUiState()
}
