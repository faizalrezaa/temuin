package com.example.temuin.data.repository

import com.example.temuin.data.model.InboxItem
import kotlinx.coroutines.flow.Flow

interface InboxRepository {
    fun getInboxItems(): Flow<List<InboxItem>>
    suspend fun setujuiKlaim(id: String): Result<Unit>
    suspend fun tolakKlaim(id: String): Result<Unit>
    fun refresh() {}

    /** Link WhatsApp penemu untuk klaim yang sudah disetujui. */
    suspend fun getChatLink(reportId: String): Result<String> =
        Result.failure(UnsupportedOperationException())
}
