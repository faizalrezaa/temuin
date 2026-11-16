package com.example.temuin.ui.match

import com.example.temuin.data.match.MockMatchRepository
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchViewModelTest {

    @Test
    fun initialStateIsLoading() {
        val viewModel = MatchViewModel(
            repository = MockMatchRepository(),
            dispatcher = Dispatchers.Unconfined,
        )
        assertEquals(MatchUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun loadMatchesSuccess() {
        val repository = MockMatchRepository()
        val viewModel = MatchViewModel(
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.loadMatches("sample-id")

        val state = viewModel.uiState.value
        assertTrue(state is MatchUiState.Success)
        assertEquals(3, (state as MatchUiState.Success).result.items.size)
    }

    @Test
    fun loadMatchesEmpty() {
        val repository = MockMatchRepository()
        val viewModel = MatchViewModel(
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.loadMatches("empty")

        assertEquals(MatchUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun loadMatchesBlankReportIdEmitsInvalidRequestError() {
        val viewModel = MatchViewModel(
            repository = MockMatchRepository(),
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.loadMatches("   ")

        val state = viewModel.uiState.value
        assertTrue(state is MatchUiState.Error)
        val failure = (state as MatchUiState.Error).failure
        assertFalse(failure.canRetry)
        assertTrue(failure.message.contains("tidak valid"))
    }

    @Test
    fun loadMatchesApiExceptionEmitsErrorWithDetails() {
        val repository = MockMatchRepository(
            shouldFail = true,
            failureStatusCode = 429,
        )
        val viewModel = MatchViewModel(
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.loadMatches("sample-id")

        val state = viewModel.uiState.value
        assertTrue(state is MatchUiState.Error)
        val failure = (state as MatchUiState.Error).failure
        assertTrue(failure.canRetry)
        assertTrue(failure.message.contains("Terlalu banyak permintaan"))
    }

    @Test
    fun retryReloadsLastReportId() {
        val repository = MockMatchRepository(
            shouldFail = true,
            failureStatusCode = 500,
        )
        val viewModel = MatchViewModel(
            repository = repository,
            dispatcher = Dispatchers.Unconfined,
        )

        viewModel.loadMatches("report-456")
        assertTrue(viewModel.uiState.value is MatchUiState.Error)

        // Fix repository failure and retry
        repository.shouldFail = false
        viewModel.retry()

        assertTrue(viewModel.uiState.value is MatchUiState.Success)
    }
}
