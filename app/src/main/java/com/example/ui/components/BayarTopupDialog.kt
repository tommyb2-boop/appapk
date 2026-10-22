package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.TopupTransaction
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultInputColors
import com.example.util.FormatUtils

@Composable
fun BayarTopupDialog(
    topup: TopupTransaction,
    onDismiss: () -> Unit,
    onConfirmPayment: (topupId: Long, nominalBayar: Double, isFullLunas: Boolean) -> Unit
) {
    val sisaPiutang = topup.sisaNominal
    // Mode pembayaran: true = LUNAS, false = SEBAGIAN
    var isLunasMode by remember { mutableStateOf(true) }

    // State nominal pembayaran dengan format titik ribuan
    var nominalBayarStr by remember {
        mutableStateOf(FormatUtils.formatThousandDots(sisaPiutang.toLong().toString()))
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val cleanNominal = FormatUtils.cleanDigits(nominalBayarStr).toDoubleOrNull() ?: 0.0
    val sisaSetelahBayar = (sisaPiutang - (if (isLunasMode) sisaPiutang else cleanNominal)).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .testTag("bayar_topup_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Dialog
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFFFEF3C7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Amber700,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pembayaran Piutang Topup",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = topup.nama,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate700,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
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

                // Rincian Tagihan Awal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Nilai Transaksi:", fontSize = 11.sp, color = Slate700)
                            Text(FormatUtils.formatRupiah(topup.nominal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
                        }

                        if (topup.totalDibayar > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Telah Dibayar Sebelumnya:", fontSize = 11.sp, color = Emerald800)
                                Text(FormatUtils.formatRupiah(topup.totalDibayar), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Piutang Saat Ini:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red700)
                            Text(FormatUtils.formatRupiah(sisaPiutang), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Red700)
                        }
                    }
                }

                Text(
                    text = "Pilih Metode Pembayaran:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                // Kotak Pilihan: Ceklis Lunas & Ceklis Sebagian
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ceklis Lunas
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isLunasMode = true
                                nominalBayarStr = FormatUtils.formatThousandDots(sisaPiutang.toLong().toString())
                                errorMsg = null
                            }
                            .testTag("radio_topup_lunas"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLunasMode) Color(0xFFECFDF5) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.5.dp,
                            if (isLunasMode) Emerald800 else Color(0xFFCBD5E1)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isLunasMode,
                                onCheckedChange = {
                                    isLunasMode = true
                                    nominalBayarStr = FormatUtils.formatThousandDots(sisaPiutang.toLong().toString())
                                    errorMsg = null
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Emerald800,
                                    checkmarkColor = Color.White
                                )
                            )
                            Column {
                                Text(
                                    text = "Lunas",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLunasMode) Emerald800 else Slate900
                                )
                                Text(
                                    text = "Bayar Penuh",
                                    fontSize = 10.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }

                    // Ceklis Sebagian (Cicilan)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isLunasMode = false
                                // Kosongkan atau beri nilai default setengah/0 agar admin mengetik nominal
                                if (nominalBayarStr == FormatUtils.formatThousandDots(sisaPiutang.toLong().toString())) {
                                    nominalBayarStr = ""
                                }
                                errorMsg = null
                            }
                            .testTag("radio_topup_sebagian"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isLunasMode) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            1.5.dp,
                            if (!isLunasMode) Amber700 else Color(0xFFCBD5E1)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = !isLunasMode,
                                onCheckedChange = {
                                    isLunasMode = false
                                    if (nominalBayarStr == FormatUtils.formatThousandDots(sisaPiutang.toLong().toString())) {
                                        nominalBayarStr = ""
                                    }
                                    errorMsg = null
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Amber700,
                                    checkmarkColor = Color.White
                                )
                            )
                            Column {
                                Text(
                                    text = "Sebagian",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isLunasMode) Amber700 else Slate900
                                )
                                Text(
                                    text = "Bayar Cicil",
                                    fontSize = 10.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }
                }

                // Kotak Input Nominal Pembayaran
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (isLunasMode) "Total Nominal Piutang (Lunas):" else "Nominal Pembayaran Sebagian (Menyala):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (!isLunasMode) Amber700 else Slate800
                    )

                    OutlinedTextField(
                        value = if (isLunasMode) FormatUtils.formatThousandDots(sisaPiutang.toLong().toString()) else nominalBayarStr,
                        onValueChange = { input ->
                            if (!isLunasMode) {
                                val clean = FormatUtils.cleanDigits(input)
                                nominalBayarStr = FormatUtils.formatThousandDots(clean)
                                errorMsg = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_nominal_bayar_topup"),
                        enabled = !isLunasMode, // Menyala hanya ketika ceklis Sebagian
                        prefix = {
                            Text(
                                "Rp ",
                                fontWeight = FontWeight.Bold,
                                color = if (!isLunasMode) Amber700 else Slate700,
                                fontSize = 15.sp
                            )
                        },
                        placeholder = { Text("0", fontSize = 15.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(10.dp),
                        colors = defaultInputColors()
                    )

                    if (!isLunasMode) {
                        Text(
                            text = "💡 Ketik nominal yang dibayarkan nasabah saat ini.",
                            fontSize = 10.5.sp,
                            color = Slate700
                        )
                    }
                }

                // Keterangan Sisa Piutang Setelah Pembayaran
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isLunasMode || sisaSetelahBayar <= 0.0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
                    border = BorderStroke(
                        1.dp,
                        if (isLunasMode || sisaSetelahBayar <= 0.0) Color(0xFF86EFAC) else Color(0xFFFECDD3)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLunasMode || sisaSetelahBayar <= 0.0) "Status Akhir: LUNAS" else "Sisa Piutang Nanti:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLunasMode || sisaSetelahBayar <= 0.0) Emerald800 else Red700
                        )
                        Text(
                            text = if (isLunasMode || sisaSetelahBayar <= 0.0) "Rp 0" else FormatUtils.formatRupiah(sisaSetelahBayar),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLunasMode || sisaSetelahBayar <= 0.0) Emerald800 else Red700
                        )
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = Red700,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Tombol Aksi Batal & Konfirmasi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_cancel_bayar_topup"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate800)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (isLunasMode) {
                                onConfirmPayment(topup.id, sisaPiutang, true)
                            } else {
                                val nominal = FormatUtils.cleanDigits(nominalBayarStr).toDoubleOrNull() ?: 0.0
                                if (nominal <= 0) {
                                    errorMsg = "Nominal pembayaran harus lebih dari 0."
                                    return@Button
                                }
                                val isFull = nominal >= sisaPiutang
                                onConfirmPayment(topup.id, nominal, isFull)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_confirm_bayar_topup"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLunasMode) Emerald800 else Amber700,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLunasMode) "Bayar Lunas" else "Bayar Sebagian",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
