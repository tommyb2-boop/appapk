package com.example.ui.theme

import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Primary Financial Emerald Green (High Contrast)
val Emerald950 = Color(0xFF022C22)
val Emerald900 = Color(0xFF064E3B)
val Emerald800 = Color(0xFF065F46)
val Emerald700 = Color(0xFF047857)
val Emerald600 = Color(0xFF059669)
val Emerald500 = Color(0xFF10B981)
val Emerald100 = Color(0xFFD1FAE5)
val Emerald50 = Color(0xFFECFDF5)

// Secondary Teal / Slate (Deep, sharp contrast for readability)
val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A) // Utama: Hitam arang sangat pekat (sangat jelas di latar putih/abu)
val Slate800 = Color(0xFF1E293B) // Sekunder: Slate pekat tajam
val Slate700 = Color(0xFF334155) // Label / Subjudul: Slate tegas terbaca
val Slate600 = Color(0xFF475569) // Keterangan tambahan
val Slate500 = Color(0xFF64748B) // Placeholder
val Slate400 = Color(0xFF94A3B8) // Border aktif
val Slate200 = Color(0xFFE2E8F0) // Border kartu & garis pemisah
val Slate100 = Color(0xFFF1F5F9) // Latar kartu sekunder
val Slate50 = Color(0xFFF8FAFC)  // Latar layar utama

// Semantic Accents (Jelas & Kontras Tinggi)
val Amber800 = Color(0xFF92400E)
val Amber700 = Color(0xFFB45309)
val Amber100 = Color(0xFFFEF3C7)
val Red800 = Color(0xFF991B1B)
val Red700 = Color(0xFFB91C1C)
val Red100 = Color(0xFFFEE2E2)
val Blue700 = Color(0xFF1D4ED8)
val Blue100 = Color(0xFFDBEAFE)

val SurfaceCard = Color(0xFFFFFFFF)
val SurfaceBackground = Color(0xFFF8FAFC)

@Composable
fun defaultInputColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Slate900,
    unfocusedTextColor = Slate900,
    focusedLabelColor = Emerald800,
    unfocusedLabelColor = Slate800,
    focusedPlaceholderColor = Slate600,
    unfocusedPlaceholderColor = Slate600,
    focusedBorderColor = Emerald800,
    unfocusedBorderColor = Slate400,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    cursorColor = Emerald800
)

@Composable
fun defaultFilterChipColors(
    selectedColor: Color = Emerald800
): SelectableChipColors = FilterChipDefaults.filterChipColors(
    selectedContainerColor = selectedColor,
    selectedLabelColor = Color.White,
    containerColor = Color(0xFFE2E8F0),
    labelColor = Slate900
)
