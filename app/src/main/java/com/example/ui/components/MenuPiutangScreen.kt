package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KreditPiutang
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.PiutangInstallment
import com.example.ui.theme.Slate600
import com.example.ui.theme.defaultInputColors

@Composable
fun MenuPiutangScreen(
    kreditList: List<KreditPiutang>,
    installmentList: List<PiutangInstallment> = emptyList(),
    onListClick: (KreditPiutang) -> Unit,
    onCustomerClick: (Long) -> Unit,
    onAddClick: () -> Unit = {},
    onRekapClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showRekapDialog by remember { mutableStateOf(false) }

    val filteredList = remember(kreditList, searchQuery) {
        if (searchQuery.isBlank()) {
            kreditList
        } else {
            val q = searchQuery.trim().lowercase()
            kreditList.filter {
                it.customerName.lowercase().contains(q) ||
                it.customerCode.lowercase().contains(q) ||
                it.itemName.lowercase().contains(q) ||
                it.id.toString().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("menu_piutang_screen")
    ) {
        // Baris Header & Tombol Tambah + Rekap (seperti di Hutang dan Topup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Piutang Nasabah",
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
                // Tombol Rekap Piutang (seperti yang ada di hutang dan topup)
                OutlinedButton(
                    onClick = {
                        if (onRekapClick != null) onRekapClick()
                        else showRekapDialog = true
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, Emerald800),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("rekap_piutang_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Emerald800
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rekap",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Emerald800,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }

                // Tombol Tambah Piutang / Kredit Baru
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_piutang_button")
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

        if (showRekapDialog) {
            RekapPiutangDialog(
                kreditList = kreditList,
                installmentList = installmentList,
                onDismiss = { showRekapDialog = false }
            )
        }

        // Kotak Pencarian di Halaman Piutang - Selalu Tampil
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Cari nama nasabah, kode, atau barang...",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Piutang",
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
                    .testTag("search_piutang_field")
            )
        }

        if (kreditList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Belum ada transaksi piutang",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Gunakan tombol (+) di kiri bawah untuk membuat Nasabah atau Kredit baru.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            lineHeight = 14.sp
                        ),
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
                        text = "Tidak ditemukan transaksi piutang untuk \"$searchQuery\"",
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
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("menu_piutang_list"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    KreditPiutangRowItem(
                        item = item,
                        onListClick = { onListClick(item) },
                        onCustomerClick = { onCustomerClick(item.customerId) }
                    )
                }
            }
        }
    }
}

/**
 * Daftar Nasabah Depan: Tiap baris HANYA berisi:
 * - Jenis Akad
 * - Nama Kredit Barang/Pinjaman Tunai
 * - (Nominal Rupiah)
 * - Tombol di samping nama nasabah untuk detail
 * - Sebelah kanan ada tombol [LIST]
 */
@Composable
private fun KreditPiutangRowItem(
    item: KreditPiutang,
    onListClick: () -> Unit,
    onCustomerClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("kredit_item_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Informasi Utama: Akad, Nama Barang/Pinjaman, (Nominal Rupiah), & Link Nasabah
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                // Baris Nasabah (tombol di samping nama nasabah)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onCustomerClick() }
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Detail Nasabah",
                        tint = Emerald800,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.customerName} (${item.customerCode})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate900,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Jenis Akad
                Text(
                    text = item.akadType,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate800,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                // Nama Kredit Barang / Pinjaman Tunai
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Slate900,
                        fontSize = 15.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                // (Nominal Rupiah)
                Text(
                    text = "(${FormatUtils.formatRupiah(item.totalPiutang)})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Emerald800,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        lineHeight = 15.sp
                    ),
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            // Tombol [LIST] di sebelah kanan
            Button(
                onClick = onListClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald800,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("list_button_${item.id}")
            ) {
                Text(
                    text = "LIST",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        lineHeight = 14.sp
                    )
                )
            }
        }
    }
}
