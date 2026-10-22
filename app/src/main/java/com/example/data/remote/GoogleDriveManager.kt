package com.example.data.remote

import android.content.Context
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class DriveAuthStatus(
    val isConnected: Boolean,
    val accountEmail: String?,
    val lastBackupTime: Long
)

data class DriveOperationResult(
    val isSuccess: Boolean,
    val message: String,
    val fileId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class RestoredBackupData(
    val nasabahList: List<Nasabah>,
    val kreditList: List<KreditPiutang>,
    val installmentList: List<PiutangInstallment>,
    val hutangList: List<Hutang>,
    val topupList: List<TopupTransaction>,
    val hutangPaymentList: List<HutangPayment> = emptyList()
)

class GoogleDriveManager(
    private val context: Context,
    private val firebaseSyncManager: FirebaseSyncManager = FirebaseSyncManager(context)
) {

    private val prefs = context.getSharedPreferences("gdrive_prefs", Context.MODE_PRIVATE)

    companion object {
        const val DRIVE_FILE_NAME = "Backup_Manajemen_Hutang_Piutang.json"
        const val BACKUP_CACHE_FILE = "gdrive_last_backup.json"
    }

    fun getAuthStatus(): DriveAuthStatus {
        val isConnected = prefs.getBoolean("gdrive_connected", false)
        val email = prefs.getString("gdrive_email", null)
        val lastBackup = prefs.getLong("gdrive_last_backup_time", 0L)
        return DriveAuthStatus(isConnected, email, lastBackup)
    }

    fun isFirstLaunch(): Boolean {
        return !prefs.getBoolean("has_prompted_first_launch_auth", false)
    }

    fun setFirstLaunchPrompted() {
        prefs.edit().putBoolean("has_prompted_first_launch_auth", true).apply()
    }

    fun connectAccount(email: String) {
        val clean = email.trim()
        prefs.edit()
            .putBoolean("gdrive_connected", true)
            .putString("gdrive_email", clean)
            .apply()
    }

    fun disconnectAccount() {
        prefs.edit()
            .putBoolean("gdrive_connected", false)
            .remove("gdrive_email")
            .remove("gdrive_token")
            .apply()
    }

    /**
     * Serialisasi seluruh data aplikasi ke format string JSON yang rapi
     */
    fun generateJsonBackup(
        nasabahList: List<Nasabah>,
        kreditList: List<KreditPiutang>,
        installmentList: List<PiutangInstallment>,
        hutangList: List<Hutang>,
        topupList: List<TopupTransaction>,
        hutangPaymentList: List<HutangPayment> = emptyList(),
        account: String = "Akun Pengguna"
    ): String {
        val root = JSONObject().apply {
            put("version", 1)
            put("appName", "Manajemen Hutang Piutang")
            put("createdAt", System.currentTimeMillis())
            put("account", account)

            // Nasabah
            val arrNasabah = JSONArray()
            nasabahList.forEach {
                arrNasabah.put(JSONObject().apply {
                    put("id", it.id)
                    put("customerCode", it.customerCode)
                    put("name", it.name)
                    put("whatsappNumber", it.whatsappNumber)
                    put("pinLogin", it.pinLogin)
                    put("createdAt", it.createdAt)
                })
            }
            put("nasabah", arrNasabah)

            // Kredit
            val arrKredit = JSONArray()
            kreditList.forEach {
                arrKredit.put(JSONObject().apply {
                    put("id", it.id)
                    put("customerId", it.customerId)
                    put("customerCode", it.customerCode)
                    put("customerName", it.customerName)
                    put("akadType", it.akadType)
                    put("itemName", it.itemName)
                    put("nominalPokok", it.nominalPokok)
                    put("persentaseBunga", it.persentaseBunga)
                    put("totalPiutang", it.totalPiutang)
                    put("jumlahSetoran", it.jumlahSetoran)
                    put("durasiTenor", it.durasiTenor)
                    put("tenorUnit", it.tenorUnit)
                    put("hariPenagihan", it.hariPenagihan ?: "")
                    put("tanggalPenagihan", it.tanggalPenagihan ?: 0)
                    put("totalTerbayar", it.totalTerbayar)
                    put("status", it.status)
                    put("tanggalMulai", it.tanggalMulai)
                    put("createdAt", it.createdAt)
                })
            }
            put("kredit", arrKredit)

            // Installments
            val arrInst = JSONArray()
            installmentList.forEach {
                arrInst.put(JSONObject().apply {
                    put("id", it.id)
                    put("kreditId", it.kreditId)
                    put("customerId", it.customerId)
                    put("customerName", it.customerName)
                    put("angsuranKe", it.angsuranKe)
                    put("nominalRupiah", it.nominalRupiah)
                    put("tanggalJatuhTempo", it.tanggalJatuhTempo)
                    put("status", it.status)
                    put("metodePembayaran", it.metodePembayaran ?: "")
                    put("tanggalBayar", it.tanggalBayar ?: 0L)
                    put("catatan", it.catatan ?: "")
                })
            }
            put("installments", arrInst)

            // Hutang
            val arrHutang = JSONArray()
            hutangList.forEach {
                arrHutang.put(JSONObject().apply {
                    put("id", it.id)
                    put("namaSumber", it.namaSumber)
                    put("nominalHutang", it.nominalHutang)
                    put("hutangDibayar", it.hutangDibayar)
                    put("sisaHutang", it.sisaHutang)
                    put("tanggalCatat", it.tanggalCatat)
                    put("catatan", it.catatan ?: "")
                })
            }
            put("hutang", arrHutang)

            // Topup
            val arrTopup = JSONArray()
            topupList.forEach {
                arrTopup.put(JSONObject().apply {
                    put("id", it.id)
                    put("nama", it.nama)
                    put("nominal", it.nominal)
                    put("totalDibayar", it.totalDibayar)
                    put("keuntungan", it.keuntungan)
                    put("tanggalTransaksi", it.tanggalTransaksi)
                    put("tanggalJatuhTempo", it.tanggalJatuhTempo)
                    put("isLunas", it.isLunas)
                    put("tanggalBayar", it.tanggalBayar ?: 0L)
                    put("catatan", it.catatan ?: "")
                })
            }
            put("topup", arrTopup)

            // Hutang Payment
            val arrPay = JSONArray()
            hutangPaymentList.forEach {
                arrPay.put(JSONObject().apply {
                    put("id", it.id)
                    put("hutangId", it.hutangId)
                    put("namaSumber", it.namaSumber)
                    put("nominalBayar", it.nominalBayar)
                    put("tanggalBayar", it.tanggalBayar)
                    put("catatan", it.catatan ?: "")
                })
            }
            put("hutangPayments", arrPay)
        }
        return root.toString(2)
    }

    /**
     * Parsing JSON String kembali ke objek RestoredBackupData
     */
    fun parseJsonBackup(jsonContent: String): RestoredBackupData {
        val root = JSONObject(jsonContent)

        // Parse Nasabah
        val nasabahList = mutableListOf<Nasabah>()
        val arrNasabah = root.optJSONArray("nasabah") ?: JSONArray()
        for (i in 0 until arrNasabah.length()) {
            val obj = arrNasabah.getJSONObject(i)
            nasabahList.add(
                Nasabah(
                    id = obj.optLong("id", 0L),
                    customerCode = obj.optString("customerCode", "cust-000"),
                    name = obj.optString("name", "Tanpa Nama"),
                    whatsappNumber = obj.optString("whatsappNumber", ""),
                    pinLogin = obj.optString("pinLogin", "1234"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Kredit
        val kreditList = mutableListOf<KreditPiutang>()
        val arrKredit = root.optJSONArray("kredit") ?: JSONArray()
        for (i in 0 until arrKredit.length()) {
            val obj = arrKredit.getJSONObject(i)
            val hari = obj.optString("hariPenagihan").ifEmpty { null }
            val tglPenagihan = obj.optInt("tanggalPenagihan", 0).takeIf { it > 0 }
            kreditList.add(
                KreditPiutang(
                    id = obj.optLong("id", 0L),
                    customerId = obj.optLong("customerId", 0L),
                    customerCode = obj.optString("customerCode", ""),
                    customerName = obj.optString("customerName", ""),
                    akadType = obj.optString("akadType", "Kredit Barang"),
                    itemName = obj.optString("itemName", "-"),
                    nominalPokok = obj.optDouble("nominalPokok", 0.0),
                    persentaseBunga = obj.optDouble("persentaseBunga", 0.0),
                    totalPiutang = obj.optDouble("totalPiutang", 0.0),
                    jumlahSetoran = obj.optDouble("jumlahSetoran", 0.0),
                    durasiTenor = obj.optInt("durasiTenor", 1),
                    tenorUnit = obj.optString("tenorUnit", "Bulan"),
                    hariPenagihan = hari,
                    tanggalPenagihan = tglPenagihan,
                    totalTerbayar = obj.optDouble("totalTerbayar", 0.0),
                    status = obj.optString("status", "BERJALAN"),
                    tanggalMulai = obj.optLong("tanggalMulai", System.currentTimeMillis()),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Parse Installments
        val installmentList = mutableListOf<PiutangInstallment>()
        val arrInst = root.optJSONArray("installments") ?: JSONArray()
        for (i in 0 until arrInst.length()) {
            val obj = arrInst.getJSONObject(i)
            installmentList.add(
                PiutangInstallment(
                    id = obj.optLong("id", 0L),
                    kreditId = obj.optLong("kreditId", 0L),
                    customerId = obj.optLong("customerId", 0L),
                    customerName = obj.optString("customerName", ""),
                    angsuranKe = obj.optInt("angsuranKe", 1),
                    nominalRupiah = obj.optDouble("nominalRupiah", 0.0),
                    tanggalJatuhTempo = obj.optLong("tanggalJatuhTempo", System.currentTimeMillis()),
                    status = obj.optString("status", "NON"),
                    metodePembayaran = obj.optString("metodePembayaran").ifEmpty { null },
                    tanggalBayar = if (obj.optLong("tanggalBayar", 0L) > 0) obj.optLong("tanggalBayar") else null,
                    catatan = obj.optString("catatan").ifEmpty { null }
                )
            )
        }

        // Parse Hutang
        val hutangList = mutableListOf<Hutang>()
        val arrHutang = root.optJSONArray("hutang") ?: JSONArray()
        for (i in 0 until arrHutang.length()) {
            val obj = arrHutang.getJSONObject(i)
            val nominal = obj.optDouble("nominalHutang", 0.0)
            val bayar = obj.optDouble("hutangDibayar", 0.0)
            hutangList.add(
                Hutang(
                    id = obj.optLong("id", 0L),
                    namaSumber = obj.optString("namaSumber", "Sumber Hutang"),
                    nominalHutang = nominal,
                    hutangDibayar = bayar,
                    sisaHutang = obj.optDouble("sisaHutang", nominal - bayar),
                    tanggalCatat = obj.optLong("tanggalCatat", System.currentTimeMillis()),
                    catatan = obj.optString("catatan").ifEmpty { null }
                )
            )
        }

        // Parse Topup
        val topupList = mutableListOf<TopupTransaction>()
        val arrTopup = root.optJSONArray("topup") ?: JSONArray()
        for (i in 0 until arrTopup.length()) {
            val obj = arrTopup.getJSONObject(i)
            val nominal = obj.optDouble("nominal", 0.0)
            val isLunas = obj.optBoolean("isLunas", false)
            val totalDibayar = obj.optDouble("totalDibayar", if (isLunas) nominal else 0.0)
            topupList.add(
                TopupTransaction(
                    id = obj.optLong("id", 0L),
                    nama = obj.optString("nama", "Topup"),
                    nominal = nominal,
                    totalDibayar = totalDibayar,
                    keuntungan = obj.optDouble("keuntungan", 0.0),
                    tanggalTransaksi = obj.optLong("tanggalTransaksi", System.currentTimeMillis()),
                    tanggalJatuhTempo = obj.optLong("tanggalJatuhTempo", System.currentTimeMillis()),
                    isLunas = isLunas,
                    tanggalBayar = if (obj.optLong("tanggalBayar", 0L) > 0) obj.optLong("tanggalBayar") else null,
                    catatan = obj.optString("catatan").ifEmpty { null }
                )
            )
        }

        // Parse Hutang Payment
        val hutangPaymentList = mutableListOf<HutangPayment>()
        val arrPay = root.optJSONArray("hutangPayments") ?: JSONArray()
        for (i in 0 until arrPay.length()) {
            val obj = arrPay.getJSONObject(i)
            hutangPaymentList.add(
                HutangPayment(
                    id = obj.optLong("id", 0L),
                    hutangId = obj.optLong("hutangId", 0L),
                    namaSumber = obj.optString("namaSumber", ""),
                    nominalBayar = obj.optDouble("nominalBayar", 0.0),
                    tanggalBayar = obj.optLong("tanggalBayar", System.currentTimeMillis()),
                    catatan = obj.optString("catatan").ifEmpty { null }
                )
            )
        }

        return RestoredBackupData(nasabahList, kreditList, installmentList, hutangList, topupList, hutangPaymentList)
    }

    /**
     * Backup data ke Cloud Server (Firebase Firestore) dan file cache lokal
     */
    suspend fun backupToGoogleDrive(
        nasabahList: List<Nasabah>,
        kreditList: List<KreditPiutang>,
        installmentList: List<PiutangInstallment>,
        hutangList: List<Hutang>,
        topupList: List<TopupTransaction>,
        hutangPaymentList: List<HutangPayment> = emptyList(),
        emailOverride: String? = null
    ): DriveOperationResult = withContext(Dispatchers.IO) {
        val auth = getAuthStatus()
        val account = emailOverride?.trim()?.takeIf { it.isNotBlank() } ?: auth.accountEmail ?: "Akun Cloud"

        try {
            val totalRecords = nasabahList.size + kreditList.size + installmentList.size + hutangList.size + topupList.size + hutangPaymentList.size
            val summary = "${nasabahList.size} Nasabah, ${kreditList.size} Kredit, ${hutangList.size} Hutang, ${topupList.size} Topup"

            // 1. Buat string JSON
            val jsonContent = generateJsonBackup(
                nasabahList,
                kreditList,
                installmentList,
                hutangList,
                topupList,
                hutangPaymentList,
                account
            )

            // 2. Simpan cadangan ke cache internal lokal
            val backupFile = File(context.filesDir, BACKUP_CACHE_FILE)
            backupFile.writeText(jsonContent)

            // 3. Simpan cadangan snapshot ke Firebase Firestore (Collection cloud_backups / {account})
            var cloudSuccess = false
            var cloudMessage = ""
            if (account.isNotBlank() && account.contains("@")) {
                connectAccount(account)
                val cloudResult = firebaseSyncManager.saveBackupSnapshotToCloud(
                    email = account,
                    jsonContent = jsonContent,
                    totalRecords = totalRecords,
                    summary = summary
                )
                cloudSuccess = cloudResult.isSuccess
                cloudMessage = cloudResult.message

                // Jalankan juga sync tabel Firestore
                try {
                    firebaseSyncManager.syncAllToFirestore(
                        nasabahList,
                        kreditList,
                        installmentList,
                        hutangList,
                        topupList,
                        hutangPaymentList
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val now = System.currentTimeMillis()
            prefs.edit().putLong("gdrive_last_backup_time", now).apply()

            val displayMsg = if (cloudSuccess) {
                "Cadangan berhasil disimpan ke Cloud Server untuk akun $account ($totalRecords data)."
            } else {
                "Cadangan lokal berhasil disimpan ($totalRecords data). " + if (cloudMessage.isNotBlank()) "Cloud: $cloudMessage" else ""
            }

            DriveOperationResult(
                isSuccess = true,
                message = displayMsg,
                fileId = "CLOUD-${now % 1000000}",
                timestamp = now
            )
        } catch (e: Exception) {
            e.printStackTrace()
            DriveOperationResult(
                isSuccess = false,
                message = "Gagal mem-backup data: ${e.message}"
            )
        }
    }

    /**
     * Restore data dari Cloud Server (Firestore) atau file cache
     */
    suspend fun restoreFromGoogleDrive(emailOverride: String? = null): Pair<DriveOperationResult, RestoredBackupData?> = withContext(Dispatchers.IO) {
        val auth = getAuthStatus()
        val account = emailOverride?.trim()?.takeIf { it.isNotBlank() } ?: auth.accountEmail

        try {
            // 1. Coba ambil dari Firestore Cloud Snapshot berdasarkan email akun
            if (!account.isNullOrBlank() && account.contains("@")) {
                val (cloudResult, jsonFromCloud) = firebaseSyncManager.fetchBackupSnapshotFromCloud(account)
                if (cloudResult.isSuccess && !jsonFromCloud.isNullOrBlank()) {
                    val data = parseJsonBackup(jsonFromCloud)
                    // Simpan ke cache lokal juga
                    File(context.filesDir, BACKUP_CACHE_FILE).writeText(jsonFromCloud)
                    connectAccount(account)

                    val result = DriveOperationResult(
                        isSuccess = true,
                        message = "Pulihkan data dari Cloud berhasil! (${data.nasabahList.size} Nasabah, ${data.kreditList.size} Kredit, ${data.hutangList.size} Hutang, ${data.topupList.size} Topup) untuk akun $account"
                    )
                    return@withContext Pair(result, data)
                }
            }

            // 2. Coba ambil dari Firestore collection individual jika snapshot belum ada
            val (downloadResult, downloadData) = firebaseSyncManager.downloadAllFromFirestore()
            if (downloadResult.isSuccess && downloadData != null && downloadResult.totalRecords > 0) {
                val result = DriveOperationResult(
                    isSuccess = true,
                    message = "Pulihkan data dari Cloud Database berhasil! (${downloadData.nasabahList.size} Nasabah, ${downloadData.kreditList.size} Kredit, ${downloadData.hutangList.size} Hutang, ${downloadData.topupList.size} Topup)"
                )
                return@withContext Pair(result, downloadData)
            }

            // 3. Jika cloud tidak ada / offline, coba baca file cache lokal di perangkat
            val backupFile = File(context.filesDir, BACKUP_CACHE_FILE)
            if (backupFile.exists() && backupFile.length() > 0L) {
                val localJson = backupFile.readText()
                val data = parseJsonBackup(localJson)
                val result = DriveOperationResult(
                    isSuccess = true,
                    message = "Data dipulihkan dari cadangan lokal perangkat (${data.nasabahList.size} Nasabah, ${data.kreditList.size} Kredit)."
                )
                return@withContext Pair(result, data)
            }

            val targetAcc = if (!account.isNullOrBlank()) "akun '$account'" else "akun Anda"
            Pair(
                DriveOperationResult(
                    isSuccess = false,
                    message = "Data cadangan untuk $targetAcc tidak ditemukan di Cloud Server. Pastikan email sama dengan saat melakukan backup di HP pertama."
                ),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(
                DriveOperationResult(isSuccess = false, message = "Gagal me-restore data: ${e.message}"),
                null
            )
        }
    }

    /**
     * Tulis data JSON cadangan langsung ke OutputStream (Storage Access Framework)
     */
    fun writeBackupToStream(outputStream: OutputStream, data: RestoredBackupData): Boolean {
        return try {
            val json = generateJsonBackup(
                data.nasabahList,
                data.kreditList,
                data.installmentList,
                data.hutangList,
                data.topupList,
                data.hutangPaymentList,
                getAuthStatus().accountEmail ?: "Ekspor File"
            )
            outputStream.bufferedWriter().use { it.write(json) }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Baca data JSON cadangan langsung dari InputStream (Storage Access Framework)
     */
    fun readBackupFromStream(inputStream: InputStream): Pair<Boolean, RestoredBackupData?> {
        return try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val data = parseJsonBackup(json)
            Pair(true, data)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, null)
        }
    }
}
