package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.defaultInputColors
import com.example.util.FormatUtils

@Composable
fun AddHutangDialog(
    existingSumberList: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (namaSumber: String, nominal: Double, keterangan: String?) -> Unit
) {
    var namaSumber by remember { mutableStateOf("") }
    var nominalInput by remember { mutableStateOf("") }
    var keterangan by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val filteredSumberSuggestions = remember(namaSumber, existingSumberList) {
        val q = namaSumber.trim()
        if (q.isNotEmpty()) {
            existingSumberList.filter {
                it.contains(q, ignoreCase = true) && !it.equals(q, ignoreCase = true)
            }.take(5)
        } else {
            emptyList()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_hutang_dialog"),
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            contentColor = Slate900,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Catat Hutang Pribadi Baru",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        fontSize = 16.sp,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = namaSumber,
                    onValueChange = {
                        namaSumber = it
                        errorText = null
                    },
                    label = { Text("Nama Sumber Hutang", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Bank BCA / Mas Budi", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_hutang_sumber_input")
                )

                // Referensi nama sumber hutang sebelumnya (autocomplete)
                if (filteredSumberSuggestions.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("hutang_sumber_suggestions_box")
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            Text(
                                text = "💡 Pilih sumber hutang sebelumnya:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Slate700,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            filteredSumberSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            namaSumber = suggestion
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = Red700,
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

                OutlinedTextField(
                    value = nominalInput,
                    onValueChange = {
                        nominalInput = FormatUtils.formatThousandDots(it)
                        errorText = null
                    },
                    label = { Text("Nominal Hutang (Rp)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: 1.000.000", fontSize = 12.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_hutang_nominal_input"),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (Opsional)", fontSize = 12.sp) },
                    placeholder = { Text("Contoh: Pinjaman renovasi rumah", fontSize = 12.sp) },
                    singleLine = true,
                    colors = defaultInputColors(),
                    modifier = Modifier.fillMaxWidth()
                )

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
                            if (namaSumber.isBlank()) {
                                errorText = "Nama sumber hutang harus diisi"
                                return@Button
                            }
                            val nom = FormatUtils.cleanDigits(nominalInput).toDoubleOrNull() ?: 0.0
                            if (nom <= 0) {
                                errorText = "Nominal hutang harus lebih dari 0"
                                return@Button
                            }
                            onSave(namaSumber, nom, keterangan.ifBlank { null })
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_add_hutang_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Slate900,
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
