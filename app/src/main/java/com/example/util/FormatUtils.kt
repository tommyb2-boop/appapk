package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val localeId = Locale("id", "ID")
    private val numberFormat = NumberFormat.getNumberInstance(localeId).apply {
        maximumFractionDigits = 0
    }
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", localeId)
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", localeId)
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", localeId)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", localeId)

    fun formatRupiah(amount: Double): String {
        return "Rp " + numberFormat.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateOnly(timestamp: Long): String {
        return dateOnlyFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun parseRupiahInput(input: String): Double {
        val clean = input.replace("[^0-9]".toRegex(), "")
        return clean.toDoubleOrNull() ?: 0.0
    }

    /**
     * Memformat string angka otomatis dengan titik pemisah ribuan (contoh: 1000000 -> 1.000.000)
     */
    fun formatThousandDots(input: String): String {
        val clean = input.filter { it.isDigit() }
        if (clean.isBlank()) return ""
        val value = clean.toLongOrNull() ?: return clean
        return numberFormat.format(value)
    }

    /**
     * Mengambil digit angka murni dari string berformat ribuan
     */
    fun cleanDigits(input: String): String {
        return input.filter { it.isDigit() }
    }
}
