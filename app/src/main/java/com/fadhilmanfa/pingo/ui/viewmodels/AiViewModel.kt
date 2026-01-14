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

    fun askAi(prompt: String) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _uiState.value = AiUiState.Loading
            val response = repository.getAiResponse(prompt)
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
