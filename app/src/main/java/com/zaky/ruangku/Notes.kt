package com.zaky.ruangku

import java.io.Serializable

data class Notes(
    val id: Int,
    val judul: String,
    val catatan: String,
    val status: String,
    val tanggal: String,
    val kategori: String,
    val prioritas: String
): Serializable {
    companion object {
        val PILIHAN_STATUS = arrayOf("Belum Selesai", "Proses", "Selesai")
        val PILIHAN_KATEGORI = arrayOf("Pribadi", "Kantor", "Sekolah", "Kampus", "Keluarga", "Lainnya")
        val PILIHAN_PRIORITAS = arrayOf("High", "Medium", "Low")
    }
}
