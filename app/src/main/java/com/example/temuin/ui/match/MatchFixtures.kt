package com.example.temuin.ui.match

import com.example.temuin.data.match.MatchAttribute
import com.example.temuin.data.match.MatchItem
import com.example.temuin.data.match.MatchLevel
import com.example.temuin.data.match.MatchReport
import com.example.temuin.data.match.MatchResult
import com.example.temuin.data.match.MatchSource

/**
 * Sample data for Compose previews and unit tests.
 * It mirrors the shape of a /reports/{id}/matches response and is not live server data.
 */
object MatchFixtures {

    private val walletKantin = MatchReport(
        id = "found-wallet-kantin",
        title = "Dompet kulit hitam",
        category = "WALLET",
        color = "Hitam",
        description = "Dompet kulit hitam dengan resleting, ditemukan di meja kantin.",
        occurredAt = "2026-11-16T05:00:00Z",
        locationName = "Kantin FILKOM",
        ownerName = "Raka",
    )

    private val walletGazebo = MatchReport(
        id = "found-wallet-gazebo",
        title = "Dompet hitam",
        category = "WALLET",
        color = "Hitam",
        description = "Dompet hitam tanpa kartu, ditemukan di bangku gazebo.",
        occurredAt = "2026-11-17T02:30:00Z",
        locationName = "Gazebo FILKOM",
        ownerName = "Nadia",
    )

    private val walletCokelat = MatchReport(
        id = "found-wallet-cokelat",
        title = "Dompet cokelat",
        category = "WALLET",
        color = "Cokelat",
        description = "Dompet cokelat berisi kartu nama, ditemukan di lobi gedung A.",
        occurredAt = "2026-11-25T08:00:00Z",
        locationName = "Lobi Gedung A",
        ownerName = "Bagas",
    )

    private val highMatch = MatchItem(
        report = walletKantin,
        level = MatchLevel.HIGH,
        matchedAttributes = listOf(
            MatchAttribute.CATEGORY,
            MatchAttribute.COLOR,
            MatchAttribute.LOCATION,
            MatchAttribute.TIME,
            MatchAttribute.KEYWORDS,
        ),
        notComparableAttributes = listOf(MatchAttribute.PHOTO),
        reason = "Kategori, warna, dan lokasi cocok. Waktu temuan dekat dengan waktu hilang.",
    )

    private val mediumMatch = MatchItem(
        report = walletGazebo,
        level = MatchLevel.MEDIUM,
        matchedAttributes = listOf(
            MatchAttribute.CATEGORY,
            MatchAttribute.COLOR,
            MatchAttribute.KEYWORDS,
        ),
        differentAttributes = listOf(MatchAttribute.LOCATION),
        notComparableAttributes = listOf(MatchAttribute.PHOTO),
        reason = "Warna dan kategori sama, tetapi lokasi berbeda.",
    )

    private val lowMatch = MatchItem(
        report = walletCokelat,
        level = MatchLevel.LOW,
        matchedAttributes = listOf(MatchAttribute.CATEGORY),
        differentAttributes = listOf(MatchAttribute.COLOR, MatchAttribute.LOCATION),
        notComparableAttributes = listOf(MatchAttribute.PHOTO, MatchAttribute.TIME),
        reason = "Hanya kategori yang sama. Warna dan lokasi berbeda.",
    )

    val aiSuccess = MatchResult(
        source = MatchSource.AI,
        candidatesFound = 3,
        items = listOf(highMatch, mediumMatch, lowMatch),
    )

    val ruleBased = MatchResult(
        source = MatchSource.RULE_BASED,
        notices = listOf("Layanan AI tidak dikonfigurasi, hasil disusun dari aturan."),
        candidatesFound = 1,
        items = listOf(highMatch),
    )

    val empty = MatchResult(
        source = MatchSource.AI,
        notices = listOf("Tidak ada kandidat aktif yang cocok dalam 30 hari terakhir."),
        candidatesFound = 0,
        items = emptyList(),
    )

    val incomplete = MatchResult(
        source = MatchSource.AI,
        candidatesFound = 1,
        items = listOf(
            highMatch.copy(
                level = MatchLevel.MEDIUM,
                matchedAttributes = listOf(MatchAttribute.CATEGORY),
                notComparableAttributes = listOf(
                    MatchAttribute.COLOR,
                    MatchAttribute.LOCATION,
                    MatchAttribute.PHOTO,
                ),
                reason = "Data warna dan lokasi belum dicatat, jadi kecocokan belum bisa dipastikan.",
                incompleteData = true,
            ),
        ),
    )
}
