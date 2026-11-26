package com.example.temuin.data.help

import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.HelpChatReplyDto
import com.example.temuin.data.remote.HelpChatRequestDto
import com.example.temuin.data.remote.HelpChatTurnDto
import com.example.temuin.data.remote.HelpTopicDto
import com.example.temuin.data.remote.TemuinApi
import kotlinx.coroutines.CancellationException

data class HelpTopic(val id: String, val title: String, val description: String, val answer: String)
data class HelpTurn(val role: String, val content: String)
data class HelpReportReference(
    val id: String,
    val type: String,
    val title: String,
    val category: String,
    val color: String?,
    val locationName: String,
    val locationArea: String?,
    val photoUrl: String?,
)
data class HelpReply(
    val reply: String,
    val topic: String,
    val source: String,
    val reports: List<HelpReportReference> = emptyList(),
)

interface HelpRepository {
    suspend fun topics(): Result<List<HelpTopic>>
    suspend fun chat(message: String, history: List<HelpTurn>): Result<HelpReply>
}

class RemoteHelpRepository(private val api: TemuinApi = ApiClient.api) : HelpRepository {
    override suspend fun topics(): Result<List<HelpTopic>> = request {
        val response = api.helpTopics()
        require(response.success)
        response.data.orEmpty().map { it.toDomain() }
    }

    override suspend fun chat(message: String, history: List<HelpTurn>): Result<HelpReply> = request {
        val response = api.helpChat(
            HelpChatRequestDto(
                message = message,
                history = history.map { HelpChatTurnDto(it.role, it.content) },
            ),
        )
        require(response.success)
        (response.data ?: error("Respons bantuan kosong")).toDomain()
    }

    private suspend fun <T> request(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }
}

private fun HelpTopicDto.toDomain() = HelpTopic(id, title, description, answer)
private fun HelpChatReplyDto.toDomain() = HelpReply(
    reply = reply,
    topic = topic,
    source = source,
    reports = reports.orEmpty().map { report ->
        HelpReportReference(
            id = report.id,
            type = report.type,
            title = report.title,
            category = report.category,
            color = report.color,
            locationName = report.locationName,
            locationArea = report.locationArea,
            photoUrl = report.photoUrl,
        )
    },
)

val localHelpTopics = listOf(
    HelpTopic("REPORTING", "Buat Laporan", "Laporkan barang hilang atau ditemukan.", "Buka Beranda, tekan Lapor, pilih jenis barang, lalu isi ciri, lokasi, waktu, dan foto jika ada."),
    HelpTopic("SEARCH", "Cari Barang", "Cari laporan dan kecocokan.", "Cari laporan dari Beranda dengan kata kunci atau filter. Buka laporan hilang milikmu, lalu tekan Cari Kecocokan untuk melihat kandidat."),
    HelpTopic("MAP_CUSTODY", "Peta & Penitipan", "Lihat titik pengambilan kampus.", "Buka Peta untuk melihat titik pengambilan. Saat membuat laporan barang temuan, pilih lokasi penitipan yang tersedia."),
    HelpTopic("CLAIM_INBOX", "Klaim & Inbox", "Ajukan klaim dan pantau statusnya.", "Dari detail barang temuan, ajukan klaim dengan ciri khusus yang hanya diketahui pemilik. Pantau statusnya melalui Inbox."),
)
