package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.KreditPiutang
import com.example.data.model.PiutangInstallment
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultFilterChipColors
import com.example.util.FormatUtils

@Composable
fun InstallmentListSheet(
    kredit: KreditPiutang,
    installments: List<PiutangInstallment>,
    onDismiss: () -> Unit,
    onPayClick: (PiutangInstallment) -> Unit
) {
    var filterMode by remember { mutableStateOf("SEMUA") } // "SEMUA", "JATUH_TEMPO", "BELUM_JATUH_TEMPO"

    val now = System.currentTimeMillis()
    val filteredList = remember(installments, filterMode) {
        when (filterMode) {
            "JATUH_TEMPO" -> installments.filter { it.tanggalJatuhTempo <= now && it.status != "LUNAS" }
            "BELUM_JATUH_TEMPO" -> installments.filter { it.tanggalJatuhTempo > now && it.status != "LUNAS" }
            else -> installments
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .testTag("installment_list_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Sheet
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daftar Piutang Angsuran",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 16.sp,
                                lineHeight = 18.sp
                            )
                        )
                        Text(
                            text = "${kredit.itemName} • ${kredit.customerName}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips: Semua, Jatuh Tempo, Belum Jatuh Tempo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "SEMUA" to "Semua",
                        "JATUH_TEMPO" to "Jatuh Tempo",
                        "BELUM_JATUH_TEMPO" to "Belum Jatuh Tempo"
                    ).forEach { (key, label) ->
                        val isSelected = filterMode == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { filterMode = key },
                            label = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) Color.White else Slate900,
                                        fontSize = 11.sp,
                                        lineHeight = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            colors = defaultFilterChipColors(),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Slate200)

                // List Rincian Piutang
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada daftar piutang untuk filter ini.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate800)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList, key = { it.id }) { inst ->
                            InstallmentCardItem(
                                installment = inst,
                                onPayClick = { onPayClick(inst) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Isinya tiap baris:
 * - Nominal Rupiah
 * - Tanggal Jatuh Tempo
 * - tombol [BAYAR]
 */
@Composable
private fun InstallmentCardItem(
    installment: PiutangInstallment,
    onPayClick: () -> Unit
) {
    val isLunas = installment.status == "LUNAS"
    val isOverdue = !isLunas && installment.tanggalJatuhTempo < System.currentTimeMillis()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("installment_item_${installment.id}"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLunas) Color(0xFFF0FDF4) else if (isOverdue) Color(0xFFFEF2F2) else Color(0xFFFFFFFF),
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLunas) Color(0xFF86EFAC) else if (isOverdue) Color(0xFFFECACA) else Slate200),
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
                    Text(
                        text = "Angsuran ${installment.angsuranKe}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 13.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isLunas) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "LUNAS (${installment.metodePembayaran ?: "TUNAI"})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF14532D),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    lineHeight = 11.sp
                                )
                            )
                        }
                    } else if (isOverdue) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEE2E2), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "JATUH TEMPO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF991B1B),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    lineHeight = 11.sp
                                )
                            )
                        }
                    }
                }

                // Nominal Rupiah
                Text(
                    text = FormatUtils.formatRupiah(installment.nominalRupiah),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 16.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                // Tanggal Jatuh Tempo
                Text(
                    text = "Jatuh Tempo: ${FormatUtils.formatDate(installment.tanggalJatuhTempo)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isOverdue) Red700 else Slate800,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 12.sp
                    )
                )
            }

            // Tombol [BAYAR] jika belum lunas
            if (!isLunas) {
                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("pay_installment_btn_${installment.id}")
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
            }
        }
    }
}
