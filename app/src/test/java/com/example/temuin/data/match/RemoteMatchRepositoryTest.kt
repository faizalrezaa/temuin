package com.example.temuin.data.match

import com.example.temuin.data.remote.TemuinApi
import com.google.gson.JsonParser
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress

class RemoteMatchRepositoryTest {

    private var server: HttpServer? = null

    @After
    fun tearDown() {
        server?.stop(0)
    }

    @Test
    fun getMatchesMapsContractFields() = runBlocking {
        var requestMethod = ""
        var requestBody = ""
        val baseUrl = startServer(200, SAMPLE_RESPONSE) { exchange ->
            requestMethod = exchange.requestMethod
            requestBody = exchange.requestBody.bufferedReader().readText()
        }
        val repository = RemoteMatchRepository(apiFor(baseUrl))

        val result = repository.getMatches("report-1").getOrThrow()

        assertEquals("POST", requestMethod)
        assertEquals("{}", requestBody)
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
    fun httpErrorSurfacesStatusAndErrorCode() = runBlocking {
        val baseUrl = startServer(
            429,
            """{"success":false,"message":"Terlalu banyak","error":{"code":"RATE_LIMITED","message":"Terlalu banyak"}}""",
        )
        val repository = RemoteMatchRepository(apiFor(baseUrl))

        val failure = repository.getMatches("report-1").exceptionOrNull()

        assertTrue(failure is MatchApiException)
        assertEquals(429, (failure as MatchApiException).statusCode)
        assertEquals("RATE_LIMITED", failure.errorCode)
    }

    @Test
    fun unsuccessfulEnvelopeIsReportedAsRejectedRequest() = runBlocking {
        val baseUrl = startServer(200, """{"success":false,"message":"Laporan tidak aktif"}""")
        val repository = RemoteMatchRepository(apiFor(baseUrl))

        val failure = repository.getMatches("report-1").exceptionOrNull()

        assertTrue(failure is MatchApiException)
        assertEquals(422, (failure as MatchApiException).statusCode)
    }

    @Test
    fun malformedBodyIsReportedAsParseFailure() = runBlocking {
        val baseUrl = startServer(200, "not json")
        val repository = RemoteMatchRepository(apiFor(baseUrl))

        val failure = repository.getMatches("report-1").exceptionOrNull()

        assertTrue(failure is MatchParseException)
    }

    @Test
    fun duplicateAndInvalidReportIdsCannotReachTheKeyedList() = runBlocking {
        val envelope = JsonParser.parseString(SAMPLE_RESPONSE).asJsonObject
        val items = envelope.getAsJsonObject("data").getAsJsonArray("items")
        val original = items[0].deepCopy()
        items.add(original.deepCopy())
        val missingId = original.deepCopy().asJsonObject
        missingId.getAsJsonObject("report").remove("id")
        items.add(missingId)
        val blankId = original.deepCopy().asJsonObject
        blankId.getAsJsonObject("report").addProperty("id", " ")
        items.add(blankId)
        val missingTitle = original.deepCopy().asJsonObject
        missingTitle.getAsJsonObject("report").remove("title")
        items.add(missingTitle)
        val repository = RemoteMatchRepository(apiFor(startServer(200, envelope.toString())))

        val result = repository.getMatches("report-1").getOrThrow()

        assertEquals(listOf("found-wallet-kantin"), result.items.map { it.report.id })
    }

    private fun apiFor(baseUrl: String): TemuinApi =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TemuinApi::class.java)

    private fun startServer(
        status: Int,
        body: String,
        onRequest: (com.sun.net.httpserver.HttpExchange) -> Unit = {},
    ): String {
        val httpServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        httpServer.createContext("/api/v1/reports/report-1/matches") { exchange ->
            onRequest(exchange)
            val bytes = body.toByteArray()
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        httpServer.start()
        server = httpServer
        return "http://127.0.0.1:${httpServer.address.port}/api/v1/"
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
