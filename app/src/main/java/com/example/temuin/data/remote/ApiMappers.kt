package com.example.temuin.data.remote

import com.example.temuin.data.model.Item
import com.example.temuin.data.model.ItemStatus
import com.example.temuin.data.model.PickupPoint
import com.example.temuin.data.model.PickupPointIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Konversi DTO API → model UI yang sudah dipakai layar-layar. */
object ApiMappers {

    private val categoryLabels = mapOf(
        "ELECTRONICS" to "Elektronik",
        "WALLET" to "Dompet",
        "ID_CARD" to "Kartu Identitas",
        "KEYS" to "Kunci",
        "BAG" to "Tas",
        "CLOTHING" to "Pakaian",
        "ACCESSORIES" to "Aksesoris",
        "BOOKS" to "Buku",
        "STATIONERY" to "Alat Tulis",
        "BOTTLE" to "Botol Minum",
        "OTHER" to "Lainnya",
    )

    fun categoryLabel(code: String?) = categoryLabels[code] ?: "Lainnya"

    /** Tebak kategori dari judul barang (form Lapor tidak punya pilihan kategori). */
    fun guessCategory(text: String): String {
        val t = text.lowercase()
        fun has(vararg k: String) = k.any { t.contains(it) }
        return when {
            has("ktm", "ktp", "kartu", "sim ", "id card", "e-ktp") -> "ID_CARD"
            has("dompet", "wallet") -> "WALLET"
            has("kunci", "key") -> "KEYS"
            has("hp", "handphone", "iphone", "samsung", "laptop", "charger", "earbud", "tws", "headset",
                "airpods", "powerbank", "power bank", "mouse", "flashdisk", "kabel", "jam tangan pintar", "smartwatch") -> "ELECTRONICS"
            has("tas", "ransel", "backpack", "tote") -> "BAG"
            has("jaket", "baju", "kaos", "hoodie", "topi", "sepatu", "sandal") -> "CLOTHING"
            has("botol", "tumbler", "tempat minum") -> "BOTTLE"
            has("buku", "novel", "modul") -> "BOOKS"
            has("pulpen", "pensil", "kalkulator", "penggaris", "tempat pensil") -> "STATIONERY"
            has("cincin", "kalung", "gelang", "jam", "kacamata", "anting") -> "ACCESSORIES"
            else -> "OTHER"
        }
    }

    // ---------- Waktu ----------

    private fun isoParser() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun parseIso(value: String?): Date? =
        value?.let { runCatching { isoParser().parse(it.take(19)) }.getOrNull() }

    fun toIso(date: Date): String = isoParser().format(date) + "Z"

    /** "5 menit lalu", "Kemarin", "12 Nov" … */
    fun relativeTime(value: String?): String {
        val date = parseIso(value) ?: return "-"
        val diffMin = (System.currentTimeMillis() - date.time) / 60_000
        return when {
            diffMin < 1 -> "Baru saja"
            diffMin < 60 -> "$diffMin menit lalu"
            diffMin < 24 * 60 -> "${diffMin / 60} jam lalu"
            diffMin < 48 * 60 -> "Kemarin"
            diffMin < 7 * 24 * 60 -> "${diffMin / (24 * 60)} hari lalu"
            else -> SimpleDateFormat("d MMM yyyy", Locale.forLanguageTag("id-ID")).format(date)
        }
    }

    // ---------- Report → Item ----------

    private fun custodyText(r: ReportDto): String = when {
        r.type == "LOST" -> "-"
        r.custody == "DEPOSITED" -> r.pickupPoint?.name ?: "Dititipkan di pos"
        r.custody == "AWAITING_DROPOFF" -> "Menunggu dititipkan di ${r.pickupPoint?.name ?: "pos"}"
        r.custody == "UNAVAILABLE" -> "Barang tidak tersedia"
        else -> "Dibawa oleh penemu"
    }

    fun toItem(r: ReportDto, contact: String? = null): Item {
        val location = listOfNotNull(
            r.locationName?.takeIf { it.isNotBlank() },
            r.locationArea?.takeIf { it.isNotBlank() && r.locationName?.contains(it) != true },
        ).joinToString(" · ").ifBlank { "-" }

        val desc = buildString {
            append(r.description.orEmpty())
            if (!r.color.isNullOrBlank()) append("\nWarna: ${r.color}")
        }.trim()

        return Item(
            id = r.id,
            idLaporan = "#" + r.id.take(8).uppercase(),
            title = r.title,
            kategori = categoryLabel(r.category),
            location = location,
            time = relativeTime(r.occurredAt ?: r.createdAt),
            status = if (r.type == "LOST") ItemStatus.DICARI else ItemStatus.DITEMUKAN,
            imageUrl = r.photoUrl,
            deskripsi = desc.ifBlank { "-" },
            titikPengambilan = custodyText(r),
            infoPelaporNama = r.owner?.name ?: "-",
            infoPelaporFakultas = r.owner?.faculty ?: "-",
            kontakPelapor = contact ?: "Tekan tombol di bawah untuk menghubungi",
            isOwner = r.viewer?.isOwner ?: false,
            canClaim = r.viewer?.canClaim ?: false,
            canContact = r.viewer?.canContact ?: false,
            myClaimStatus = r.viewer?.myClaimStatus,
        )
    }

    // ---------- Pickup point ----------

    fun toPickupPoint(p: PickupPointDto): PickupPoint {
        val name = p.name ?: "Pos Satpam"
        return PickupPoint(
            id = p.id,
            name = name,
            shortName = p.building ?: name.removePrefix("Pos Satpam ").trim(),
            locationDetail = listOfNotNull(p.description, p.openingHours?.let { "Jam buka: $it" })
                .joinToString(" · ").ifBlank { p.building ?: name },
            iconType = PickupPointIcon.SECURITY,
            latitude = p.latitude ?: -7.9528,
            longitude = p.longitude ?: 112.6145,
        )
    }
}
