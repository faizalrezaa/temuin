package com.example.temuin.ui.match

import com.example.temuin.data.match.MatchResult

sealed interface MatchUiState {
    data object Loading : MatchUiState
    data object Empty : MatchUiState
    data class Success(val result: MatchResult) : MatchUiState
    data class Error(val failure: MatchFailure) : MatchUiState
}

data class MatchFailure(
    val message: String,
    val canRetry: Boolean,
    val showManualSearch: Boolean = true,
)
