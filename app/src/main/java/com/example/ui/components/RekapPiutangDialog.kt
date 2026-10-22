package com.example.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.KreditPiutang
import com.example.data.model.PiutangInstallment
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import com.example.util.PdfReceiptGenerator
import java.util.Calendar

@Composable
fun RekapPiutangDialog(
    kreditList: List<KreditPiutang>,
    installmentList: List<PiutangInstallment> = emptyList(),
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
    var statusFilterTab by remember { mutableIntStateOf(0) } // 0 = Semua, 1 = Lunas, 2 = Belum Lunas

    // Filter data kredit berdasarkan rentang tanggal dan status
    val filteredKredit = remember(kreditList, startMillis, endMillis, statusFilterTab) {
        kreditList.filter { k ->
            val dateMatch = k.tanggalMulai in startMillis..endMillis
            val isLunas = k.totalTerbayar >= k.totalPiutang && k.totalPiutang > 0
            val statusMatch = when (statusFilterTab) {
                1 -> isLunas
                2 -> !isLunas
                else -> true
            }
            dateMatch && statusMatch
        }.sortedByDescending { it.tanggalMulai }
    }

    // Perhitungan ringkasan
    val totalPiutang = filteredKredit.sumOf { it.totalPiutang }
    val totalTerbayar = filteredKredit.sumOf { it.totalTerbayar }
    val totalSisaPiutang = (totalPiutang - totalTerbayar).coerceAtLeast(0.0)
    val lunasCount = filteredKredit.count { it.totalTerbayar >= it.totalPiutang && it.totalPiutang > 0 }
    val belumLunasCount = filteredKredit.count { it.totalTerbayar < it.totalPiutang }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 740.dp)
                .testTag("rekap_piutang_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // HEADER
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
                            tint = Emerald800,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rekap Piutang Nasabah",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp
                                )
                            )
                            Text(
                                text = "Filter periode & unduh laporan PDF",
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

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. FILTER TANGGAL (Dari Tanggal s/d Sampai Tanggal)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Rentang Periode Transaksi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate800,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Tombol Dari Tanggal
                                    DateSelectorBox(
                                        label = "Dari",
                                        dateMillis = startMillis,
                                        modifier = Modifier.weight(1f),
                                        onDateSelected = { newDate ->
                                            startMillis = newDate
                                            if (startMillis > endMillis) endMillis = startMillis
                                        }
                                    )

                                    // Tombol Sampai Tanggal
                                    DateSelectorBox(
                                        label = "Sampai",
                                        dateMillis = endMillis,
                                        modifier = Modifier.weight(1f),
                                        onDateSelected = { newDate ->
                                            endMillis = newDate
                                            if (endMillis < startMillis) startMillis = endMillis
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. KARTU RINGKASAN REKAP
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                            border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ringkasan Piutang Terpilih",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Emerald800,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Emerald800
                                    ) {
                                        Text(
                                            text = "${filteredKredit.size} Kontrak",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Total Piutang", fontSize = 10.sp, color = Slate700)
                                        Text(
                                            text = FormatUtils.formatRupiah(totalPiutang),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Terbayar", fontSize = 10.sp, color = Slate700)
                                        Text(
                                            text = FormatUtils.formatRupiah(totalTerbayar),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Emerald800,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Sisa Piutang", fontSize = 10.sp, color = Slate700)
                                        Text(
                                            text = FormatUtils.formatRupiah(totalSisaPiutang),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Red700,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. TAB FILTER STATUS (Semua / Lunas / Belum Lunas)
                    item {
                        TabRow(
                            selectedTabIndex = statusFilterTab,
                            containerColor = Color(0xFFF1F5F9),
                            contentColor = Emerald800,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[statusFilterTab]),
                                    color = Emerald800,
                                    height = 3.dp
                                )
                            }
                        ) {
                            Tab(
                                selected = statusFilterTab == 0,
                                onClick = { statusFilterTab = 0 },
                                text = { Text("Semua (${filteredKredit.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = statusFilterTab == 1,
                                onClick = { statusFilterTab = 1 },
                                text = { Text("Lunas ($lunasCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = statusFilterTab == 2,
                                onClick = { statusFilterTab = 2 },
                                text = { Text("Belum Lunas ($belumLunasCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }

                    // 4. DAFTAR KONTRAK PIUTANG
                    if (filteredKredit.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Tidak ada transaksi piutang pada periode ini",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate700,
                                            fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredKredit, key = { it.id }) { item ->
                            val sisa = (item.totalPiutang - item.totalTerbayar).coerceAtLeast(0.0)
                            val isLunas = sisa <= 0.0 && item.totalPiutang > 0

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.customerName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Slate900,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text(
                                                text = "${item.itemName} • ${FormatUtils.formatDateOnly(item.tanggalMulai)}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Slate700,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isLunas) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                            border = BorderStroke(1.dp, if (isLunas) Color(0xFFA7F3D0) else Color(0xFFFECDD3))
                                        ) {
                                            Text(
                                                text = if (isLunas) "LUNAS" else "BELUM LUNAS",
                                                color = if (isLunas) Emerald800 else Red700,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    HorizontalDivider(color = Slate200, thickness = 0.8.dp)
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Piutang", fontSize = 9.sp, color = Slate700)
                                            Text(
                                                FormatUtils.formatRupiah(item.totalPiutang),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Slate900
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("Terbayar", fontSize = 9.sp, color = Slate700)
                                            Text(
                                                FormatUtils.formatRupiah(item.totalTerbayar),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = Emerald800
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Sisa Piutang", fontSize = 9.sp, color = Slate700)
                                            Text(
                                                FormatUtils.formatRupiah(sisa),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                color = if (sisa > 0) Red700 else Slate700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FOOTER TOMBOL UNDUH PDF
                Surface(
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Slate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val pdfFile = PdfReceiptGenerator.createRekapPiutangPdf(
                                    context = context,
                                    startDate = startMillis,
                                    endDate = endMillis,
                                    kreditList = filteredKredit
                                )
                                if (pdfFile != null) {
                                    Toast.makeText(context, "Laporan PDF berhasil dibuat", Toast.LENGTH_SHORT).show()
                                    PdfReceiptGenerator.openPdf(context, pdfFile)
                                } else {
                                    Toast.makeText(context, "Gagal membuat laporan PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald800,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("btn_download_rekap_piutang_pdf")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Unduh PDF Rekap (${filteredKredit.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
