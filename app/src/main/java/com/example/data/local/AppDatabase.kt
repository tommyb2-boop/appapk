package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Nasabah::class,
        KreditPiutang::class,
        PiutangInstallment::class,
        Hutang::class,
        TopupTransaction::class,
        HutangPayment::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nasabahDao(): NasabahDao
    abstract fun kreditDao(): KreditDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun hutangDao(): HutangDao
    abstract fun topupDao(): TopupDao
    abstract fun hutangPaymentDao(): HutangPaymentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `hutang_payment` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `hutangId` INTEGER NOT NULL,
                        `namaSumber` TEXT NOT NULL,
                        `nominalBayar` REAL NOT NULL,
                        `tanggalBayar` INTEGER NOT NULL,
                        `catatan` TEXT
                    )
                """.trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `topup_transaction` ADD COLUMN `totalDibayar` REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "manajemen_hutang_piutang.db"
                )
                .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
