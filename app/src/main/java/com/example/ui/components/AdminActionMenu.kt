package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald800
import com.example.ui.theme.Slate900

@Composable
fun AdminActionMenu(
    onNewNasabahClick: () -> Unit,
    onNewKreditClick: () -> Unit,
    onDaftarNasabahClick: () -> Unit,
    onRekapPiutangClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(start = 16.dp, bottom = 20.dp) // Posisikan di sebelah kiri bawah
            .testTag("admin_action_menu"),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Expanded Items
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 0. Tombol "Rekap Piutang"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onRekapPiutangClick()
                        },
                        containerColor = Emerald800,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("fab_rekap_piutang")
                    ) {
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Rekap Piutang", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate900,
                        contentColor = Color.White,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "Rekap Piutang",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 1. Tombol "Daftar Nasabah"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onDaftarNasabahClick()
                        },
                        containerColor = Color(0xFF0369A1), // Sky 700 / Ocean Blue
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("fab_daftar_nasabah")
                    ) {
                        Icon(imageVector = Icons.Default.Group, contentDescription = "Daftar Nasabah", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate900,
                        contentColor = Color.White,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "Daftar Nasabah",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 2. Tombol "Kredit Baru"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onNewKreditClick()
                        },
                        containerColor = Emerald800,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("fab_kredit_baru")
                    ) {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = "Kredit Baru", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate900,
                        contentColor = Color.White,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "Kredit Baru",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // 3. Tombol "Nasabah Baru"
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            isExpanded = false
                            onNewNasabahClick()
                        },
                        containerColor = Slate900,
                        contentColor = Color.White,
                        elevation = FloatingActionButtonDefaults.elevation(4.dp),
                        modifier = Modifier
                            .size(46.dp)
                            .testTag("fab_nasabah_baru")
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Nasabah Baru", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Slate900,
                        contentColor = Color.White,
                        tonalElevation = 4.dp
                    ) {
                        Text(
                            text = "Nasabah Baru",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                lineHeight = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Main FAB Toggle "+" di sebelah kiri bawah
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = if (isExpanded) Slate900 else Emerald800,
            contentColor = Color.White,
            shape = RoundedCornerShape(14.dp),
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .size(54.dp)
                .testTag("admin_main_fab")
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Menu Admin",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
