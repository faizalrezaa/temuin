package com.example.temuin.data.match

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetSocketAddress

class RemoteMatchRepositoryTest {

    private var server: HttpServer? = null

    @After
    fun tearDown() {
        server?.stop(0)
    }

    @Test
    fun parseMatchResponseMapsContractFields() {
        val result = parseMatchResponse(SAMPLE_RESPONSE)

        assertEquals(MatchSource.AI, result.source)
        assertEquals(1, result.notices.size)
        assertEquals(1, result.candidatesFound)
        assertEquals(1, result.items.size)

        val item = result.items.single()
        assertEquals(MatchLevel.HIGH, item.level)
        assertEquals("Dompet kulit hitam", item.report.title)
        assertEquals("Kantin FILKOM", item.report.locationName)
        assertEquals("Raka", item.report.ownerName)
        assertEquals(
            listOf(MatchAttribute.CATEGORY, MatchAttribute.COLOR),
            item.matchedAttributes,
        )
        assertEquals(listOf(MatchAttribute.PHOTO), item.notComparableAttributes)
        assertFalse(item.incompleteData)
    }

    @Test
    fun postToContractPathReturnsParsedMatches() = runBlocking {
        var requestMethod = ""
        val baseUrl = startServer(200, SAMPLE_RESPONSE) { requestMethod = it }
        val repository = RemoteMatchRepository(baseUrl, Dispatchers.Unconfined)

        val result = repository.getMatches("report-1").getOrThrow()

        assertEquals("POST", requestMethod)
        assertEquals(1, result.items.size)
    }

    @Test
    fun httpErrorSurfacesStatusAndErrorCode() = runBlocking {
        val baseUrl = startServer(
            429,
            """{"success":false,"message":"Terlalu banyak","data":null,"error":{"code":"RATE_LIMITED"}}""",
        )
        val repository = RemoteMatchRepository(baseUrl, Dispatchers.Unconfined)

        val failure = repository.getMatches("report-1").exceptionOrNull()

        assertTrue(failure is MatchApiException)
        assertEquals(429, (failure as MatchApiException).statusCode)
        assertEquals("RATE_LIMITED", failure.errorCode)
    }

    @Test
    fun malformedBodyIsReportedAsParseFailure() = runBlocking {
        val baseUrl = startServer(200, "not json")
        val repository = RemoteMatchRepository(baseUrl, Dispatchers.Unconfined)

        val failure = repository.getMatches("report-1").exceptionOrNull()

        assertTrue(failure is MatchParseException)
    }

    private fun startServer(
        status: Int,
        body: String,
        onRequest: (String) -> Unit = {},
    ): String {
        val httpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        httpServer.createContext("/api/v1/reports/report-1/matches") { exchange ->
            onRequest(exchange.requestMethod)
            val bytes = body.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        httpServer.start()
        server = httpServer
        return "http://127.0.0.1:${httpServer.address.port}/api/v1"
    }

    private companion object {
        val SAMPLE_RESPONSE = """
            {
              "success": true,
              "message": "OK",
              "data": {
                "source": "AI",
                "notices": ["Layanan AI aktif"],
                "candidates_found": 1,
                "items": [
                  {
                    "report": {
                      "id": "found-wallet-kantin",
                      "title": "Dompet kulit hitam",
                      "category": "WALLET",
                      "color": "Hitam",
                      "description": "Ditemukan di meja kantin.",
                      "occurred_at": "2026-11-16T05:00:00Z",
                      "location_name": "Kantin FILKOM",
                      "owner_name": "Raka"
                    },
                    "level": "HIGH",
                    "matched_attributes": ["CATEGORY", "COLOR"],
                    "different_attributes": [],
                    "not_comparable_attributes": ["PHOTO"],
                    "reason": "Kategori dan warna cocok.",
                    "pre_score": 0.91,
                    "incomplete_data": false
                  }
                ]
              },
              "error": null
            }
        """.trimIndent()
    }
}
