# Alur video Tugas 2: State dan ViewModel

Target durasi 55–58 detik, batas tugas 60 detik. Fokus pada form Sign Up.

## Persiapan sebelum merekam

1. Buka branch `tugas-2` di Android Studio, tunggu Gradle Sync, lalu jalankan aplikasi.
2. Gunakan emulator di panel Running Devices Android Studio. Buka `AuthViewModel.kt` di sebelahnya jika kode perlu terlihat. Perbesar emulator agar isian dan pesan error terbaca dalam rekaman.
3. Aktifkan rotasi otomatis pada perangkat. Gunakan tombol Rotate pada emulator untuk berpindah portrait ke landscape. Jangan mengunci orientasi atau menambahkan `configChanges` untuk melewati pembuatan ulang Activity.
4. Dari Login, pilih Daftar Sekarang. Mulai dari form kosong. Restart aplikasi sebelum take jika sebelumnya sudah mengisi form.
5. Siapkan data contoh di bawah dan narasi. Gunakan password demo, bukan password pribadi.
6. Rekam video lokal. Jangan mengirim atau mengumpulkan ke Classroom secara otomatis.

## Data contoh

- Nama: Alif Negifta Wibawaputra
- NIM: 245150200111066
- Fakultas: Fakultas Ilmu Komputer
- Nomor telepon contoh: 0812-3456-7890
- Password demo: Temuin123
- Konfirmasi: Temuin123
- Persetujuan: dicentang

Nomor di atas hanya untuk contoh format. Tidak ada pesan atau panggilan yang dikirim.

## Urutan rekaman

### 00–05 detik: perkenalan

Tampilkan form Sign Up dalam orientasi portrait.

Narasi: “Saya Alif dari Kelompok 10. Ini penerapan State dan ViewModel pada form Temuin.”

### 05–14 detik: bukti validasi

Scroll ke tombol Validasi & Proses Data, lalu klik saat form kosong. Tunjukkan pesan persetujuan dan konfirmasi password. Scroll ke atas untuk memperlihatkan error nama, NIM, fakultas, dan telepon. Form tidak berpindah ke hasil.

Narasi: “Saat dikirim kosong, ViewModel memeriksa seluruh isian dan menampilkan error pada field yang belum valid.”

### 14–30 detik: isi data valid

Isi data contoh, pilih fakultas, dan centang persetujuan. Perlihatkan bahwa error berkurang setelah nilai diperbaiki. Tutup keyboard. Jangan menekan submit dahulu.

Narasi: “Saya lengkapi datanya. Nama, NIM, fakultas, telepon, password, dan persetujuan disimpan sebagai state di ViewModel.”

Bagian mengetik boleh dipercepat atau dipotong jika durasi melebihi target. Jangan memotong transisi rotasi atau mengganti isian di antara kedua orientasi.

### 30–43 detik: bukti ketahanan rotasi

Putar perangkat ke landscape, lalu scroll secukupnya untuk memperlihatkan nilai nama, NIM, fakultas, telepon, password yang tetap terisi dalam bentuk tersamarkan, dan checkbox yang tetap dicentang. Kembalikan ke portrait. Perubahan orientasi harus terlihat dalam rekaman yang berkesinambungan.

Narasi: “Ketika orientasi berubah, seluruh isian tetap ada, termasuk pilihan fakultas dan checkbox, karena datanya berada di ViewModel.”

### 43–55 detik: submit dan hasil pengolahan

Klik Validasi & Proses Data. Tampilkan halaman Hasil Pengolahan Form dengan identitas gabungan serta nomor +6281234567890.

Narasi: “Setelah valid, nama, NIM, dan fakultas digabung menjadi identitas akun. Nomor telepon juga diubah ke format plus enam dua.”

### 55–58 detik: penutup

Pertahankan tampilan hasil.

Narasi: “Ini demo pengolahan lokal. Password tidak ditampilkan pada hasil.”

## Jika diminta menjelaskan kode

Penjelasan kode bersifat tambahan. Untuk batas 60 detik, prioritaskan bukti perilaku aplikasi di atas. Jika perlu, gunakan 3–4 detik dari perkenalan untuk menyorot bagian berikut di editor.

- `SignupUiState`: semua nilai form dan checkbox berada dalam satu state.
- `validateSignup()`: menentukan error untuk setiap input.
- `submitSignup()`: menghentikan proses jika ada error, lalu membuat hasil gabungan setelah valid.
- `SignupScreen()`: membaca state melalui `collectAsStateWithLifecycle()` dan meneruskan perubahan ke ViewModel.
- `AppNavigation()`: satu instance AuthViewModel di luar NavHost digunakan oleh form dan hasil. Instance dipertahankan saat Activity dibuat ulang karena rotasi.

Jangan mengatakan bahwa registrasi sudah tersimpan di Firebase, identitas kampus sudah diverifikasi, atau state pasti bertahan setelah aplikasi ditutup. Implementasi ini menangani perubahan konfigurasi selama proses aplikasi masih hidup.

## Bahan pembaruan laporan

Bagian validasi sudah menjadi implementasi, bukan saran pengembangan. Tambahkan penjelasan error per field, penggabungan nama + NIM + fakultas, normalisasi telepon, penyimpanan checkbox, serta cuplikan video sebelum dan sesudah rotasi. Layanan autentikasi server tetap dapat dicantumkan sebagai pengembangan berikutnya.
