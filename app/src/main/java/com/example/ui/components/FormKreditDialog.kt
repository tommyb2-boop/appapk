package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Nasabah
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultFilterChipColors
import com.example.ui.theme.defaultInputColors
import com.example.util.FormatUtils
import java.util.Locale

@Composable
fun FormKreditDialog(
    nasabahList: List<Nasabah>,
    existingItemNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        customerId: Long,
        akadType: String,
        itemName: String,
        nominalPokok: Double,
        persenBunga: Double,
        jumlahSetoran: Double,
        durasiTenor: Int,
        tenorUnit: String,
        hariPenagihan: String?,
        tanggalPenagihan: Int?
    ) -> Unit
) {
    var selectedNasabah by remember { mutableStateOf(nasabahList.firstOrNull()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var akadType by remember { mutableStateOf("Kredit Barang") } // "Kredit Barang" / "Pinjaman Tunai"
    var itemName by remember { mutableStateOf("") }

    // Filter referensi jenis / nama transaksi barang atau pinjaman yang pernah diinput
    val filteredItemSuggestions = remember(itemName, existingItemNames) {
        val q = itemName.trim()
        if (q.isNotEmpty()) {
            existingItemNames.filter {
                it.contains(q, ignoreCase = true) && !it.equals(q, ignoreCase = true)
            }.take(5)
        } else {
            emptyList()
        }
    }

    // Nominal dalam format titik ribuan otomatis
    var nominalPokokStr by remember { mutableStateOf("1.000.000") }
    var nominalBungaStr by remember { mutableStateOf("100.000") }
    var persenBungaStr by remember { mutableStateOf("10") }

    var jumlahSetoranStr by remember { mutableStateOf("110.000") }
    var durasiTenorStr by remember { mutableStateOf("10") }
    var tenorUnit by remember { mutableStateOf("Minggu") } // "Minggu" atau "Bulan"

    // Hari dan Tanggal Penagihan
    var hariPenagihan by remember { mutableStateOf("Kamis") }
    var tanggalPenagihanStr by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Helpers untuk sinkronisasi otomatis dengan pemisah ribuan titik
    fun updateFromPercent(newPercentStr: String) {
        persenBungaStr = newPercentStr
        val pokok = FormatUtils.cleanDigits(nominalPokokStr).toDoubleOrNull() ?: 0.0
        val persen = newPercentStr.toDoubleOrNull() ?: 0.0
        val marginNominal = (pokok * (persen / 100.0)).toLong()
        nominalBungaStr = FormatUtils.formatThousandDots(marginNominal.toString())

        val total = pokok + marginNominal
        val tenor = durasiTenorStr.toIntOrNull() ?: 1
        if (tenor > 0) {
            val setoran = Math.round(total.toDouble() / tenor).toLong()
            jumlahSetoranStr = FormatUtils.formatThousandDots(setoran.toString())
        }
    }

    fun updateFromNominalBunga(newNominalFormatted: String) {
        val cleanMargin = FormatUtils.cleanDigits(newNominalFormatted)
        nominalBungaStr = FormatUtils.formatThousandDots(cleanMargin)
        val pokok = FormatUtils.cleanDigits(nominalPokokStr).toDoubleOrNull() ?: 0.0
        val margin = cleanMargin.toDoubleOrNull() ?: 0.0
        if (pokok > 0) {
            val persen = (margin / pokok) * 100.0
            persenBungaStr = String.format(Locale.US, "%.1f", persen)
        }

        val total = pokok + margin
        val tenor = durasiTenorStr.toIntOrNull() ?: 1
        if (tenor > 0) {
            val setoran = Math.round(total / tenor).toLong()
            jumlahSetoranStr = FormatUtils.formatThousandDots(setoran.toString())
        }
    }

    fun updateFromSetoran(newSetoranFormatted: String) {
        val cleanSetoran = FormatUtils.cleanDigits(newSetoranFormatted)
        jumlahSetoranStr = FormatUtils.formatThousandDots(cleanSetoran)
        val pokok = FormatUtils.cleanDigits(nominalPokokStr).toDoubleOrNull() ?: 0.0
        val margin = FormatUtils.cleanDigits(nominalBungaStr).toDoubleOrNull() ?: 0.0
        val total = pokok + margin
        val setoran = cleanSetoran.toDoubleOrNull() ?: 0.0
        if (setoran > 0) {
            val calculatedTenor = Math.ceil(total / setoran).toInt().coerceAtLeast(1)
            durasiTenorStr = calculatedTenor.toString()
        }
    }

    fun updateFromTenor(newTenorStr: String) {
        durasiTenorStr = newTenorStr
        val pokok = FormatUtils.cleanDigits(nominalPokokStr).toDoubleOrNull() ?: 0.0
        val margin = FormatUtils.cleanDigits(nominalBungaStr).toDoubleOrNull() ?: 0.0
        val total = pokok + margin
        val tenor = newTenorStr.toIntOrNull() ?: 0
        if (tenor > 0) {
            val setoran = Math.round(total / tenor).toLong()
            jumlahSetoranStr = FormatUtils.formatThousandDots(setoran.toString())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 740.dp)
                .testTag("form_kredit_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Tetap (Fixed Header)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Formulir Kredit Baru",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 16.sp,
                            lineHeight = 18.sp
                        )
                    )
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

                // Lembar Kerja Form (Scrollable Body)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Spacer(modifier = Modifier.height(2.dp))

                    // 1. Kotak "Pilih Nasabah Peminjam" wajib berupa Dropdown daftar nasabah
                    Text(
                        text = "Pilih Nasabah Peminjam:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 12.sp
                        )
                    )
                Spacer(modifier = Modifier.height(4.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, Slate400, RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("dropdown_select_nasabah"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedNasabah?.let { "${it.name} (${it.customerCode})" }
                                ?: "Pilih Nasabah...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (selectedNasabah != null) Slate900 else Slate700,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Slate900)
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .background(Color.White)
                    ) {
                        if (nasabahList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Belum ada nasabah terdaftar", color = Slate900, fontWeight = FontWeight.Medium) },
                                onClick = { dropdownExpanded = false }
                            )
                        } else {
                            nasabahList.forEach { n ->
                                DropdownMenuItem(
                                    text = { Text("${n.name} (${n.customerCode})", color = Slate900, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedNasabah = n
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Jenis Akad: Kredit Barang / Pinjaman Tunai
                Text(
                    text = "Jenis Akad:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Kredit Barang", "Pinjaman Tunai").forEach { akad ->
                        val isSelected = akadType == akad
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                akadType = akad
                                if (itemName.isBlank() || itemName == "Pinjaman Tunai" || itemName.contains("Barang")) {
                                    itemName = if (akad == "Kredit Barang") "Barang Elektronik" else "Pinjaman Modal"
                                }
                            },
                            label = {
                                Text(
                                    text = akad,
                                    color = if (isSelected) Color.White else Slate900,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = defaultFilterChipColors(),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nama Kredit Barang / Pinjaman Tunai
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nama Barang / Tujuan Pinjaman", fontSize = 12.sp) },
                    placeholder = { Text("Misal: HP Samsung A54 / Pinjaman Usaha", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_item_name")
                )

                // Referensi nama / jenis transaksi yang pernah diinput sebelumnya (autocomplete)
                if (filteredItemSuggestions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("kredit_item_suggestions_box")
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "💡 Pilih jenis / nama transaksi sebelumnya:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate700,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            filteredItemSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            itemName = suggestion
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Emerald800,
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

                Spacer(modifier = Modifier.height(8.dp))

                // 1. Pokok Pinjaman / Harga Barang (Kotak Pendek & Format Titik Otomatis)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pokok Pinjaman / Harga Barang (Rp):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    OutlinedTextField(
                        value = nominalPokokStr,
                        onValueChange = {
                            val formatted = FormatUtils.formatThousandDots(it)
                            nominalPokokStr = formatted
                            updateFromPercent(persenBungaStr)
                        },
                        placeholder = { Text("Contoh: 1.000.000", fontSize = 13.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = defaultInputColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_nominal_pokok"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Margin Bunga (Rp) dan Persentase (%) yang terhubung otomatis
                Text(
                    text = "Margin Bunga / Laba (Terhubung Otomatis):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Margin Rupiah
                    Column(modifier = Modifier.weight(1.3f)) {
                        OutlinedTextField(
                            value = nominalBungaStr,
                            onValueChange = {
                                updateFromNominalBunga(it)
                            },
                            placeholder = { Text("Bunga (Rp)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = defaultInputColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_bunga_rupiah"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }

                    // Persentase (%)
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = persenBungaStr,
                            onValueChange = {
                                val clean = it.filter { c -> c.isDigit() || c == '.' }
                                updateFromPercent(clean)
                            },
                            placeholder = { Text("Margin (%)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = defaultInputColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_persen_bunga"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Total Piutang Box preview
                val pokokVal = FormatUtils.cleanDigits(nominalPokokStr).toDoubleOrNull() ?: 0.0
                val bungaVal = FormatUtils.cleanDigits(nominalBungaStr).toDoubleOrNull() ?: 0.0
                val totalPiutangVal = pokokVal + bungaVal

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFDCFCE7),
                        contentColor = Slate900
                    ),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Piutang Nasabah:",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate900,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(totalPiutangVal),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF14532D),
                                fontSize = 15.sp
                            )
                        )
                    }
                }

                // 3. Jumlah Setoran Rupiah & Durasi Tenor
                Text(
                    text = "Rincian Setoran & Durasi Tenor:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1.4f)) {
                        OutlinedTextField(
                            value = jumlahSetoranStr,
                            onValueChange = {
                                updateFromSetoran(it)
                            },
                            placeholder = { Text("Setoran (Rp)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = defaultInputColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_jumlah_setoran"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }

                    Column(modifier = Modifier.weight(0.9f)) {
                        OutlinedTextField(
                            value = durasiTenorStr,
                            onValueChange = {
                                val clean = it.filter { c -> c.isDigit() }
                                updateFromTenor(clean)
                            },
                            placeholder = { Text("Tenor (x)", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = defaultInputColors(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_durasi_tenor"),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                // Preview Skema Angsuran (Setoran tetap dan sisa di angsuran terakhir jika ada selisih)
                val cleanSetoran = FormatUtils.cleanDigits(jumlahSetoranStr).toDoubleOrNull() ?: 0.0
                val tenorCount = durasiTenorStr.toIntOrNull() ?: 0
                if (cleanSetoran > 0 && tenorCount > 0 && totalPiutangVal > 0) {
                    val sisaTerakhir = (totalPiutangVal - (cleanSetoran * (tenorCount - 1))).coerceAtLeast(0.0)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                            if (tenorCount > 1 && Math.abs(sisaTerakhir - cleanSetoran) > 1.0) {
                                Text(
                                    text = "Skema: ${tenorCount - 1}x @ ${FormatUtils.formatRupiah(cleanSetoran)} + 1x sisa @ ${FormatUtils.formatRupiah(sisaTerakhir)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                                Text(
                                    text = "Angsuran 1 s/d ${tenorCount - 1} tetap nominal ${FormatUtils.formatRupiah(cleanSetoran)}, angsuran ke-$tenorCount sisa pelunasan.",
                                    fontSize = 10.sp,
                                    color = Slate600
                                )
                            } else {
                                Text(
                                    text = "Skema: ${tenorCount}x @ ${FormatUtils.formatRupiah(cleanSetoran)}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pilihan Periode Tenor & Hari/Tanggal Penagihan (1 baris berdampingan)
                Text(
                    text = "Periode & Jadwal Penagihan:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 11.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kotak Dropdown 1: Periode (Per Minggu / Per Bulan)
                    var periodeDropdownExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.2.dp, Slate400),
                            color = Color(0xFFF8FAFC),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable { periodeDropdownExpanded = true }
                                .testTag("select_periode_dropdown")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (tenorUnit == "Minggu") "Per Minggu" else "Per Bulan",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Slate900,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Slate900,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = periodeDropdownExpanded,
                            onDismissRequest = { periodeDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Per Minggu",
                                        fontWeight = if (tenorUnit == "Minggu") FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    tenorUnit = "Minggu"
                                    periodeDropdownExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Per Bulan",
                                        fontWeight = if (tenorUnit == "Bulan") FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    tenorUnit = "Bulan"
                                    periodeDropdownExpanded = false
                                }
                            )
                        }
                    }

                    // Kotak Dropdown 2: Hari (jika Per Minggu) atau Tanggal (jika Per Bulan)
                    if (tenorUnit == "Minggu") {
                        // Dropdown Nama Hari
                        var hariDropdownExpanded by remember { mutableStateOf(false) }
                        val listHari = listOf("Kosong", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, if (hariPenagihan.isNotBlank() && hariPenagihan != "Kosong") Emerald800 else Slate400),
                                color = if (hariPenagihan.isNotBlank() && hariPenagihan != "Kosong") Color(0xFFDCFCE7) else Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clickable { hariDropdownExpanded = true }
                                    .testTag("select_hari_penagihan")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = if (hariPenagihan.isNotBlank() && hariPenagihan != "Kosong") Emerald800 else Slate700,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (hariPenagihan.isNotBlank() && hariPenagihan != "Kosong") "Hari: $hariPenagihan" else "Hari: (Kosong)",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (hariPenagihan.isNotBlank() && hariPenagihan != "Kosong") Color(0xFF14532D) else Slate700,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Slate900,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = hariDropdownExpanded,
                                onDismissRequest = { hariDropdownExpanded = false }
                            ) {
                                listHari.forEach { h ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (h == "Kosong") "— Kosongkan —" else "Hari $h",
                                                fontSize = 12.sp,
                                                fontWeight = if (hariPenagihan == h) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            hariPenagihan = if (h == "Kosong") "" else h
                                            hariDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        // Dropdown Tanggal (1..31)
                        var tglDropdownExpanded by remember { mutableStateOf(false) }
                        val listTanggal = listOf("Kosong") + (1..31).map { "$it" }
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.2.dp, if (tanggalPenagihanStr.isNotBlank() && tanggalPenagihanStr != "Kosong") Emerald800 else Slate400),
                                color = if (tanggalPenagihanStr.isNotBlank() && tanggalPenagihanStr != "Kosong") Color(0xFFDCFCE7) else Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clickable { tglDropdownExpanded = true }
                                    .testTag("select_tanggal_penagihan")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f, fill = false)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = if (tanggalPenagihanStr.isNotBlank() && tanggalPenagihanStr != "Kosong") Emerald800 else Slate700,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (tanggalPenagihanStr.isNotBlank() && tanggalPenagihanStr != "Kosong") "Tgl: $tanggalPenagihanStr" else "Tgl: (Kosong)",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (tanggalPenagihanStr.isNotBlank() && tanggalPenagihanStr != "Kosong") Color(0xFF14532D) else Slate700,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            ),
                                            maxLines = 1
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Slate900,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = tglDropdownExpanded,
                                onDismissRequest = { tglDropdownExpanded = false }
                            ) {
                                listTanggal.forEach { t ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (t == "Kosong") "— Kosongkan —" else "Tanggal $t",
                                                fontSize = 12.sp,
                                                fontWeight = if (tanggalPenagihanStr == t) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            tanggalPenagihanStr = if (t == "Kosong") "" else t
                                            tglDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMsg != null) {
                    Text(
                        text = errorMsg!!,
                        color = Red700,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            } // Akhir Scrollable Column

            // FOOTER TETAP (Selalu Terlihat di Bawah)
            HorizontalDivider(color = Slate200, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Batal", color = Slate800, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val customer = selectedNasabah
                        if (customer == null) {
                            errorMsg = "Pilih nasabah peminjam terlebih dahulu"
                            return@Button
                        }
                        if (itemName.isBlank()) {
                            errorMsg = "Nama barang/pinjaman tidak boleh kosong"
                            return@Button
                        }
                        val pokok = FormatUtils.cleanDigits(nominalPokokStr).toDoubleOrNull() ?: 0.0
                        if (pokok <= 0) {
                            errorMsg = "Nominal pokok harus lebih dari 0"
                            return@Button
                        }
                        val persen = persenBungaStr.toDoubleOrNull() ?: 0.0
                        val setoran = FormatUtils.cleanDigits(jumlahSetoranStr).toDoubleOrNull() ?: 0.0
                        val tenor = durasiTenorStr.toIntOrNull() ?: 1

                        val hari = if (hariPenagihan.isNotBlank() && !hariPenagihan.startsWith("Kosong")) hariPenagihan else null
                        val tgl = if (tanggalPenagihanStr.isNotBlank() && !tanggalPenagihanStr.startsWith("Kosong")) tanggalPenagihanStr.toIntOrNull() else null

                        onSave(
                            customer.id,
                            akadType,
                            itemName,
                            pokok,
                            persen,
                            setoran,
                            tenor,
                            tenorUnit,
                            hari,
                            tgl
                        )
                    },
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("submit_kredit_btn"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Emerald800,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Simpan Kredit",
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
