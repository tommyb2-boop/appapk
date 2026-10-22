package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun LoginScreen(
    onLoginSubmit: (whatsapp: String, pin: String) -> Boolean,
    onDownloadCloudData: ((onComplete: (Boolean, String) -> Unit) -> Unit)? = null,
    isSyncing: Boolean = false,
    defaultAdminPhone: String = "081234567890",
    defaultAdminPin: String = "1234",
    modifier: Modifier = Modifier
) {
    var whatsappNumber by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Dialog Download Data untuk Customer
    var showDownloadDialog by remember { mutableStateOf(false) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadStatusMessage by remember { mutableStateOf<String?>(null) }
    var downloadStatusSuccess by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    fun attemptLogin(inputPhone: String = whatsappNumber, inputPin: String = pin) {
        val cleanPhone = inputPhone.trim().replace("-", "").replace(" ", "")
        val cleanPin = inputPin.trim()

        if (cleanPhone.isBlank()) {
            errorMessage = "Nomor WhatsApp wajib diisi"
            return
        }
        if (cleanPin.isBlank()) {
            errorMessage = "PIN akses wajib diisi"
            return
        }
        if (cleanPin.length < 4) {
            errorMessage = "PIN minimal 4 digit angka"
            return
        }

        isSubmitting = true
        errorMessage = null

        val success = onLoginSubmit(cleanPhone, cleanPin)
        if (!success) {
            errorMessage = "Nomor WhatsApp atau PIN salah. Silakan periksa kembali."
            isSubmitting = false
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("login_screen"),
        color = Color(0xFFF8FAFC)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Bar di Pojok Kanan Atas: Tombol Download Data
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            isDownloading = true
                            downloadStatusMessage = null
                            showDownloadDialog = true
                            if (onDownloadCloudData != null) {
                                onDownloadCloudData.invoke { success, msg ->
                                    isDownloading = false
                                    downloadStatusSuccess = success
                                    downloadStatusMessage = msg
                                }
                            } else {
                                isDownloading = false
                                downloadStatusSuccess = true
                                downloadStatusMessage = "Data lokal telah mutakhir. Silakan masuk dengan akun Anda."
                            }
                        },
                        enabled = !isDownloading && !isSyncing,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = Emerald700
                        ),
                        border = BorderStroke(1.2.dp, Emerald700),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("login_download_data_button")
                    ) {
                        if (isSyncing || isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Emerald700
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "Download Data",
                                tint = Emerald700,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isSyncing || isDownloading) "Mengunduh..." else "Download Data",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Emerald700,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Banner Status Respon Download Data (Berhasil atau Gagal)
                AnimatedVisibility(
                    visible = isDownloading || downloadStatusMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("login_download_status_banner"),
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isDownloading -> Color(0xFFEFF6FF)
                            downloadStatusSuccess -> Color(0xFFECFDF5)
                            else -> Color(0xFFFEF2F2)
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isDownloading -> Color(0xFFBFDBFE)
                                downloadStatusSuccess -> Color(0xFFA7F3D0)
                                else -> Color(0xFFFECACA)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF2563EB)
                                )
                            } else if (downloadStatusSuccess) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Red700,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    isDownloading -> "Menghubungkan ke cloud & mengunduh data terbaru..."
                                    else -> downloadStatusMessage ?: ""
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = when {
                                        isDownloading -> Color(0xFF1E40AF)
                                        downloadStatusSuccess -> Color(0xFF065F46)
                                        else -> Color(0xFFB91C1C)
                                    },
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Hero Logo & Title
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Emerald700, Emerald800)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Logo Aplikasi",
                        modifier = Modifier.size(42.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Manajemen Hutang Piutang",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Emerald700,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Akses Masuk Aplikasi",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600,
                            fontSize = 13.sp
                        )
                    )
                }

                Text(
                    text = "Gunakan Nomor WhatsApp dan PIN terdaftar untuk masuk",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Login Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Error Alert Banner
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            errorMessage?.let { errorText ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Error",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = errorText,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFB91C1C),
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // WhatsApp Number Field
                        Text(
                            text = "Nomor WhatsApp",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = whatsappNumber,
                            onValueChange = {
                                whatsappNumber = it
                                errorMessage = null
                            },
                            placeholder = {
                                Text("081234567890", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Nomor WhatsApp",
                                    tint = Emerald700,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald700,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_whatsapp_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // PIN Field
                        Text(
                            text = "PIN Akses (4-6 Digit)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 6) {
                                    pin = input
                                    errorMessage = null
                                }
                            },
                            placeholder = {
                                Text("Masukkan PIN", color = Color(0xFF94A3B8), fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "PIN",
                                    tint = Emerald700,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPinVisible = !isPinVisible },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPinVisible) "Sembunyikan PIN" else "Lihat PIN",
                                        tint = Slate600,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    attemptLogin()
                                }
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Emerald700,
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_pin_input")
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                attemptLogin()
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald800,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Memeriksa...",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Masuk ke Aplikasi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Information Footer
                Text(
                    text = "Aplikasi aman & data tersinkronisasi ke Firebase Firestore.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Dialog Download & Update Data Piutang
            if (showDownloadDialog) {
                Dialog(onDismissRequest = { if (!isDownloading) showDownloadDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        contentColor = Slate900,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_download_dialog")
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isDownloading -> Color(0xFFF1F5F9)
                                            downloadStatusSuccess -> Color(0xFFECFDF5)
                                            else -> Color(0xFFFEF2F2)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp,
                                        color = Emerald700
                                    )
                                } else if (downloadStatusSuccess) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Berhasil",
                                        tint = Emerald700,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = "Download",
                                        tint = if (downloadStatusMessage != null) Red700 else Emerald700,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = when {
                                    isDownloading -> "Menyinkronkan Data..."
                                    downloadStatusSuccess -> "Update Data Berhasil!"
                                    downloadStatusMessage != null -> "Update Data Gagal"
                                    else -> "Update Data Akun"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        downloadStatusSuccess -> Emerald800
                                        downloadStatusMessage != null && !downloadStatusSuccess -> Red700
                                        else -> Slate900
                                    },
                                    fontSize = 17.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isDownloading) {
                                Text(
                                    text = "Menghubungkan ke server cloud untuk menyinkronkan data akun dan tagihan terbaru. Mohon tunggu sebentar...",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Slate700,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 16.sp,
                                        fontSize = 12.sp
                                    )
                                )
                            } else if (downloadStatusMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (downloadStatusSuccess) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, if (downloadStatusSuccess) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = downloadStatusMessage!!,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (downloadStatusSuccess) Color(0xFF065F46) else Red700,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        if (downloadStatusSuccess) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Data akun telah mutakhir. Silakan masukkan No. WhatsApp & PIN Anda untuk masuk dan melihat rincian piutang.",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF047857),
                                                    fontSize = 11.sp
                                                ),
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            if (downloadStatusSuccess) {
                                Button(
                                    onClick = { showDownloadDialog = false },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("btn_close_success_download"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Emerald700,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("OK, Masuk Sekarang", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            } else if (!isDownloading) {
                                Button(
                                    onClick = {
                                        isDownloading = true
                                        downloadStatusMessage = null
                                        onDownloadCloudData?.invoke { success, msg ->
                                            isDownloading = false
                                            downloadStatusSuccess = success
                                            downloadStatusMessage = msg
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("confirm_download_cloud_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Emerald700,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Coba Unduh Lagi", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            if (!isDownloading) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { showDownloadDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Tutup", color = Slate700, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
