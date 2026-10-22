package com.example.ui.components

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import java.util.Calendar

sealed class DueBillItem {
    abstract val id: Long
    abstract val title: String
    abstract val subtitle: String
    abstract val nominal: Double
    abstract val dueDateMillis: Long
    abstract val isOverdue: Boolean
    abstract val daysLate: Int

    data class Installment(
        val installment: PiutangInstallment,
        val nasabah: Nasabah?,
        val kredit: KreditPiutang?,
        val isCustomerView: Boolean = false,
        override val id: Long = installment.id,
        override val title: String = if (isCustomerView) (kredit?.itemName ?: "Kredit Angsuran") else (nasabah?.name ?: "Nasabah #${installment.customerId}"),
        override val subtitle: String = if (isCustomerView) "Angsuran ke-${installment.angsuranKe} • ${nasabah?.customerCode ?: ""}" else "Angsuran ke-${installment.angsuranKe} • ${kredit?.itemName ?: "Piutang"}",
        override val nominal: Double = installment.nominalRupiah,
        override val dueDateMillis: Long = installment.tanggalJatuhTempo,
        override val isOverdue: Boolean,
        override val daysLate: Int
    ) : DueBillItem()

    data class Topup(
        val topup: TopupTransaction,
        override val id: Long = topup.id,
        override val title: String = topup.nama,
        override val subtitle: String = "Piutang Topup / Pulsa",
        override val nominal: Double = topup.sisaNominal,
        override val dueDateMillis: Long = topup.tanggalJatuhTempo,
        override val isOverdue: Boolean,
        override val daysLate: Int
    ) : DueBillItem()
}

