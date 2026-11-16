package com.example.temuin.data.source.fake

import com.example.temuin.data.model.LaporanFormData
import com.example.temuin.data.repository.LaporanRepository
import kotlinx.coroutines.delay

class FakeLaporanRepository : LaporanRepository {
    override suspend fun submitLaporan(form: LaporanFormData): Result<Unit> {
        delay(1500L) // simulasi panggilan API / backend
        return Result.success(Unit)
    }
}
