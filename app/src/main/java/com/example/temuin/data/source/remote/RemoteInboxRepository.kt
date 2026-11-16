package com.example.temuin.data.source.remote

import android.util.Log
import com.example.temuin.data.model.InboxItem
import com.example.temuin.data.model.StatusLaporanType
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ApiMappers
import com.example.temuin.data.remote.ClaimDto
import com.example.temuin.data.remote.RejectClaimRequest
import com.example.temuin.data.remote.TemuinApi
import com.example.temuin.data.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * Inbox:
 * - "Permintaan Klaim" = klaim PENDING yang masuk ke barang temuan saya (GET /claims/incoming)
 * - "Status Laporan"   = perkembangan klaim yang saya ajukan (GET /claims/mine)
 */
class RemoteInboxRepository(private val api: TemuinApi = ApiClient.api) : InboxRepository {

    private val refresh = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getInboxItems(): Flow<List<InboxItem>> = refresh.flatMapLatest {
        flow { emit(load()) }
    }

    override fun refresh() { refresh.value++ }

    private suspend fun load(): List<InboxItem> {
        val incoming = runCatching { api.incomingClaims(status = "PENDING").data.orEmpty() }
            .onFailure { Log.w(TAG, "incoming: ${ApiClient.errorMessage(it)}") }
            .getOrDefault(emptyList())
            .map(::toPermintaan)

        val mine = runCatching { api.myClaims().data.orEmpty() }
            .onFailure { Log.w(TAG, "mine: ${ApiClient.errorMessage(it)}") }
            .getOrDefault(emptyList())
            .mapNotNull(::toStatus)

        return incoming + mine
    }

    private fun toPermintaan(c: ClaimDto) = InboxItem.PermintaanKlaim(
        id = c.id,
        judulBarang = c.report?.title ?: "Barang",
        imageUrl = c.report?.photoUrl.orEmpty(),
        waktu = ApiMappers.relativeTime(c.createdAt),
        namaPengklaim = c.claimant?.name ?: "Pengguna",
        jawabanVerifikasi = c.secretDescription.orEmpty(),
    )

    private fun toStatus(c: ClaimDto): InboxItem.StatusLaporan? {
        val pickupName = c.pickup?.pickupPoint?.name
        val (type, pesan) = when (c.status) {
            "APPROVED" -> if (c.pickup?.method == "AT_PICKUP_POINT") {
                StatusLaporanType.TITIP_DI_POS to
                    (c.pickup.instructions ?: "Klaim disetujui. Ambil barang di ${pickupName ?: "pos"} dengan menunjukkan KTM.")
            } else {
                StatusLaporanType.DISETUJUI_PENEMU to
                    (c.pickup?.instructions ?: "Klaim disetujui. Hubungi penemu (${c.finder?.name ?: "-"}) untuk serah terima.")
            }
            "COMPLETED" -> StatusLaporanType.DISETUJUI_PENEMU to "Serah terima selesai. Barang sudah kamu terima."
            "REJECTED" -> StatusLaporanType.KLAIM_DITOLAK to
                "Klaim ditolak penemu" + (c.rejectionReason?.let { ": $it" } ?: ".")
            "ITEM_UNAVAILABLE" -> StatusLaporanType.KLAIM_DITOLAK to "Barang sudah tidak tersedia untuk diklaim."
            else -> return null // PENDING / CANCELLED tidak ditampilkan
        }
        return InboxItem.StatusLaporan(
            id = c.id,
            judulBarang = c.report?.title ?: "Barang",
            imageUrl = c.report?.photoUrl.orEmpty(),
            waktu = ApiMappers.relativeTime(c.updatedAt ?: c.createdAt),
            status = type,
            pesan = pesan,
            nomorReferensi = "#" + c.id.take(8).uppercase(),
        )
    }

    override suspend fun setujuiKlaim(id: String): Result<Unit> =
        runCatching { api.approveClaim(id); Unit }
            .recoverCatching { throw Exception(ApiClient.errorMessage(it)) }
            .also { refresh.value++ }

    override suspend fun tolakKlaim(id: String): Result<Unit> =
        runCatching { api.rejectClaim(id, RejectClaimRequest(DEFAULT_REJECT_REASON)); Unit }
            .recoverCatching { throw Exception(ApiClient.errorMessage(it)) }
            .also { refresh.value++ }

    private companion object {
        const val TAG = "RemoteInboxRepo"
        const val DEFAULT_REJECT_REASON = "Ciri-ciri yang disebutkan tidak sesuai dengan barang"
    }
}
