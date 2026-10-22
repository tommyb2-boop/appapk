package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    /**
     * Generate PDF Struk Pembayaran Angsuran
     */
    fun createStrukPdf(
        context: Context,
        installment: PiutangInstallment,
        kredit: KreditPiutang?,
        nasabah: Nasabah?
    ): File? {
        return try {
            val document = PdfDocument()
            // Ukuran receipt format thermal/slip: 350 x 550 pt
            val pageInfo = PdfDocument.PageInfo.Builder(380, 560, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // Header Background Box
            paint.color = Color.parseColor("#0F5132") // Emerald header
            canvas.drawRect(0f, 0f, 380f, 75f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 15f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("STRUK BUKTI PEMBAYARAN", 190f, 32f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Aplikasi Manajemen Hutang Piutang", 190f, 50f, paint)
            canvas.drawText("Tanggal: ${FormatUtils.formatDateTime(installment.tanggalBayar ?: System.currentTimeMillis())}", 190f, 65f, paint)

            // Body Area
            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.DKGRAY
            var y = 100f
            val lineSpacing = 18f

            fun drawRow(label: String, value: String, isBold: Boolean = false) {
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.parseColor("#4B5563")
                paint.textSize = 11f
                canvas.drawText(label, 20f, y, paint)

                paint.typeface = if (isBold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = if (isBold) Color.BLACK else Color.parseColor("#1F2937")
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(value, 360f, y, paint)
                paint.textAlign = Paint.Align.LEFT

                y += lineSpacing
            }

            drawRow("No. Transaksi", "TRX-${installment.id}-${System.currentTimeMillis() % 100000}")
            drawRow("Kode Nasabah", nasabah?.customerCode ?: "-")
            drawRow("Nama Nasabah", nasabah?.name ?: installment.customerName, isBold = true)
            drawRow("Nomor WhatsApp", nasabah?.whatsappNumber ?: "-")

            // Separator line
            y += 5f
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(20f, y, 360f, y, paint)
            y += 15f

            drawRow("Jenis Akad", kredit?.akadType ?: "Kredit")
            drawRow("Kredit / Barang", kredit?.itemName ?: "-")
            drawRow("Tagihan Angsuran", "Ke-${installment.angsuranKe} dari ${kredit?.durasiTenor ?: "-"}")
            drawRow("Metode Pembayaran", installment.metodePembayaran ?: "TUNAI", isBold = true)

            // Highlight Box for Nominal
            y += 10f
            paint.color = Color.parseColor("#E8F5E9")
            canvas.drawRect(20f, y, 360f, y + 55f, paint)

            paint.color = Color.parseColor("#0F5132")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            canvas.drawText("TOTAL DIBAYARKAN:", 35f, y + 22f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 18f
            canvas.drawText(FormatUtils.formatRupiah(installment.nominalRupiah), 35f, y + 45f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.textSize = 14f
            paint.color = Color.parseColor("#15803D")
            canvas.drawText("[ LUNAS ]", 345f, y + 36f, paint)
            paint.textAlign = Paint.Align.LEFT

            y += 75f

            // Footer notes
            paint.color = Color.GRAY
            paint.textSize = 9f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Terima kasih atas pembayaran Anda.", 190f, y, paint)
            y += 14f
            canvas.drawText("Bukti pembayaran ini sah dan tercatat di sistem digital.", 190f, y, paint)

            document.finishPage(page)

            // Save to cache file
            val outputDir = File(context.cacheDir, "receipts").apply { mkdirs() }
            val outputFile = File(outputDir, "Struk_Angsuran_${installment.id}.pdf")
            val outputStream = FileOutputStream(outputFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate PDF Kwitansi Resmi Nasabah
     */
    fun createKwitansiPdf(
        context: Context,
        nasabah: Nasabah,
        installment: PiutangInstallment,
        kredit: KreditPiutang?
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(450, 320, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            // Outer Border
            paint.color = Color.parseColor("#0F5132")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawRect(15f, 15f, 435f, 305f, paint)

            // Header
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0F5132")
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("KWITANSI TANDA TERIMA", 30f, 42f, paint)

            paint.color = Color.GRAY
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("No: KWT-${installment.id}/${nasabah.customerCode}", 30f, 56f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Tanggal: ${FormatUtils.formatDateOnly(installment.tanggalBayar ?: System.currentTimeMillis())}", 420f, 42f, paint)
            paint.textAlign = Paint.Align.LEFT

            // Line
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas.drawLine(30f, 68f, 420f, 68f, paint)

            // Content
            var y = 92f
            fun row(label: String, value: String) {
                paint.color = Color.DKGRAY
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(label, 30f, y, paint)

                paint.color = Color.BLACK
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(": $value", 160f, y, paint)
                y += 20f
            }

            row("Telah Diterima Dari", "${nasabah.name} (${nasabah.customerCode})")
            row("Guna Pembayaran", "Angsuran ${installment.angsuranKe} - ${kredit?.itemName ?: "Piutang"}")
            row("Jumlah Uang", FormatUtils.formatRupiah(installment.nominalRupiah))
            row("Status Pelunasan", if (installment.status == "LUNAS") "LUNAS (${installment.metodePembayaran ?: "TUNAI"})" else "BELUM LUNAS")

            // Amount box
            paint.color = Color.parseColor("#E8F5E9")
            canvas.drawRect(30f, 190f, 210f, 235f, paint)
            paint.color = Color.parseColor("#0F5132")
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(FormatUtils.formatRupiah(installment.nominalRupiah), 40f, 220f, paint)

            // Signature block
            paint.color = Color.BLACK
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Penerima / Petugas Kasir", 350f, 200f, paint)
            canvas.drawText("( Tanda Tangan & Cap )", 350f, 255f, paint)
            canvas.drawText("Admin Keuangan", 350f, 270f, paint)

            document.finishPage(page)

            val outputDir = File(context.cacheDir, "kwitansi").apply { mkdirs() }
            val outputFile = File(outputDir, "Kwitansi_${nasabah.customerCode}_${installment.id}.pdf")
            val outputStream = FileOutputStream(outputFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Helper untuk membagikan file PDF ke WhatsApp
     */
    fun sharePdfToWhatsApp(
        context: Context,
        pdfFile: File,
        targetPhone: String?,
        captionText: String
    ) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, captionText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Target WhatsApp specifically if available
            val cleanPhone = targetPhone?.replace("[^0-9]".toRegex(), "") ?: ""
            val formattedPhone = when {
                cleanPhone.startsWith("0") -> "62" + cleanPhone.substring(1)
                cleanPhone.startsWith("62") -> cleanPhone
                cleanPhone.isNotEmpty() -> cleanPhone
                else -> ""
            }

            // WhatsApp package intent
            shareIntent.setPackage("com.whatsapp")

            try {
                context.startActivity(shareIntent)
            } catch (noWa: Exception) {
                // If direct WhatsApp package fails, try generic chooser
                shareIntent.setPackage(null)
                val chooser = Intent.createChooser(shareIntent, "Kirim Struk Pembayaran")
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan struk: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Helper untuk kirim chat teks WhatsApp rangkuman nasabah
     */
    fun sendWhatsAppText(
        context: Context,
        phone: String,
        message: String
    ) {
        try {
            val cleanPhone = phone.replace("[^0-9]".toRegex(), "")
            val formattedPhone = when {
                cleanPhone.startsWith("0") -> "62" + cleanPhone.substring(1)
                cleanPhone.startsWith("62") -> cleanPhone
                else -> cleanPhone
            }

            val encodedMessage = Uri.encode(message)
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback generic send
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Kirim WhatsApp"))
        }
    }

    /**
     * Buka file PDF menggunakan aplikasi viewer bawaan Android
     */
    fun openPdf(context: Context, pdfFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "PDF disimpan di: ${pdfFile.name}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generate PDF Rekapitulasi Hutang & Pembayaran
     */
    fun createRekapHutangPdf(
        context: Context,
        startDate: Long,
        endDate: Long,
        hutangList: List<Hutang>,
        paymentList: List<HutangPayment>
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            // Header Banner
            paint.color = Color.parseColor("#0F5132")
            canvas.drawRect(0f, 0f, 595f, 75f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("LAPORAN REKAPITULASI HUTANG & PEMBAYARAN", 25f, 34f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val periodStr = "Periode: ${FormatUtils.formatDateOnly(startDate)} s/d ${FormatUtils.formatDateOnly(endDate)}"
            canvas.drawText(periodStr, 25f, 52f, paint)
            canvas.drawText("Dicetak: ${FormatUtils.formatDateTime(System.currentTimeMillis())}", 25f, 65f, paint)

            var y = 95f

            // Summary Box
            val totalHutang = hutangList.sumOf { it.nominalHutang }
            val totalBayar = paymentList.sumOf { it.nominalBayar }
            val sisaHutang = (totalHutang - totalBayar).coerceAtLeast(0.0)

            paint.color = Color.parseColor("#F1F5F9")
            canvas.drawRect(25f, y, 570f, y + 45f, paint)
            paint.color = Color.parseColor("#CBD5E1")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(25f, y, 570f, y + 45f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#334155")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("TOTAL HUTANG BARU", 35f, y + 18f, paint)
            canvas.drawText("TOTAL PEMBAYARAN", 220f, y + 18f, paint)
            canvas.drawText("SISA HUTANG", 410f, y + 18f, paint)

            paint.textSize = 12f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText(FormatUtils.formatRupiah(totalHutang), 35f, y + 36f, paint)
            paint.color = Color.parseColor("#15803D")
            canvas.drawText(FormatUtils.formatRupiah(totalBayar), 220f, y + 36f, paint)
            paint.color = Color.parseColor("#B91C1C")
            canvas.drawText(FormatUtils.formatRupiah(sisaHutang), 410f, y + 36f, paint)

            y += 65f

            // Section 1: Riwayat Transaksi Hutang Baru
            paint.color = Color.parseColor("#0F5132")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("1. Daftar Transaksi Hutang Baru (${hutangList.size} Catatan)", 25f, y, paint)
            y += 15f

            // Table Header 1
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(25f, y, 570f, y + 20f, paint)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NO", 30f, y + 14f, paint)
            canvas.drawText("TANGGAL", 65f, y + 14f, paint)
            canvas.drawText("SUMBER HUTANG", 150f, y + 14f, paint)
            canvas.drawText("NOMINAL", 330f, y + 14f, paint)
            canvas.drawText("KETERANGAN", 440f, y + 14f, paint)
            y += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (hutangList.isEmpty()) {
                paint.color = Color.GRAY
                canvas.drawText("Tidak ada transaksi hutang baru dalam periode ini.", 30f, y + 16f, paint)
                y += 24f
            } else {
                hutangList.take(15).forEachIndexed { index, item ->
                    paint.color = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                    canvas.drawRect(25f, y, 570f, y + 18f, paint)
                    paint.color = Color.parseColor("#334155")
                    canvas.drawText("${index + 1}", 30f, y + 13f, paint)
                    canvas.drawText(FormatUtils.formatDateOnly(item.tanggalCatat), 65f, y + 13f, paint)
                    canvas.drawText(item.namaSumber.take(24), 150f, y + 13f, paint)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(FormatUtils.formatRupiah(item.nominalHutang), 330f, y + 13f, paint)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText((item.catatan ?: "-").take(20), 440f, y + 13f, paint)
                    y += 18f
                }
            }

            y += 15f

            // Section 2: Riwayat Pembayaran Hutang
            paint.color = Color.parseColor("#0F5132")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("2. Riwayat Pembayaran Hutang (${paymentList.size} Transaksi)", 25f, y, paint)
            y += 15f

            // Table Header 2
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(25f, y, 570f, y + 20f, paint)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NO", 30f, y + 14f, paint)
            canvas.drawText("TANGGAL BAYAR", 65f, y + 14f, paint)
            canvas.drawText("SUMBER HUTANG", 170f, y + 14f, paint)
            canvas.drawText("NOMINAL BAYAR", 350f, y + 14f, paint)
            canvas.drawText("CATATAN", 470f, y + 14f, paint)
            y += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (paymentList.isEmpty()) {
                paint.color = Color.GRAY
                canvas.drawText("Tidak ada riwayat pembayaran dalam periode ini.", 30f, y + 16f, paint)
                y += 24f
            } else {
                paymentList.take(20).forEachIndexed { index, p ->
                    paint.color = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                    canvas.drawRect(25f, y, 570f, y + 18f, paint)
                    paint.color = Color.parseColor("#334155")
                    canvas.drawText("${index + 1}", 30f, y + 13f, paint)
                    canvas.drawText(FormatUtils.formatDateOnly(p.tanggalBayar), 65f, y + 13f, paint)
                    canvas.drawText(p.namaSumber.take(24), 170f, y + 13f, paint)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.parseColor("#15803D")
                    canvas.drawText(FormatUtils.formatRupiah(p.nominalBayar), 350f, y + 13f, paint)
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    paint.color = Color.parseColor("#334155")
                    canvas.drawText((p.catatan ?: "Bayar").take(18), 470f, y + 13f, paint)
                    y += 18f
                }
            }

            // Footer notes
            y = 800f
            paint.color = Color.GRAY
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Dokumen ini dicetak otomatis dari Aplikasi Manajemen Keuangan & Hutang Piutang.", 297f, y, paint)

            document.finishPage(page)

            val outputDir = File(context.cacheDir, "rekap").apply { mkdirs() }
            val outputFile = File(outputDir, "Rekap_Hutang_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(outputFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate PDF Rekapitulasi Transaksi Topup / Piutang Langsung
     */
    fun createRekapTopupPdf(
        context: Context,
        startDate: Long,
        endDate: Long,
        topupList: List<TopupTransaction>
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            // Header Banner
            paint.color = Color.parseColor("#B45309") // Amber / Gold theme
            canvas.drawRect(0f, 0f, 595f, 75f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("LAPORAN REKAPITULASI TRANSAKSI TOPUP / PULSA", 25f, 34f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val periodStr = "Periode: ${FormatUtils.formatDateOnly(startDate)} s/d ${FormatUtils.formatDateOnly(endDate)}"
            canvas.drawText(periodStr, 25f, 52f, paint)
            canvas.drawText("Dicetak: ${FormatUtils.formatDateTime(System.currentTimeMillis())}", 25f, 65f, paint)

            var y = 95f

            // Summary Box
            val totalOmset = topupList.sumOf { it.nominal }
            val totalUntung = topupList.sumOf { it.keuntungan }
            val lunasCount = topupList.count { it.isLunas }
            val belumLunasCount = topupList.count { !it.isLunas }

            paint.color = Color.parseColor("#FEF3C7")
            canvas.drawRect(25f, y, 570f, y + 45f, paint)
            paint.color = Color.parseColor("#FDE68A")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(25f, y, 570f, y + 45f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#92400E")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("TOTAL OMSET TOPUP", 35f, y + 18f, paint)
            canvas.drawText("TOTAL KEUNTUNGAN", 220f, y + 18f, paint)
            canvas.drawText("STATUS TRANSAKSI", 410f, y + 18f, paint)

            paint.textSize = 12f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText(FormatUtils.formatRupiah(totalOmset), 35f, y + 36f, paint)
            paint.color = Color.parseColor("#B45309")
            canvas.drawText(FormatUtils.formatRupiah(totalUntung), 220f, y + 36f, paint)
            paint.color = Color.parseColor("#15803D")
            canvas.drawText("$lunasCount Lunas • $belumLunasCount Belum", 410f, y + 36f, paint)

            y += 65f

            // Table Header
            paint.color = Color.parseColor("#0F5132")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Daftar Transaksi Topup (${topupList.size} Transaksi)", 25f, y, paint)
            y += 15f

            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(25f, y, 570f, y + 20f, paint)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NO", 28f, y + 14f, paint)
            canvas.drawText("TANGGAL", 55f, y + 14f, paint)
            canvas.drawText("NAMA PELANGGAN", 130f, y + 14f, paint)
            canvas.drawText("NOMINAL", 270f, y + 14f, paint)
            canvas.drawText("UNTUNG", 365f, y + 14f, paint)
            canvas.drawText("STATUS", 445f, y + 14f, paint)
            canvas.drawText("TGL BAYAR", 505f, y + 14f, paint)
            y += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (topupList.isEmpty()) {
                paint.color = Color.GRAY
                canvas.drawText("Tidak ada riwayat transaksi topup dalam rentang tanggal ini.", 30f, y + 16f, paint)
                y += 24f
            } else {
                topupList.take(30).forEachIndexed { index, t ->
                    paint.color = if (index % 2 == 0) Color.WHITE else Color.parseColor("#F8FAFC")
                    canvas.drawRect(25f, y, 570f, y + 18f, paint)

                    paint.color = Color.parseColor("#334155")
                    canvas.drawText("${index + 1}", 28f, y + 13f, paint)
                    canvas.drawText(FormatUtils.formatDateOnly(t.tanggalTransaksi), 55f, y + 13f, paint)
                    canvas.drawText(t.nama.take(20), 130f, y + 13f, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    paint.color = Color.parseColor("#0F172A")
                    canvas.drawText(FormatUtils.formatRupiah(t.nominal), 270f, y + 13f, paint)

                    paint.color = Color.parseColor("#B45309")
                    canvas.drawText(FormatUtils.formatRupiah(t.keuntungan), 365f, y + 13f, paint)

                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    if (t.isLunas) {
                        paint.color = Color.parseColor("#15803D")
                        canvas.drawText("LUNAS", 445f, y + 13f, paint)
                        paint.color = Color.parseColor("#475569")
                        val bayarStr = t.tanggalBayar?.let { FormatUtils.formatDateOnly(it) } ?: "-"
                        canvas.drawText(bayarStr, 505f, y + 13f, paint)
                    } else {
                        paint.color = Color.parseColor("#B91C1C")
                        canvas.drawText("BELUM", 445f, y + 13f, paint)
                        paint.color = Color.parseColor("#475569")
                        canvas.drawText("-", 505f, y + 13f, paint)
                    }

                    y += 18f
                }
            }

            // Footer notes
            y = 800f
            paint.color = Color.GRAY
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Dokumen ini dicetak otomatis dari Aplikasi Manajemen Keuangan & Hutang Piutang.", 297f, y, paint)

            document.finishPage(page)

            val outputDir = File(context.cacheDir, "rekap").apply { mkdirs() }
            val outputFile = File(outputDir, "Rekap_Topup_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(outputFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generate PDF Rekapitulasi Piutang Nasabah
     */
    fun createRekapPiutangPdf(
        context: Context,
        startDate: Long,
        endDate: Long,
        kreditList: List<KreditPiutang>
    ): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true }

            // Header Banner
            paint.color = Color.parseColor("#065F46") // Emerald theme
            canvas.drawRect(0f, 0f, 595f, 75f, paint)

            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("LAPORAN REKAPITULASI PIUTANG NASABAH", 25f, 34f, paint)

            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val periodStr = "Periode: ${FormatUtils.formatDateOnly(startDate)} s/d ${FormatUtils.formatDateOnly(endDate)}"
            canvas.drawText(periodStr, 25f, 52f, paint)
            canvas.drawText("Dicetak: ${FormatUtils.formatDateTime(System.currentTimeMillis())}", 25f, 65f, paint)

            var y = 95f

            // Summary Box
            val totalPiutang = kreditList.sumOf { it.totalPiutang }
            val totalTerbayar = kreditList.sumOf { it.totalTerbayar }
            val sisaPiutang = (totalPiutang - totalTerbayar).coerceAtLeast(0.0)

            paint.color = Color.parseColor("#ECFDF5")
            canvas.drawRect(25f, y, 570f, y + 45f, paint)
            paint.color = Color.parseColor("#A7F3D0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRect(25f, y, 570f, y + 45f, paint)
            paint.style = Paint.Style.FILL

            paint.color = Color.parseColor("#064E3B")
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("TOTAL KONTRAK PIUTANG", 35f, y + 18f, paint)
            canvas.drawText("TOTAL PIUTANG TERBAYAR", 220f, y + 18f, paint)
            canvas.drawText("SISA PIUTANG BERJALAN", 410f, y + 18f, paint)

            paint.textSize = 12f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText(FormatUtils.formatRupiah(totalPiutang), 35f, y + 36f, paint)
            paint.color = Color.parseColor("#15803D")
            canvas.drawText(FormatUtils.formatRupiah(totalTerbayar), 220f, y + 36f, paint)
            paint.color = Color.parseColor("#B91C1C")
            canvas.drawText(FormatUtils.formatRupiah(sisaPiutang), 410f, y + 36f, paint)

            y += 65f

            // Table Section: Daftar Transaksi Piutang
            paint.color = Color.parseColor("#065F46")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Daftar Transaksi Piutang (${kreditList.size} Kontrak)", 25f, y, paint)
            y += 15f

            // Table Header
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(25f, y, 570f, y + 20f, paint)
            paint.color = Color.parseColor("#1E293B")
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("NO", 30f, y + 14f, paint)
            canvas.drawText("TANGGAL", 55f, y + 14f, paint)
            canvas.drawText("NASABAH", 125f, y + 14f, paint)
            canvas.drawText("BARANG / PINJAMAN", 230f, y + 14f, paint)
            canvas.drawText("TOTAL PIUTANG", 355f, y + 14f, paint)
            canvas.drawText("TERBAYAR", 445f, y + 14f, paint)
            canvas.drawText("SISA", 525f, y + 14f, paint)
            y += 20f

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            if (kreditList.isEmpty()) {
                paint.color = Color.GRAY
                paint.textSize = 10f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("Tidak ada transaksi piutang pada rentang tanggal ini", 297f, y + 25f, paint)
                paint.textAlign = Paint.Align.LEFT
            } else {
                var no = 1
                for (item in kreditList.take(30)) {
                    if (y > 770f) break
                    if (no % 2 == 0) {
                        paint.color = Color.parseColor("#F8FAFC")
                        canvas.drawRect(25f, y, 570f, y + 18f, paint)
                    }
                    paint.color = Color.parseColor("#0F172A")
                    paint.textSize = 8f
                    canvas.drawText("$no", 30f, y + 12f, paint)
                    canvas.drawText(FormatUtils.formatDateOnly(item.tanggalMulai), 55f, y + 12f, paint)
                    val namaStr = if (item.customerName.length > 18) item.customerName.take(17) + ".." else item.customerName
                    canvas.drawText(namaStr, 125f, y + 12f, paint)
                    val itemStr = if (item.itemName.length > 20) item.itemName.take(19) + ".." else item.itemName
                    canvas.drawText(itemStr, 230f, y + 12f, paint)
                    canvas.drawText(FormatUtils.formatRupiah(item.totalPiutang), 355f, y + 12f, paint)
                    paint.color = Color.parseColor("#15803D")
                    canvas.drawText(FormatUtils.formatRupiah(item.totalTerbayar), 445f, y + 12f, paint)
                    paint.color = Color.parseColor("#B91C1C")
                    val sisa = (item.totalPiutang - item.totalTerbayar).coerceAtLeast(0.0)
                    canvas.drawText(FormatUtils.formatRupiah(sisa), 525f, y + 12f, paint)

                    no++
                    y += 18f
                }
            }

            // Footer notes
            y = 800f
            paint.color = Color.GRAY
            paint.textSize = 8.5f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Dokumen ini dicetak otomatis dari Aplikasi Manajemen Keuangan & Hutang Piutang.", 297f, y, paint)

            document.finishPage(page)

            val outputDir = File(context.cacheDir, "rekap").apply { mkdirs() }
            val outputFile = File(outputDir, "Rekap_Piutang_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(outputFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
