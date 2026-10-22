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
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultInputColors

@Composable
fun FormNasabahDialog(
    suggestedCode: String,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, phone: String, pin: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf(suggestedCode) }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("1234") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("form_nasabah_dialog"),
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
                    text = "Profil Nasabah Baru",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Kode Nasabah
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Kode Nasabah (ID)", fontSize = 12.sp) },
                    placeholder = { Text("cust-001", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nasabah_code_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nama Lengkap
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nama Lengkap Nasabah", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Budi Santoso", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nasabah_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Nomor WhatsApp
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        phone = it.filter { c -> c.isDigit() || c == '+' }
                        errorMessage = null
                    },
                    label = { Text("Nomor WhatsApp", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: 081234567890", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nasabah_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // PIN Login
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("PIN Login (4-6 Angka)", fontSize = 12.sp) },
                    placeholder = { Text("1234", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nasabah_pin_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
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
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(text = "Batal", color = Slate800, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                errorMessage = "Nama nasabah wajib diisi"
                                return@Button
                            }
                            if (phone.isBlank()) {
                                errorMessage = "Nomor WhatsApp wajib diisi"
                                return@Button
                            }
                            onSave(name, code, phone, pin)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_nasabah_btn"),
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
