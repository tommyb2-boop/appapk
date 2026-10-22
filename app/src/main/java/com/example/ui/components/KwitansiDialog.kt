package com.example.ui.components

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.KwitansiData
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import com.example.util.PdfReceiptGenerator

@Composable
fun KwitansiDialog(
    data: KwitansiData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val inst = data.installment
    val nasabah = data.nasabah
    val kredit = data.kredit

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("kwitansi_dialog"),
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
                        text = "Kwitansi Pembayaran",
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

                // Card Kwitansi
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Emerald800, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFCFDFD),
                        contentColor = Slate900
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "KWITANSI TANDA TERIMA",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald800,
                                fontSize = 14.sp,
                                lineHeight = 16.sp
                            )
                        )
                        Text(
                            text = "No. KWT-${inst.id}/${nasabah.customerCode}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 10.sp,
                                lineHeight = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                        KwitansiRow("Telah Diterima Dari", "${nasabah.name} (${nasabah.customerCode})")
                        KwitansiRow("Guna Pembayaran", "Angsuran ${inst.angsuranKe} - ${kredit?.itemName ?: "Piutang"}")
                        KwitansiRow("Jatuh Tempo", FormatUtils.formatDate(inst.tanggalJatuhTempo))
                        KwitansiRow(
                            "Status",
                            if (inst.status == "LUNAS") "LUNAS (${inst.metodePembayaran ?: "TUNAI"})" else "BELUM LUNAS",
                            isHighlight = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Nominal Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Slate200, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Jumlah Uang:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate800,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = FormatUtils.formatRupiah(inst.nominalRupiah),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald800,
                                    fontSize = 15.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tanggal Cetak: ${FormatUtils.formatDate(System.currentTimeMillis())}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Right
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kirim PDF Kwitansi ke WhatsApp
                Button(
                    onClick = {
                        val pdf = PdfReceiptGenerator.createKwitansiPdf(context, nasabah, inst, kredit)
                        if (pdf != null) {
                            val caption = "Kwitansi Tanda Terima Resmi untuk ${nasabah.name}:\n" +
                                    "Pembayaran Angsuran ${inst.angsuranKe} (${FormatUtils.formatRupiah(inst.nominalRupiah)})."
                            PdfReceiptGenerator.sharePdfToWhatsApp(context, pdf, nasabah.whatsappNumber, caption)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_kwitansi_pdf_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF15803D),
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
                        text = "Kirim Kwitansi ke WhatsApp (PDF)",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 14.sp
                        )
                    )
                }

                TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = "Tutup", style = MaterialTheme.typography.labelMedium.copy(color = Slate800, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun KwitansiRow(label: String, value: String, isHighlight: Boolean = false) {
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
                color = if (isHighlight) Emerald800 else Slate900,
                fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
        )
    }
}
