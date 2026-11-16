package com.example.temuin.data.model

enum class TipeLaporan { HILANG, DITEMUKAN }

enum class MetodePenyerahan {
    POS_SATPAM,     // Titip di Pos Satpam Kampus
    LANGSUNG_PEMILIK // Langsung ke pemilik
}

data class LaporanFormData(
    val tipe: TipeLaporan = TipeLaporan.DITEMUKAN,
    val fotoUris: List<String> = emptyList(),
    val judulBarang: String = "",
    val deskripsi: String = "",
    // Khusus DITEMUKAN
    val waktuPenemuan: String = "",
    val lokasiPenemuan: String = "",
    val pertanyaanVerifikasi: String = "",
    val metodePenyerahan: MetodePenyerahan = MetodePenyerahan.POS_SATPAM,
    val lokasiPenitipan: String = "Pos Satpam Gerbang Utama (Jl. Veteran)",
    // Khusus HILANG
    val lokasiKehilangan: String = ""
)
