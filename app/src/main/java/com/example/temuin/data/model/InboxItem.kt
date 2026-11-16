package com.example.temuin.data.model

sealed class InboxItem {
    abstract val id: String
    abstract val judulBarang: String
    abstract val imageUrl: String
    abstract val waktu: String

    data class PermintaanKlaim(
        override val id: String,
        override val judulBarang: String,
        override val imageUrl: String,
        override val waktu: String,
        val namaPengklaim: String,
        val jawabanVerifikasi: String,
        val tipsLabel: String = "Verifikasi kepemilikan sebelum menyetujui klaim"
    ) : InboxItem()

    data class StatusLaporan(
        override val id: String,
        override val judulBarang: String,
        override val imageUrl: String,
        override val waktu: String,
        val status: StatusLaporanType,
        val pesan: String,
        val nomorReferensi: String? = null,
        val reportId: String? = null,
        /** Tampilkan tombol "Chat via WhatsApp" (klaim disetujui → boleh hubungi penemu). */
        val canChat: Boolean = false
    ) : InboxItem()
}

enum class InboxFilter(val label: String) {
    SEMUA("Semua"),
    PERMINTAAN_KLAIM("Permintaan Klaim"),
    STATUS_LAPORAN("Status Laporan")
}

enum class StatusLaporanType {
    MENUNGGU_VERIFIKASI,
    DISETUJUI_PENEMU,
    KLAIM_DITOLAK,
    TITIP_DI_POS
}
