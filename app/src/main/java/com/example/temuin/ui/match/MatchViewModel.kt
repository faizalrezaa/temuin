package com.example.temuin.ui.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temuin.data.match.MatchApiException
import com.example.temuin.data.match.MatchParseException
import com.example.temuin.data.match.MatchRepository
import com.example.temuin.data.match.RemoteMatchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchViewModel(
    private val repository: MatchRepository = RemoteMatchRepository(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState.asStateFlow()

    private var lastReportId: String? = null
    private var loadJob: Job? = null

    fun loadMatches(reportId: String) {
        loadJob?.cancel()
        lastReportId = null
        if (reportId.isBlank()) {
            _uiState.value = MatchUiState.Error(MatchPresenter.failureFor(400))
            return
        }

        lastReportId = reportId
        _uiState.value = MatchUiState.Loading

        loadJob = viewModelScope.launch(dispatcher) {
            val result = repository.getMatches(reportId)
            currentCoroutineContext().ensureActive()
            result.fold(
                onSuccess = { matchResult ->
                    _uiState.value = MatchPresenter.toUiState(matchResult)
                },
                onFailure = { throwable ->
                    val failure = when (throwable) {
                        is MatchApiException -> MatchPresenter.failureFor(
                            throwable.statusCode,
                            throwable.errorCode
                        )
                        is MatchParseException -> MatchPresenter.invalidResponseFailure()
                        else -> MatchPresenter.failureFor(null)
                    }
                    _uiState.value = MatchUiState.Error(failure)
                }
            )
        }
    }

    fun retry() {
        val reportId = lastReportId ?: return
        loadMatches(reportId)
    }
}
