package com.example.temuin.data.model

enum class ItemStatus {
    DICARI, DITEMUKAN
}

data class Item(
    val id: String,
    val idLaporan: String,
    val title: String,
    val kategori: String,
    val location: String,
    val time: String,
    val status: ItemStatus,
    val imageUrl: String? = null,
    val deskripsi: String,
    val titikPengambilan: String,
    val infoPelaporNama: String,
    val infoPelaporFakultas: String,
    val kontakPelapor: String
)
