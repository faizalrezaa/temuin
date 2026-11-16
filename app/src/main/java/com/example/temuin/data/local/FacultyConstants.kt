package com.example.temuin.data.local

data class Faculty(val code: String, val name: String)

val UB_FACULTIES = listOf(
    Faculty("FILKOM",  "Fakultas Ilmu Komputer"),
    Faculty("FT",      "Fakultas Teknik"),
    Faculty("FEB",     "Fakultas Ekonomi dan Bisnis"),
    Faculty("FH",      "Fakultas Hukum"),
    Faculty("FIA",     "Fakultas Ilmu Administrasi"),
    Faculty("FISIP",   "Fakultas Ilmu Sosial dan Ilmu Politik"),
    Faculty("FMIPA",   "Fakultas MIPA"),
    Faculty("FP",      "Fakultas Pertanian"),
    Faculty("FTP",     "Fakultas Teknologi Pertanian"),
    Faculty("FPet",    "Fakultas Peternakan"),
    Faculty("FPi",     "Fakultas Perikanan dan Ilmu Kelautan"),
    Faculty("FK",      "Fakultas Kedokteran"),
    Faculty("FKG",     "Fakultas Kedokteran Gigi"),
    Faculty("FKIK",    "Fakultas Kedokteran Hewan"),
    Faculty("FKes",    "Fakultas Ilmu Kesehatan"),
    Faculty("FKUB",   "Program Pascasarjana")
)
