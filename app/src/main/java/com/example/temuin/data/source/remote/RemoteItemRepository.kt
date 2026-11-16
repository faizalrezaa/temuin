package com.example.temuin.data.source.remote

import android.util.Log
import com.example.temuin.data.model.Item
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ApiMappers
import com.example.temuin.data.remote.SubmitClaimRequest
import com.example.temuin.data.remote.TemuinApi
import com.example.temuin.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RemoteItemRepository(private val api: TemuinApi = ApiClient.api) : ItemRepository {

    /** Feed laporan aktif (hilang + ditemukan), terbaru dulu. */
    override fun getItems(): Flow<List<Item>> = flow {
        val items = runCatching { api.reports().data.orEmpty().map { ApiMappers.toItem(it) } }
            .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; Log.w(TAG, "getItems: ${ApiClient.errorMessage(it)}") }
            .getOrDefault(emptyList())
        emit(items)
    }

    override fun getItemById(id: String): Flow<Item?> = flow {
        val item = runCatching {
            val report = api.report(id).data!!
            ApiMappers.toItem(report)
        }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; Log.w(TAG, "getItemById: ${ApiClient.errorMessage(it)}") }.getOrNull()
        emit(item)
    }

    override fun getItemsAtPickupPoint(pointId: String): Flow<List<Item>> = flow {
        val items = runCatching {
            api.pickupPointItems(pointId).data?.items.orEmpty().map { ApiMappers.toItem(it) }
        }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; Log.w(TAG, "getItemsAtPickupPoint: ${ApiClient.errorMessage(it)}") }
            .getOrDefault(emptyList())
        emit(items)
    }

    override suspend fun submitClaim(reportId: String, secretDescription: String): Result<Unit> =
        runCatching { api.submitClaim(reportId, SubmitClaimRequest(secretDescription.trim())); Unit }
            .recoverCatching { throw Exception(ApiClient.errorMessage(it)) }

    override suspend fun getContactLink(reportId: String): Result<String> =
        runCatching {
            val c = api.contact(reportId).data!!
            c.whatsappLink ?: c.whatsapp?.let { "https://wa.me/" + it.filter(Char::isDigit).replaceFirst(Regex("^0"), "62") }
            ?: error("Pelapor belum mencantumkan nomor WhatsApp")
        }.recoverCatching { throw Exception(if (it is IllegalStateException) it.message else ApiClient.errorMessage(it)) }

    private companion object { const val TAG = "RemoteItemRepo" }
}
