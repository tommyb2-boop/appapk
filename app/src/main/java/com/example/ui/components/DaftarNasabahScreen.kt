package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultInputColors
import com.example.util.FormatUtils
import com.example.util.PdfReceiptGenerator

/**
 * Halaman Daftar Nasabah:
 * - Header menampilkan total jumlah nasabah
 * - Piutang per nasabah detail dalam kotak / garis batas per masing-masing nasabah
 * - Masing-masing nama nasabah ada icon WhatsApp untuk kirim detail piutang ke nasabah
 * - Tombol Edit Nasabah di samping nama untuk edit WA, PIN, dan ubah jenis tenor
 * - Berisi: Nominal Piutang, Nominal Terbayar, Nominal Sisa Piutang
 * - Di bawahnya: List tagihan nasabah yang di samping kanannya ada tombol [Kwitansi]
 */
@Composable
fun DaftarNasabahScreen(
    nasabahList: List<Nasabah>,
    kreditList: List<KreditPiutang>,
    installmentList: List<PiutangInstallment>,
    onBack: () -> Unit,
    onCustomerDetailClick: (Long) -> Unit,
    onKwitansiClick: (PiutangInstallment, KreditPiutang?, Nasabah) -> Unit,
    onEditNasabahClick: (Nasabah) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    BackHandler { onBack() }

    var searchQuery by remember { mutableStateOf("") }

    val filteredNasabah = remember(nasabahList, searchQuery) {
        if (searchQuery.isBlank()) {
            nasabahList
        } else {
            val q = searchQuery.trim().lowercase()
            nasabahList.filter {
                it.name.lowercase().contains(q) ||
                it.customerCode.lowercase().contains(q) ||
                it.whatsappNumber.contains(q)
            }
        }
    }

    // Total finansial seluruh nasabah
    val grandTotalPiutang = kreditList.sumOf { it.totalPiutang }
    val grandTotalTerbayar = kreditList.sumOf { it.totalTerbayar }
    val grandTotalSisa = (grandTotalPiutang - grandTotalTerbayar).coerceAtLeast(0.0)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("daftar_nasabah_screen"),
        containerColor = Color(0xFFF8FAFC),
        contentColor = Slate900,
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Emerald800,
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 14.dp)
                ) {
                    // Baris Navigasi & Judul Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("daftar_nasabah_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali ke Dashboard",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daftar Nasabah & Piutang",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    lineHeight = 18.sp
                                )
                            )
                            // Header menampilkan total jumlah nasabah
                            Text(
                                text = "Total Jumlah Nasabah: ${nasabahList.size} Orang",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFECFDF5),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }

                        // Badge Count
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${nasabahList.size}",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kotak Ringkasan Global
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Total Piutang", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                Text(
                                    text = FormatUtils.formatRupiah(grandTotalPiutang),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Terbayar", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                Text(
                                    text = FormatUtils.formatRupiah(grandTotalTerbayar),
                                    color = Color(0xFFA7F3D0),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Sisa Piutang", color = Color(0xFFE2E8F0), fontSize = 10.sp)
                                Text(
                                    text = FormatUtils.formatRupiah(grandTotalSisa),
                                    color = Color(0xFFFECDD3),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar untuk mempermudah pencarian nama nasabah
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama nasabah / kode / WhatsApp...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate700, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    colors = defaultInputColors(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_nasabah_input")
                )
            }

            if (filteredNasabah.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Slate700
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Nasabah tidak ditemukan" else "Belum ada nasabah terdaftar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Slate900,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Coba kata kunci pencarian yang lain." else "Tambah nasabah melalui tombol (+) di menu Piutang.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate800),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("daftar_nasabah_lazy_list"),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNasabah, key = { it.id }) { nasabah ->
                        val customerKredits = kreditList.filter { it.customerId == nasabah.id }
                        val customerInsts = installmentList.filter { it.customerId == nasabah.id }

                        NasabahDetailCardBox(
                            nasabah = nasabah,
                            kredits = customerKredits,
                            installments = customerInsts,
                            onCustomerClick = { onCustomerDetailClick(nasabah.id) },
                            onEditClick = { onEditNasabahClick(nasabah) },
                            onKwitansiClick = { inst, kredit ->
                                onKwitansiClick(inst, kredit, nasabah)
                            },
                            onSendWhatsAppClick = {
                                val custTotalPiutang = customerKredits.sumOf { it.totalPiutang }
                                val custTotalTerbayar = customerKredits.sumOf { it.totalTerbayar }
                                val custSisaPiutang = (custTotalPiutang - custTotalTerbayar).coerceAtLeast(0.0)

                                val builder = StringBuilder()
                                builder.append("Halo Bapak/Ibu ${nasabah.name} (${nasabah.customerCode}),\n")
                                builder.append("Berikut rincian piutang & tagihan Anda:\n")
                                builder.append("• PIN Akses Login Aplikasi: ${nasabah.pinLogin.ifBlank { "1234" }}\n")
                                builder.append("• Nominal Piutang: ${FormatUtils.formatRupiah(custTotalPiutang)}\n")
                                builder.append("• Nominal Terbayar: ${FormatUtils.formatRupiah(custTotalTerbayar)}\n")
                                builder.append("• Nominal Sisa Piutang: ${FormatUtils.formatRupiah(custSisaPiutang)}\n\n")

                                if (customerInsts.isNotEmpty()) {
                                    builder.append("Daftar Tagihan:\n")
                                    customerInsts.forEach { inst ->
                                        val statusStr = if (inst.status == "LUNAS") "LUNAS" else "BELUM LUNAS"
                                        builder.append("- Angsuran ke-${inst.angsuranKe}: ${FormatUtils.formatRupiah(inst.nominalRupiah)} [${statusStr}] (Jatuh tempo: ${FormatUtils.formatDate(inst.tanggalJatuhTempo)})\n")
                                    }
                                    builder.append("\n")
                                }
                                builder.append("Terima kasih atas kerja samanya.")

                                PdfReceiptGenerator.sendWhatsAppText(context, nasabah.whatsappNumber, builder.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Komponen Kotak atau Garis Batas per masing-masing nasabah:
 * - Nama nasabah & Kode
 * - Icon WhatsApp untuk kirim detail ke nasabah
 * - Nominal Piutang, Nominal Terbayar, Nominal Sisa Piutang
 * - Di bawahnya: List tagihan nasabah yg di samping kanannya ada tombol [Kwitansi]
 */
@Composable
private fun NasabahDetailCardBox(
    nasabah: Nasabah,
    kredits: List<KreditPiutang>,
    installments: List<PiutangInstallment>,
    onCustomerClick: () -> Unit,
    onEditClick: () -> Unit,
    onKwitansiClick: (PiutangInstallment, KreditPiutang?) -> Unit,
    onSendWhatsAppClick: () -> Unit
) {
    val custTotalPiutang = kredits.sumOf { it.totalPiutang }
    val custTotalTerbayar = kredits.sumOf { it.totalTerbayar }
    val custSisaPiutang = (custTotalPiutang - custTotalTerbayar).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_nasabah_${nasabah.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate900
        ),
        // Kotak atau garis batas per masing-masing nasabah
        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header Nasabah: Nama, Kode, WhatsApp Icon Kirim Detail
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info Nama Nasabah & Kode (dapat diklik untuk menuju detail)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCustomerClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFDCFCE7), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Emerald800,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = nasabah.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 15.sp,
                                    lineHeight = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Badge Kode Nasabah
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = nasabah.customerCode,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate900,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = if (nasabah.whatsappNumber.isNotBlank()) "WA: ${nasabah.whatsappNumber}" else "Tidak ada nomor",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate700,
                                    fontSize = 11.sp,
                                    lineHeight = 13.sp
                                )
                            )

                            // PIN Login Nasabah yang diisi pada saat pendaftaran
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "PIN Login",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "PIN: ${nasabah.pinLogin.ifBlank { "1234" }}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF92400E),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Tombol Edit Nasabah (Edit WA, PIN & Restrukturisasi Tenor)
                    OutlinedButton(
                        onClick = onEditClick,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.2.dp, Amber700),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFFFFBEB),
                            contentColor = Amber700
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
                        modifier = Modifier.testTag("edit_nasabah_btn_${nasabah.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Nasabah",
                            tint = Amber700,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Amber700,
                                fontSize = 11.sp,
                                lineHeight = 12.sp
                            )
                        )
                    }

                    // Icon WhatsApp untuk kirim detail ke nasabah
                    Button(
                        onClick = onSendWhatsAppClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF15803D), // WhatsApp Green
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 9.dp, vertical = 5.dp),
                        modifier = Modifier.testTag("send_wa_btn_${nasabah.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Kirim WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "WA",
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

            Spacer(modifier = Modifier.height(10.dp))

            // Kotak Finansial: Nominal Piutang, Nominal Terbayar, Nominal Sisa Piutang
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8FAFC),
                    contentColor = Slate900
                ),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Nominal Piutang
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nominal Piutang",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                lineHeight = 11.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(custTotalPiutang),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }

                    // Nominal Terbayar
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nominal Terbayar",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                lineHeight = 11.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(custTotalTerbayar),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Emerald800,
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }

                    // Nominal Sisa Piutang
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Sisa Piutang",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                lineHeight = 11.sp
                            )
                        )
                        Text(
                            text = FormatUtils.formatRupiah(custSisaPiutang),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (custSisaPiutang > 0) Red700 else Emerald800,
                                fontSize = 12.sp,
                                lineHeight = 14.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Di bawahnya: List Tagihan Nasabah
            Text(
                text = "Daftar Tagihan (${installments.size}):",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (installments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada catatan tagihan untuk nasabah ini",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate700,
                            fontSize = 11.sp
                        )
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    installments.forEach { inst ->
                        val relatedKredit = kredits.find { it.id == inst.kreditId }
                        val isLunas = inst.status == "LUNAS"

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isLunas) Color(0xFFF0FDF4) else Color(0xFFF8FAFC), RoundedCornerShape(6.dp))
                                .border(1.dp, if (isLunas) Color(0xFF86EFAC) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Angsuran ke-${inst.angsuranKe}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900,
                                            fontSize = 12.sp,
                                            lineHeight = 13.sp
                                        )
                                    )
                                    if (relatedKredit != null && relatedKredit.itemName.isNotBlank()) {
                                        Text(
                                            text = " (${relatedKredit.itemName})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Slate700,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    // Status Badge ("LUNAS" / "NON")
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (isLunas) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = if (isLunas) "LUNAS" else "NON",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if (isLunas) Color(0xFF14532D) else Color(0xFF991B1B),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.padding(top = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = FormatUtils.formatRupiah(inst.nominalRupiah),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isLunas) Emerald800 else Slate900,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Text(
                                        text = "Tempo: ${FormatUtils.formatDate(inst.tanggalJatuhTempo)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Slate700,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            }

                            // Di samping kanannya ada tombol [Kwitansi]
                            OutlinedButton(
                                onClick = { onKwitansiClick(inst, relatedKredit) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("kwitansi_btn_${inst.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Emerald800
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Kwitansi",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Emerald800,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
