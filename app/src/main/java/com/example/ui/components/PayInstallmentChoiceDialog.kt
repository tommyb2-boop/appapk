package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.PiutangInstallment
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils

@Composable
fun PayInstallmentChoiceDialog(
    installment: PiutangInstallment,
    onDismiss: () -> Unit,
    onConfirmOk: (metode: String) -> Unit
) {
    var selectedMetode by remember { mutableStateOf("TUNAI") } // "TUNAI" atau "NON TUNAI"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pay_choice_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Konfirmasi Pembayaran",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp,
                        lineHeight = 18.sp
                    )
                )

                Text(
                    text = "${installment.customerName} • Angsuran ${installment.angsuranKe}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate800,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    ),
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Card info nominal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = Slate900
                    ),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nominal Tagihan",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                lineHeight = 12.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(installment.nominalRupiah),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Emerald800,
                                fontSize = 18.sp,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Pilih Metode Pembayaran:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Slate900,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pilihan [TUNAI] atau [NON TUNAI]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tombol TUNAI
                    val isTunai = selectedMetode == "TUNAI"
                    Button(
                        onClick = { selectedMetode = "TUNAI" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("method_tunai_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTunai) Emerald800 else Color(0xFFF1F5F9),
                            contentColor = if (isTunai) Color.White else Slate900
                        ),
                        border = if (!isTunai) BorderStroke(1.5.dp, Slate400) else null
                    ) {
                        Text(
                            text = "TUNAI",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isTunai) Color.White else Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }

                    // Tombol NON TUNAI
                    val isNonTunai = selectedMetode == "NON TUNAI"
                    Button(
                        onClick = { selectedMetode = "NON TUNAI" },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("method_non_tunai_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isNonTunai) Emerald800 else Color(0xFFF1F5F9),
                            contentColor = if (isNonTunai) Color.White else Slate900
                        ),
                        border = if (!isNonTunai) BorderStroke(1.5.dp, Slate400) else null
                    ) {
                        Text(
                            text = "NON TUNAI",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = if (isNonTunai) Color.White else Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Tombol [OK] di bawahnya
                Button(
                    onClick = { onConfirmOk(selectedMetode) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_payment_ok_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "OK",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 16.sp
                        )
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Batal",
                        style = MaterialTheme.typography.labelMedium.copy(color = Slate800, fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}
