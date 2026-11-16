package com.example.temuin.data.repository

import com.example.temuin.data.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface ItemRepository {
    fun getItems(): Flow<List<Item>>
    fun getItemById(id: String): Flow<Item?>

    /** Feed dengan filter dari API. category = kode kategori (null = semua), status = ACTIVE/COMPLETED/ALL. */
    fun getItems(category: String?, status: String): Flow<List<Item>> = getItems()

    /** Pilihan kategori (kode → label) untuk filter. */
    suspend fun getCategories(): List<Pair<String, String>> = emptyList()

    /** Barang yang tersedia (DEPOSITED) di satu pos. */
    fun getItemsAtPickupPoint(pointId: String): Flow<List<Item>> = flowOf(emptyList())

    /** Ajukan klaim atas barang temuan. */
    suspend fun submitClaim(reportId: String, secretDescription: String): Result<Unit> =
        Result.failure(UnsupportedOperationException())

    /** Link WhatsApp pelapor (untuk laporan barang hilang). */
    suspend fun getContactLink(reportId: String): Result<String> =
        Result.failure(UnsupportedOperationException())
}
