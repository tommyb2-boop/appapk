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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
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
fun EditNasabahDialog(
    nasabah: Nasabah,
    kredits: List<KreditPiutang>,
    onDismiss: () -> Unit,
    onSave: (
        nasabahId: Long,
        newName: String,
        newPhone: String,
        newPin: String,
        kreditIdToRestructure: Long?,
        tambahanBunga: Double,
        newTenorUnit: String,
        newHariPenagihan: String?,
        newTanggalPenagihan: Int?,
        newJumlahSetoran: Double,
        newDurasiTenor: Int
    ) -> Unit
) {
    // 1. Data Akun Nasabah
    var name by remember { mutableStateOf(nasabah.name) }
    var phone by remember { mutableStateOf(nasabah.whatsappNumber) }
    var pin by remember { mutableStateOf(nasabah.pinLogin) }

    // Filter piutang yang masih berjalan (belum lunas)
    val activeKredits = remember(kredits) {
        kredits.filter { it.status != "LUNAS" }
    }

    // Toggle apakah ingin merestrukturisasi tenor piutang
    var enableRestructure by remember { mutableStateOf(activeKredits.isNotEmpty()) }
    var selectedKreditIndex by remember { mutableIntStateOf(0) }
    val selectedKredit = activeKredits.getOrNull(selectedKreditIndex)

    // Perhitungan sisa piutang lama
    val sisaPiutangLama = remember(selectedKredit) {
        if (selectedKredit != null) {
            (selectedKredit.totalPiutang - selectedKredit.totalTerbayar).coerceAtLeast(0.0)
        } else 0.0
    }

    // 2. Tambahan Bunga (Nominal) - Diletakkan di atas tenor
    var tambahanBungaStr by remember { mutableStateOf("0") }
    val cleanTambahanBunga = FormatUtils.cleanDigits(tambahanBungaStr).toDoubleOrNull() ?: 0.0
    val totalSisaBaru = (sisaPiutangLama + cleanTambahanBunga).coerceAtLeast(0.0)

    // 3. Jenis Tenor (Minggu / Bulan)
    var tenorUnit by remember(selectedKredit) {
        mutableStateOf(selectedKredit?.tenorUnit ?: "Bulan")
    }
    var hariPenagihan by remember(selectedKredit) {
        mutableStateOf(selectedKredit?.hariPenagihan ?: "Kamis")
    }
    var tanggalPenagihanStr by remember(selectedKredit) {
        mutableStateOf(selectedKredit?.tanggalPenagihan?.toString() ?: "15")
    }

    // 4. Jumlah Setoran & Durasi Tenor (Sinkronisasi Dua Arah)
    var jumlahSetoranStr by remember(selectedKredit, totalSisaBaru) {
        val initialSetoran = if (selectedKredit != null && selectedKredit.jumlahSetoran > 0) {
            selectedKredit.jumlahSetoran.toLong()
        } else {
            (totalSisaBaru / 10).toLong().coerceAtLeast(10000L)
        }
        mutableStateOf(FormatUtils.formatThousandDots(initialSetoran.toString()))
    }

    var durasiTenorStr by remember(selectedKredit, totalSisaBaru, jumlahSetoranStr) {
        val setoran = FormatUtils.cleanDigits(jumlahSetoranStr).toDoubleOrNull() ?: 0.0
        val tenor = if (setoran > 0) Math.ceil(totalSisaBaru / setoran).toInt().coerceAtLeast(1) else 1
        mutableStateOf(tenor.toString())
    }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var dropdownKreditExpanded by remember { mutableStateOf(false) }
    var dropdownHariExpanded by remember { mutableStateOf(false) }

    val hariOptions = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

    // Fungsi update kalkulasi dua arah
    fun updateFromSetoran(newSetoranInput: String) {
        val clean = FormatUtils.cleanDigits(newSetoranInput)
        jumlahSetoranStr = FormatUtils.formatThousandDots(clean)
        val setoran = clean.toDoubleOrNull() ?: 0.0
        if (setoran > 0 && totalSisaBaru > 0) {
            val calcTenor = Math.ceil(totalSisaBaru / setoran).toInt().coerceAtLeast(1)
            durasiTenorStr = calcTenor.toString()
        }
    }

    fun updateFromTenor(newTenorInput: String) {
        durasiTenorStr = newTenorInput
        val tenor = newTenorInput.toIntOrNull() ?: 0
        if (tenor > 0 && totalSisaBaru > 0) {
            val calcSetoran = Math.round(totalSisaBaru / tenor)
            jumlahSetoranStr = FormatUtils.formatThousandDots(calcSetoran.toString())
        }
    }

    fun updateTambahanBunga(newBungaInput: String) {
        val clean = FormatUtils.cleanDigits(newBungaInput)
        tambahanBungaStr = FormatUtils.formatThousandDots(clean)
        val bungaVal = clean.toDoubleOrNull() ?: 0.0
        val newTotal = (sisaPiutangLama + bungaVal).coerceAtLeast(0.0)
        val tenor = durasiTenorStr.toIntOrNull() ?: 1
        if (tenor > 0) {
            val calcSetoran = Math.round(newTotal / tenor)
            jumlahSetoranStr = FormatUtils.formatThousandDots(calcSetoran.toString())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 760.dp)
                .testTag("edit_nasabah_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Tetap
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFDCFCE7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Emerald800,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Edit Nasabah & Tagihan",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 16.sp
                                )
                            )
                            Text(
                                text = "${nasabah.name} (${nasabah.customerCode})",
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

                // Scrollable Body Form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // SECTION 1: DATA AKUN NASABAH
                    Text(
                        text = "1. Data Akun & Login Nasabah",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald800
                    )

                    // Nama Nasabah
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap Nasabah") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp), tint = Slate700)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_nasabah_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = defaultInputColors()
                    )

                    // Nomor WhatsApp
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Nomor WhatsApp (Akun Login)") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp), tint = Slate700)
                        },
                        placeholder = { Text("08xxxxxxxxxx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_nasabah_phone_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = defaultInputColors()
                    )

                    // PIN Login (6 Digit)
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) pin = it },
                        label = { Text("PIN Login (6 Digit Angka)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp), tint = Amber700)
                        },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_nasabah_pin_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = defaultInputColors()
                    )

                    HorizontalDivider(color = Slate200, thickness = 1.dp)

                    // SECTION 2: RESTRUKTURISASI / UBAH JENIS TENOR & TAGIHAN
                    Text(
                        text = "2. Ubah Jenis Tenor & Jadwal Tagihan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald800
                    )

                    if (activeKredits.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Nasabah ini tidak memiliki piutang aktif yang berjalan. Anda tetap dapat menyimpan pembaruan Nomor WhatsApp dan PIN di atas.",
                                fontSize = 11.5.sp,
                                color = Slate700,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        // Checkbox untuk mengaktifkan perubahan tenor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { enableRestructure = !enableRestructure },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = enableRestructure,
                                onCheckedChange = { enableRestructure = it },
                                colors = CheckboxDefaults.colors(checkedColor = Emerald800)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Aktifkan perubahan tenor / restrukturisasi piutang",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                        }

                        if (enableRestructure && selectedKredit != null) {
                            // Pilihan jika nasabah memiliki lebih dari 1 piutang aktif
                            if (activeKredits.size > 1) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = "${selectedKredit.itemName} (Sisa: ${FormatUtils.formatRupiah(sisaPiutangLama)})",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Pilih Kontrak Piutang") },
                                        trailingIcon = {
                                            IconButton(onClick = { dropdownKreditExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = defaultInputColors()
                                    )
                                    DropdownMenu(
                                        expanded = dropdownKreditExpanded,
                                        onDismissRequest = { dropdownKreditExpanded = false }
                                    ) {
                                        activeKredits.forEachIndexed { index, k ->
                                            DropdownMenuItem(
                                                text = { Text("${k.itemName} - Sisa: ${FormatUtils.formatRupiah(k.totalPiutang - k.totalTerbayar)}") },
                                                onClick = {
                                                    selectedKreditIndex = index
                                                    dropdownKreditExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Rincian Sisa Piutang Berjalan
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "📦 Barang/Akad: ${selectedKredit.itemName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Total Piutang Awal:", fontSize = 11.sp, color = Slate700)
                                        Text(FormatUtils.formatRupiah(selectedKredit.totalPiutang), fontSize = 11.sp, color = Slate900)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Telah Terbayar:", fontSize = 11.sp, color = Emerald800)
                                        Text(FormatUtils.formatRupiah(selectedKredit.totalTerbayar), fontSize = 11.sp, color = Emerald800, fontWeight = FontWeight.SemiBold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sisa Piutang Saat Ini:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red700)
                                        Text(FormatUtils.formatRupiah(sisaPiutangLama), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Red700)
                                    }
                                }
                            }

                            // KOTAK TAMBAHAN BUNGA (NOMINAL) - Di letakkan di atas tenor sesuai permintaan user!
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Tambahan Bunga (Nominal Rupiah):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber700
                                )
                                OutlinedTextField(
                                    value = tambahanBungaStr,
                                    onValueChange = { updateTambahanBunga(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_tambahan_bunga"),
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = Amber700) },
                                    placeholder = { Text("0") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = defaultInputColors()
                                )
                                Text(
                                    text = "💡 Tambahkan nominal bunga jika nasabah meminta tenor diperpanjang lebih lama.",
                                    fontSize = 10.5.sp,
                                    color = Slate700
                                )
                            }

                            // Ringkasan Total Tagihan Akhir
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
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
                                        text = "Total Sisa Tagihan Baru:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF92400E)
                                    )
                                    Text(
                                        text = FormatUtils.formatRupiah(totalSisaBaru),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }

                            // PILIHAN TENOR MINGGUAN / BULANAN (Sama persis seperti Form Kredit)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Pilih Jenis Waktu Tenor:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Pilihan Mingguan
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { tenorUnit = "Minggu" }
                                            .testTag("radio_tenor_minggu"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (tenorUnit == "Minggu") Emerald800 else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (tenorUnit == "Minggu") Emerald800 else Color(0xFFCBD5E1))
                                    ) {
                                        Text(
                                            text = "Mingguan",
                                            color = if (tenorUnit == "Minggu") Color.White else Slate900,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .padding(vertical = 10.dp)
                                                .align(Alignment.CenterVertically),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }

                                    // Pilihan Bulanan
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { tenorUnit = "Bulan" }
                                            .testTag("radio_tenor_bulan"),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (tenorUnit == "Bulan") Emerald800 else Color(0xFFF8FAFC),
                                        border = BorderStroke(1.dp, if (tenorUnit == "Bulan") Emerald800 else Color(0xFFCBD5E1))
                                    ) {
                                        Text(
                                            text = "Bulanan",
                                            color = if (tenorUnit == "Bulan") Color.White else Slate900,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier
                                                .padding(vertical = 10.dp)
                                                .align(Alignment.CenterVertically),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            // Detail Hari Penagihan (Jika Mingguan) atau Tanggal Penagihan (Jika Bulanan)
                            if (tenorUnit == "Minggu") {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = "Hari Penagihan: $hariPenagihan",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Jadwal Hari Penagihan") },
                                        trailingIcon = {
                                            IconButton(onClick = { dropdownHariExpanded = true }) {
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = defaultInputColors()
                                    )
                                    DropdownMenu(
                                        expanded = dropdownHariExpanded,
                                        onDismissRequest = { dropdownHariExpanded = false }
                                    ) {
                                        hariOptions.forEach { h ->
                                            DropdownMenuItem(
                                                text = { Text(h) },
                                                onClick = {
                                                    hariPenagihan = h
                                                    dropdownHariExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = tanggalPenagihanStr,
                                    onValueChange = { input ->
                                        val digits = input.filter { it.isDigit() }
                                        val num = digits.toIntOrNull()
                                        if (digits.isEmpty() || (num != null && num in 1..31)) {
                                            tanggalPenagihanStr = digits
                                        }
                                    },
                                    label = { Text("Tanggal Penagihan Setiap Bulan (1 - 31)") },
                                    placeholder = { Text("Contoh: 15") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = defaultInputColors()
                                )
                            }

                            // KOTAK ISI MANUAL JUMLAH SETORAN & DURASI TENOR
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Nominal Setoran
                                OutlinedTextField(
                                    value = jumlahSetoranStr,
                                    onValueChange = { updateFromSetoran(it) },
                                    label = { Text("Nominal Setoran") },
                                    prefix = { Text("Rp ", fontWeight = FontWeight.Bold, color = Emerald800, fontSize = 13.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("edit_nasabah_setoran_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = defaultInputColors()
                                )

                                // Durasi Tenor (Kali)
                                OutlinedTextField(
                                    value = durasiTenorStr,
                                    onValueChange = { updateFromTenor(it) },
                                    label = { Text("Durasi Tenor") },
                                    suffix = { Text("x", fontWeight = FontWeight.Bold, color = Slate700, fontSize = 13.sp) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("edit_nasabah_tenor_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = defaultInputColors()
                                )
                            }

                            // Preview Simulasi Angsuran Baru
                            val calcSetoran = FormatUtils.cleanDigits(jumlahSetoranStr).toDoubleOrNull() ?: 0.0
                            val calcTenor = durasiTenorStr.toIntOrNull() ?: 1
                            if (calcSetoran > 0 && calcTenor > 0) {
                                val sisaAkhir = totalSisaBaru - (calcSetoran * (calcTenor - 1))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFECFDF5),
                                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "📋 Simulasi Jadwal Angsuran Baru:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald800
                                        )
                                        if (calcTenor > 1 && sisaAkhir != calcSetoran && sisaAkhir > 0) {
                                            Text(
                                                text = "• ${calcTenor - 1}x setoran @ ${FormatUtils.formatRupiah(calcSetoran)}\n• 1x setoran terakhir @ ${FormatUtils.formatRupiah(sisaAkhir)}",
                                                fontSize = 11.sp,
                                                color = Slate800,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        } else {
                                            Text(
                                                text = "• $calcTenor kali setoran @ ${FormatUtils.formatRupiah(calcSetoran)}",
                                                fontSize = 11.sp,
                                                color = Slate800,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
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
                }

                HorizontalDivider(color = Slate200, thickness = 1.dp)

                // Tombol Aksi Bawah (Fixed Footer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_cancel_edit_nasabah"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate800)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMsg = "Nama nasabah tidak boleh kosong."
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMsg = "Nomor WhatsApp tidak boleh kosong."
                                return@Button
                            }
                            if (pin.length < 4) {
                                errorMsg = "PIN login minimal 4 digit angka."
                                return@Button
                            }

                            if (enableRestructure && selectedKredit != null) {
                                val setoran = FormatUtils.cleanDigits(jumlahSetoranStr).toDoubleOrNull() ?: 0.0
                                val tenor = durasiTenorStr.toIntOrNull() ?: 0
                                if (setoran <= 0 && tenor <= 0) {
                                    errorMsg = "Masukkan jumlah setoran atau tenor yang valid."
                                    return@Button
                                }
                                val tglPenagihan = if (tenorUnit == "Bulan") tanggalPenagihanStr.toIntOrNull() else null

                                onSave(
                                    nasabah.id,
                                    name.trim(),
                                    phone.trim(),
                                    pin.trim(),
                                    selectedKredit.id,
                                    cleanTambahanBunga,
                                    tenorUnit,
                                    if (tenorUnit == "Minggu") hariPenagihan else null,
                                    tglPenagihan,
                                    setoran,
                                    tenor
                                )
                            } else {
                                // Simpan update akun saja
                                onSave(
                                    nasabah.id,
                                    name.trim(),
                                    phone.trim(),
                                    pin.trim(),
                                    null,
                                    0.0,
                                    "Bulan",
                                    null,
                                    null,
                                    0.0,
                                    1
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_save_edit_nasabah"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald800,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simpan Perubahan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
