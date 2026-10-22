package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun rupiahFormatting_isCorrect() {
    val formatted = com.example.util.FormatUtils.formatRupiah(1500000.0)
    assertTrue(formatted.contains("1.500.000") || formatted.contains("1,500,000"))
  }

  @Test
  fun loanInterestCalculation_isCorrect() {
    val pokok = 2000000.0
    val marginPersen = 10.0
    val totalPiutang = pokok + (pokok * (marginPersen / 100.0))
    assertEquals(2200000.0, totalPiutang, 0.01)

    val tenor = 10
    val setoran = totalPiutang / tenor
    assertEquals(220000.0, setoran, 0.01)
  }

  @Test
  fun installmentRemainderCalculation_isCorrect() {
    // User scenario: Total piutang 1.005.000, setoran 100.000 -> 11 installments
    // First 10 installments = 100.000 each, 11th installment = 5.000
    val totalPiutang = 1005000.0
    val nominalSetoranInput = 100000.0
    val tenor = Math.ceil(totalPiutang / nominalSetoranInput).toInt()
    assertEquals(11, tenor)

    val installments = mutableListOf<Double>()
    for (i in 1..tenor) {
      val nominalThis = if (i < tenor) {
        nominalSetoranInput
      } else {
        val previousTotal = nominalSetoranInput * (tenor - 1)
        val remainder = (totalPiutang - previousTotal).coerceAtLeast(0.0)
        remainder
      }
      installments.add(nominalThis)
    }

    assertEquals(11, installments.size)
    for (i in 0 until 10) {
      assertEquals(100000.0, installments[i], 0.001)
    }
    assertEquals(5000.0, installments[10], 0.001)
    assertEquals(totalPiutang, installments.sum(), 0.001)
  }

  @Test
  fun topupPartialPaymentCalculation_isCorrect() {
    val topup = com.example.data.model.TopupTransaction(
      id = 1L,
      nama = "Token Listrik",
      nominal = 100000.0,
      totalDibayar = 35000.0,
      keuntungan = 2500.0,
      tanggalTransaksi = System.currentTimeMillis(),
      tanggalJatuhTempo = System.currentTimeMillis() + 86400000L,
      isLunas = false
    )

    assertEquals(65000.0, topup.sisaNominal, 0.001)
    assertFalse(topup.isLunas)

    // After full payment
    val paidTopup = topup.copy(
      totalDibayar = 100000.0,
      isLunas = true
    )
    assertEquals(0.0, paidTopup.sisaNominal, 0.001)
    assertTrue(paidTopup.isLunas)
  }
}
