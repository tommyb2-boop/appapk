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
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
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
fun RekapHutangDialog(
    hutangList: List<Hutang>,
    paymentList: List<HutangPayment>,
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Hutang Baru, 1 = Riwayat Pembayaran

    // Filter data sesuai rentang tanggal
    val filteredHutang = remember(hutangList, startMillis, endMillis) {
        hutangList.filter { it.tanggalCatat in startMillis..endMillis }
            .sortedByDescending { it.tanggalCatat }
    }
    val filteredPayments = remember(paymentList, startMillis, endMillis) {
        paymentList.filter { it.tanggalBayar in startMillis..endMillis }
            .sortedByDescending { it.tanggalBayar }
    }

    // Ringkasan nominal
    val totalHutangBaru = filteredHutang.sumOf { it.nominalHutang }
    val totalBayar = filteredPayments.sumOf { it.nominalBayar }
    val selisih = (totalHutangBaru - totalBayar).coerceAtLeast(0.0)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 740.dp)
                .testTag("rekap_hutang_dialog"),
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
                            tint = Slate900,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Rekap Hutang & Pembayaran",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp
                                )
                            )
                            Text(
                                text = "Pilih periode tanggal transaksi",
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

                // BODY KONTEN (Filter Tanggal + Ringkasan + List)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // 1. Selector Dari Tanggal & Sampai Tanggal
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Kotak Dari Tanggal
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

                        // Kotak Sampai Tanggal
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

                    // Tombol Quick Range (Bulan Ini, 30 Hari, Semua)
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

                    // 2. KARTU RINGKASAN
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Hutang Baru", fontSize = 10.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatRupiah(totalHutangBaru), fontSize = 12.sp, color = Slate900, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pembayaran", fontSize = 10.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatRupiah(totalBayar), fontSize = 12.sp, color = Emerald800, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Sisa", fontSize = 10.sp, color = Slate700, fontWeight = FontWeight.SemiBold)
                                Text(FormatUtils.formatRupiah(selisih), fontSize = 12.sp, color = if (selisih > 0) Red700 else Emerald800, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. TAB NAVIGASI (Hutang Baru vs Pembayaran)
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Slate900,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Emerald800,
                                height = 2.5.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "Hutang Baru (${filteredHutang.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 0) Slate900 else Slate700
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Pembayaran (${filteredPayments.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) Slate900 else Slate700
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. DAFTAR RIWAYAT
                    if (selectedTab == 0) {
                        if (filteredHutang.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada hutang baru di periode ini",
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
                                items(filteredHutang, key = { it.id }) { item ->
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
                                                        text = item.namaSumber,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Slate900
                                                    )
                                                    Text(
                                                        text = FormatUtils.formatDateOnly(item.tanggalCatat),
                                                        fontSize = 10.sp,
                                                        color = Slate700
                                                    )
                                                }
                                                Text(
                                                    text = FormatUtils.formatRupiah(item.nominalHutang),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Slate900
                                                )
                                            }
                                            if (!item.catatan.isNullOrBlank()) {
                                                Text(
                                                    text = "Ket: ${item.catatan}",
                                                    fontSize = 10.sp,
                                                    color = Slate700,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Dibayar: ${FormatUtils.formatRupiah(item.hutangDibayar)}",
                                                    fontSize = 10.sp,
                                                    color = Emerald800
                                                )
                                                Text(
                                                    text = "Sisa: ${FormatUtils.formatRupiah(item.sisaHutang)}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.sisaHutang > 0) Red700 else Emerald800
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (filteredPayments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada pembayaran di periode ini",
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
                                items(filteredPayments, key = { it.id }) { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Bayar: ${item.namaSumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(0xFF14532D)
                                                    )
                                                    Text(
                                                        text = FormatUtils.formatDateOnly(item.tanggalBayar),
                                                        fontSize = 10.sp,
                                                        color = Slate700
                                                    )
                                                }
                                                Text(
                                                    text = FormatUtils.formatRupiah(item.nominalBayar),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Emerald800
                                                )
                                            }
                                            if (!item.catatan.isNullOrBlank()) {
                                                Text(
                                                    text = "Catatan: ${item.catatan}",
                                                    fontSize = 10.sp,
                                                    color = Slate700,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FOOTER TETAP (Batal & Simpan PDF)
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
                            val pdfFile = PdfReceiptGenerator.createRekapHutangPdf(
                                context = context,
                                startDate = startMillis,
                                endDate = endMillis,
                                hutangList = filteredHutang,
                                paymentList = filteredPayments
                            )
                            if (pdfFile != null) {
                                Toast.makeText(context, "Rekap PDF berhasil dibuat!", Toast.LENGTH_SHORT).show()
                                PdfReceiptGenerator.openPdf(context, pdfFile)
                            } else {
                                Toast.makeText(context, "Gagal membuat PDF", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_pdf_rekap_hutang_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald800,
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

@Composable
fun DateSelectorBox(
    label: String,
    dateMillis: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateStr = FormatUtils.formatDateOnly(dateMillis)

    Surface(
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Slate400),
        color = Color(0xFFF8FAFC),
        modifier = modifier
            .height(52.dp)
            .clickable {
                val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        val selectedCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        }
                        onDateSelected(selectedCal.timeInMillis)
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 9.5.sp,
                    color = Slate700,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = dateStr,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = Slate700,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun QuickRangeChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF1F5F9),
        border = BorderStroke(1.dp, Slate200),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Slate800,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
