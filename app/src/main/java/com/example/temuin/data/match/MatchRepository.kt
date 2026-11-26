package com.example.temuin.data.match

import com.example.temuin.ui.match.MatchFixtures

/**
 * Exception thrown when a match recommendation API request fails.
 */
class MatchApiException(
    val statusCode: Int?,
    val errorCode: String? = null,
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message ?: "Match API request failed with status: $statusCode (code: $errorCode)", cause)

/**
 * Repository interface for lost-and-found match recommendations.
 */
interface MatchRepository {
    suspend fun getMatches(reportId: String): Result<MatchResult>
}

/**
 * In-memory mock implementation of [MatchRepository] for local previews and tests.
 */
class MockMatchRepository(
    var fixtureResult: MatchResult? = null,
    var shouldFail: Boolean = false,
    var failureStatusCode: Int? = null,
    var failureErrorCode: String? = null,
) : MatchRepository {

    override suspend fun getMatches(reportId: String): Result<MatchResult> {
        if (shouldFail) {
            return Result.failure(
                MatchApiException(
                    statusCode = failureStatusCode,
                    errorCode = failureErrorCode,
                )
            )
        }

        val result = fixtureResult ?: when (reportId) {
            "empty" -> MatchFixtures.empty
            "rule-based" -> MatchFixtures.ruleBased
            "incomplete" -> MatchFixtures.incomplete
            else -> MatchFixtures.aiSuccess
        }

        return Result.success(result)
    }
}
