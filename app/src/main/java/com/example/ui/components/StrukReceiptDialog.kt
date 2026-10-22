package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.StrukReceiptData
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import com.example.util.PdfReceiptGenerator

@Composable
fun StrukReceiptDialog(
    data: StrukReceiptData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val installment = data.installment
    val kredit = data.kredit
    val nasabah = data.nasabah

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("struk_receipt_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Struk Pembayaran",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 16.sp,
                            lineHeight = 18.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Slip Desain Struk Thermal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Slate400, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFBFBFB),
                        contentColor = Slate900
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Header Struk
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF14532D),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PEMBAYARAN LUNAS",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D),
                                    fontSize = 13.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }

                        Text(
                            text = "Aplikasi Manajemen Hutang Piutang",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 11.sp,
                                lineHeight = 12.sp,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                        // Data Fields
                        StrukRow("No. Transaksi", "TRX-${installment.id}")
                        StrukRow("Tanggal & Waktu", FormatUtils.formatDateTime(installment.tanggalBayar ?: System.currentTimeMillis()))
                        StrukRow("Nama Nasabah", nasabah?.name ?: installment.customerName)
                        StrukRow("Kode Nasabah", nasabah?.customerCode ?: "-")
                        StrukRow("No. WhatsApp", nasabah?.whatsappNumber ?: "-")
                        StrukRow("Jenis Akad", kredit?.akadType ?: "Kredit")
                        StrukRow("Nama Barang/Pinjaman", kredit?.itemName ?: "-")
                        StrukRow("Angsuran Ke", "${installment.angsuranKe} dari ${kredit?.durasiTenor ?: "-"}")
                        StrukRow("Metode Bayar", installment.metodePembayaran ?: "TUNAI", isBold = true)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                        // Total Nominal Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TOTAL DIBAYARKAN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800,
                                    fontSize = 11.sp,
                                    lineHeight = 12.sp
                                )
                            )
                            Text(
                                text = FormatUtils.formatRupiah(installment.nominalRupiah),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald800,
                                    fontSize = 15.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol [Kirim ke WhatsApp] (Kirim PDF Struk ke WA)
                Button(
                    onClick = {
                        val pdf = PdfReceiptGenerator.createStrukPdf(context, installment, kredit, nasabah)
                        if (pdf != null) {
                            val caption = "Halo ${nasabah?.name ?: installment.customerName},\n" +
                                    "Berikut kami kirimkan file PDF Struk Bukti Pembayaran angsuran ke-${installment.angsuranKe} " +
                                    "sebesar ${FormatUtils.formatRupiah(installment.nominalRupiah)} (${installment.metodePembayaran ?: "TUNAI"}). Status: LUNAS. Terima kasih."
                            PdfReceiptGenerator.sharePdfToWhatsApp(context, pdf, nasabah?.whatsappNumber, caption)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("send_struk_whatsapp_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF15803D), // WhatsApp Green
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kirim ke WhatsApp (PDF)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 14.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Selesai",
                        style = MaterialTheme.typography.labelMedium.copy(color = Slate800, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun StrukRow(label: String, value: String, isBold: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Slate800,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (isBold) Emerald800 else Slate900,
                fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
        )
    }
}
