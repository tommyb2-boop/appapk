package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import com.example.util.PdfReceiptGenerator

@Composable
fun DetailNasabahScreen(
    nasabah: Nasabah,
    kreditList: List<KreditPiutang>,
    installments: List<PiutangInstallment>,
    onBack: () -> Unit,
    onKwitansiClick: (PiutangInstallment, KreditPiutang?) -> Unit,
    modifier: Modifier = Modifier,
    isCustomerView: Boolean = false,
    dueBillsCount: Int = 0,
    onNotificationClick: () -> Unit = {},
    onEditNasabahClick: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    BackHandler {
        if (isCustomerView) {
            onLogout()
        } else {
            onBack()
        }
    }

    // Perhitungan Keuangan Nasabah
    val totalPiutang = kreditList.sumOf { it.totalPiutang }
    val totalTerbayar = kreditList.sumOf { it.totalTerbayar }
    val sisaPiutang = (totalPiutang - totalTerbayar).coerceAtLeast(0.0)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("detail_nasabah_screen"),
        containerColor = Color(0xFFF8FAFC),
        contentColor = Slate900,
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Emerald800,
                    contentColor = Color.White
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (!isCustomerView) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("detail_back_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Kembali",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFDCFCE7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Emerald800,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                            }

                            Column {
                                Text(
                                    text = "${nasabah.name} (${nasabah.customerCode})",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        lineHeight = 19.sp
                                    )
                                )
                                if (isCustomerView) {
                                    Text(
                                        text = "Portal Informasi Piutang Anda",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFFD1FAE5),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        // Tombol Tagihan & Keluar khusus untuk Customer View
                        if (isCustomerView) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (dueBillsCount > 0) {
                                    Box(
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        IconButton(
                                            onClick = onNotificationClick,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .testTag("customer_due_bills_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = "Tagihan Jatuh Tempo",
                                                tint = Color(0xFFFDE047), // Gold / Yellow
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                        Surface(
                                            shape = CircleShape,
                                            color = Red700,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = if (dueBillsCount > 9) "9+" else "$dueBillsCount",
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }

                                OutlinedButton(
                                    onClick = onLogout,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        contentColor = Color.White
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("customer_logout_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Keluar",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Keluar",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }

                        // Tombol Edit Nasabah khusus untuk Admin View
                        if (!isCustomerView && onEditNasabahClick != null) {
                            OutlinedButton(
                                onClick = onEditNasabahClick,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.15f),
                                    contentColor = Color.White
                                ),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("detail_edit_nasabah_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Nasabah",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Di bawah nama pelanggan (misal: cust-001)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = if (isCustomerView) 46.dp else 42.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "WhatsApp: ${nasabah.whatsappNumber}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFECFDF5),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            )
                        )
                        if (!isCustomerView) {
                            Text(
                                text = "PIN Login: ${nasabah.pinLogin}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFECFDF5),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isCustomerView) {
                // Footer Detail Nasabah: Hanya tombol [Kirim WhatsApp] untuk Admin
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = Color.White,
                    tonalElevation = 6.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                val textMessage = "Halo ${nasabah.name} (${nasabah.customerCode}),\n" +
                                        "Berikut rincian informasi piutang Anda:\n" +
                                        "• Total Nominal Piutang: ${FormatUtils.formatRupiah(totalPiutang)}\n" +
                                        "• Total Dibayarkan: ${FormatUtils.formatRupiah(totalTerbayar)}\n" +
                                        "• Total Sisa Piutang: ${FormatUtils.formatRupiah(sisaPiutang)}\n\n" +
                                        "Terima kasih atas kerja samanya."
                                PdfReceiptGenerator.sendWhatsAppText(context, nasabah.whatsappNumber, textMessage)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_whatsapp_summary_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF15803D), // WA Green
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kirim WhatsApp",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 15.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Kotak keterangan: Total Piutang Terbayar dan Sisa Piutang
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Slate900
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Piutang Terbayar",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(totalTerbayar),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Emerald800,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Sisa Piutang",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(sisaPiutang),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (sisaPiutang > 0) Red700 else Emerald800,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                lineHeight = 17.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Daftar Seluruh Piutang Nasabah",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 13.sp,
                    lineHeight = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Langsung tampilkan list seluruh piutang nasabah:
            // Nominal Rupiah, Tanggal Jatuh Tempo, Keterangan ("LUNAS" / "NON"). Di sebelah kanan ada tombol [Kwitansi].
            if (installments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan piutang untuk nasabah ini.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate800)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("detail_nasabah_installment_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(installments, key = { it.id }) { item ->
                        val relatedKredit = kreditList.find { it.id == item.kreditId }
                        NasabahInstallmentItem(
                            installment = item,
                            onKwitansiClick = { onKwitansiClick(item, relatedKredit) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tiap baris berisi:
 * - Nominal Rupiah
 * - Tanggal Jatuh Tempo
 * - Keterangan ("LUNAS" / "NON")
 * - Di sebelah kanan ada tombol [Kwitansi]
 */
@Composable
private fun NasabahInstallmentItem(
    installment: PiutangInstallment,
    onKwitansiClick: () -> Unit
) {
    val isLunas = installment.status == "LUNAS"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLunas) Color(0xFFF0FDF4) else Color(0xFFFFFFFF),
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLunas) Color(0xFF86EFAC) else Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Nominal Rupiah
                    Text(
                        text = FormatUtils.formatRupiah(installment.nominalRupiah),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 14.sp,
                            lineHeight = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))

                    // Keterangan ("LUNAS" / "NON")
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isLunas) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLunas) "LUNAS" else "NON",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLunas) Color(0xFF14532D) else Color(0xFF991B1B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                lineHeight = 11.sp
                            )
                        )
                    }
                }

                // Tanggal Jatuh Tempo
                Text(
                    text = "Jatuh Tempo: ${FormatUtils.formatDate(installment.tanggalJatuhTempo)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate800,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Tombol [Kwitansi] di sebelah kanan
            OutlinedButton(
                onClick = onKwitansiClick,
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("kwitansi_btn_${installment.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Emerald800
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Kwitansi",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Emerald800,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    )
                )
            }
        }
    }
}
