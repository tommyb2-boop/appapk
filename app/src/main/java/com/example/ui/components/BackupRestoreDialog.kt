package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import com.example.data.remote.DriveAuthStatus
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.ExcelExporter
import com.example.util.FormatUtils

@Composable
fun BackupRestoreDialog(
    driveAuthStatus: DriveAuthStatus,
    isDriveLoading: Boolean,
    nasabahList: List<Nasabah>,
    kreditList: List<KreditPiutang>,
    installmentList: List<PiutangInstallment>,
    hutangList: List<Hutang>,
    topupList: List<TopupTransaction>,
    hutangPaymentList: List<HutangPayment> = emptyList(),
    onDismiss: () -> Unit,
    onBackupToDrive: (email: String) -> Unit,
    onRestoreFromDrive: (email: String) -> Unit,
    onSaveJsonFile: () -> Unit = {},
    onOpenJsonFile: () -> Unit = {},
    onConnectDrive: (email: String) -> Unit = {},
    onDisconnectDrive: () -> Unit = {}
) {
    val context = LocalContext.current
    var userGoogleEmailInput by remember {
        mutableStateOf(driveAuthStatus.accountEmail ?: "")
    }
    var emailError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_restore_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Backup & Restore Database",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 16.sp,
                            lineHeight = 18.sp
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 1. CADANGAN CLOUD ONLINE (SINKRONISASI ANTAR-PERANGKAT VIA EMAIL)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF0FDF4),
                        contentColor = Slate900
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Emerald800,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Cadangan Cloud (Antar-HP)",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald800,
                                        fontSize = 13.sp,
                                        lineHeight = 14.sp
                                    )
                                )
                            }
                            if (driveAuthStatus.isConnected && !driveAuthStatus.accountEmail.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFDCFCE7), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        color = Color(0xFF166534),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Cadangkan database ke server Cloud menggunakan email Anda. Di HP ke-2, cukup ketik email yang sama lalu tekan 'Pulihkan dari Cloud' untuk menyalin seluruh data secara otomatis.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        if (driveAuthStatus.lastBackupTime > 0L) {
                            Text(
                                text = "Terakhir Cadang: ${FormatUtils.formatDateTime(driveAuthStatus.lastBackupTime)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Emerald800,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = userGoogleEmailInput,
                            onValueChange = {
                                userGoogleEmailInput = it
                                emailError = null
                            },
                            placeholder = { Text("nama.anda@gmail.com", fontSize = 11.sp, color = Color(0xFF94A3B8)) },
                            label = { Text("Email Akun Cadangan", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = Emerald800)
                            },
                            isError = emailError != null,
                            supportingText = emailError?.let { { Text(it, color = Red700, fontSize = 10.sp) } },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("user_google_email_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Tombol Cadangkan ke Cloud
                            Button(
                                onClick = {
                                    val clean = userGoogleEmailInput.trim()
                                    if (clean.isBlank()) {
                                        emailError = "Email akun cadangan wajib diisi"
                                    } else if (!clean.contains("@") || !clean.contains(".")) {
                                        emailError = "Format email tidak valid (contoh: nama@gmail.com)"
                                    } else {
                                        emailError = null
                                        onConnectDrive(clean)
                                        onBackupToDrive(clean)
                                    }
                                },
                                enabled = !isDriveLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("backup_to_drive_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald800,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isDriveLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Cadang Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Tombol Pulihkan dari Cloud
                            OutlinedButton(
                                onClick = {
                                    val clean = userGoogleEmailInput.trim()
                                    if (clean.isBlank()) {
                                        emailError = "Email akun cadangan wajib diisi"
                                    } else if (!clean.contains("@") || !clean.contains(".")) {
                                        emailError = "Format email tidak valid (contoh: nama@gmail.com)"
                                    } else {
                                        emailError = null
                                        onConnectDrive(clean)
                                        onRestoreFromDrive(clean)
                                    }
                                },
                                enabled = !isDriveLoading,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_from_drive_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(14.dp), tint = Emerald800)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Pulihkan Cloud", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald800)
                            }
                        }

                        if (driveAuthStatus.isConnected) {
                            TextButton(
                                onClick = onDisconnectDrive,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Ganti / Hapus Akun", color = Red700, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. SIMPAN / BUKA FILE CADANGAN .JSON (GOOGLE DRIVE / MEMORI HP VIA SAF)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC),
                        contentColor = Slate900
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "File JSON (Google Drive / HP)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900,
                                    fontSize = 13.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }

                        Text(
                            text = "Simpan atau pulihkan file .JSON cadangan langsung ke Google Drive atau memori HP Anda melalui File Manager resmi sistem Android.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onSaveJsonFile,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_json_file_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Slate900,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Simpan File",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenJsonFile,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_json_file_btn"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp), tint = Slate900)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buka File",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate900,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. EKSPOR LAPORAN EXCEL (.XLS)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF8FAFC),
                        contentColor = Slate900
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Laporan File Excel (.XLS)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857),
                                    fontSize = 13.sp,
                                    lineHeight = 14.sp
                                )
                            )
                        }

                        Text(
                            text = "Ekspor rekap piutang, hutang, dan topup ke file Excel rapi untuk dicetak atau dikirimkan ke email.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate800,
                                fontSize = 11.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val xls = ExcelExporter.generateExcelFile(
                                        context,
                                        nasabahList,
                                        kreditList,
                                        installmentList,
                                        hutangList,
                                        topupList,
                                        hutangPaymentList
                                    )
                                    if (xls != null) {
                                        ExcelExporter.openEmailWithExcelAttachment(context, xls)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_xls_email_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF047857),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Kirim Email",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Button(
                                onClick = {
                                    val xls = ExcelExporter.generateExcelFile(
                                        context,
                                        nasabahList,
                                        kreditList,
                                        installmentList,
                                        hutangList,
                                        topupList,
                                        hutangPaymentList
                                    )
                                    if (xls != null) {
                                        ExcelExporter.shareExcelFile(context, xls)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_xls_share_btn"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF065F46),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Unduh / Share",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text(text = "Tutup", style = MaterialTheme.typography.labelMedium.copy(color = Slate800, fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
