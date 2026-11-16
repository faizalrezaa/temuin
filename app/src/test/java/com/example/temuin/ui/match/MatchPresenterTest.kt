package com.example.temuin.ui.match

import com.example.temuin.data.match.MatchAttribute
import com.example.temuin.data.match.MatchLevel
import com.example.temuin.data.match.MatchSource
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchPresenterTest {

    @Test
    fun emptyItemsMapToEmptyState() {
        assertEquals(MatchUiState.Empty, MatchPresenter.toUiState(MatchFixtures.empty))
    }

    @Test
    fun successKeepsServerOrder() {
        val state = MatchPresenter.toUiState(MatchFixtures.aiSuccess) as MatchUiState.Success

        assertEquals(
            listOf("found-wallet-kantin", "found-wallet-gazebo", "found-wallet-cokelat"),
            state.result.items.map { it.report.id },
        )
    }

    @Test
    fun ruleBasedSourceIsExposed() {
        val state = MatchPresenter.toUiState(MatchFixtures.ruleBased) as MatchUiState.Success

        assertEquals(MatchSource.RULE_BASED, state.result.source)
        assertEquals("Sumber: Aturan", MatchPresenter.sourceLabel(state.result.source))
    }

    @Test
    fun levelLabelsAreIndonesian() {
        assertEquals("Tinggi", MatchPresenter.levelLabel(MatchLevel.HIGH))
        assertEquals("Sedang", MatchPresenter.levelLabel(MatchLevel.MEDIUM))
        assertEquals("Rendah", MatchPresenter.levelLabel(MatchLevel.LOW))
    }

    @Test
    fun attributeLabelsCoverEveryContractValue() {
        assertEquals("Kategori", MatchPresenter.attributeLabel(MatchAttribute.CATEGORY))
        assertEquals("Warna", MatchPresenter.attributeLabel(MatchAttribute.COLOR))
        assertEquals("Lokasi", MatchPresenter.attributeLabel(MatchAttribute.LOCATION))
        assertEquals("Waktu", MatchPresenter.attributeLabel(MatchAttribute.TIME))
        assertEquals("Foto", MatchPresenter.attributeLabel(MatchAttribute.PHOTO))
        assertEquals("Kata kunci", MatchPresenter.attributeLabel(MatchAttribute.KEYWORDS))
    }

    @Test
    fun categoryLabelsUseReadableNames() {
        assertEquals("Dompet", MatchPresenter.categoryLabel("WALLET"))
        assertEquals("Kartu identitas", MatchPresenter.categoryLabel("ID_CARD"))
        assertEquals("Lainnya", MatchPresenter.categoryLabel("OTHER"))
    }

    @Test
    fun unauthorizedIsNotRetryableAndHidesManualSearch() {
        val failure = MatchPresenter.failureFor(401)

        assertFalse(failure.canRetry)
        assertFalse(failure.showManualSearch)
        assertTrue(failure.message.contains("masuk kembali"))
    }

    @Test
    fun notFoundIsNotRetryable() {
        val failure = MatchPresenter.failureFor(404)

        assertFalse(failure.canRetry)
        assertTrue(failure.showManualSearch)
        assertTrue(failure.message.contains("tidak ditemukan"))
    }

    @Test
    fun inactiveReportIsExplained() {
        val failure = MatchPresenter.failureFor(422, "REPORT_NOT_ACTIVE")

        assertFalse(failure.canRetry)
        assertTrue(failure.message.contains("tidak aktif"))
    }

    @Test
    fun otherUnprocessableResponseIsNotRetryable() {
        assertFalse(MatchPresenter.failureFor(422, "VALIDATION_ERROR").canRetry)
    }

    @Test
    fun rateLimitAndServerErrorsAreRetryable() {
        assertTrue(MatchPresenter.failureFor(429).canRetry)
        assertTrue(MatchPresenter.failureFor(500).canRetry)
    }

    @Test
    fun networkAndInvalidResponseFailuresAreRetryable() {
        assertTrue(MatchPresenter.failureFor(null).canRetry)
        assertTrue(MatchPresenter.invalidResponseFailure().canRetry)
    }

    @Test
    fun occurredAtIsFormattedInTheRequestedTimeZone() {
        val formatted = MatchPresenter.formatOccurredAt(
            "2026-11-16T03:00:00Z",
            TimeZone.getTimeZone("UTC"),
        )

        assertEquals("16 Nov 2026, 03:00", formatted)
    }

    @Test
    fun occurredAtAcceptsOffsetTimestamp() {
        val formatted = MatchPresenter.formatOccurredAt(
            "2026-11-16T10:00:00+07:00",
            TimeZone.getTimeZone("UTC"),
        )

        assertEquals("16 Nov 2026, 03:00", formatted)
    }

    @Test
    fun unknownTimestampFallsBackToRawValue() {
        assertEquals("not-a-date", MatchPresenter.formatOccurredAt("not-a-date"))
    }
}
