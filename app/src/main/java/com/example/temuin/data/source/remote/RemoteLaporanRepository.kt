package com.example.temuin.data.source.remote

import android.content.Context
import android.net.Uri
import com.example.temuin.data.model.LaporanFormData
import com.example.temuin.data.model.MetodePenyerahan
import com.example.temuin.data.model.TipeLaporan
import com.example.temuin.data.remote.ApiClient
import com.example.temuin.data.remote.ApiMappers
import com.example.temuin.data.remote.CreateReportRequest
import com.example.temuin.data.remote.PickupPointDto
import com.example.temuin.data.remote.TemuinApi
import com.example.temuin.data.repository.LaporanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RemoteLaporanRepository(
    context: Context,
    private val api: TemuinApi = ApiClient.api,
) : LaporanRepository {

    private val appContext = context.applicationContext
    private var pickupPoints: List<PickupPointDto> = emptyList()

    override suspend fun getPickupPointNames(): List<String> {
        if (pickupPoints.isEmpty()) {
            pickupPoints = runCatching { api.pickupPoints().data.orEmpty() }.getOrDefault(emptyList())
        }
        return pickupPoints.mapNotNull { it.name }
    }

    override suspend fun submitLaporan(form: LaporanFormData): Result<Unit> = runCatching {
        val isFound = form.tipe == TipeLaporan.DITEMUKAN

        // 1. Upload foto pertama (API hanya menyimpan satu photo_url per laporan)
        val photoUrl = form.fotoUris.firstOrNull()?.let { uploadPhoto(Uri.parse(it)) }

        // 2. Deskripsi publik (wajib min. 5 karakter)
        val description = buildString {
            append(form.deskripsi.trim().ifBlank { form.judulBarang.trim() })
            if (isFound && form.pertanyaanVerifikasi.isNotBlank()) {
                append("\n\nPertanyaan verifikasi: ${form.pertanyaanVerifikasi.trim()}")
            }
        }.let { if (it.length < 5) "Laporan: $it" else it }

        // 3. Penitipan (hanya FOUND)
        var custody: String? = null
        var pickupPointId: String? = null
        if (isFound) {
            if (form.metodePenyerahan == MetodePenyerahan.POS_SATPAM) {
                getPickupPointNames()
                pickupPointId = pickupPoints.firstOrNull { it.name == form.lokasiPenitipan }?.id
                    ?: pickupPoints.firstOrNull()?.id
                custody = if (pickupPointId != null) "AWAITING_DROPOFF" else "HELD_BY_FINDER"
            } else {
                custody = "HELD_BY_FINDER"
            }
        }

        val request = CreateReportRequest(
            type = if (isFound) "FOUND" else "LOST",
            title = form.judulBarang.trim(),
            category = ApiMappers.guessCategory(form.judulBarang + " " + form.deskripsi),
            description = description,
            occurredAt = ApiMappers.toIso(parseWaktu(form.waktuPenemuan)),
            locationName = (if (isFound) form.lokasiPenemuan else form.lokasiKehilangan).trim(),
            photoUrl = photoUrl,
            custody = custody,
            pickupPointId = pickupPointId,
        )
        api.createReport(request)
        Unit
    }.recoverCatching { throw Exception(ApiClient.errorMessage(it)) }

    /** Format dari WaktuPenemuanField: "Senin, 16 Nov 2026 - 14:30 WIB". Kosong/invalid → sekarang. */
    private fun parseWaktu(value: String): Date {
        val now = Date(System.currentTimeMillis() - 60_000) // occurred_at tidak boleh di masa depan
        if (value.isBlank()) return now
        val parsed = runCatching {
            SimpleDateFormat("EEEE, d MMM yyyy - HH:mm 'WIB'", Locale.forLanguageTag("id-ID")).parse(value)
        }.getOrNull() ?: return now
        return if (parsed.after(now)) now else parsed
    }

    private suspend fun uploadPhoto(uri: Uri): String = withContext(Dispatchers.IO) {
        val resolver = appContext.contentResolver
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Foto tidak dapat dibaca")
        if (bytes.size > 5 * 1024 * 1024) error("Ukuran foto maksimal 5MB")
        val part = MultipartBody.Part.createFormData(
            "file", "photo.${mime.substringAfter('/', "jpg")}", bytes.toRequestBody(mime.toMediaType())
        )
        api.upload(part, "reports".toRequestBody("text/plain".toMediaType())).data!!.url
    }
}
