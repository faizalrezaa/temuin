package com.example.temuin.data.match

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Remote implementation of [MatchRepository] for API Contract v1:
 * POST /reports/{id}/matches.
 */
class RemoteMatchRepository(
    private val baseUrl: String = DEFAULT_BASE_URL,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : MatchRepository {

    override suspend fun getMatches(reportId: String): Result<MatchResult> = withContext(ioDispatcher) {
        runCatching {
            val connection = openConnection(reportId)
            try {
                val status = connection.responseCode
                val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    .orEmpty()

                if (status !in 200..299) throw apiException(status, body)
                parseMatchResponse(body)
            } finally {
                connection.disconnect()
            }
        }
    }

    private fun openConnection(reportId: String): HttpURLConnection {
        val encodedId = URLEncoder.encode(reportId, "UTF-8")
        val url = URL("${baseUrl.trimEnd('/')}/reports/$encodedId/matches")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            outputStream.use { it.write("{}".toByteArray()) }
        }
    }

    private fun apiException(status: Int, body: String): MatchApiException {
        val error = runCatching { JSONObject(body).opt("error") }.getOrNull()
        val code = when (error) {
            is JSONObject -> error.stringOrNull("code")
            is String -> error.ifBlank { null }
            else -> null
        }
        val message = when (error) {
            is JSONObject -> error.stringOrNull("message")
            is String -> error.takeIf { it.isNotBlank() }
            else -> null
        }
        return MatchApiException(status, code, message)
    }

    companion object {
        // shortcut: emulator host loopback is hardcoded for coursework; move to BuildConfig when environments exist.
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080/api/v1"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 30_000
    }
}

/** Thrown when a 2xx body does not match the contract or is not valid JSON. */
class MatchParseException(cause: Throwable? = null) :
    Exception("Respons rekomendasi tidak dapat dibaca.", cause)

internal fun parseMatchResponse(raw: String): MatchResult {
    val root = try {
        JSONObject(raw)
    } catch (error: Exception) {
        throw MatchParseException(error)
    }

    if (!root.optBoolean("success", true)) {
        val error = root.opt("error")
        val code = if (error is JSONObject) error.stringOrNull("code") else null
        throw MatchApiException(statusCode = 422, errorCode = code)
    }

    val data = root.optJSONObject("data") ?: throw MatchParseException()
    val itemsJson = data.optJSONArray("items") ?: JSONArray()
    val items = buildList {
        for (index in 0 until itemsJson.length()) {
            val item = itemsJson.optJSONObject(index) ?: continue
            val report = item.optJSONObject("report") ?: continue
            add(
                MatchItem(
                    report = report.toMatchReport(),
                    level = item.stringOrNull("level").toLevel(),
                    matchedAttributes = item.optJSONArray("matched_attributes").toAttributes(),
                    differentAttributes = item.optJSONArray("different_attributes").toAttributes(),
                    notComparableAttributes = item.optJSONArray("not_comparable_attributes").toAttributes(),
                    reason = item.optString("reason"),
                    incompleteData = item.optBoolean("incomplete_data", false),
                )
            )
        }
    }

    return MatchResult(
        source = when (data.stringOrNull("source")?.uppercase()) {
            "AI" -> MatchSource.AI
            else -> MatchSource.RULE_BASED
        },
        notices = data.optJSONArray("notices").toStringList(),
        candidatesFound = data.optInt("candidates_found", items.size),
        items = items,
    )
}

private fun JSONObject.toMatchReport() = MatchReport(
    id = optString("id"),
    title = optString("title"),
    category = optString("category"),
    color = stringOrNull("color"),
    description = optString("description"),
    occurredAt = optString("occurred_at"),
    locationName = optString("location_name"),
    ownerName = stringOrNull("owner_name"),
)

private fun JSONObject.stringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) optString(key).takeIf { it.isNotBlank() } else null

private fun JSONArray?.toAttributes(): List<MatchAttribute> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val value = optString(index).uppercase()
            MatchAttribute.entries.firstOrNull { it.name == value }?.let(::add)
        }
    }
}

private fun JSONArray?.toStringList(): List<String> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            optString(index).takeIf { it.isNotBlank() }?.let(::add)
        }
    }
}

private fun String?.toLevel(): MatchLevel = when (this?.uppercase()) {
    "HIGH" -> MatchLevel.HIGH
    "MEDIUM" -> MatchLevel.MEDIUM
    else -> MatchLevel.LOW
}
