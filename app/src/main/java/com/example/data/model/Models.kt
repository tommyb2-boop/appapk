package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model data Nasabah
 */
@Entity(tableName = "nasabah")
data class Nasabah(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerCode: String, // misal: cust-001
    val name: String,
    val whatsappNumber: String,
    val pinLogin: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Model data Kontrak Kredit Barang / Pinjaman Tunai
 */
@Entity(tableName = "kredit_piutang")
data class KreditPiutang(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerCode: String,
    val customerName: String,
    val akadType: String, // "Kredit Barang" atau "Pinjaman Tunai"
    val itemName: String, // Nama Kredit Barang / Pinjaman Tunai
    val nominalPokok: Double,
    val persentaseBunga: Double, // % margin / bunga
    val totalPiutang: Double, // Pokok + (Pokok * % / 100)
    val jumlahSetoran: Double, // Angsuran per periode
    val durasiTenor: Int, // Jumlah tenor (misal 10x)
    val tenorUnit: String = "Bulan", // "Minggu" atau "Bulan"
    val hariPenagihan: String? = null, // Misal: "Kamis" (untuk mingguan, opsional)
    val tanggalPenagihan: Int? = null, // Misal: 15 (untuk bulanan, opsional)
    val totalTerbayar: Double = 0.0,
    val status: String = "BERJALAN", // "BERJALAN", "LUNAS"
    val tanggalMulai: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Model data Rincian Piutang / Tagihan per Jatuh Tempo (Angsuran)
 */
@Entity(tableName = "piutang_installment")
data class PiutangInstallment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val kreditId: Long,
    val customerId: Long,
    val customerName: String,
    val angsuranKe: Int,
    val nominalRupiah: Double,
    val tanggalJatuhTempo: Long,
    val status: String = "NON", // "NON" (belum lunas) atau "LUNAS"
    val metodePembayaran: String? = null, // "TUNAI" atau "NON TUNAI"
    val tanggalBayar: Long? = null,
    val catatan: String? = null
)

/**
 * Model data Pencatatan Hutang Pribadi
 */
@Entity(tableName = "hutang_pribadi")
data class Hutang(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val namaSumber: String, // Nama Sumber hutang (e.g. Bank BCA, Rekan Kerja, Toko)
    val nominalHutang: Double,
    val hutangDibayar: Double = 0.0,
    val sisaHutang: Double = nominalHutang - hutangDibayar,
    val tanggalCatat: Long = System.currentTimeMillis(),
    val catatan: String? = null
)

/**
 * Model data Pencatatan Piutang / Transaksi Langsung (Topup)
 * Layanan Topup dengan pencatatan keuntungan / margin rupiah
 */
@Entity(tableName = "topup_transaction")
data class TopupTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nama: String,
    val nominal: Double,
    val totalDibayar: Double = 0.0,
    val keuntungan: Double = 0.0, // Keuntungan layanan Topup (Rupiah)
    val tanggalTransaksi: Long = System.currentTimeMillis(),
    val tanggalJatuhTempo: Long,
    val isLunas: Boolean = false,
    val tanggalBayar: Long? = null,
    val catatan: String? = null
) {
    val sisaNominal: Double get() = (nominal - totalDibayar).coerceAtLeast(0.0)
}

/**
 * Model data Riwayat Pembayaran Hutang Pribadi
 */
@Entity(tableName = "hutang_payment")
data class HutangPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hutangId: Long,
    val namaSumber: String,
    val nominalBayar: Double,
    val tanggalBayar: Long = System.currentTimeMillis(),
    val catatan: String? = null
)
