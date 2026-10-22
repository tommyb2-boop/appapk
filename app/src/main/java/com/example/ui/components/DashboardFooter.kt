package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils

@Composable
fun DashboardFooter(
    totalHutang: Double,
    totalPiutang: Double,
    totalTopup: Double,
    modifier: Modifier = Modifier
) {
    val totalUangSaya = totalPiutang + totalTopup - totalHutang

    Card(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .testTag("dashboard_footer"),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header ringkasan
            Text(
                text = "Rincian Kalkulasi Keuangan",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Slate900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Grid 3 kolom: Total Hutang, Total Piutang, Total Topup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total Piutang
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Total Piutang",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    )
                    Text(
                        text = FormatUtils.formatRupiah(totalPiutang),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Emerald800,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            lineHeight = 15.sp
                        )
                    )
                }

                // Total Hutang
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Hutang",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    )
                    Text(
                        text = FormatUtils.formatRupiah(totalHutang),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Red700,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            lineHeight = 15.sp
                        )
                    )
                }

                // Total Topup
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Total Topup",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            lineHeight = 13.sp
                        )
                    )
                    Text(
                        text = FormatUtils.formatRupiah(totalTopup),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Amber700,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            lineHeight = 15.sp
                        )
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Slate200,
                thickness = 1.dp
            )

            // Baris Total Uang Saya (Hasil Akhir)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (totalUangSaya >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Uang Saya",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900,
                            fontSize = 13.sp,
                            lineHeight = 15.sp
                        )
                    )
                    Text(
                        text = "(Piutang + Topup - Hutang)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate800,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp
                        )
                    )
                }

                Text(
                    text = FormatUtils.formatRupiah(totalUangSaya),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = if (totalUangSaya >= 0) Color(0xFF14532D) else Color(0xFF991B1B),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        lineHeight = 19.sp
                    ),
                    modifier = Modifier.testTag("total_uang_saya_text")
                )
            }
        }
    }
}
