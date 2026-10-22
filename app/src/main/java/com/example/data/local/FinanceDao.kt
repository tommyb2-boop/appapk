package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Hutang
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NasabahDao {
    @Query("SELECT * FROM nasabah ORDER BY createdAt DESC")
    fun getAllNasabah(): Flow<List<Nasabah>>

    @Query("SELECT * FROM nasabah ORDER BY createdAt DESC")
    suspend fun getNasabahListSync(): List<Nasabah>

    @Query("SELECT * FROM nasabah WHERE id = :id LIMIT 1")
    fun getNasabahById(id: Long): Flow<Nasabah?>

    @Query("SELECT * FROM nasabah WHERE id = :id LIMIT 1")
    suspend fun getNasabahByIdSync(id: Long): Nasabah?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNasabah(nasabah: Nasabah): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Nasabah>)

    @Update
    suspend fun updateNasabah(nasabah: Nasabah)

    @Delete
    suspend fun deleteNasabah(nasabah: Nasabah)

    @Query("DELETE FROM nasabah")
    suspend fun deleteAll()
}

@Dao
interface KreditDao {
    @Query("SELECT * FROM kredit_piutang ORDER BY createdAt DESC")
    fun getAllKredit(): Flow<List<KreditPiutang>>

    @Query("SELECT * FROM kredit_piutang ORDER BY createdAt DESC")
    suspend fun getKreditListSync(): List<KreditPiutang>

    @Query("SELECT * FROM kredit_piutang WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getKreditByCustomer(customerId: Long): Flow<List<KreditPiutang>>

    @Query("SELECT * FROM kredit_piutang WHERE id = :id LIMIT 1")
    suspend fun getKreditById(id: Long): KreditPiutang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKredit(kredit: KreditPiutang): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<KreditPiutang>)

    @Update
    suspend fun updateKredit(kredit: KreditPiutang)

    @Delete
    suspend fun deleteKredit(kredit: KreditPiutang)

    @Query("DELETE FROM kredit_piutang")
    suspend fun deleteAll()
}

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM piutang_installment ORDER BY tanggalJatuhTempo ASC")
    fun getAllInstallments(): Flow<List<PiutangInstallment>>

    @Query("SELECT * FROM piutang_installment ORDER BY tanggalJatuhTempo ASC")
    suspend fun getInstallmentListSync(): List<PiutangInstallment>

    @Query("SELECT * FROM piutang_installment WHERE kreditId = :kreditId ORDER BY angsuranKe ASC")
    fun getInstallmentsByKredit(kreditId: Long): Flow<List<PiutangInstallment>>

    @Query("SELECT * FROM piutang_installment WHERE customerId = :customerId ORDER BY tanggalJatuhTempo ASC")
    fun getInstallmentsByCustomer(customerId: Long): Flow<List<PiutangInstallment>>

    @Query("SELECT * FROM piutang_installment WHERE id = :id LIMIT 1")
    suspend fun getInstallmentById(id: Long): PiutangInstallment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PiutangInstallment>)

    @Update
    suspend fun updateInstallment(item: PiutangInstallment)

    @Query("DELETE FROM piutang_installment WHERE kreditId = :kreditId")
    suspend fun deleteByKreditId(kreditId: Long)

    @Query("DELETE FROM piutang_installment WHERE kreditId = :kreditId AND status != 'LUNAS'")
    suspend fun deleteUnpaidInstallmentsByKreditId(kreditId: Long)

    @Query("SELECT * FROM piutang_installment WHERE kreditId = :kreditId AND status = 'LUNAS' ORDER BY angsuranKe ASC")
    suspend fun getPaidInstallmentsByKreditId(kreditId: Long): List<PiutangInstallment>

    @Query("DELETE FROM piutang_installment")
    suspend fun deleteAll()
}

@Dao
interface HutangDao {
    @Query("SELECT * FROM hutang_pribadi ORDER BY tanggalCatat DESC")
    fun getAllHutang(): Flow<List<Hutang>>

    @Query("SELECT * FROM hutang_pribadi ORDER BY tanggalCatat DESC")
    suspend fun getHutangListSync(): List<Hutang>

    @Query("SELECT * FROM hutang_pribadi WHERE id = :id LIMIT 1")
    suspend fun getHutangById(id: Long): Hutang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHutang(hutang: Hutang): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<Hutang>)

    @Update
    suspend fun updateHutang(hutang: Hutang)

    @Delete
    suspend fun deleteHutang(hutang: Hutang)

    @Query("DELETE FROM hutang_pribadi")
    suspend fun deleteAll()
}

@Dao
interface TopupDao {
    @Query("SELECT * FROM topup_transaction ORDER BY tanggalTransaksi DESC")
    fun getAllTopup(): Flow<List<TopupTransaction>>

    @Query("SELECT * FROM topup_transaction ORDER BY tanggalTransaksi DESC")
    suspend fun getTopupListSync(): List<TopupTransaction>

    @Query("SELECT * FROM topup_transaction WHERE id = :id LIMIT 1")
    suspend fun getTopupById(id: Long): TopupTransaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopup(topup: TopupTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<TopupTransaction>)

    @Update
    suspend fun updateTopup(topup: TopupTransaction)

    @Delete
    suspend fun deleteTopup(topup: TopupTransaction)

    @Query("DELETE FROM topup_transaction")
    suspend fun deleteAll()
}

@Dao
interface HutangPaymentDao {
    @Query("SELECT * FROM hutang_payment ORDER BY tanggalBayar DESC")
    fun getAllPayments(): Flow<List<com.example.data.model.HutangPayment>>

    @Query("SELECT * FROM hutang_payment ORDER BY tanggalBayar DESC")
    suspend fun getPaymentsListSync(): List<com.example.data.model.HutangPayment>

    @Query("SELECT * FROM hutang_payment WHERE hutangId = :hutangId ORDER BY tanggalBayar DESC")
    fun getPaymentsByHutangId(hutangId: Long): Flow<List<com.example.data.model.HutangPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: com.example.data.model.HutangPayment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<com.example.data.model.HutangPayment>)

    @Query("DELETE FROM hutang_payment")
    suspend fun deleteAll()
}
