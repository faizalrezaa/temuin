package com.example.temuin.ui.match

import com.example.temuin.data.match.MatchAttribute
import com.example.temuin.data.match.MatchLevel
import com.example.temuin.data.match.MatchResult
import com.example.temuin.data.match.MatchSource
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object MatchPresenter {

    fun toUiState(result: MatchResult): MatchUiState =
        if (result.items.isEmpty()) MatchUiState.Empty else MatchUiState.Success(result)

    fun failureFor(httpStatus: Int?, errorCode: String? = null): MatchFailure = when {
        httpStatus == null -> MatchFailure(
            message = "Tidak dapat terhubung ke server. Periksa koneksi lalu coba lagi.",
            canRetry = true,
        )
        httpStatus == 401 -> MatchFailure(
            message = "Sesi kamu sudah berakhir. Silakan masuk kembali.",
            canRetry = false,
            showManualSearch = false,
        )
        httpStatus == 404 -> MatchFailure(
            message = "Laporan tidak ditemukan atau sudah dihapus.",
            canRetry = false,
        )
        httpStatus == 422 && errorCode == "REPORT_NOT_ACTIVE" -> MatchFailure(
            message = "Laporan ini sudah tidak aktif, jadi rekomendasi tidak bisa dicari.",
            canRetry = false,
        )
        httpStatus == 429 -> MatchFailure(
            message = "Terlalu banyak permintaan. Tunggu sebentar lalu coba lagi.",
            canRetry = true,
        )
        httpStatus in 500..599 -> MatchFailure(
            message = "Layanan rekomendasi sedang bermasalah. Coba lagi beberapa saat.",
            canRetry = true,
        )
        httpStatus == 400 -> MatchFailure(
            message = "Permintaan rekomendasi tidak valid.",
            canRetry = false,
        )
        httpStatus == 422 -> MatchFailure(
            message = "Rekomendasi tidak bisa diproses untuk laporan ini.",
            canRetry = false,
        )
        else -> MatchFailure(
            message = "Rekomendasi gagal dimuat. Coba lagi.",
            canRetry = true,
        )
    }

    fun invalidResponseFailure(): MatchFailure = MatchFailure(
        message = "Respons rekomendasi tidak dapat dibaca. Coba lagi.",
        canRetry = true,
    )

    fun levelLabel(level: MatchLevel): String = when (level) {
        MatchLevel.HIGH -> "Tinggi"
        MatchLevel.MEDIUM -> "Sedang"
        MatchLevel.LOW -> "Rendah"
    }

    fun attributeLabel(attribute: MatchAttribute): String = when (attribute) {
        MatchAttribute.CATEGORY -> "Kategori"
        MatchAttribute.COLOR -> "Warna"
        MatchAttribute.LOCATION -> "Lokasi"
        MatchAttribute.TIME -> "Waktu"
        MatchAttribute.PHOTO -> "Foto"
        MatchAttribute.KEYWORDS -> "Kata kunci"
    }

    fun sourceLabel(source: MatchSource): String = when (source) {
        MatchSource.AI -> "Sumber: AI"
        MatchSource.RULE_BASED -> "Sumber: Aturan"
    }

    fun categoryLabel(code: String): String = when (code) {
        "ELECTRONICS" -> "Elektronik"
        "WALLET" -> "Dompet"
        "ID_CARD" -> "Kartu identitas"
        "KEYS" -> "Kunci"
        "BAG" -> "Tas"
        "CLOTHING" -> "Pakaian"
        "ACCESSORIES" -> "Aksesoris"
        "BOOKS" -> "Buku"
        "STATIONERY" -> "Alat tulis"
        "BOTTLE" -> "Botol"
        "OTHER" -> "Lainnya"
        else -> code.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
    }

    fun formatOccurredAt(raw: String, timeZone: TimeZone = TimeZone.getDefault()): String {
        val patterns = listOf("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", "yyyy-MM-dd'T'HH:mm:ssXXX")
        for (pattern in patterns) {
            try {
                val parser = SimpleDateFormat(pattern, Locale.US).apply {
                    this.timeZone = TimeZone.getTimeZone("UTC")
                    isLenient = false
                }
                val date = parser.parse(raw) ?: continue
                return SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).apply {
                    this.timeZone = timeZone
                }.format(date)
            } catch (_: Exception) {
                // Try the next supported RFC3339 shape.
            }
        }
        return raw
    }
}
