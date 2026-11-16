package com.example.temuin.data.source.fake

import com.example.temuin.data.model.Item
import com.example.temuin.data.model.ItemStatus
import com.example.temuin.data.repository.ItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeItemRepository : ItemRepository {
    private val dummyItems = listOf(
        Item(
            id = "1",
            idLaporan = "TMN-84920",
            title = "Dompet Kulit Eiger Coklat",
            kategori = "Aksesori & Dompet",
            location = "Kantin Vokasi • Meja 12",
            time = "2 jam lalu",
            status = ItemStatus.DICARI,
            imageUrl = "https://images.unsplash.com/photo-1627123424574-724758594e93?w=600&q=80",
            deskripsi = "Dompet kulit warna coklat tua merk Eiger berisi Kartu Tanda Mahasiswa (KTM) atas nama Ahmad Fauzi, SIM A, dan beberapa kartu ATM. Terakhir ditaruh di atas meja kantin.",
            titikPengambilan = "Pos Satpam Vokasi UB / Meja Informasi",
            infoPelaporNama = "Ahmad Fauzi",
            infoPelaporFakultas = "Program Pendidikan Vokasi",
            kontakPelapor = "081234567890"
        ),
        Item(
            id = "2",
            idLaporan = "TMN-84921",
            title = "Botol LocknLock Hijau Mint",
            kategori = "Peralatan Minum",
            location = "Balairung Utama • Tangga",
            time = "Hari ini, 09:15",
            status = ItemStatus.DITEMUKAN,
            imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600&q=80",
            deskripsi = "Botol minum merk LocknLock warna hijau mint kondisi masih sangat bagus, tertinggal di tangga utama Balairung Universitas Brawijaya.",
            titikPengambilan = "Layanan Kemahasiswaan Rektorat Lt. 2",
            infoPelaporNama = "Siti Aminah",
            infoPelaporFakultas = "Fakultas Ilmu Administrasi",
            kontakPelapor = "089876543210"
        ),
        Item(
            id = "3",
            idLaporan = "TMN-84922",
            title = "Kacamata Frame Hitam Doff",
            kategori = "Aksesori",
            location = "Perpus Pusat Lt 3 • Meja 14",
            time = "Kemarin",
            status = ItemStatus.DICARI,
            imageUrl = "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600&q=80",
            deskripsi = "Kacamata baca dengan frame hitam doff anti-radiasi. Sangat dibutuhkan untuk kuliah dan pengerjaan skripsi.",
            titikPengambilan = "Meja Layanan Sirkulasi Perpustakaan Pusat UB",
            infoPelaporNama = "Budi Santoso",
            infoPelaporFakultas = "Fakultas Teknik",
            kontakPelapor = "085612345678"
        ),
        Item(
            id = "4",
            idLaporan = "TMN-84923",
            title = "Casio fx-991EX ClassWiz",
            kategori = "Alat Tulis & Elektronik",
            location = "Lab Mesin FT • Rak 4",
            time = "Kemarin",
            status = ItemStatus.DITEMUKAN,
            imageUrl = "https://images.unsplash.com/photo-1594980598772-a3fabd53304d?w=600&q=80",
            deskripsi = "Kalkulator saintifik Casio fx-991EX ClassWiz ditemukan di Lab Teknik Mesin. Ada stiker nama di bagian belakang tapi sudah sedikit terkelupas.",
            titikPengambilan = "Ruang Administrasi Jurusan Teknik Mesin FT UB",
            infoPelaporNama = "Rizky Pratama",
            infoPelaporFakultas = "Fakultas Teknik",
            kontakPelapor = "082198765432"
        ),
        Item(
            id = "5",
            idLaporan = "TMN-84924",
            title = "Jaket Kuning Makara (Size L)",
            kategori = "Pakaian",
            location = "Halte Bus Kuning Teknik",
            time = "2 hari lalu",
            status = ItemStatus.DICARI,
            imageUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=600&q=80",
            deskripsi = "Jaket almamater warna kuning khas UB ukuran L dengan badge Fakultas Teknik di lengan kanan. Tertinggal saat menunggu bus kampus.",
            titikPengambilan = "Pos Satpam Fakultas Teknik UB",
            infoPelaporNama = "Dewi Lestari",
            infoPelaporFakultas = "Fakultas Teknik",
            kontakPelapor = "087788990011"
        ),
        Item(
            id = "6",
            idLaporan = "TMN-84925",
            title = "Flashdisk Sandisk Cruzer…",
            kategori = "Penyimpanan Data",
            location = "Gedung A FIB • R. 203",
            time = "3 hari lalu",
            status = ItemStatus.DITEMUKAN,
            imageUrl = "https://images.unsplash.com/photo-1531492736222-243292497df5?w=600&q=80",
            deskripsi = "Flashdisk Sandisk warna hitam merah kapasitas 32GB berisi file tugas akhir dan materi kuliah.",
            titikPengambilan = "Tata Usaha Fakultas Ilmu Budaya (FIB) UB",
            infoPelaporNama = "Eko Prasetyo",
            infoPelaporFakultas = "Fakultas Ilmu Budaya",
            kontakPelapor = "081355443322"
        )
    )

    override fun getItems(): Flow<List<Item>> = flowOf(dummyItems)

    override fun getItemById(id: String): Flow<Item?> {
        val item = dummyItems.find { it.id == id }
        return flowOf(item)
    }

    companion object {
        const val PROFILE_AVATAR_URL = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=600&q=80"
    }
}
