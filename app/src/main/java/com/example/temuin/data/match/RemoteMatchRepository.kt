package com.example.temuin.data.match

import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ApiErrorEnvelope
import com.example.temuin.data.remote.MatchItemDto
import com.example.temuin.data.remote.MatchReportDto
import com.example.temuin.data.remote.MatchResultDto
import com.example.temuin.data.remote.TemuinApi
import com.google.gson.Gson
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Remote implementation of [MatchRepository] for API Contract v1:
 * POST /reports/{id}/matches, using the app-wide [TemuinApi] client.
 */
class RemoteMatchRepository(
    private val api: TemuinApi = ApiClient.api,
    private val gson: Gson = Gson(),
) : MatchRepository {

    override suspend fun getMatches(reportId: String): Result<MatchResult> = try {
        val response = api.matches(reportId)
        if (!response.success) throw MatchApiException(422)
        Result.success((response.data ?: throw MatchParseException()).toDomain())
    } catch (error: MatchApiException) {
        Result.failure(error)
    } catch (error: MatchParseException) {
        Result.failure(error)
    } catch (error: HttpException) {
        Result.failure(error.toMatchApiException())
    } catch (error: CancellationException) {
        throw error
    } catch (error: MalformedJsonException) {
        Result.failure(MatchParseException(error))
    } catch (error: IOException) {
        Result.failure(error)
    } catch (error: Exception) {
        Result.failure(MatchParseException(error))
    }

    private fun HttpException.toMatchApiException(): MatchApiException {
        val error = runCatching {
            gson.fromJson(response()?.errorBody()?.string(), ApiErrorEnvelope::class.java)
        }.getOrNull()?.error
        return MatchApiException(code(), error?.code, error?.message, this)
    }
}

/** Thrown when a 2xx body does not match the contract or cannot be parsed. */
class MatchParseException(cause: Throwable? = null) :
    Exception("Respons rekomendasi tidak dapat dibaca.", cause)

private fun MatchResultDto.toDomain(): MatchResult {
    val mappedItems = items.orEmpty().mapNotNull { it.toDomainOrNull() }
    return MatchResult(
        source = when (source?.uppercase()) {
            "AI" -> MatchSource.AI
            else -> MatchSource.RULE_BASED
        },
        notices = notices.orEmpty().filter { it.isNotBlank() },
        candidatesFound = candidatesFound ?: mappedItems.size,
        items = mappedItems,
    )
}

private fun MatchItemDto.toDomainOrNull(): MatchItem? {
    val reportDto = report ?: return null
    return MatchItem(
        report = reportDto.toDomain(),
        level = when (level?.uppercase()) {
            "HIGH" -> MatchLevel.HIGH
            "MEDIUM" -> MatchLevel.MEDIUM
            else -> MatchLevel.LOW
        },
        matchedAttributes = matchedAttributes.toAttributes(),
        differentAttributes = differentAttributes.toAttributes(),
        notComparableAttributes = notComparableAttributes.toAttributes(),
        reason = reason.orEmpty(),
        incompleteData = incompleteData ?: false,
    )
}

private fun MatchReportDto.toDomain() = MatchReport(
    id = id.orEmpty(),
    title = title.orEmpty(),
    category = category.orEmpty(),
    color = color?.takeIf { it.isNotBlank() },
    description = description.orEmpty(),
    occurredAt = occurredAt.orEmpty(),
    locationName = locationName.orEmpty(),
    ownerName = ownerName?.takeIf { it.isNotBlank() },
)

private fun List<String>?.toAttributes(): List<MatchAttribute> = orEmpty().mapNotNull { raw ->
    MatchAttribute.entries.firstOrNull { it.name == raw.uppercase() }
}
