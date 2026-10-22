package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.Hutang
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import com.example.data.remote.FirebaseSyncManager
import com.example.data.remote.GoogleDriveManager
import com.example.data.remote.RestoredBackupData
import com.example.data.remote.SyncResult
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FinanceRepository(
    private val database: AppDatabase,
    private val firebaseSyncManager: FirebaseSyncManager,
    private val googleDriveManager: GoogleDriveManager
) {
    private val nasabahDao = database.nasabahDao()
    private val kreditDao = database.kreditDao()
    private val installmentDao = database.installmentDao()
    private val hutangDao = database.hutangDao()
    private val topupDao = database.topupDao()
    private val hutangPaymentDao = database.hutangPaymentDao()

    // Flows for UI
    val allNasabah: Flow<List<Nasabah>> = nasabahDao.getAllNasabah()
    val allKredit: Flow<List<KreditPiutang>> = kreditDao.getAllKredit()
    val allInstallments: Flow<List<PiutangInstallment>> = installmentDao.getAllInstallments()
    val allHutang: Flow<List<Hutang>> = hutangDao.getAllHutang()
    val allTopup: Flow<List<TopupTransaction>> = topupDao.getAllTopup()
    val allHutangPayments: Flow<List<com.example.data.model.HutangPayment>> = hutangPaymentDao.getAllPayments()

    fun getNasabahById(id: Long): Flow<Nasabah?> = nasabahDao.getNasabahById(id)
    fun getKreditByCustomer(customerId: Long): Flow<List<KreditPiutang>> = kreditDao.getKreditByCustomer(customerId)
    fun getInstallmentsByKredit(kreditId: Long): Flow<List<PiutangInstallment>> = installmentDao.getInstallmentsByKredit(kreditId)
    fun getInstallmentsByCustomer(customerId: Long): Flow<List<PiutangInstallment>> = installmentDao.getInstallmentsByCustomer(customerId)

    suspend fun getNasabahByIdSync(id: Long): Nasabah? = nasabahDao.getNasabahByIdSync(id)
    suspend fun getKreditByIdSync(id: Long): KreditPiutang? = kreditDao.getKreditById(id)

    suspend fun insertNasabah(nasabah: Nasabah): Long {
        return nasabahDao.insertNasabah(nasabah)
    }

    /**
     * Menambahkan Kredit baru beserta auto-generate jadwal angsuran per tenor
     */
    suspend fun insertKreditWithInstallments(kredit: KreditPiutang): Long {
        val kreditId = kreditDao.insertKredit(kredit)

        val installments = mutableListOf<PiutangInstallment>()
        val calendar = Calendar.getInstance().apply {
            timeInMillis = kredit.tanggalMulai
        }

        // Jika mingguan dan hari penagihan dipilih (misal: setiap hari Kamis)
        val targetDayOfWeek = if (kredit.tenorUnit == "Minggu" && !kredit.hariPenagihan.isNullOrBlank()) {
            when (kredit.hariPenagihan.trim().lowercase()) {
                "senin" -> Calendar.MONDAY
                "selasa" -> Calendar.TUESDAY
                "rabu" -> Calendar.WEDNESDAY
                "kamis" -> Calendar.THURSDAY
                "jumat" -> Calendar.FRIDAY
                "sabtu" -> Calendar.SATURDAY
                "minggu" -> Calendar.SUNDAY
                else -> null
            }
        } else null

        if (targetDayOfWeek != null) {
            // Geser ke hari penagihan pertama yang cocok
            while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else if (kredit.tenorUnit == "Bulan" && kredit.tanggalPenagihan != null && kredit.tanggalPenagihan in 1..31) {
            // Jika bulanan dan tanggal penagihan dipilih (misal: tanggal 15 setiap bulan)
            val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            calendar.set(Calendar.DAY_OF_MONTH, kredit.tanggalPenagihan.coerceAtMost(maxDay))
        }

        for (i in 1..kredit.durasiTenor) {
            if (i > 1 || (targetDayOfWeek == null && (kredit.tenorUnit != "Bulan" || kredit.tanggalPenagihan == null))) {
                when (kredit.tenorUnit) {
                    "Minggu" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                    else -> calendar.add(Calendar.MONTH, 1) // default Bulan
                }
            }

            val nominalPerAngsuran = if (kredit.jumlahSetoran > 0 && kredit.durasiTenor > 1) {
                if (i < kredit.durasiTenor) {
                    kredit.jumlahSetoran
                } else {
                    val sisaTerakhir = kredit.totalPiutang - (kredit.jumlahSetoran * (kredit.durasiTenor - 1))
                    if (sisaTerakhir > 0) sisaTerakhir else kredit.jumlahSetoran
                }
            } else if (kredit.durasiTenor > 0) {
                kredit.totalPiutang / kredit.durasiTenor
            } else {
                kredit.totalPiutang
            }

            installments.add(
                PiutangInstallment(
                    kreditId = kreditId,
                    customerId = kredit.customerId,
                    customerName = kredit.customerName,
                    angsuranKe = i,
                    nominalRupiah = nominalPerAngsuran,
                    tanggalJatuhTempo = calendar.timeInMillis,
                    status = "NON"
                )
            )
        }

        installmentDao.insertAll(installments)
        return kreditId
    }

    /**
     * Membayar Angsuran Piutang (TUNAI / NON TUNAI)
     */
    suspend fun payInstallment(
        installmentId: Long,
        metodePembayaran: String
    ): PiutangInstallment? {
        val inst = installmentDao.getInstallmentById(installmentId) ?: return null
        val updatedInst = inst.copy(
            status = "LUNAS",
            metodePembayaran = metodePembayaran,
            tanggalBayar = System.currentTimeMillis()
        )
        installmentDao.updateInstallment(updatedInst)

        // Update Kredit akumulasi terbayar
        val kredit = kreditDao.getKreditById(inst.kreditId)
        if (kredit != null) {
            val newTerbayar = kredit.totalTerbayar + inst.nominalRupiah
            val isAllPaid = newTerbayar >= (kredit.totalPiutang - 100) // toleransi rounding
            val updatedKredit = kredit.copy(
                totalTerbayar = newTerbayar,
                status = if (isAllPaid) "LUNAS" else "BERJALAN"
            )
            kreditDao.updateKredit(updatedKredit)
        }

        return updatedInst
    }

    /**
     * Menambahkan Hutang Pribadi
     */
    suspend fun insertHutang(hutang: Hutang): Long {
        return hutangDao.insertHutang(hutang)
    }

    /**
     * Membayar Hutang Pribadi
     */
    suspend fun payHutang(
        hutangId: Long,
        nominalBayar: Double,
        tanggalBayarMillis: Long = System.currentTimeMillis(),
        catatan: String? = null
    ): Boolean {
        val hutang = hutangDao.getHutangById(hutangId) ?: return false
        val newBayar = hutang.hutangDibayar + nominalBayar
        val newSisa = (hutang.nominalHutang - newBayar).coerceAtLeast(0.0)
        val updated = hutang.copy(
            hutangDibayar = newBayar,
            sisaHutang = newSisa
        )
        hutangDao.updateHutang(updated)
        hutangPaymentDao.insertPayment(
            com.example.data.model.HutangPayment(
                hutangId = hutangId,
                namaSumber = hutang.namaSumber,
                nominalBayar = nominalBayar,
                tanggalBayar = tanggalBayarMillis,
                catatan = catatan
            )
        )
        return true
    }

    /**
     * Menambahkan Transaksi Topup
     */
    suspend fun insertTopup(topup: TopupTransaction): Long {
        return topupDao.insertTopup(topup)
    }

    /**
     * Membayar Transaksi Topup (Mendukung Pembayaran Lunas atau Sebagian/Cicil)
     */
    suspend fun payTopup(
        topupId: Long,
        nominalBayar: Double = 0.0,
        isFullLunas: Boolean = true
    ): Boolean {
        val topup = topupDao.getTopupById(topupId) ?: return false
        val sisa = topup.sisaNominal
        val isLunasNow = isFullLunas || nominalBayar >= sisa
        val newDibayar = if (isLunasNow) topup.nominal else (topup.totalDibayar + nominalBayar)
        val note = if (isLunasNow) {
            topup.catatan
        } else {
            "Dibayar sebagian: ${FormatUtils.formatRupiah(nominalBayar)}"
        }

        val updated = topup.copy(
            totalDibayar = newDibayar,
            isLunas = isLunasNow,
            tanggalBayar = if (isLunasNow) System.currentTimeMillis() else topup.tanggalBayar,
            catatan = note
        )
        topupDao.updateTopup(updated)
        return true
    }

    /**
     * Memperbarui Data Akun Nasabah dan Melakukan Restrukturisasi / Perubahan Tenor Piutang
     */
    suspend fun updateNasabahAndRestructure(
        nasabahId: Long,
        newName: String,
        newPhone: String,
        newPin: String,
        kreditIdToRestructure: Long? = null,
        tambahanBunga: Double = 0.0,
        newTenorUnit: String = "Bulan",
        newHariPenagihan: String? = null,
        newTanggalPenagihan: Int? = null,
        newJumlahSetoran: Double = 0.0,
        newDurasiTenor: Int = 1
    ): Boolean {
        val nasabah = nasabahDao.getNasabahByIdSync(nasabahId) ?: return false
        val updatedNasabah = nasabah.copy(
            name = newName.trim(),
            whatsappNumber = newPhone.trim(),
            pinLogin = newPin.trim()
        )
        nasabahDao.updateNasabah(updatedNasabah)

        if (kreditIdToRestructure != null) {
            val kredit = kreditDao.getKreditById(kreditIdToRestructure) ?: return true
            val sisaPiutangLama = (kredit.totalPiutang - kredit.totalTerbayar).coerceAtLeast(0.0)
            val totalSisaBaru = sisaPiutangLama + tambahanBunga.coerceAtLeast(0.0)

            val finalTenor = if (newDurasiTenor <= 0 && newJumlahSetoran > 0) {
                Math.ceil(totalSisaBaru / newJumlahSetoran).toInt().coerceAtLeast(1)
            } else {
                newDurasiTenor.coerceAtLeast(1)
            }

            val finalSetoran = if (newJumlahSetoran > 0) newJumlahSetoran else (totalSisaBaru / finalTenor)

            // Hapus jadwal angsuran yang belum dibayar
            installmentDao.deleteUnpaidInstallmentsByKreditId(kredit.id)

            // Hitung angsuran yang sudah lunas
            val paidInstallments = installmentDao.getPaidInstallmentsByKreditId(kredit.id)
            val startAngsuranKe = paidInstallments.size + 1

            // Buat jadwal angsuran baru
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
            }

            val targetDayOfWeek = if (newTenorUnit == "Minggu" && !newHariPenagihan.isNullOrBlank()) {
                when (newHariPenagihan.trim().lowercase()) {
                    "minggu" -> Calendar.SUNDAY
                    "senin" -> Calendar.MONDAY
                    "selasa" -> Calendar.TUESDAY
                    "rabu" -> Calendar.WEDNESDAY
                    "kamis" -> Calendar.THURSDAY
                    "jumat" -> Calendar.FRIDAY
                    "sabtu" -> Calendar.SATURDAY
                    else -> null
                }
            } else null

            if (targetDayOfWeek != null) {
                while (calendar.get(Calendar.DAY_OF_WEEK) != targetDayOfWeek) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            } else if (newTenorUnit == "Bulan" && newTanggalPenagihan != null && newTanggalPenagihan in 1..31) {
                val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                calendar.set(Calendar.DAY_OF_MONTH, newTanggalPenagihan.coerceAtMost(maxDay))
            }

            val newInstallments = mutableListOf<PiutangInstallment>()
            for (i in 1..finalTenor) {
                if (i > 1 || (targetDayOfWeek == null && (newTenorUnit != "Bulan" || newTanggalPenagihan == null))) {
                    when (newTenorUnit) {
                        "Minggu" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                        else -> calendar.add(Calendar.MONTH, 1)
                    }
                }

                val nominalItem = if (finalSetoran > 0 && finalTenor > 1) {
                    if (i < finalTenor) {
                        finalSetoran
                    } else {
                        val sisaTerakhir = totalSisaBaru - (finalSetoran * (finalTenor - 1))
                        if (sisaTerakhir > 0) sisaTerakhir else finalSetoran
                    }
                } else {
                    totalSisaBaru / finalTenor
                }

                newInstallments.add(
                    PiutangInstallment(
                        kreditId = kredit.id,
                        customerId = nasabah.id,
                        customerName = updatedNasabah.name,
                        angsuranKe = startAngsuranKe + i - 1,
                        nominalRupiah = nominalItem,
                        tanggalJatuhTempo = calendar.timeInMillis,
                        status = "NON"
                    )
                )
            }

            installmentDao.insertAll(newInstallments)

            val updatedKredit = kredit.copy(
                customerName = updatedNasabah.name,
                totalPiutang = kredit.totalTerbayar + totalSisaBaru,
                durasiTenor = paidInstallments.size + finalTenor,
                jumlahSetoran = finalSetoran,
                tenorUnit = newTenorUnit,
                hariPenagihan = newHariPenagihan?.trim()?.ifBlank { null },
                tanggalPenagihan = newTanggalPenagihan,
                status = "BERJALAN"
            )
            kreditDao.updateKredit(updatedKredit)
        } else {
            // Jika hanya update data nasabah (tanpa ubah tenor), perbarui nama di kredit & angsuran
            val customerKredits = kreditDao.getKreditListSync().filter { it.customerId == nasabah.id }
            customerKredits.forEach { k ->
                kreditDao.updateKredit(k.copy(customerName = updatedNasabah.name))
            }
        }
        return true
    }

    /**
     * Sinkronisasi seluruh database lokal ke Firestore (amar-e1ad6)
     */
    suspend fun syncAllToFirestore(): SyncResult {
        val nList = nasabahDao.getNasabahListSync()
        val kList = kreditDao.getKreditListSync()
        val iList = installmentDao.getInstallmentListSync()
        val hList = hutangDao.getHutangListSync()
        val tList = topupDao.getTopupListSync()
        val pList = hutangPaymentDao.getPaymentsListSync()

        return firebaseSyncManager.syncAllToFirestore(nList, kList, iList, hList, tList, pList)
    }

    fun getLastFirestoreSync(): Long = firebaseSyncManager.getLastSyncTime()

    /**
     * Mengunduh seluruh data terbaru dari Firebase Firestore ke database lokal Room
     */
    suspend fun downloadFromFirestore(): SyncResult {
        val (result, data) = firebaseSyncManager.downloadAllFromFirestore()
        if (data != null && result.isSuccess) {
            restoreDatabase(data)
        }
        return result
    }

    /**
     * Restore data ke Room
     */
    suspend fun restoreDatabase(data: RestoredBackupData) {
        if (data.nasabahList.isNotEmpty()) {
            nasabahDao.insertAll(data.nasabahList)
        }
        if (data.kreditList.isNotEmpty()) {
            kreditDao.insertAll(data.kreditList)
        }
        if (data.installmentList.isNotEmpty()) {
            installmentDao.insertAll(data.installmentList)
        }
        if (data.hutangList.isNotEmpty()) {
            hutangDao.insertAll(data.hutangList)
        }
        if (data.topupList.isNotEmpty()) {
            topupDao.insertAll(data.topupList)
        }
        if (data.hutangPaymentList.isNotEmpty()) {
            hutangPaymentDao.insertAll(data.hutangPaymentList)
        }
    }

    suspend fun getAllDataSync(): RestoredBackupData {
        return RestoredBackupData(
            nasabahList = nasabahDao.getNasabahListSync(),
            kreditList = kreditDao.getKreditListSync(),
            installmentList = installmentDao.getInstallmentListSync(),
            hutangList = hutangDao.getHutangListSync(),
            topupList = topupDao.getTopupListSync(),
            hutangPaymentList = hutangPaymentDao.getPaymentsListSync()
        )
    }

    /**
     * Inisialisasi data awal (demo data) jika database masih kosong
     */
    suspend fun prepopulateIfEmpty() {
        val existing = nasabahDao.getNasabahListSync()
        if (existing.isNotEmpty()) return

        // 1. Data Nasabah Contoh
        val n1 = Nasabah(
            customerCode = "cust-001",
            name = "Budi Santoso",
            whatsappNumber = "081234567890",
            pinLogin = "1234"
        )
        val n2 = Nasabah(
            customerCode = "cust-002",
            name = "Siti Rahmawati",
            whatsappNumber = "085712345678",
            pinLogin = "5678"
        )
        val n3 = Nasabah(
            customerCode = "cust-003",
            name = "Agus Pratama",
            whatsappNumber = "081398765432",
            pinLogin = "9988"
        )

        val id1 = nasabahDao.insertNasabah(n1)
        val id2 = nasabahDao.insertNasabah(n2)
        val id3 = nasabahDao.insertNasabah(n3)

        // 2. Kredit Piutang
        val k1 = KreditPiutang(
            customerId = id1,
            customerCode = n1.customerCode,
            customerName = n1.name,
            akadType = "Kredit Barang",
            itemName = "Kulkas Sharp 2 Pintu",
            nominalPokok = 3000000.0,
            persentaseBunga = 10.0,
            totalPiutang = 3300000.0,
            jumlahSetoran = 330000.0,
            durasiTenor = 10,
            tenorUnit = "Bulan",
            totalTerbayar = 660000.0,
            status = "BERJALAN"
        )
        insertKreditWithInstallments(k1)

        val k2 = KreditPiutang(
            customerId = id2,
            customerCode = n2.customerCode,
            customerName = n2.name,
            akadType = "Pinjaman Tunai",
            itemName = "Pinjaman Modal Usaha",
            nominalPokok = 2000000.0,
            persentaseBunga = 5.0,
            totalPiutang = 2100000.0,
            jumlahSetoran = 525000.0,
            durasiTenor = 4,
            tenorUnit = "Bulan",
            totalTerbayar = 0.0,
            status = "BERJALAN"
        )
        insertKreditWithInstallments(k2)

        val k3 = KreditPiutang(
            customerId = id3,
            customerCode = n3.customerCode,
            customerName = n3.name,
            akadType = "Kredit Barang",
            itemName = "Mesin Cuci Polytron",
            nominalPokok = 2500000.0,
            persentaseBunga = 8.0,
            totalPiutang = 2700000.0,
            jumlahSetoran = 450000.0,
            durasiTenor = 6,
            tenorUnit = "Bulan",
            totalTerbayar = 0.0,
            status = "BERJALAN"
        )
        insertKreditWithInstallments(k3)

        // Set installment 1 & 2 for Budi as paid
        val budiInst = installmentDao.getInstallmentListSync().filter { it.customerId == id1 }
        if (budiInst.isNotEmpty()) {
            payInstallment(budiInst[0].id, "TUNAI")
        }
        if (budiInst.size > 1) {
            payInstallment(budiInst[1].id, "NON TUNAI")
        }

        // 3. Hutang Pribadi
        val h1Id = hutangDao.insertHutang(
            Hutang(
                namaSumber = "Bank Mandiri (KTA)",
                nominalHutang = 5000000.0,
                hutangDibayar = 1500000.0,
                sisaHutang = 3500000.0
            )
        )
        hutangPaymentDao.insertPayment(
            com.example.data.model.HutangPayment(
                hutangId = h1Id,
                namaSumber = "Bank Mandiri (KTA)",
                nominalBayar = 1500000.0,
                tanggalBayar = System.currentTimeMillis() - (5 * 86400000L),
                catatan = "Pembayaran angsuran ke-1"
            )
        )

        val h2Id = hutangDao.insertHutang(
            Hutang(
                namaSumber = "Pak Hendra (Pinjaman)",
                nominalHutang = 2000000.0,
                hutangDibayar = 500000.0,
                sisaHutang = 1500000.0
            )
        )
        hutangPaymentDao.insertPayment(
            com.example.data.model.HutangPayment(
                hutangId = h2Id,
                namaSumber = "Pak Hendra (Pinjaman)",
                nominalBayar = 500000.0,
                tanggalBayar = System.currentTimeMillis() - (2 * 86400000L),
                catatan = "Cicilan via transfer"
            )
        )

        // 4. Topup Transaksi (Piutang langsung)
        topupDao.insertTopup(
            TopupTransaction(
                nama = "Topup Dana - Mas Dedi (Bayar Besok)",
                nominal = 250000.0,
                keuntungan = 3000.0,
                tanggalTransaksi = System.currentTimeMillis(),
                tanggalJatuhTempo = System.currentTimeMillis() + 86400000L,
                isLunas = false
            )
        )
        topupDao.insertTopup(
            TopupTransaction(
                nama = "Pulsa & Listrik - Bu RT (Minggu Depan)",
                nominal = 150000.0,
                keuntungan = 2500.0,
                tanggalTransaksi = System.currentTimeMillis(),
                tanggalJatuhTempo = System.currentTimeMillis() + (7 * 86400000L),
                isLunas = false
            )
        )
    }

    /**
     * Factory Reset: Hapus seluruh data dari semua tabel database
     */
    suspend fun resetAllDatabase() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            database.clearAllTables()
            installmentDao.deleteAll()
            kreditDao.deleteAll()
            nasabahDao.deleteAll()
            hutangDao.deleteAll()
            hutangPaymentDao.deleteAll()
            topupDao.deleteAll()
        }
    }
}
