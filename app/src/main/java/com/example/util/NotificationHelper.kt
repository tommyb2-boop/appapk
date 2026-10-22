package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity

object NotificationHelper {

    const val CHANNEL_DUE_BILLS = "channel_due_bills"
    const val NOTIFICATION_ID_DUE_BILLS = 9901
    const val EXTRA_OPEN_DUE_BILLS = "EXTRA_OPEN_DUE_BILLS"

    /**
     * Daftarkan Notification Channel untuk Android 8.0 (API 26) ke atas
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Tagihan Jatuh Tempo"
            val descriptionText = "Notifikasi pengingat jatuh tempo angsuran kredit dan transaksi topup"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_DUE_BILLS, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Tampilkan Notifikasi Sistem di Status Bar HP saat ada tagihan jatuh tempo
     */
    fun showDueBillsNotification(
        context: Context,
        dueCount: Int,
        billSummaries: List<String>,
        title: String? = null
    ) {
        if (dueCount <= 0) {
            cancelDueBillsNotification(context)
            return
        }

        // Cek izin notifikasi di Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        createNotificationChannel(context)

        // Intent untuk langsung membuka aplikasi dan popup daftar tagihan jatuh tempo
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_DUE_BILLS, true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_DUE_BILLS,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val firstText = billSummaries.firstOrNull() ?: "Ada tagihan yang telah mencapai waktu penagihan."
        val expandedText = billSummaries.joinToString("\n")
        val notificationTitle = title ?: "⚠️ $dueCount Tagihan Jatuh Tempo!"

        val notification = NotificationCompat.Builder(context, CHANNEL_DUE_BILLS)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(notificationTitle)
            .setContentText(firstText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(expandedText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DUE_BILLS, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Hapus notifikasi sistem ketika seluruh tagihan telah lunas
     */
    fun cancelDueBillsNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID_DUE_BILLS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
