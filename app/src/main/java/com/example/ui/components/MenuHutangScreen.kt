package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
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
fun MenuHutangScreen(
    hutangList: List<Hutang>,
    paymentList: List<HutangPayment> = emptyList(),
    onPayClick: (Hutang) -> Unit,
    onAddHutangClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRekapDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(hutangList, searchQuery) {
        if (searchQuery.isBlank()) {
            hutangList
        } else {
            val q = searchQuery.trim().lowercase()
            hutangList.filter {
                it.namaSumber.lowercase().contains(q) ||
                (it.catatan?.lowercase()?.contains(q) == true) ||
                it.id.toString().contains(q)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("menu_hutang_screen")
    ) {
        // Tombol Tambah Hutang & Rekap di bagian atas list
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Catatan Hutang",
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
                // Tombol Rekap Hutang (Muncul filter dari tanggal s/d tanggal dan simpan PDF)
                OutlinedButton(
                    onClick = { showRekapDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.2.dp, Slate900),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("rekap_hutang_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = Slate900
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Rekap",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 11.sp,
                            lineHeight = 12.sp
                        )
                    )
                }

                Button(
                    onClick = onAddHutangClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate900,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("add_hutang_button")
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

        // Kotak Pencarian di Halaman Hutang - Selalu Tampil
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
                        text = "Cari nama sumber hutang atau keterangan...",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari Hutang",
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
                    .testTag("search_hutang_field")
            )
        }

        if (showRekapDialog) {
            RekapHutangDialog(
                hutangList = hutangList,
                paymentList = paymentList,
                onDismiss = { showRekapDialog = false }
            )
        }

        if (hutangList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MoneyOff,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Slate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tidak ada catatan hutang pribadi",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Slate900,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Tekan tombol 'Tambah' di atas untuk mencatat hutang baru.",
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
                        text = "Tidak ditemukan catatan hutang untuk \"$searchQuery\"",
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
                    HutangCardItem(
                        item = item,
                        onPayClick = { onPayClick(item) }
                    )
                }
            }
        }
    }
}

/**
 * Menampilkan: Nama Sumber, Nominal Hutang, Hutang Dibayar, Sisa Hutang.
 * Tiap sumber ada tombol [BAYAR].
 */
@Composable
private fun HutangCardItem(
    item: Hutang,
    onPayClick: () -> Unit
) {
    val isLunas = item.sisaHutang <= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hutang_item_${item.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLunas) Color(0xFFF0FDF4) else Color(0xFFFFFFFF),
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLunas) Color(0xFF86EFAC) else Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Baris Nama Sumber dan Tombol [BAYAR]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.namaSumber,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 15.sp,
                            lineHeight = 17.sp
                        )
                    )
                    if (!item.catatan.isNullOrBlank()) {
                        Text(
                            text = item.catatan,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate700,
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            )
                        )
                    }
                }

                // Tombol [BAYAR]
                if (!isLunas) {
                    Button(
                        onClick = onPayClick,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Red700,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 5.dp),
                        modifier = Modifier.testTag("pay_hutang_btn_${item.id}")
                    ) {
                        Text(
                            text = "BAYAR",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp,
                                lineHeight = 13.sp
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

            // Grid Rincian: Nominal Hutang, Hutang Dibayar, Sisa Hutang
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Nominal Hutang
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nominal Hutang",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    )
                    Text(
                        text = FormatUtils.formatRupiah(item.nominalHutang),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        )
                    )
                }

                // Hutang Dibayar
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hutang Dibayar",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    )
                    Text(
                        text = FormatUtils.formatRupiah(item.hutangDibayar),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Emerald800,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        )
                    )
                }

                // Sisa Hutang
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Sisa Hutang",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    )
                    Text(
                        text = FormatUtils.formatRupiah(item.sisaHutang),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.sisaHutang > 0) Red700 else Emerald800,
                            fontSize = 12.sp,
                            lineHeight = 14.sp
                        )
                    )
                }
            }
        }
    }
}
