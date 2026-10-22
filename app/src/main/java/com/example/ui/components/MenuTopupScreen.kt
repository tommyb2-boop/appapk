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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TopupTransaction
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils

import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import com.example.ui.theme.Slate600
import com.example.ui.theme.defaultInputColors

@Composable
fun MenuTopupScreen(
    topupList: List<TopupTransaction>,
    onMarkLunasClick: (Long) -> Unit,
    onAddTopupClick: () -> Unit,
    onPayTopupClick: ((topupId: Long, nominalBayar: Double, isFullLunas: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // State untuk konfirmasi notifikasi pertanyaan Yes or No sebelum pembayaran topup
    var itemToConfirmLunas by remember { mutableStateOf<TopupTransaction?>(null) }
    var showRekapDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(topupList, searchQuery) {
        if (searchQuery.isBlank()) {
            topupList
        } else {
            val q = searchQuery.trim().lowercase()
            topupList.filter {
                it.nama.lowercase().contains(q) ||
                (it.catatan?.lowercase()?.contains(q) == true) ||
                it.id.toString().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("menu_topup_screen")
    ) {
        // Baris Header & Tombol Tambah + Rekap
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Piutang Topup / Pulsa",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 15.sp,
                    lineHeight = 17.sp
                )
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Rekap Topup (Filter rentang tanggal & simpan PDF)
                OutlinedButton(
                    onClick = { showRekapDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, Amber700),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("rekap_topup_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Amber700
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rekap",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Amber700,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }

                Button(
                    onClick = onAddTopupClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber700,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_topup_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tambah",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }
            }
        }

        // Kotak Pencarian di Halaman Topup - Selalu Tampil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Cari nama pembeli atau catatan...",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Topup",
                        tint = Slate700,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus Pencarian",
                                tint = Slate700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                colors = defaultInputColors(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_topup_field")
            )
        }

        if (showRekapDialog) {
            RekapTopupDialog(
                topupList = topupList,
                onDismiss = { showRekapDialog = false }
            )
        }

        if (topupList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Belum ada transaksi topup/piutang langsung",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Pencatatan simpel untuk pinjaman kilat (bayar besok / minggu depan).",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate800),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = Slate600
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada hasil pencarian",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Tidak ditemukan transaksi topup untuk \"$searchQuery\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { searchQuery = "" },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Reset Pencarian", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    TopupCardItem(
                        item = item,
                        onMarkLunasClick = { itemToConfirmLunas = item }
                    )
                }
            }
        }
    }

    // Modal Pembayaran Topup: Pilihan Lunas atau Sebagian (Cicil)
    if (itemToConfirmLunas != null) {
        val targetItem = itemToConfirmLunas!!
        BayarTopupDialog(
            topup = targetItem,
            onDismiss = { itemToConfirmLunas = null },
            onConfirmPayment = { id, nom, isFull ->
                itemToConfirmLunas = null
                if (onPayTopupClick != null) {
                    onPayTopupClick(id, nom, isFull)
                } else {
                    onMarkLunasClick(id)
                }
            }
        )
    }
}

/**
 * Tampilan sangat simple:
 * Nama, Nominal, Tanggal Jatuh Tempo, Status, Tombol Bayar
 */
@Composable
private fun TopupCardItem(
    item: TopupTransaction,
    onMarkLunasClick: () -> Unit
) {
    val isOverdue = !item.isLunas && item.tanggalJatuhTempo < System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("topup_item_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isLunas) Color(0xFFF0FDF4) else Color(0xFFFFFFFF),
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isLunas) Color(0xFF86EFAC) else Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nama,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 14.sp,
                        lineHeight = 16.sp
                    )
                )

                if (item.totalDibayar > 0 && !item.isLunas) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Sisa: ${FormatUtils.formatRupiah(item.sisaNominal)}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Red700,
                                fontSize = 13.sp,
                                lineHeight = 14.sp
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "Cicil (Dibayar: ${FormatUtils.formatRupiah(item.totalDibayar)})",
                                color = Color(0xFF92400E),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = FormatUtils.formatRupiah(item.nominal),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Amber700,
                            fontSize = 13.sp,
                            lineHeight = 14.sp
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Text(
                    text = "Jatuh Tempo: ${FormatUtils.formatDate(item.tanggalJatuhTempo)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isOverdue) Red700 else Slate800,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    )
                )
            }

            if (!item.isLunas) {
                Button(
                    onClick = onMarkLunasClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("pay_topup_btn_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BAYAR",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LUNAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF14532D),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }
            }
        }
    }
}
