package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Hutang
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExcelExporter {

    /**
     * Generate format .XLS (HTML/XML Excel format yang kompatibel 100% dengan MS Excel & Google Sheets)
     */
    fun generateExcelFile(
        context: Context,
        nasabahList: List<Nasabah>,
        kreditList: List<KreditPiutang>,
        installmentList: List<PiutangInstallment>,
        hutangList: List<Hutang>,
        topupList: List<TopupTransaction>,
        hutangPaymentList: List<com.example.data.model.HutangPayment> = emptyList()
    ): File? {
        return try {
            val totalHutang = hutangList.sumOf { it.sisaHutang }
            val totalPiutang = kreditList.sumOf { it.totalPiutang - it.totalTerbayar }
            val totalTopup = topupList.filter { !it.isLunas }.sumOf { it.nominal }
            val totalUangSaya = totalPiutang + totalTopup - totalHutang

            val sb = StringBuilder()
            sb.append("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" ")
            sb.append("xmlns:x=\"urn:schemas-microsoft-com:office:excel\" ")
            sb.append("xmlns=\"http://www.w3.org/TR/REC-html40\">\n")
            sb.append("<head>\n")
            sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n")
            sb.append("<!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets>")
            sb.append("<x:ExcelWorksheet><x:Name>Laporan Keuangan</x:Name>")
            sb.append("<x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet>")
            sb.append("</x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]-->\n")
            sb.append("<style>\n")
            sb.append("body { font-family: Arial, sans-serif; font-size: 11pt; }\n")
            sb.append("table { border-collapse: collapse; width: 100%; margin-bottom: 25px; }\n")
            sb.append("th { background-color: #0F5132; color: #FFFFFF; font-weight: bold; padding: 6px 10px; border: 1px solid #000; text-align: left; }\n")
            sb.append("td { padding: 5px 8px; border: 1px solid #CCCCCC; }\n")
            sb.append(".title { font-size: 16pt; font-weight: bold; color: #0F5132; }\n")
            sb.append(".subtitle { font-size: 10pt; color: #555555; margin-bottom: 15px; }\n")
            sb.append(".section-header { font-size: 13pt; font-weight: bold; color: #1F2937; margin-top: 15px; margin-bottom: 5px; }\n")
            sb.append(".summary-box { background-color: #E8F5E9; font-weight: bold; }\n")
            sb.append(".lunas { color: #15803D; font-weight: bold; }\n")
            sb.append(".belum { color: #DC2626; font-weight: bold; }\n")
            sb.append("</style>\n</head>\n<body>\n")

            // Title
            val nowStr = SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())
            sb.append("<div class=\"title\">LAPORAN &amp; BACKUP DATABASE LENGKAP MANAJEMEN HUTANG PIUTANG</div>\n")
            sb.append("<div class=\"subtitle\">Tanggal Ekspor: $nowStr</div>\n")

            // 1. Ringkasan Keuangan
            sb.append("<div class=\"section-header\">1. RINGKASAN POSISI KEUANGAN</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Kategori</th><th>Nominal (Rupiah)</th><th>Keterangan</th></tr>\n")
            sb.append("<tr><td>Total Piutang Aktif</td><td>${FormatUtils.formatRupiah(totalPiutang)}</td><td>Sisa tagihan kredit nasabah belum lunas</td></tr>\n")
            sb.append("<tr><td>Total Topup Aktif</td><td>${FormatUtils.formatRupiah(totalTopup)}</td><td>Piutang langsung belum diterima</td></tr>\n")
            sb.append("<tr><td>Total Hutang Pribadi</td><td>${FormatUtils.formatRupiah(totalHutang)}</td><td>Kewajiban hutang yang belum dibayar</td></tr>\n")
            sb.append("<tr class=\"summary-box\"><td>TOTAL UANG SAYA (BERSIH)</td><td>${FormatUtils.formatRupiah(totalUangSaya)}</td><td>(Piutang + Topup) - Hutang</td></tr>\n")
            sb.append("</table>\n")

            // 2. Daftar Nasabah
            sb.append("<div class=\"section-header\">2. DAFTAR NASABAH</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Kode</th><th>Nama Nasabah</th><th>No WhatsApp</th><th>PIN Login</th><th>Tanggal Terdaftar</th></tr>\n")
            if (nasabahList.isEmpty()) {
                sb.append("<tr><td colspan=\"5\">Belum ada data nasabah</td></tr>\n")
            } else {
                nasabahList.forEach { n ->
                    sb.append("<tr><td>${n.customerCode}</td><td>${n.name}</td><td>${n.whatsappNumber}</td><td>${n.pinLogin}</td><td>${FormatUtils.formatDate(n.createdAt)}</td></tr>\n")
                }
            }
            sb.append("</table>\n")

            // 3. Kredit & Piutang Nasabah
            sb.append("<div class=\"section-header\">3. DAFTAR KREDIT BARANG &amp; PINJAMAN TUNAI</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Nasabah</th><th>Akad</th><th>Barang/Pinjaman</th><th>Pokok</th><th>Bunga %</th><th>Total Piutang</th><th>Setoran</th><th>Tenor</th><th>Jadwal Tagih</th><th>Terbayar</th><th>Sisa</th><th>Status</th></tr>\n")
            if (kreditList.isEmpty()) {
                sb.append("<tr><td colspan=\"12\">Belum ada data kredit</td></tr>\n")
            } else {
                kreditList.forEach { k ->
                    val sisa = k.totalPiutang - k.totalTerbayar
                    val statusClass = if (k.status == "LUNAS") "lunas" else "belum"
                    val jadwalTagih = when {
                        !k.hariPenagihan.isNullOrBlank() -> "Hari ${k.hariPenagihan}"
                        k.tanggalPenagihan != null -> "Tgl ${k.tanggalPenagihan}"
                        else -> "-"
                    }
                    sb.append("<tr><td>${k.customerName} (${k.customerCode})</td><td>${k.akadType}</td><td>${k.itemName}</td>")
                    sb.append("<td>${FormatUtils.formatRupiah(k.nominalPokok)}</td><td>${k.persentaseBunga}%</td>")
                    sb.append("<td>${FormatUtils.formatRupiah(k.totalPiutang)}</td><td>${FormatUtils.formatRupiah(k.jumlahSetoran)}</td>")
                    sb.append("<td>${k.durasiTenor} ${k.tenorUnit}</td><td>$jadwalTagih</td><td>${FormatUtils.formatRupiah(k.totalTerbayar)}</td>")
                    sb.append("<td>${FormatUtils.formatRupiah(sisa)}</td><td class=\"$statusClass\">${k.status}</td></tr>\n")
                }
            }
            sb.append("</table>\n")

            // 4. Jadwal Angsuran & Tagihan
            sb.append("<div class=\"section-header\">4. RINCIAN ANGSURAN &amp; JATUH TEMPO</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Nasabah</th><th>Angsuran Ke</th><th>Nominal</th><th>Jatuh Tempo</th><th>Status</th><th>Metode Bayar</th><th>Tanggal Bayar</th><th>Catatan</th></tr>\n")
            if (installmentList.isEmpty()) {
                sb.append("<tr><td colspan=\"8\">Belum ada jadwal angsuran</td></tr>\n")
            } else {
                installmentList.forEach { inst ->
                    val statusClass = if (inst.status == "LUNAS") "lunas" else "belum"
                    val tglBayar = inst.tanggalBayar?.let { FormatUtils.formatDate(it) } ?: "-"
                    val catatan = inst.catatan ?: "-"
                    sb.append("<tr><td>${inst.customerName}</td><td>Ke-${inst.angsuranKe}</td><td>${FormatUtils.formatRupiah(inst.nominalRupiah)}</td>")
                    sb.append("<td>${FormatUtils.formatDate(inst.tanggalJatuhTempo)}</td><td class=\"$statusClass\">${inst.status}</td>")
                    sb.append("<td>${inst.metodePembayaran ?: "-"}</td><td>$tglBayar</td><td>$catatan</td></tr>\n")
                }
            }
            sb.append("</table>\n")

            // 5. Catatan Hutang Pribadi
            sb.append("<div class=\"section-header\">5. CATATAN HUTANG PRIBADI</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Nama Sumber</th><th>Nominal Hutang</th><th>Hutang Dibayar</th><th>Sisa Hutang</th><th>Tanggal Catat</th><th>Catatan</th></tr>\n")
            if (hutangList.isEmpty()) {
                sb.append("<tr><td colspan=\"6\">Belum ada catatan hutang</td></tr>\n")
            } else {
                hutangList.forEach { h ->
                    val catatan = h.catatan ?: "-"
                    sb.append("<tr><td>${h.namaSumber}</td><td>${FormatUtils.formatRupiah(h.nominalHutang)}</td>")
                    sb.append("<td>${FormatUtils.formatRupiah(h.hutangDibayar)}</td><td>${FormatUtils.formatRupiah(h.sisaHutang)}</td>")
                    sb.append("<td>${FormatUtils.formatDate(h.tanggalCatat)}</td><td>$catatan</td></tr>\n")
                }
            }
            sb.append("</table>\n")

            // 6. Transaksi Topup
            sb.append("<div class=\"section-header\">6. TRANSAKSI TOPUP / PIUTANG LANGSUNG</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Nama Transaksi</th><th>Nominal</th><th>Dibayar</th><th>Sisa Tagihan</th><th>Keuntungan</th><th>Tgl Transaksi</th><th>Jatuh Tempo</th><th>Status</th><th>Tgl Bayar</th><th>Catatan</th></tr>\n")
            if (topupList.isEmpty()) {
                sb.append("<tr><td colspan=\"10\">Belum ada transaksi topup</td></tr>\n")
            } else {
                topupList.forEach { t ->
                    val statusText = if (t.isLunas) "LUNAS" else if (t.totalDibayar > 0) "SEBAGIAN" else "BELUM LUNAS"
                    val statusClass = if (t.isLunas) "lunas" else "belum"
                    val tglBayar = t.tanggalBayar?.let { FormatUtils.formatDate(it) } ?: "-"
                    val catatan = t.catatan ?: "-"
                    sb.append("<tr><td>${t.nama}</td><td>${FormatUtils.formatRupiah(t.nominal)}</td><td>${FormatUtils.formatRupiah(t.totalDibayar)}</td><td>${FormatUtils.formatRupiah(t.sisaNominal)}</td><td>${FormatUtils.formatRupiah(t.keuntungan)}</td><td>${FormatUtils.formatDate(t.tanggalTransaksi)}</td>")
                    sb.append("<td>${FormatUtils.formatDate(t.tanggalJatuhTempo)}</td><td class=\"$statusClass\">$statusText</td><td>$tglBayar</td><td>$catatan</td></tr>\n")
                }
            }
            sb.append("</table>\n")

            // 7. Riwayat Pembayaran Hutang Pribadi
            sb.append("<div class=\"section-header\">7. RIWAYAT PEMBAYARAN HUTANG PRIBADI</div>\n")
            sb.append("<table>\n")
            sb.append("<tr><th>Nama Sumber Hutang</th><th>Nominal Bayar</th><th>Tanggal Bayar</th><th>Catatan</th></tr>\n")
            if (hutangPaymentList.isEmpty()) {
                sb.append("<tr><td colspan=\"4\">Belum ada riwayat pembayaran hutang</td></tr>\n")
            } else {
                hutangPaymentList.forEach { p ->
                    val catatan = p.catatan ?: "-"
                    sb.append("<tr><td>${p.namaSumber}</td><td>${FormatUtils.formatRupiah(p.nominalBayar)}</td><td>${FormatUtils.formatDate(p.tanggalBayar)}</td><td>$catatan</td></tr>\n")
                }
            }
            sb.append("</table>\n")

            sb.append("</body>\n</html>")

            // Simpan file .xls ke cache/files
            val fileNameDate = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
            val xlsFile = File(exportDir, "Backup_Hutang_Piutang_$fileNameDate.xls")
            val fos = FileOutputStream(xlsFile)
            fos.write(sb.toString().toByteArray(Charsets.UTF_8))
            fos.flush()
            fos.close()

            xlsFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Berbagi / Buka file .XLS ke aplikasi apapun (Excel, Google Sheets, WhatsApp, Drive, dll)
     */
    fun shareExcelFile(context: Context, xlsFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                xlsFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_SUBJECT, "Backup Database Manajemen Hutang Piutang (${xlsFile.name})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "File backup database lengkap aplikasi Manajemen Hutang Piutang (.xls).\n" +
                            "Dapat dibuka di Microsoft Excel atau Google Sheets."
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Unduh / Bagikan File Backup Excel")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membagikan file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Membuka langsung halaman Email dengan file .XLS terlampir
     */
    fun openEmailWithExcelAttachment(context: Context, xlsFile: File) {
        try {
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                xlsFile
            )

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.ms-excel"
                putExtra(Intent.EXTRA_SUBJECT, "Backup Database Manajemen Hutang Piutang (${xlsFile.name})")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Terlampir file backup database lengkap aplikasi Manajemen Hutang Piutang dalam format Excel (.xls).\n\n" +
                            "File ini dapat dibuka langsung menggunakan Microsoft Excel, Google Sheets, atau aplikasi Spreadsheet lainnya.\n\n" +
                            "Tanggal ekspor: ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}"
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(emailIntent, "Kirim Backup Excel via Email")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal membuka email: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
