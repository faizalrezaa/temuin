package com.example.temuin.data.repository

import com.example.temuin.data.model.LaporanFormData

interface LaporanRepository {
    suspend fun submitLaporan(form: LaporanFormData): Result<Unit>
}