@Composable
fun DueBillsDialog(
    installments: List<PiutangInstallment>,
    kreditList: List<KreditPiutang>,
    nasabahList: List<Nasabah>,
    topupList: List<TopupTransaction>,
    onPayInstallmentClick: (PiutangInstallment) -> Unit,
    onPayTopupClick: (TopupTransaction) -> Unit,
    onDismiss: () -> Unit,
    isCustomerView: Boolean = false
) {
    val todayEndMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Hitung seluruh tagihan jatuh tempo (hari ini atau telah lewat)
    val dueInstallments = remember(installments, kreditList, nasabahList, isCustomerView) {
        val kreditMap = kreditList.associateBy { it.id }
        val nasabahMap = nasabahList.associateBy { it.id }

        installments.filter { it.status != "LUNAS" && it.tanggalJatuhTempo <= todayEndMillis }
            .map { inst ->
                val isOverdue = inst.tanggalJatuhTempo < todayStartMillis
                val diffDays = if (isOverdue) {
                    ((todayStartMillis - inst.tanggalJatuhTempo) / 86400000L).toInt().coerceAtLeast(1)
                } else 0
                DueBillItem.Installment(
                    installment = inst,
                    nasabah = nasabahMap[inst.customerId],
                    kredit = kreditMap[inst.kreditId],
                    isCustomerView = isCustomerView,
                    isOverdue = isOverdue,
                    daysLate = diffDays
                )
            }.sortedWith(compareByDescending<DueBillItem.Installment> { it.isOverdue }.thenBy { it.dueDateMillis })
    }

    val dueTopups = remember(topupList) {
        topupList.filter { !it.isLunas && it.tanggalJatuhTempo <= todayEndMillis }
            .map { t ->
                val isOverdue = t.tanggalJatuhTempo < todayStartMillis
                val diffDays = if (isOverdue) {
                    ((todayStartMillis - t.tanggalJatuhTempo) / 86400000L).toInt().coerceAtLeast(1)
                } else 0
                DueBillItem.Topup(
                    topup = t,
                    isOverdue = isOverdue,
                    daysLate = diffDays
                )
            }.sortedWith(compareByDescending<DueBillItem.Topup> { it.isOverdue }.thenBy { it.dueDateMillis })
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Semua, 1 = Piutang, 2 = Topup

    val allBills = remember(dueInstallments, dueTopups) {
        (dueInstallments + dueTopups).sortedWith(
            compareByDescending<DueBillItem> { it.isOverdue }.thenBy { it.dueDateMillis }
        )
    }

    val displayedBills = when (selectedTab) {
        1 -> dueInstallments
        2 -> dueTopups
        else -> allBills
    }

    val totalNominal = allBills.sumOf { it.nominal }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 740.dp)
                .testTag("due_bills_dialog"),
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
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFFFEE2E2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Red700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Tagihan Jatuh Tempo",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900,
                                        fontSize = 15.sp,
                                        lineHeight = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (allBills.isNotEmpty()) Red700 else Emerald800
                                ) {
                                    Text(
                                        text = "${allBills.size}",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isCustomerView) "Rincian angsuran Anda yang telah jatuh tempo" else "Segera hubungi nasabah untuk pembayaran",
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

                // BODY
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Ringkasan Kartu
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (allBills.isNotEmpty()) Color(0xFFFEF2F2) else Color(0xFFF0FDF4)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (allBills.isNotEmpty()) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isCustomerView) "Total Angsuran Jatuh Tempo" else "Total Nominal Jatuh Tempo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (allBills.isNotEmpty()) Color(0xFF991B1B) else Color(0xFF14532D)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(totalNominal),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (allBills.isNotEmpty()) Red700 else Emerald800
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isCustomerView) "${dueInstallments.size} Angsuran" else "${dueInstallments.size} Piutang • ${dueTopups.size} Topup",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                val overdueCount = allBills.count { it.isOverdue }
                                if (overdueCount > 0) {
                                    Text(
                                        text = "$overdueCount tagihan terlambat",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Red700
                                    )
                                } else {
                                    Text(
                                        text = "Semua jatuh tempo hari ini",
                                        fontSize = 10.sp,
                                        color = Emerald800
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Filter: Hanya ditampilkan untuk Admin jika ada topup
                    if (!isCustomerView && dueTopups.isNotEmpty()) {
                        TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Slate900,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Red700,
                                height = 2.5.dp
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "Semua (${allBills.size})",
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
                                    text = "Piutang (${dueInstallments.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 1) Slate900 else Slate700
                                )
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Text(
                                    text = "Topup (${dueTopups.size})",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == 2) Slate900 else Slate700
                                )
                            }
                        )
                    }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // DAFTAR TAGIHAN JATUH TEMPO
                    if (displayedBills.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(42.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Hebat! Tidak ada tagihan jatuh tempo saat ini.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Semua tagihan kredit & topup telah lunas atau belum jatuh tempo.",
                                    fontSize = 11.sp,
                                    color = Slate700
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(displayedBills, key = { "${it.javaClass.simpleName}_${it.id}" }) { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("due_bill_item_${item.id}"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.isOverdue) Color(0xFFFFF1F2) else Color(0xFFFFFBEB)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (item.isOverdue) Color(0xFFFECDD3) else Color(0xFFFDE68A)
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (item is DueBillItem.Installment) Emerald800 else Amber700
                                                    ) {
                                                        Text(
                                                            text = if (item is DueBillItem.Installment) "KREDIT" else "TOPUP",
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = item.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Slate900
                                                    )
                                                }

                                                Text(
                                                    text = item.subtitle,
                                                    fontSize = 11.sp,
                                                    color = Slate700,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }

                                            // Nominal
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = FormatUtils.formatRupiah(item.nominal),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp,
                                                    color = if (item.isOverdue) Red700 else Color(0xFFB45309)
                                                )

                                                // Status Terlambat / Jatuh Tempo Hari Ini
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (item.isOverdue) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                                    modifier = Modifier.padding(top = 3.dp)
                                                ) {
                                                    Text(
                                                        text = if (item.isOverdue) {
                                                            "Terlambat ${item.daysLate} hari"
                                                        } else {
                                                            "Jatuh Tempo: Hari Ini"
                                                        },
                                                        color = if (item.isOverdue) Red700 else Color(0xFF92400E),
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 9.5.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            color = if (item.isOverdue) Color(0xFFFECDD3) else Color(0xFFFDE68A)
                                        )

                                        // Baris Aksi Langsung (Tombol Bayar / Lunasi)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Batas: ${FormatUtils.formatDateOnly(item.dueDateMillis)}",
                                                fontSize = 10.sp,
                                                color = Slate700
                                            )

                                            when (item) {
                                                is DueBillItem.Installment -> {
                                                    if (!isCustomerView) {
                                                        Button(
                                                            onClick = {
                                                                onDismiss()
                                                                onPayInstallmentClick(item.installment)
                                                            },
                                                            shape = RoundedCornerShape(6.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = Emerald800,
                                                                contentColor = Color.White
                                                            ),
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                            modifier = Modifier.testTag("pay_due_installment_${item.installment.id}")
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Payments,
                                                                contentDescription = null,
                                                                modifier = Modifier.size(13.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(
                                                                text = "Bayar Angsuran",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    } else {
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color(0xFFF1F5F9),
                                                            border = BorderStroke(1.dp, Slate200)
                                                        ) {
                                                            Text(
                                                                text = "Silakan bayar ke kasir/toko",
                                                                fontSize = 10.5.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                color = Slate700,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                is DueBillItem.Topup -> {
                                                    Button(
                                                        onClick = {
                                                            onDismiss()
                                                            onPayTopupClick(item.topup)
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = Amber700,
                                                            contentColor = Color.White
                                                        ),
                                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                        modifier = Modifier.testTag("pay_due_topup_${item.topup.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "Lunasi Topup",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // FOOTER
                HorizontalDivider(color = Slate200, thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "Tutup", color = Slate900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
