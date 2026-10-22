package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.Hutang
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultInputColors
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PayHutangDialog(
    hutang: Hutang,
    onDismiss: () -> Unit,
    onConfirmPay: (nominal: Double, tanggal: String) -> Unit
) {
    val currentDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date())
    }

    var nominalInput by remember { mutableStateOf(FormatUtils.formatThousandDots(hutang.sisaHutang.toLong().toString())) }
    var tanggalInput by remember { mutableStateOf(currentDateStr) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pay_hutang_dialog"),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bayar Hutang",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp,
                        lineHeight = 18.sp
                    )
                )

                Text(
                    text = "Sumber: ${hutang.namaSumber} • Sisa: ${FormatUtils.formatRupiah(hutang.sisaHutang)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate800,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Input manual: "Nominal Rupiah" (Format Titik Otomatis)
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = {
                        nominalInput = FormatUtils.formatThousandDots(it)
                        errorText = null
                    },
                    label = { Text("Nominal Rupiah", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: 500.000", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_hutang_nominal_input"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input manual: "Tanggal"
                OutlinedTextField(
                    value = tanggalInput,
                    onValueChange = { tanggalInput = it },
                    label = { Text("Tanggal Pembayaran", fontSize = 12.sp) },
                    placeholder = { Text("DD/MM/YYYY", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_hutang_date_input")
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = Red700,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Batal", color = Slate800, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val nominal = FormatUtils.cleanDigits(nominalInput).toDoubleOrNull() ?: 0.0
                            if (nominal <= 0) {
                                errorText = "Masukkan nominal valid"
                                return@Button
                            }
                            if (tanggalInput.isBlank()) {
                                errorText = "Tanggal tidak boleh kosong"
                                return@Button
                            }
                            onConfirmPay(nominal, tanggalInput)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_pay_hutang_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald800,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Simpan",
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
