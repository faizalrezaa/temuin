package com.example.temuin.data.source.fake

import com.example.temuin.data.model.InboxItem
import com.example.temuin.data.model.StatusLaporanType
import com.example.temuin.data.repository.InboxRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FakeInboxRepository : InboxRepository {

    companion object {
        private val DUMMY_ITEMS = listOf(
            InboxItem.PermintaanKlaim(
                id = "klaim-001",
                judulBarang = "AirPods Pro Case Hitam",
                imageUrl = "https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?w=200",
                waktu = "2 jam lalu",
                namaPengklaim = "Farhan Kurniawan",
                jawabanVerifikasi = "Nomor seri tertulis di dalam case: H3G7Q2MNPQ7T",
                tipsLabel = "Verifikasi kepemilikan sebelum menyetujui klaim"
            ),
            InboxItem.PermintaanKlaim(
                id = "klaim-002",
                judulBarang = "KTM UB — Dimas Pratama",
                imageUrl = "https://images.unsplash.com/photo-1626544827763-d516dce335e2?w=200",
                waktu = "5 jam lalu",
                namaPengklaim = "Dimas Pratama",
                jawabanVerifikasi = "NIM tertera di kartu: 215150201111042. Prodi Teknik Informatika 2021.",
                tipsLabel = "Verifikasi data KTM dengan identitas pengklaim"
            ),
            InboxItem.PermintaanKlaim(
                id = "klaim-003",
                judulBarang = "Dompet Kulit Coklat",
                imageUrl = "https://images.unsplash.com/photo-1627123425203-fee1e5b7f0b8?w=200",
                waktu = "1 hari lalu",
                namaPengklaim = "Rizky Aditya",
                jawabanVerifikasi = "Di dalam ada SIM A atas nama saya dan kartu ATM BNI berwarna hijau.",
                tipsLabel = "Minta pengklaim tunjukkan identitas yang cocok"
            ),
            InboxItem.StatusLaporan(
                id = "status-001",
                judulBarang = "Botol LocknLock Hijau",
                imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=200",
                waktu = "3 jam lalu",
                status = StatusLaporanType.KLAIM_DITOLAK,
                pesan = "Klaim ditolak karena jawaban verifikasi tidak sesuai dengan ciri-ciri barang yang dilaporkan.",
                nomorReferensi = null
            ),
            InboxItem.StatusLaporan(
                id = "status-002",
                judulBarang = "Kacamata Frame Hitam Doff",
                imageUrl = "https://images.unsplash.com/photo-1574258495973-f010dfbb5371?w=200",
                waktu = "1 hari lalu",
                status = StatusLaporanType.DISETUJUI_PENEMU,
                pesan = "Klaimmu disetujui oleh penemu. Silakan ambil barang di Pos Satpam Fakultas Teknik UB.",
                nomorReferensi = "TEM-20241115-0042"
            )
        )
    }

    private val items = DUMMY_ITEMS.toMutableList()

    override fun getInboxItems(): Flow<List<InboxItem>> = flow {
        delay(600)
        emit(items.toList())
    }

    override suspend fun setujuiKlaim(id: String): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }

    override suspend fun tolakKlaim(id: String): Result<Unit> {
        delay(1000)
        return Result.success(Unit)
    }
}
