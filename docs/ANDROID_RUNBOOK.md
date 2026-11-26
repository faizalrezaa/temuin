# Menjalankan Temuin Android

## Backend lokal

Siapkan PostgreSQL dan jalankan API sesuai README [temuin-be](https://github.com/haramurti/temuin-be). Cek `http://localhost:8080/health` dari komputer. Untuk data contoh, ikuti petunjuk seed lokal di repo backend.

## Android Studio

Buka repo ini dan pilih konfigurasi `app` serta perangkat virtual Android. Untuk menghubungkan build debug ke backend lokal, tambahkan baris berikut ke `~/.gradle/gradle.properties`, lalu lakukan Gradle Sync dan tekan Run.

```properties
temuinDebugApiUrl=http://10.0.2.2:8080/api/v1/
```

Untuk build lewat terminal:

```sh
./gradlew :app:assembleDebug -PtemuinDebugApiUrl=http://10.0.2.2:8080/api/v1/
```

Build tanpa override memakai endpoint HTTPS bawaan. Build release selalu memakai endpoint tersebut. Untuk menggunakan emulator di samping editor, buka **View → Tool Windows → Running Devices**.

## Coba fitur AI

1. Masuk dengan akun backend dan buka laporan milikmu dari Beranda atau Profil.
2. Pilih **Cari Kecocokan** untuk membuka daftar kandidat. Hasil yang dinilai DeepSeek menampilkan **Penjelasan AI** dengan bagian Kesamaan dan Perbedaan.
3. Dari Beranda, buka tombol robot **Bantuan AI Temuin**. Pilih topik atau tanyakan barang yang dicari. Ketuk kartu laporan terkait untuk membuka detail.

Kedua fitur memakai DeepSeek melalui SinarCloud. Atur `SINARCLOUD_API_KEY` di backend. Saat provider tidak tersedia, kecocokan memakai aturan dan chat menjawab dari laporan aktif atau panduan Temuin sesuai pertanyaan.
