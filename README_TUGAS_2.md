# Temuin: Tugas 2 State dan ViewModel

Branch sementara: `tugas-2`. Pengembangan ini melengkapi form Login dan Sign Up untuk tugas pada halaman 52–53 materi PAPB10.

## Cakupan

- Validasi seluruh elemen input Sign Up di AuthViewModel, termasuk checkbox.
- Validasi identitas dan password pada Login. Validasi format Login bukan autentikasi akun.
- Pesan error di masing-masing input setelah percobaan submit. Setelah itu, error diperbarui ketika isian berubah.
- Submit ditolak selama masih ada error.
- Submit Sign Up menggabungkan nama yang dirapikan, NIM, dan fakultas menjadi satu identitas akun, lalu menormalkan nomor telepon ke format +62.
- Halaman hasil menampilkan identitas dan kontak tanpa password.
- Semua nilai input, error, checkbox, dan hasil berada di ViewModel. UI mengamati StateFlow dengan collectAsStateWithLifecycle.
- Layout form dan hasil dapat di-scroll dalam orientasi portrait maupun landscape.

## Aturan validasi

- Nama wajib diisi, memiliki minimal dua huruf, dan hanya menerima huruf, spasi, titik, apostrof, atau tanda hubung.
- NIM menggunakan format demo mahasiswa UB: 15 digit. Pemeriksaan format tidak membuktikan status mahasiswa aktif.
- Fakultas wajib dipilih dari empat opsi demo yang disediakan.
- Telepon menerima nomor seluler Indonesia dengan awalan 08, 628, atau +628. Spasi, tanda hubung, titik, dan tanda kurung dihapus. Panjang format lokal 10–13 digit.
- Password tidak boleh kosong atau hanya spasi, minimal delapan karakter. Password tidak dipangkas saat dibandingkan.
- Konfirmasi password wajib sama persis dengan password.
- Checkbox persetujuan wajib dicentang sebelum submit.
- Login menerima NIM 15 digit, NIP 18 digit, atau email dengan domain ub.ac.id maupun subdomainnya, serta password minimal delapan karakter.

## Alur data

Input pengguna → fungsi pembaruan ViewModel → StateFlow → UI.

Klik submit → validasi di ViewModel → jika salah tetap di form dengan error → jika benar hasil pengolahan disimpan di ViewModel → navigasi ke halaman hasil.

ViewModel dibuat di luar NavHost pada AppNavigation, lalu instance yang sama diberikan ke Login, Sign Up, dan halaman hasil. Tombol Kembali ke Form mengembalikan pengguna ke form dengan isian sebelumnya. State bertahan ketika konfigurasi berubah selama proses aplikasi tetap hidup. Tidak ada jaminan pemulihan setelah process death atau force stop, dan password tidak disimpan ke SavedStateHandle atau penyimpanan permanen.

## Lokasi implementasi

- `app/src/main/java/com/example/temuin/ui/auth/AuthViewModel.kt`: model state, validasi, dan pengolahan hasil.
- `app/src/main/java/com/example/temuin/ui/auth/SignUpScreen.kt`: form, error, dan checkbox.
- `app/src/main/java/com/example/temuin/ui/auth/LoginScreen.kt`: form Login dan validasinya.
- `app/src/main/java/com/example/temuin/ui/auth/SubmissionScreen.kt`: hasil gabungan input.
- `app/src/main/java/com/example/temuin/navigation/AppNavigation.kt`: instance ViewModel dan alur navigasi.
- `docs/VIDEO_TUGAS_2.md`: persiapan, data contoh, urutan rekaman, dan narasi maksimal 60 detik.

## Menjalankan

Buka folder repository di Android Studio, pilih branch tugas-2, lakukan Gradle Sync, lalu Run pada emulator atau perangkat. Gunakan Gradle Wrapper dan SDK yang ditentukan project. Dependency lifecycle-runtime-compose dan lifecycle-viewmodel-compose dinyatakan eksplisit menggunakan versi Lifecycle project yang sama.

## Batas demo

Submit memproses data lokal. Belum ada pembuatan akun server, penyimpanan database, pengecekan NIM ke universitas, pengiriman WhatsApp, atau autentikasi Firebase. Daftar fakultas merupakan pilihan demo, belum mencakup seluruh fakultas UB.

## Referensi pola state

- https://developer.android.com/develop/ui/compose/state-hoisting
- https://developer.android.com/develop/ui/compose/state-saving
