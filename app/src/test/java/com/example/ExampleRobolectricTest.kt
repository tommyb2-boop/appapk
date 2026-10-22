package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Manajemen Hutang Piutang", appName)
  }

  @Test
  fun `launch MainActivity without crash`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    org.junit.Assert.assertNotNull(controller.get())
  }

  @get:org.junit.Rule
  val composeTestRule = androidx.compose.ui.test.junit4.createAndroidComposeRule<MainActivity>()

  @Test
  fun `test login flow and navigation to Daftar Nasabah and back`() {
    composeTestRule.waitForIdle()

    // Assert login screen elements exist
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_screen")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button")).assertExists()

    // Enter admin WhatsApp and PIN to log in (tanpa demo admin)
    val phoneInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input"))
    phoneInput.performScrollTo()
    phoneInput.performTextInput("081234567890")

    val pinInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input"))
    pinInput.performScrollTo()
    pinInput.performTextInput("1234")

    val submitBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button"))
    submitBtn.performScrollTo()
    submitBtn.performClick()
    composeTestRule.waitForIdle()

    // Assert main dashboard is shown
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("dashboard_menu_tabs")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_header_logout")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_profile_settings")).assertExists()

    // Find admin_main_fab and click
    val fabToggle = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("admin_main_fab"))
    fabToggle.assertExists()
    fabToggle.performClick()
    composeTestRule.waitForIdle()

    // Find fab_daftar_nasabah and click
    val fabDaftarNasabah = composeTestRule.onNode(
        androidx.compose.ui.test.hasTestTag("fab_daftar_nasabah"),
        useUnmergedTree = true
    )
    fabDaftarNasabah.assertExists()
    fabDaftarNasabah.performClick()
    composeTestRule.waitForIdle()

    // Assert daftar_nasabah_screen exists
    val daftarNasabahScreen = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("daftar_nasabah_screen"))
    daftarNasabahScreen.assertExists()

    // Click back button
    val backBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("daftar_nasabah_back_btn"))
    backBtn.assertExists()
    backBtn.performClick()
    composeTestRule.waitForIdle()

    // Assert main dashboard is back
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("dashboard_menu_tabs")).assertExists()

    // Test opening Profile Settings Dialog
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_profile_settings")).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("profile_settings_dialog")).assertExists()

    // Close Profile Settings Dialog
    composeTestRule.onNode(androidx.compose.ui.test.hasContentDescription("Tutup")).performClick()
    composeTestRule.waitForIdle()

    // Logout
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_header_logout")).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test customer login isolation with no back to admin and working logout`() {
    composeTestRule.waitForIdle()

    // 1. Enter customer WhatsApp and PIN (Siti Rahmawati)
    val phoneInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input"))
    phoneInput.performScrollTo()
    phoneInput.performTextInput("085712345678")

    val pinInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input"))
    pinInput.performScrollTo()
    pinInput.performTextInput("5678")

    val submitBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button"))
    submitBtn.performScrollTo()
    submitBtn.performClick()
    composeTestRule.waitForIdle()

    // 2. Assert customer detail page is displayed
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("detail_nasabah_screen")).assertExists()

    // 3. Confirm there is NO back button to admin and NO admin dashboard
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("detail_back_button")).assertDoesNotExist()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("dashboard_menu_tabs")).assertDoesNotExist()

    // 4. Confirm customer logout button exists and works
    val logoutBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("customer_logout_btn"))
    logoutBtn.assertExists()
    logoutBtn.performClick()
    composeTestRule.waitForIdle()

    // 5. Assert back at Login Screen
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_screen")).assertExists()
  }

  @Test
  fun `test notification bell icon and due bills dialog`() {
    composeTestRule.waitForIdle()

    // Login as admin
    val phoneInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input"))
    phoneInput.performScrollTo()
    phoneInput.performTextInput("081234567890")

    val pinInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input"))
    pinInput.performScrollTo()
    pinInput.performTextInput("1234")

    val submitBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button"))
    submitBtn.performScrollTo()
    submitBtn.performClick()
    composeTestRule.waitForIdle()

    // Assert dashboard header exists
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("dashboard_header")).assertExists()

    // Assert notification bell exists in header
    val notifBell = composeTestRule.onNode(
        androidx.compose.ui.test.hasTestTag("btn_notification_due_bills"),
        useUnmergedTree = true
    )
    notifBell.assertExists()
    notifBell.performClick()
    composeTestRule.waitForIdle()

    // Assert due bills dialog appears
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("due_bills_dialog")).assertExists()

    // Close due bills dialog
    composeTestRule.onNode(androidx.compose.ui.test.hasContentDescription("Tutup")).performClick()
    composeTestRule.waitForIdle()

    // Logout
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_header_logout")).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test search fields in piutang, hutang, and topup menus`() {
    composeTestRule.waitForIdle()

    // Login as admin
    val phoneInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input"))
    phoneInput.performScrollTo()
    phoneInput.performTextInput("081234567890")

    val pinInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input"))
    pinInput.performScrollTo()
    pinInput.performTextInput("1234")

    val submitBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button"))
    submitBtn.performScrollTo()
    submitBtn.performClick()
    composeTestRule.waitForIdle()

    // 1. Piutang menu is active by default -> search_piutang_field should exist
    val searchPiutang = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("search_piutang_field"))
    searchPiutang.assertExists()
    searchPiutang.performTextInput("Mesin Cuci")
    composeTestRule.waitForIdle()

    // 2. Switch to Hutang tab
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tab_hutang")).performClick()
    composeTestRule.waitForIdle()
    val searchHutang = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("search_hutang_field"))
    searchHutang.assertExists()
    searchHutang.performTextInput("Bank")
    composeTestRule.waitForIdle()

    // 3. Switch to Topup tab
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tab_topup")).performClick()
    composeTestRule.waitForIdle()
    val searchTopup = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("search_topup_field"))
    searchTopup.assertExists()
    searchTopup.performTextInput("Ahmad")
    composeTestRule.waitForIdle()

    // Logout
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_header_logout")).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test topup add dialog status dropdown and due date picker`() {
    composeTestRule.waitForIdle()

    // Login as admin
    val phoneInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input"))
    phoneInput.performScrollTo()
    phoneInput.performTextInput("081234567890")

    val pinInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input"))
    pinInput.performScrollTo()
    pinInput.performTextInput("1234")

    val submitBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button"))
    submitBtn.performScrollTo()
    submitBtn.performClick()
    composeTestRule.waitForIdle()

    // Switch to Topup tab
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("tab_topup")).performClick()
    composeTestRule.waitForIdle()

    // Click Tambah Topup button
    val addTopupBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("add_topup_button"))
    addTopupBtn.assertExists()
    addTopupBtn.performClick()
    composeTestRule.waitForIdle()

    // Assert AddTopupDialog is open
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("add_topup_dialog")).assertExists()

    // Assert status dropdown exists
    val statusDropdown = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("topup_status_dropdown"))
    statusDropdown.assertExists()

    // Assert due date picker dropdown exists (default status is "Nanti")
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("topup_due_date_dropdown")).assertExists()

    // Cancel / dismiss dialog
    composeTestRule.onNode(androidx.compose.ui.test.hasText("Batal")).performClick()
    composeTestRule.waitForIdle()

    // Logout
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_header_logout")).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test backup and restore dialog elements and cloud email input`() {
    composeTestRule.waitForIdle()

    // Login as admin
    val phoneInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_whatsapp_input"))
    phoneInput.performScrollTo()
    phoneInput.performTextInput("081234567890")

    val pinInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_pin_input"))
    pinInput.performScrollTo()
    pinInput.performTextInput("1234")

    val submitBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("login_submit_button"))
    submitBtn.performScrollTo()
    submitBtn.performClick()
    composeTestRule.waitForIdle()

    // Click backup and restore button in header
    val backupBtn = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("backup_restore_button"))
    backupBtn.assertExists()
    backupBtn.performClick()
    composeTestRule.waitForIdle()

    // Assert BackupRestoreDialog is displayed
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("backup_restore_dialog")).assertExists()

    // Assert Cloud Email input exists
    val emailInput = composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("user_google_email_input"))
    emailInput.assertExists()
    emailInput.performTextInput("tomibudiawan@gmail.com")
    composeTestRule.waitForIdle()

    // Assert Cloud action buttons exist
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("backup_to_drive_btn")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("restore_from_drive_btn")).assertExists()

    // Assert File SAF action buttons exist
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("export_json_file_btn")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("import_json_file_btn")).assertExists()

    // Assert Excel action buttons exist
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("export_xls_email_btn")).assertExists()
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("export_xls_share_btn")).assertExists()

    // Dismiss dialog
    composeTestRule.onNode(androidx.compose.ui.test.hasContentDescription("Tutup")).performClick()
    composeTestRule.waitForIdle()

    // Logout
    composeTestRule.onNode(androidx.compose.ui.test.hasTestTag("btn_header_logout")).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `test json serialization and parsing roundtrip fidelity`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val driveManager = com.example.data.remote.GoogleDriveManager(context)

    val sampleNasabah = listOf(
        com.example.data.model.Nasabah(
            id = 101L,
            customerCode = "cust-101",
            name = "Tomi Budiawan",
            whatsappNumber = "081299998888",
            pinLogin = "4321"
        )
    )

    val sampleKredit = listOf(
        com.example.data.model.KreditPiutang(
            id = 201L,
            customerId = 101L,
            customerCode = "cust-101",
            customerName = "Tomi Budiawan",
            akadType = "Kredit Barang",
            itemName = "Laptop ASUS",
            nominalPokok = 5000000.0,
            persentaseBunga = 10.0,
            totalPiutang = 5500000.0,
            jumlahSetoran = 550000.0,
            durasiTenor = 10,
            tenorUnit = "Bulan",
            hariPenagihan = null,
            tanggalPenagihan = 15,
            totalTerbayar = 550000.0,
            status = "BERJALAN"
        )
    )

    val sampleInstallments = listOf(
        com.example.data.model.PiutangInstallment(
            id = 301L,
            kreditId = 201L,
            customerId = 101L,
            customerName = "Tomi Budiawan",
            angsuranKe = 1,
            nominalRupiah = 550000.0,
            tanggalJatuhTempo = System.currentTimeMillis(),
            status = "LUNAS",
            metodePembayaran = "TUNAI"
        )
    )

    val sampleHutang = listOf(
        com.example.data.model.Hutang(
            id = 401L,
            namaSumber = "Koperasi Makmur",
            nominalHutang = 1000000.0,
            hutangDibayar = 200000.0,
            sisaHutang = 800000.0
        )
    )

    val sampleTopup = listOf(
        com.example.data.model.TopupTransaction(
            id = 501L,
            nama = "Token Listrik 100rb",
            nominal = 102500.0,
            totalDibayar = 102500.0,
            keuntungan = 2500.0,
            tanggalTransaksi = System.currentTimeMillis(),
            tanggalJatuhTempo = System.currentTimeMillis(),
            isLunas = true
        )
    )

    val samplePayments = listOf(
        com.example.data.model.HutangPayment(
            id = 601L,
            hutangId = 401L,
            namaSumber = "Koperasi Makmur",
            nominalBayar = 200000.0,
            tanggalBayar = System.currentTimeMillis(),
            catatan = "Bayar pertama"
        )
    )

    // 1. Serialize to JSON
    val json = driveManager.generateJsonBackup(
        nasabahList = sampleNasabah,
        kreditList = sampleKredit,
        installmentList = sampleInstallments,
        hutangList = sampleHutang,
        topupList = sampleTopup,
        hutangPaymentList = samplePayments,
        account = "tomibudiawan@gmail.com"
    )

    // 2. Parse from JSON
    val restored = driveManager.parseJsonBackup(json)

    // 3. Verify fidelity
    assertEquals(1, restored.nasabahList.size)
    assertEquals("Tomi Budiawan", restored.nasabahList[0].name)
    assertEquals("081299998888", restored.nasabahList[0].whatsappNumber)

    assertEquals(1, restored.kreditList.size)
    assertEquals("Laptop ASUS", restored.kreditList[0].itemName)
    assertEquals(5500000.0, restored.kreditList[0].totalPiutang, 0.01)

    assertEquals(1, restored.installmentList.size)
    assertEquals("LUNAS", restored.installmentList[0].status)
    assertEquals("TUNAI", restored.installmentList[0].metodePembayaran)

    assertEquals(1, restored.hutangList.size)
    assertEquals("Koperasi Makmur", restored.hutangList[0].namaSumber)
    assertEquals(800000.0, restored.hutangList[0].sisaHutang, 0.01)

    assertEquals(1, restored.topupList.size)
    assertEquals(102500.0, restored.topupList[0].nominal, 0.01)
    assertEquals(true, restored.topupList[0].isLunas)

    assertEquals(1, restored.hutangPaymentList.size)
    assertEquals(200000.0, restored.hutangPaymentList[0].nominalBayar, 0.01)
  }
}

