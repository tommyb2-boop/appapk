package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Hutang
import com.example.data.model.KreditPiutang
import com.example.data.model.TopupTransaction
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils

@Composable
fun ProfileSettingsDialog(
    currentAppName: String,
    currentAdminName: String,
    currentAdminBio: String,
    currentAdminPhone: String,
    currentAdminPin: String,
    kreditList: List<KreditPiutang>,
    hutangList: List<Hutang>,
    topupList: List<TopupTransaction>,
    onDismiss: () -> Unit,
    onSaveAppName: (String) -> Unit,
    onSaveBiodata: (name: String, bio: String) -> Unit,
    onSaveCredentials: (phone: String, pin: String) -> Unit,
    onResetDatabase: () -> Unit = {},
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Profil, 1 = Login Admin, 2 = Kalkulasi, 3 = Reset Data

    var appNameInput by remember { mutableStateOf(currentAppName) }
    var adminNameInput by remember { mutableStateOf(currentAdminName) }
    var adminBioInput by remember { mutableStateOf(currentAdminBio) }

    var adminPhoneInput by remember { mutableStateOf(currentAdminPhone) }
    var adminPinInput by remember { mutableStateOf(currentAdminPin) }
    var isPinVisible by remember { mutableStateOf(false) }

    // Dialog state untuk konfirmasi Reset Database dengan kode acak
    var showResetConfirmationDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("profile_settings_dialog"),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Dialog
                Surface(
                    color = Emerald800,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Pengaturan & Profil Admin",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                )
                                Text(
                                    text = currentAdminName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFD1FAE5),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Tab Row Navigation (4 Tab: Profil, Login Admin, Kalkulasi, Reset Data)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Emerald800,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = if (selectedTab == 3) Red700 else Emerald800,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Profil",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(15.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Login",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Kalkulasi",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(15.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        text = {
                            Text(
                                "Reset",
                                color = if (selectedTab == 3) Red700 else Slate700,
                                fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        },
                        icon = { Icon(Icons.Default.DeleteForever, contentDescription = null, tint = if (selectedTab == 3) Red700 else Slate700, modifier = Modifier.size(15.dp)) }
                    )
                }

                // Content per Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> TabProfilUsaha(
                            appName = appNameInput,
                            onAppNameChange = { appNameInput = it },
                            adminName = adminNameInput,
                            onAdminNameChange = { adminNameInput = it },
                            adminBio = adminBioInput,
                            onAdminBioChange = { adminBioInput = it },
                            onSave = {
                                onSaveAppName(appNameInput)
                                onSaveBiodata(adminNameInput, adminBioInput)
                            }
                        )
                        1 -> TabLoginAdmin(
                            adminPhone = adminPhoneInput,
                            onPhoneChange = { adminPhoneInput = it },
                            adminPin = adminPinInput,
                            onPinChange = { adminPinInput = it },
                            isPinVisible = isPinVisible,
                            onTogglePinVisibility = { isPinVisible = !isPinVisible },
                            onSave = { onSaveCredentials(adminPhoneInput, adminPinInput) }
                        )
                        2 -> TabKalkulasiKeuangan(
                            kreditList = kreditList,
                            hutangList = hutangList,
                            topupList = topupList
                        )
                        3 -> TabResetDatabase(
                            onStartReset = { showResetConfirmationDialog = true }
                        )
                    }
                }

                // Footer Actions
                Surface(
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onLogout,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Red700),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.testTag("dialog_btn_logout")
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp), tint = Red700)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = Color.White)
                        ) {
                            Text("Tutup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Konfirmasi Factory Reset dengan Kode Acak Manual dari Sistem
    if (showResetConfirmationDialog) {
        FactoryResetConfirmationDialog(
            onDismiss = { showResetConfirmationDialog = false },
            onConfirmReset = {
                showResetConfirmationDialog = false
                onResetDatabase()
            }
        )
    }
}

/**
 * Dialog Konfirmasi Factory Reset dengan kode verifikasi acak yang dibuat oleh sistem
 */
@Composable
private fun FactoryResetConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmReset: () -> Unit
) {
    // Generate kode unik acak sistem 6 digit angka
    val generatedCode = remember { (100000..999999).random().toString() }
    var inputCode by remember { mutableStateOf("") }
    val isCodeMatch = inputCode.trim() == generatedCode

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Peringatan Hapus Data",
                    tint = Red700,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Konfirmasi Hapus Total (Reset Database)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Red700,
                    textAlign = TextAlign.Center
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PERINGATAN: Tindakan ini akan menghapus SELURUH data pada aplikasi secara permanen! Data nasabah, catatan kredit piutang, jadwal angsuran, hutang pribadi, dan transaksi layanan topup akan hilang dan TIDAK DAPAT dikembalikan.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate700,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                )

                // Kotak Kode Acak Buatan Sistem
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.5.dp, Color(0xFFF87171)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "KODE KONFIRMASI SISTEM:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Red700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = generatedCode,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color(0xFF991B1B),
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 4.sp
                            )
                        )
                    }
                }

                Text(
                    text = "Ketik kode angka 6-digit di atas secara persis untuk membuka tombol hapus:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate800,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp
                    )
                )

                OutlinedTextField(
                    value = inputCode,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            inputCode = input
                        }
                    },
                    placeholder = { Text("Ketik $generatedCode di sini") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isCodeMatch) Emerald700 else Red700,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_reset_confirmation_code")
                )

                if (inputCode.isNotBlank() && !isCodeMatch) {
                    Text(
                        text = "Kode belum sesuai, silakan ketik $generatedCode",
                        color = Red700,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmReset,
                enabled = isCodeMatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFCBD5E1),
                    disabledContentColor = Color(0xFF64748B)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_confirm_factory_reset")
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Hapus Total Data", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_factory_reset")
            ) {
                Text("Batal", color = Slate800, fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Tab 4: Reset Database / Factory Reset
 */
@Composable
private fun TabResetDatabase(
    onStartReset: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
            border = BorderStroke(1.5.dp, Color(0xFFFDA4AF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Red700,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Zona Bahaya: Reset Total Database",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Red700,
                            fontSize = 14.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Fitur ini akan menghapus semua data yang tersimpan di dalam aplikasi (Factory Reset):",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontSize = 12.sp, lineHeight = 16.sp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Text("• Seluruh Data Nasabah & Kontak", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.Medium)
                    Text("• Seluruh Kontrak Kredit & Piutang", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.Medium)
                    Text("• Seluruh Riwayat Angsuran & Kwitansi", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.Medium)
                    Text("• Seluruh Catatan Hutang Pribadi", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.Medium)
                    Text("• Seluruh Transaksi Layanan Topup", fontSize = 11.sp, color = Slate700, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFFECDD3)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🔒 Pengaman Keamanan: Sistem akan meminta Anda memasukkan kode verifikasi acak 6 digit sebelum penghapusan diproses.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Red700,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onStartReset,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red700,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_open_factory_reset")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Database / Factory Reset",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TabProfilUsaha(
    appName: String,
    onAppNameChange: (String) -> Unit,
    adminName: String,
    onAdminNameChange: (String) -> Unit,
    adminBio: String,
    onAdminBioChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Nama Aplikasi
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🏷️ Nama Aplikasi",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = "Nama ini akan ditampilkan pada header dan dokumen cetak struk.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp),
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = appName,
                    onValueChange = onAppNameChange,
                    placeholder = { Text("Manajemen Hutang Piutang") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald700,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_app_name")
                )
            }
        }

        // Section Biodata Admin
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "👤 Biodata Admin",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 13.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Nama Admin / Pemilik:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate800, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = adminName,
                    onValueChange = onAdminNameChange,
                    placeholder = { Text("Contoh: Administrator / Tomi") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald700,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_admin_name")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Keterangan Usaha / Profil:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate800, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = adminBio,
                    onValueChange = onAdminBioChange,
                    placeholder = { Text("Pengelola Catatan Usaha & Layanan Keuangan") },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald700,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_admin_bio")
                )
            }
        }

        Button(
            onClick = onSave,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_profile")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Perubahan Profil", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TabLoginAdmin(
    adminPhone: String,
    onPhoneChange: (String) -> Unit,
    adminPin: String,
    onPinChange: (String) -> Unit,
    isPinVisible: Boolean,
    onTogglePinVisibility: () -> Unit,
    onSave: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "🔐 Kredensial Login Administrator",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 13.sp
                    )
                )
                Text(
                    text = "Nomor WhatsApp dan PIN ini digunakan untuk masuk ke dashboard admin.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate600, fontSize = 11.sp),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Text(
                    text = "Nomor WhatsApp Admin:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate800, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = adminPhone,
                    onValueChange = onPhoneChange,
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp)) },
                    placeholder = { Text("081234567890") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald700,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_admin_phone")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "PIN Akses Admin (4-6 Angka):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Slate800, fontSize = 11.sp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            onPinChange(input)
                        }
                    },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        IconButton(onClick = onTogglePinVisibility, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald700,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_admin_pin")
                )
            }
        }

        Button(
            onClick = onSave,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_save_credentials")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Kredensial Login", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TabKalkulasiKeuangan(
    kreditList: List<KreditPiutang>,
    hutangList: List<Hutang>,
    topupList: List<TopupTransaction>
) {
    val scrollState = rememberScrollState()

    // 1. Perhitungan Piutang
    val totalPiutang = kreditList.sumOf { it.totalPiutang }
    val totalPiutangTerbayar = kreditList.sumOf { it.totalTerbayar }
    val sisaPiutang = (totalPiutang - totalPiutangTerbayar).coerceAtLeast(0.0)

    // 2. Perhitungan Layanan Topup
    val totalNominalTopup = topupList.sumOf { it.nominal }
    val totalTopupTerbayar = topupList.filter { it.isLunas }.sumOf { it.nominal }
    val totalTopupBelumTerbayar = topupList.filter { !it.isLunas }.sumOf { it.nominal }

    // 3. Perhitungan Hutang Pribadi
    val totalHutang = hutangList.sumOf { it.nominalHutang }
    val totalHutangDibayar = hutangList.sumOf { it.hutangDibayar }
    val sisaHutang = hutangList.sumOf { it.sisaHutang }

    // RUMUS 1: "Uang Masuk" = Piutang Terbayar + Topup Terbayar (Laba tidak ditampilkan)
    val uangMasuk = totalPiutangTerbayar + totalTopupTerbayar

    // RUMUS 2: "Uang Masuk - Hutang"
    val uangMasukMinusHutang = uangMasuk - totalHutang
    val uangMasukMinusSisaHutang = uangMasuk - sisaHutang

    // RUMUS 3: Rekap Gabungan Piutang + Topup
    val grandTotalTerbayar = totalPiutangTerbayar + totalTopupTerbayar
    val grandTotalBelumTerbayar = sisaPiutang + totalTopupBelumTerbayar
    val grandTotalKeseluruhan = totalPiutang + totalNominalTopup

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // 1. KARTU UANG MASUK (Piutang Terbayar + Topup Terbayar)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            border = BorderStroke(1.5.dp, Color(0xFF6EE7B7))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1. UANG MASUK",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF065F46),
                                    fontSize = 13.sp
                                )
                            )
                        }
                        Text(
                            text = "(Piutang Terbayar + Topup Terbayar)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF047857),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Text(
                        text = FormatUtils.formatRupiah(uangMasuk),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF064E3B),
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Piutang Terbayar (Masuk):", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(totalPiutangTerbayar), fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Topup Terbayar (Masuk):", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(totalTopupTerbayar), fontWeight = FontWeight.Bold, color = Color(0xFF065F46), fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Uang Masuk:", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(uangMasuk), fontWeight = FontWeight.ExtraBold, color = Emerald800, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. KARTU UANG MASUK - HUTANG (Menggantikan Bagian 2)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uangMasukMinusHutang >= 0) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
            ),
            border = BorderStroke(
                1.5.dp,
                if (uangMasukMinusHutang >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uangMasukMinusHutang >= 0) Icons.Default.Check else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (uangMasukMinusHutang >= 0) Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. UANG MASUK - HUTANG",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (uangMasukMinusHutang >= 0) Color(0xFF14532D) else Color(0xFF991B1B),
                                    fontSize = 13.sp
                                )
                            )
                        }
                        Text(
                            text = "(Uang Masuk Dikurangi Hutang)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uangMasukMinusHutang >= 0) Color(0xFF15803D) else Color(0xFFB91C1C),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Text(
                        text = FormatUtils.formatRupiah(uangMasukMinusHutang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (uangMasukMinusHutang >= 0) Color(0xFF14532D) else Color(0xFF991B1B),
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, if (uangMasukMinusHutang >= 0) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Uang Masuk:", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(uangMasuk), fontWeight = FontWeight.Bold, color = Color(0xFF047857), fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Hutang Pribadi:", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(totalHutang), fontWeight = FontWeight.Bold, color = Red700, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hutang Sudah Dibayar:", color = Slate700, fontSize = 10.sp)
                            Text(FormatUtils.formatRupiah(totalHutangDibayar), color = Slate700, fontSize = 10.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sisa Hutang Belum Lunas:", color = Slate700, fontSize = 10.sp)
                            Text(FormatUtils.formatRupiah(sisaHutang), color = Red700, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Kas Bersih Terhadap Sisa Hutang:", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = FormatUtils.formatRupiah(uangMasukMinusSisaHutang),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (uangMasukMinusSisaHutang >= 0) Color(0xFF15803D) else Color(0xFFB91C1C),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. REKAP PIUTANG, REKAP TOPUP & JUMLAH KEDUANYA (BAGIAN BAWAH)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.5.dp, Color(0xFFCBD5E1))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Bagian Rekap
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = Slate900,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "3. REKAP PIUTANG & TOPUP",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900,
                            fontSize = 13.sp
                        )
                    )
                }

                // 1. Rekap Piutang (Terbayar dan Belum Terbayar)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Rekap Piutang Nasabah",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46),
                                fontSize = 11.sp
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Piutang Terbayar:", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(totalPiutangTerbayar), fontWeight = FontWeight.Bold, color = Emerald800, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Piutang Belum Terbayar:", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(sisaPiutang), fontWeight = FontWeight.Bold, color = Red700, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Piutang:", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(totalPiutang), fontWeight = FontWeight.ExtraBold, color = Slate900, fontSize = 11.sp)
                        }
                    }
                }

                // 2. Rekap Topup (Terbayar dan Belum Terbayar) - di bawahnya
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Rekap Piutang Topup / Pulsa",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Amber700,
                                fontSize = 11.sp
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Topup Terbayar:", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(totalTopupTerbayar), fontWeight = FontWeight.Bold, color = Emerald800, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Topup Belum Terbayar:", color = Slate800, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(FormatUtils.formatRupiah(totalTopupBelumTerbayar), fontWeight = FontWeight.Bold, color = Red700, fontSize = 11.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Topup:", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(totalNominalTopup), fontWeight = FontWeight.ExtraBold, color = Slate900, fontSize = 11.sp)
                        }
                    }
                }

                // 3. Jumlah Keduanya (Total Terbayar, Total Belum Terbayar, dan Grand Total)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.2.dp, Color(0xFF93C5FD)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = "Total Jumlah Keduanya (Piutang + Topup)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E40AF),
                                fontSize = 11.sp
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Terbayar:", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(grandTotalTerbayar), fontWeight = FontWeight.ExtraBold, color = Emerald800, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Belum Terbayar:", color = Slate900, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(FormatUtils.formatRupiah(grandTotalBelumTerbayar), fontWeight = FontWeight.ExtraBold, color = Red700, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total Keseluruhan:", color = Color(0xFF1E40AF), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                            Text(FormatUtils.formatRupiah(grandTotalKeseluruhan), fontWeight = FontWeight.Black, color = Color(0xFF1E40AF), fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}
