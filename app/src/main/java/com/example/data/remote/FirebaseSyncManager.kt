package com.example.data.remote

import android.content.Context
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class SyncResult(
    val isSuccess: Boolean,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalRecords: Int = 0
)

class FirebaseSyncManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    companion object {
        const val FIREBASE_API_KEY = "AIzaSyDrUSIl6FBJysL_ngyYiTAFLJbc4rhc32s"
        const val FIREBASE_PROJECT_ID = "amar-e1ad6"
        const val FIREBASE_APP_ID = "1:958717767365:web:b43984cd6b7f6ec8cecaac"
        const val FIREBASE_STORAGE_BUCKET = "amar-e1ad6.firebasestorage.app"
        const val FIREBASE_SENDER_ID = "958717767365"
    }

    init {
        ensureFirebaseInitialized()
    }

    private fun ensureFirebaseInitialized() {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey(FIREBASE_API_KEY)
                    .setApplicationId(FIREBASE_APP_ID)
                    .setProjectId(FIREBASE_PROJECT_ID)
                    .setStorageBucket(FIREBASE_STORAGE_BUCKET)
                    .setGcmSenderId(FIREBASE_SENDER_ID)
                    .build()
                FirebaseApp.initializeApp(context, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong("last_firestore_sync", 0L)
    }

    private fun saveLastSyncTime(time: Long) {
        prefs.edit().putLong("last_firestore_sync", time).apply()
    }

    suspend fun syncAllToFirestore(
        nasabahList: List<Nasabah>,
        kreditList: List<KreditPiutang>,
        installmentList: List<PiutangInstallment>,
        hutangList: List<Hutang>,
        topupList: List<TopupTransaction>,
        paymentList: List<HutangPayment> = emptyList()
    ): SyncResult = withContext(Dispatchers.IO) {
        ensureFirebaseInitialized()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val batch = firestore.batch()

            // 1. Sync Nasabah
            for (item in nasabahList) {
                val docRef = firestore.collection("nasabah").document("cust_${item.id}")
                val map = hashMapOf(
                    "id" to item.id,
                    "customerCode" to item.customerCode,
                    "name" to item.name,
                    "whatsappNumber" to item.whatsappNumber,
                    "pinLogin" to item.pinLogin,
                    "createdAt" to item.createdAt,
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            // 2. Sync Kredit
            for (item in kreditList) {
                val docRef = firestore.collection("kredit_piutang").document("kredit_${item.id}")
                val map = hashMapOf(
                    "id" to item.id,
                    "customerId" to item.customerId,
                    "customerCode" to item.customerCode,
                    "customerName" to item.customerName,
                    "akadType" to item.akadType,
                    "itemName" to item.itemName,
                    "nominalPokok" to item.nominalPokok,
                    "persentaseBunga" to item.persentaseBunga,
                    "totalPiutang" to item.totalPiutang,
                    "jumlahSetoran" to item.jumlahSetoran,
                    "durasiTenor" to item.durasiTenor,
                    "tenorUnit" to item.tenorUnit,
                    "totalTerbayar" to item.totalTerbayar,
                    "status" to item.status,
                    "tanggalMulai" to item.tanggalMulai,
                    "createdAt" to item.createdAt,
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            // 3. Sync Installments
            for (item in installmentList) {
                val docRef = firestore.collection("piutang_installment").document("inst_${item.id}")
                val map = hashMapOf(
                    "id" to item.id,
                    "kreditId" to item.kreditId,
                    "customerId" to item.customerId,
                    "customerName" to item.customerName,
                    "angsuranKe" to item.angsuranKe,
                    "nominalRupiah" to item.nominalRupiah,
                    "tanggalJatuhTempo" to item.tanggalJatuhTempo,
                    "status" to item.status,
                    "metodePembayaran" to (item.metodePembayaran ?: ""),
                    "tanggalBayar" to (item.tanggalBayar ?: 0L),
                    "catatan" to (item.catatan ?: ""),
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            // 4. Sync Hutang
            for (item in hutangList) {
                val docRef = firestore.collection("hutang_pribadi").document("hutang_${item.id}")
                val map = hashMapOf(
                    "id" to item.id,
                    "namaSumber" to item.namaSumber,
                    "nominalHutang" to item.nominalHutang,
                    "hutangDibayar" to item.hutangDibayar,
                    "sisaHutang" to item.sisaHutang,
                    "tanggalCatat" to item.tanggalCatat,
                    "catatan" to (item.catatan ?: ""),
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            // 5. Sync Topup
            for (item in topupList) {
                val docRef = firestore.collection("topup_transaction").document("topup_${item.id}")
                val map = hashMapOf(
                    "id" to item.id,
                    "nama" to item.nama,
                    "nominal" to item.nominal,
                    "totalDibayar" to item.totalDibayar,
                    "keuntungan" to item.keuntungan,
                    "tanggalTransaksi" to item.tanggalTransaksi,
                    "tanggalJatuhTempo" to item.tanggalJatuhTempo,
                    "isLunas" to item.isLunas,
                    "tanggalBayar" to (item.tanggalBayar ?: 0L),
                    "catatan" to (item.catatan ?: ""),
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            // 6. Sync Hutang Payment
            for (item in paymentList) {
                val docRef = firestore.collection("hutang_payment").document("pay_${item.id}")
                val map = hashMapOf(
                    "id" to item.id,
                    "hutangId" to item.hutangId,
                    "namaSumber" to item.namaSumber,
                    "nominalBayar" to item.nominalBayar,
                    "tanggalBayar" to item.tanggalBayar,
                    "catatan" to (item.catatan ?: ""),
                    "syncedAt" to System.currentTimeMillis()
                )
                batch.set(docRef, map, SetOptions.merge())
            }

            // Sync Metadata
            val now = System.currentTimeMillis()
            val totalCount = nasabahList.size + kreditList.size + installmentList.size + hutangList.size + topupList.size + paymentList.size
            val metaRef = firestore.collection("sync_metadata").document("last_sync")
            batch.set(
                metaRef,
                hashMapOf(
                    "lastSyncedAt" to now,
                    "totalRecords" to totalCount,
                    "appVersion" to "1.0",
                    "device" to "Android"
                ),
                SetOptions.merge()
            )

            batch.commit().await()
            saveLastSyncTime(now)

            SyncResult(
                isSuccess = true,
                message = "Berhasil mengunggah $totalCount data ke Firebase Firestore (amar-e1ad6)",
                timestamp = now,
                totalRecords = totalCount
            )
        } catch (e: Exception) {
            e.printStackTrace()
            val now = System.currentTimeMillis()
            saveLastSyncTime(now)
            SyncResult(
                isSuccess = false,
                message = "Gagal terhubung ke Firestore: ${e.localizedMessage ?: "Cek koneksi internet"}",
                timestamp = now
            )
        }
    }

    /**
     * Download / Ambil seluruh data dari Firestore ke Room
     * Digunakan oleh Customer atau Admin untuk mengambil data tagihan terbaru dari cloud
     */
    suspend fun downloadAllFromFirestore(): Pair<SyncResult, RestoredBackupData?> = withContext(Dispatchers.IO) {
        ensureFirebaseInitialized()
        try {
            kotlinx.coroutines.withTimeout(6000L) {
                val firestore = FirebaseFirestore.getInstance()

                // 1. Fetch Nasabah
                val nasabahSnapshot = firestore.collection("nasabah").get().await()
                val nList = nasabahSnapshot.documents.mapNotNull { doc ->
                    try {
                        Nasabah(
                            id = doc.getLong("id") ?: 0L,
                            customerCode = doc.getString("customerCode") ?: "",
                            name = doc.getString("name") ?: "",
                            whatsappNumber = doc.getString("whatsappNumber") ?: "",
                            pinLogin = doc.getString("pinLogin") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // 2. Fetch Kredit
                val kreditSnapshot = firestore.collection("kredit_piutang").get().await()
                val kList = kreditSnapshot.documents.mapNotNull { doc ->
                    try {
                        KreditPiutang(
                            id = doc.getLong("id") ?: 0L,
                            customerId = doc.getLong("customerId") ?: 0L,
                            customerCode = doc.getString("customerCode") ?: "",
                            customerName = doc.getString("customerName") ?: "",
                            akadType = doc.getString("akadType") ?: "Murabahah",
                            itemName = doc.getString("itemName") ?: "",
                            nominalPokok = doc.getDouble("nominalPokok") ?: 0.0,
                            persentaseBunga = doc.getDouble("persentaseBunga") ?: 0.0,
                            totalPiutang = doc.getDouble("totalPiutang") ?: 0.0,
                            jumlahSetoran = doc.getDouble("jumlahSetoran") ?: 0.0,
                            durasiTenor = doc.getLong("durasiTenor")?.toInt() ?: 1,
                            tenorUnit = doc.getString("tenorUnit") ?: "Bulan",
                            totalTerbayar = doc.getDouble("totalTerbayar") ?: 0.0,
                            status = doc.getString("status") ?: "BERJALAN",
                            tanggalMulai = doc.getLong("tanggalMulai") ?: System.currentTimeMillis(),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // 3. Fetch Installments
                val instSnapshot = firestore.collection("piutang_installment").get().await()
                val iList = instSnapshot.documents.mapNotNull { doc ->
                    try {
                        PiutangInstallment(
                            id = doc.getLong("id") ?: 0L,
                            kreditId = doc.getLong("kreditId") ?: 0L,
                            customerId = doc.getLong("customerId") ?: 0L,
                            customerName = doc.getString("customerName") ?: "",
                            angsuranKe = doc.getLong("angsuranKe")?.toInt() ?: 1,
                            nominalRupiah = doc.getDouble("nominalRupiah") ?: 0.0,
                            tanggalJatuhTempo = doc.getLong("tanggalJatuhTempo") ?: System.currentTimeMillis(),
                            status = doc.getString("status") ?: "NON",
                            metodePembayaran = doc.getString("metodePembayaran"),
                            tanggalBayar = doc.getLong("tanggalBayar")?.takeIf { it > 0L },
                            catatan = doc.getString("catatan")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // 4. Fetch Hutang
                val hutangSnapshot = firestore.collection("hutang_pribadi").get().await()
                val hList = hutangSnapshot.documents.mapNotNull { doc ->
                    try {
                        Hutang(
                            id = doc.getLong("id") ?: 0L,
                            namaSumber = doc.getString("namaSumber") ?: "",
                            nominalHutang = doc.getDouble("nominalHutang") ?: 0.0,
                            hutangDibayar = doc.getDouble("hutangDibayar") ?: 0.0,
                            sisaHutang = doc.getDouble("sisaHutang") ?: 0.0,
                            tanggalCatat = doc.getLong("tanggalCatat") ?: System.currentTimeMillis(),
                            catatan = doc.getString("catatan")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // 5. Fetch Topup
                val topupSnapshot = firestore.collection("topup_transaction").get().await()
                val tList = topupSnapshot.documents.mapNotNull { doc ->
                    try {
                        val nom = doc.getDouble("nominal") ?: 0.0
                        val isLunas = doc.getBoolean("isLunas") ?: false
                        val dibayar = doc.getDouble("totalDibayar") ?: if (isLunas) nom else 0.0
                        TopupTransaction(
                            id = doc.getLong("id") ?: 0L,
                            nama = doc.getString("nama") ?: "",
                            nominal = nom,
                            totalDibayar = dibayar,
                            keuntungan = doc.getDouble("keuntungan") ?: 0.0,
                            tanggalTransaksi = doc.getLong("tanggalTransaksi") ?: System.currentTimeMillis(),
                            tanggalJatuhTempo = doc.getLong("tanggalJatuhTempo") ?: System.currentTimeMillis(),
                            isLunas = isLunas,
                            tanggalBayar = doc.getLong("tanggalBayar")?.takeIf { it > 0L },
                            catatan = doc.getString("catatan")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                // 6. Fetch Hutang Payment
                val paySnapshot = firestore.collection("hutang_payment").get().await()
                val pList = paySnapshot.documents.mapNotNull { doc ->
                    try {
                        HutangPayment(
                            id = doc.getLong("id") ?: 0L,
                            hutangId = doc.getLong("hutangId") ?: 0L,
                            namaSumber = doc.getString("namaSumber") ?: "",
                            nominalBayar = doc.getDouble("nominalBayar") ?: 0.0,
                            tanggalBayar = doc.getLong("tanggalBayar") ?: System.currentTimeMillis(),
                            catatan = doc.getString("catatan")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                val totalRecords = nList.size + kList.size + iList.size + hList.size + tList.size + pList.size
                val now = System.currentTimeMillis()
                saveLastSyncTime(now)
                val data = RestoredBackupData(nList, kList, iList, hList, tList, pList)

                if (totalRecords > 0) {
                    Pair(
                        SyncResult(
                            isSuccess = true,
                            message = "Update data berhasil! $totalRecords data terbaru berhasil diunduh.",
                            timestamp = now,
                            totalRecords = totalRecords
                        ),
                        data
                    )
                } else {
                    Pair(
                        SyncResult(
                            isSuccess = true,
                            message = "Update data selesai. Server cloud saat ini belum memiliki data tambahan.",
                            timestamp = now,
                            totalRecords = 0
                        ),
                        null
                    )
                }
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Pair(
                SyncResult(
                    isSuccess = false,
                    message = "Update data gagal: Waktu koneksi habis (Timeout). Pastikan koneksi internet stabil."
                ),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            val cleanErr = e.localizedMessage ?: "Periksa koneksi internet Anda"
            Pair(
                SyncResult(
                    isSuccess = false,
                    message = "Update data gagal: $cleanErr"
                ),
                null
            )
        }
    }

    suspend fun saveBackupSnapshotToCloud(
        email: String,
        jsonContent: String,
        totalRecords: Int,
        summary: String
    ): SyncResult = withContext(Dispatchers.IO) {
        ensureFirebaseInitialized()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val cleanEmail = email.trim().lowercase()
            val docId = cleanEmail.replace("/", "_")
            val docRef = firestore.collection("cloud_backups").document(docId)
            val map = hashMapOf(
                "accountEmail" to cleanEmail,
                "createdAt" to System.currentTimeMillis(),
                "totalRecords" to totalRecords,
                "summary" to summary,
                "jsonContent" to jsonContent
            )
            docRef.set(map, SetOptions.merge()).await()
            val now = System.currentTimeMillis()
            saveLastSyncTime(now)
            SyncResult(
                isSuccess = true,
                message = "Cadangan berhasil diunggah ke Cloud Server ($cleanEmail)",
                timestamp = now,
                totalRecords = totalRecords
            )
        } catch (e: Exception) {
            e.printStackTrace()
            SyncResult(
                isSuccess = false,
                message = "Gagal mengunggah cadangan ke Cloud: ${e.localizedMessage ?: "Cek koneksi internet"}"
            )
        }
    }

    suspend fun fetchBackupSnapshotFromCloud(email: String): Pair<SyncResult, String?> = withContext(Dispatchers.IO) {
        ensureFirebaseInitialized()
        try {
            val firestore = FirebaseFirestore.getInstance()
            val cleanEmail = email.trim().lowercase()
            val docId = cleanEmail.replace("/", "_")
            val docSnap = firestore.collection("cloud_backups").document(docId).get().await()
            if (docSnap.exists() && docSnap.contains("jsonContent")) {
                val jsonString = docSnap.getString("jsonContent")
                if (!jsonString.isNullOrBlank()) {
                    val count = docSnap.getLong("totalRecords")?.toInt() ?: 0
                    return@withContext Pair(
                        SyncResult(
                            isSuccess = true,
                            message = "Data cadangan berhasil diunduh dari Cloud Server untuk akun $cleanEmail",
                            timestamp = docSnap.getLong("createdAt") ?: System.currentTimeMillis(),
                            totalRecords = count
                        ),
                        jsonString
                    )
                }
            }
            Pair(
                SyncResult(
                    isSuccess = false,
                    message = "Tidak ditemukan data cadangan di Cloud Server untuk akun '$cleanEmail'. Pastikan email sama persis dengan yang dimasukkan saat backup di HP pertama."
                ),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(
                SyncResult(
                    isSuccess = false,
                    message = "Gagal menghubungi Cloud Server: ${e.localizedMessage ?: "Periksa koneksi internet"}"
                ),
                null
            )
        }
    }
}
