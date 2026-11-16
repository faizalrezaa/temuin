!cat << 'EOF' > GEMINI.md
# Project Context: Temuin (Android App)
- **Tech Stack:** Kotlin, Jetpack Compose, Material 3, MVVM Architecture, StateFlow.
- **UI & Theme:** Gunakan token warna dan tipografi dari package `ui.theme` yang sudah ada.
- **Structure:** Pisahkan Stateful Screen (terhubung ke ViewModel) dengan Stateless Content Composable.
- **Data Layer (Dummy to BE Ready):**
    - Jangan pernah menaruh data dummy langsung di dalam UI Composable atau ViewModel.
    - Selalu buat `interface` Repository di package `data/repository/` (mengembalikan `Flow<List<Model>>` atau `Result<Model>`).
    - Buat implementasi `Fake[Feature]Repository` yang berisi data dummy realistis (konteks kampus Universitas Brawijaya / Malang) untuk tahap awal.
    - ViewModel hanya boleh bergantung pada `interface` Repository (bukan langsung ke class Fake/Dummy) agar mudah diganti ke Firebase/REST API nanti.
- **Build Verification:** Jalankan `./gradlew assembleDebug` setelah membuat atau mengubah kode.
  EOF