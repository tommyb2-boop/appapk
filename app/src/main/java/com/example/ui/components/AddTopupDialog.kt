package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultInputColors
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddTopupDialog(
    existingNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (nama: String, nominal: Double, keuntungan: Double, tanggalTransaksi: Long, tanggalJatuhTempo: Long, isLunas: Boolean) -> Unit
) {
    val context = LocalContext.current
    val defaultDateStr = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    var namaInput by remember { mutableStateOf("") }
    var nominalInput by remember { mutableStateOf("") }
    var keuntunganInput by remember { mutableStateOf("") }
    var tanggalInput by remember { mutableStateOf(defaultDateStr) }

    // Status Pembayaran: "Nanti" (belum lunas) atau "Lunas"
    var statusDropdownExpanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf("Nanti") }

    // Tanggal Jatuh Tempo default: Besok (1 hari ke depan)
    val defaultJatuhTempo = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
    var jatuhTempoMillis by remember { mutableStateOf(defaultJatuhTempo) }

    val datePickerDialog = remember(jatuhTempoMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = jatuhTempoMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                jatuhTempoMillis = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    var errorText by remember { mutableStateOf<String?>(null) }

    // Filter referensi nama yang pernah diinput sebelumnya
    val filteredNameSuggestions = remember(namaInput, existingNames) {
        val q = namaInput.trim()
        if (q.isNotEmpty()) {
            existingNames.filter {
                it.contains(q, ignoreCase = true) && !it.equals(q, ignoreCase = true)
            }.take(5)
        } else {
            emptyList()
        }
    }

    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_topup_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = Amber700,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Tambah Transaksi Layanan Topup",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 15.sp,
                            lineHeight = 17.sp
                        ),
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                Text(
                    text = "Layanan Topup dengan input nominal & keuntungan manual",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700, fontSize = 11.sp),
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                // Input Nama Layanan / Pelanggan
                OutlinedTextField(
                    value = namaInput,
                    onValueChange = {
                        namaInput = it
                        errorText = null
                    },
                    label = { Text("Nama Layanan / Pelanggan", fontSize = 12.sp) },
                    placeholder = { Text("Misal: Mas Dedi (Topup Dana)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_topup_nama_input")
                )

                // Referensi nama yang pernah diinput sebelumnya (autocomplete)
                if (filteredNameSuggestions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("topup_name_suggestions_box")
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "💡 Pilih nama yang pernah diinput:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate700,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            filteredNameSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            namaInput = suggestion
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Amber700,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate900,
                                            fontSize = 12.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input Tanggal Transaksi
                OutlinedTextField(
                    value = tanggalInput,
                    onValueChange = {
                        tanggalInput = it
                        errorText = null
                    },
                    label = { Text("Tanggal Transaksi (DD/MM/YYYY)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: 14/10/2026", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Tanggal",
                            tint = Amber700,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_topup_tanggal_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Input Nominal Transaksi (Tulis Manual Saja, Tanpa Pilihan Chip, Format Titik Otomatis)
                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = {
                        nominalInput = FormatUtils.formatThousandDots(it)
                        errorText = null
                    },
                    label = { Text("Nominal Transaksi (Rp)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: 100.000", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_topup_nominal_input"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Kotak Manual Keuntungan (Tulis Manual Saja, Tanpa Pilihan Chip, Format Titik Otomatis)
                OutlinedTextField(
                    value = keuntunganInput,
                    onValueChange = {
                        keuntunganInput = FormatUtils.formatThousandDots(it)
                        errorText = null
                    },
                    label = { Text("Keuntungan Layanan (Rp)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: 3.000", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Keuntungan",
                            tint = Emerald700,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_topup_keuntungan_input"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Baris Status Pembayaran / Perkiraan Jatuh Tempo Bayar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Perkiraan Jatuh Tempo Bayar:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Slate900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )

                    // Kotak Dropdown di sebelah kanan dengan isi: Lunas & Nanti
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.2.dp, if (selectedStatus == "Lunas") Emerald700 else Amber700),
                            color = if (selectedStatus == "Lunas") Color(0xFFECFDF5) else Color(0xFFFFFBEB),
                            modifier = Modifier
                                .clickable { statusDropdownExpanded = true }
                                .testTag("topup_status_dropdown")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = selectedStatus,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (selectedStatus == "Lunas") Emerald700 else Amber700,
                                        fontSize = 12.sp
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Status",
                                    tint = if (selectedStatus == "Lunas") Emerald700 else Amber700,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Emerald700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Lunas", fontWeight = FontWeight.Bold, color = Emerald700, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedStatus = "Lunas"
                                    statusDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Amber700,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Nanti", fontWeight = FontWeight.Bold, color = Amber700, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedStatus = "Nanti"
                                    statusDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Jika Lunas: Tampilkan info bahwa transaksi otomatis masuk terhitung lunas
                if (selectedStatus == "Lunas") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Emerald700,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Otomatis tercatat Lunas (masuk ke Uang Masuk)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF065F46),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                } else {
                    // Jika Nanti / Belum Lunas: Di bawahnya muncul dropdown tanggal untuk dipilih sebagai acuan jatuh tempo
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topup_due_date_container")
                    ) {
                        Text(
                            text = "Pilih Tanggal Jatuh Tempo:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Dropdown Tanggal Jatuh Tempo
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }
                                .testTag("topup_due_date_dropdown")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Tanggal Jatuh Tempo",
                                        tint = Amber700,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = FormatUtils.formatDateOnly(jatuhTempoMillis),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Tanggal",
                                    tint = Slate700,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "*Akan muncul di notifikasi peringatan jatuh tempo",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate700,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(top = 3.dp, start = 2.dp)
                        )
                    }
                }

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

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(text = "Batal", color = Slate800, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (namaInput.isBlank()) {
                                errorText = "Nama transaksi layanan harus diisi"
                                return@Button
                            }
                            val nom = FormatUtils.cleanDigits(nominalInput).toDoubleOrNull() ?: 0.0
                            if (nom <= 0) {
                                errorText = "Nominal transaksi harus lebih dari 0"
                                return@Button
                            }
                            val untung = FormatUtils.cleanDigits(keuntunganInput).toDoubleOrNull() ?: 0.0

                            // Parse tanggal transaksi
                            val tglMillis = try {
                                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                sdf.parse(tanggalInput.trim())?.time ?: System.currentTimeMillis()
                            } catch (_: Exception) {
                                System.currentTimeMillis()
                            }

                            val isLunas = (selectedStatus == "Lunas")
                            val jt = if (isLunas) tglMillis else jatuhTempoMillis
                            onSave(namaInput, nom, untung, tglMillis, jt, isLunas)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_add_topup_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Amber700,
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
