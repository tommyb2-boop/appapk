package com.example.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TopupTransaction
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import com.example.util.PdfReceiptGenerator
import java.util.Calendar

@Composable
fun RekapTopupDialog(
    topupList: List<TopupTransaction>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Waktu default: Dari awal bulan ini s/d akhir hari ini
    val initialStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val initialEndMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    var startMillis by remember { mutableLongStateOf(initialStartMillis) }
    var endMillis by remember { mutableLongStateOf(initialEndMillis) }
    var statusFilter by remember { mutableStateOf("SEMUA") } // SEMUA, LUNAS, BELUM

    // Filter data
    val filteredTopup = remember(topupList, startMillis, endMillis, statusFilter) {
        topupList.filter { t ->
            val dateMatch = t.tanggalTransaksi in startMillis..endMillis
            val statusMatch = when (statusFilter) {
                "LUNAS" -> t.isLunas
                "BELUM" -> !t.isLunas
                else -> true
            }
            dateMatch && statusMatch
        }.sortedByDescending { it.tanggalTransaksi }
    }

    // Perhitungan ringkasan
    val totalOmset = filteredTopup.sumOf { it.nominal }
    val totalKeuntungan = filteredTopup.sumOf { it.keuntungan }
    val lunasCount = filteredTopup.count { it.isLunas }
    val belumCount = filteredTopup.count { !it.isLunas }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 740.dp)
                .testTag("rekap_topup_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HEADER TETAP
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = Amber700,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rekap Transaksi Topup / Pulsa",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp
                                )
                            )
                            Text(
                                text = "Pilih periode & riwayat transaksi",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate700,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate700,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = Slate200, thickness = 1.dp)

                // BODY KONTEN
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // 1. Selector Rentang Tanggal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DateSelectorBox(
                            label = "Dari Tanggal",
                            dateMillis = startMillis,
                            onDateSelected = { selected ->
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = selected
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                startMillis = cal.timeInMillis
                            },
                            modifier = Modifier.weight(1f)
                        )

                        DateSelectorBox(
                            label = "Sampai Tanggal",
                            dateMillis = endMillis,
                            onDateSelected = { selected ->
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = selected
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                endMillis = cal.timeInMillis
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Quick Range Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickRangeChip(
                            text = "Bulan Ini",
                            onClick = {
                                val calStart = Calendar.getInstance().apply {
                                    set(Calendar.DAY_OF_MONTH, 1)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                val calEnd = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                startMillis = calStart.timeInMillis
                                endMillis = calEnd.timeInMillis
                            }
                        )
                        QuickRangeChip(
                            text = "30 Hari Terakhir",
                            onClick = {
                                val calEnd = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 23)
                                    set(Calendar.MINUTE, 59)
                                    set(Calendar.SECOND, 59)
                                    set(Calendar.MILLISECOND, 999)
                                }
                                val calStart = Calendar.getInstance().apply {
                                    timeInMillis = calEnd.timeInMillis - (30L * 86400000L)
                                    set(Calendar.HOUR_OF_DAY, 0)
                                    set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                startMillis = calStart.timeInMillis
                                endMillis = calEnd.timeInMillis
                            }
                        )
                        QuickRangeChip(
                            text = "Semua Catatan",
                            onClick = {
                                startMillis = 0L
                                endMillis = System.currentTimeMillis() + 86400000L
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Status (Semua / Lunas / Belum Lunas)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate800)
                        listOf("SEMUA" to "Semua", "LUNAS" to "Lunas", "BELUM" to "Belum").forEach { (code, label) ->
                            val isSelected = statusFilter == code
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Amber700 else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSelected) Amber700 else Slate200),
                                modifier = Modifier.clickable { statusFilter = code }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Slate800,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. KARTU RINGKASAN
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Total Omset", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatRupiah(totalOmset), fontSize = 12.sp, color = Slate900, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Keuntungan (Laba)", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatRupiah(totalKeuntungan), fontSize = 12.sp, color = Amber700, fontWeight = FontWeight.ExtraBold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Status Transaksi", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.SemiBold)
                                Text("$lunasCount Lunas • $belumCount Blm", fontSize = 11.sp, color = Color(0xFF14532D), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. DAFTAR TRANSAKSI TOPUP
                    if (filteredTopup.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada transaksi topup pada periode ini",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(filteredTopup, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.isLunas) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (item.isLunas) Color(0xFFBBF7D0) else Color(0xFFFDE68A)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.nama,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Slate900
                                                )
                                                Text(
                                                    text = "Tgl: ${FormatUtils.formatDateOnly(item.tanggalTransaksi)}",
                                                    fontSize = 10.sp,
                                                    color = Slate700
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = FormatUtils.formatRupiah(item.nominal),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Slate900
                                                )
                                                Text(
                                                    text = "Laba: +${FormatUtils.formatRupiah(item.keuntungan)}",
                                                    fontSize = 10.sp,
                                                    color = Amber700,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                            color = Slate200
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (item.isLunas) {
                                                val bayarStr = item.tanggalBayar?.let { FormatUtils.formatDateOnly(it) } ?: "Hari Ini"
                                                Text(
                                                    text = "✓ LUNAS (Bayar: $bayarStr)",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Emerald800
                                                )
                                            } else {
                                                Text(
                                                    text = "• BELUM LUNAS",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Red700
                                                )
                                            }

                                            val jtStr = FormatUtils.formatDateOnly(item.tanggalJatuhTempo)
                                            Text(
                                                text = "Jatuh Tempo: $jtStr",
                                                fontSize = 9.5.sp,
                                                color = Slate700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FOOTER TETAP (Tutup & Simpan PDF)
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Tutup", color = Slate800, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val pdfFile = PdfReceiptGenerator.createRekapTopupPdf(
                                context = context,
                                startDate = startMillis,
                                endDate = endMillis,
                                topupList = filteredTopup
                            )
                            if (pdfFile != null) {
                                Toast.makeText(context, "Rekap PDF Topup berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                PdfReceiptGenerator.openPdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Gagal membuat PDF rekap", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_pdf_rekap_topup_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber700,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Simpan PDF",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
