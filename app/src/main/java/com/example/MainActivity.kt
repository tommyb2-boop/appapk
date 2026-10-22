package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.components.RekapPiutangDialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.KreditPiutang
import com.example.ui.FinanceViewModel
import com.example.ui.components.AddHutangDialog
import com.example.ui.components.AddTopupDialog
import com.example.ui.components.AdminActionMenu
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.BayarTopupDialog
import com.example.ui.components.DaftarNasabahScreen
import com.example.ui.components.DashboardFooter
import com.example.ui.components.DashboardHeader
import com.example.ui.components.DetailNasabahScreen
import com.example.ui.components.DueBillsDialog
import com.example.ui.components.EditNasabahDialog
import com.example.ui.components.FormKreditDialog
import com.example.ui.components.FormNasabahDialog
import com.example.ui.components.InstallmentListSheet
import com.example.ui.components.KwitansiDialog
import com.example.ui.components.LoginScreen
import com.example.ui.components.MenuHutangScreen
import com.example.ui.components.MenuPiutangScreen
import com.example.ui.components.MenuTopupScreen
import com.example.ui.components.PayHutangDialog
import com.example.ui.components.PayInstallmentChoiceDialog
import com.example.ui.components.ProfileSettingsDialog
import com.example.ui.components.StrukReceiptDialog
import com.example.ui.components.SuperAdminScreen
import com.example.util.ExcelExporter
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald800
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Red700
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.ui.components.DueBillsDialog
import com.example.ui.theme.Slate900
import com.example.util.FormatUtils
import com.example.util.NotificationHelper
import java.util.Calendar
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        checkDueBillsIntent(intent)

        setContent {
            MyApplicationTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkDueBillsIntent(intent)
    }

    private fun checkDueBillsIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationHelper.EXTRA_OPEN_DUE_BILLS, false) == true) {
            viewModel.setShowDueBillsDialog(true)
        }
    }
}

@Composable
fun MainAppContent(viewModel: FinanceViewModel) {
    val context = LocalContext.current

    // Listen for toast messages
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // Observe data
    val nasabahList by viewModel.allNasabah.collectAsStateWithLifecycle()
    val kreditList by viewModel.allKredit.collectAsStateWithLifecycle()
    val installmentList by viewModel.allInstallments.collectAsStateWithLifecycle()
    val hutangList by viewModel.allHutang.collectAsStateWithLifecycle()
    val topupList by viewModel.allTopup.collectAsStateWithLifecycle()
    val hutangPaymentList by viewModel.allHutangPayments.collectAsStateWithLifecycle()

    // Navigation and tabs
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedCustomerId by viewModel.selectedCustomerId.collectAsStateWithLifecycle()

    // Dialog flags
    val showFormNasabah by viewModel.showFormNasabah.collectAsStateWithLifecycle()
    val showDaftarNasabah by viewModel.showDaftarNasabah.collectAsStateWithLifecycle()
    val showFormKredit by viewModel.showFormKredit.collectAsStateWithLifecycle()
    val showBackupDialog by viewModel.showBackupDialog.collectAsStateWithLifecycle()
    val showFirstLaunchGoogleAuth by viewModel.showFirstLaunchGoogleAuth.collectAsStateWithLifecycle()
    val showDueBillsDialog by viewModel.showDueBillsDialog.collectAsStateWithLifecycle()

    val activeKreditForList by viewModel.activeKreditForList.collectAsStateWithLifecycle()
    val activePayingInstallment by viewModel.activePayingInstallment.collectAsStateWithLifecycle()
    val activeStrukReceipt by viewModel.activeStrukReceipt.collectAsStateWithLifecycle()
    val activeKwitansi by viewModel.activeKwitansi.collectAsStateWithLifecycle()
    val activeHutangForPay by viewModel.activeHutangForPay.collectAsStateWithLifecycle()
    val showAddHutangDialog by viewModel.showAddHutangDialog.collectAsStateWithLifecycle()
    val showAddTopupDialog by viewModel.showAddTopupDialog.collectAsStateWithLifecycle()
    val activeEditNasabah by viewModel.activeEditNasabah.collectAsStateWithLifecycle()
    val activeTopupForPay by viewModel.activeTopupForPay.collectAsStateWithLifecycle()

    // Authentication & Profile state
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val adminPhone by viewModel.adminPhone.collectAsStateWithLifecycle()
    val adminPin by viewModel.adminPin.collectAsStateWithLifecycle()
    val appName by viewModel.appName.collectAsStateWithLifecycle()
    val adminBio by viewModel.adminBio.collectAsStateWithLifecycle()
    val loggedInUserName by viewModel.loggedInUserName.collectAsStateWithLifecycle()
    val loggedInUserType by viewModel.loggedInUserType.collectAsStateWithLifecycle()
    val loggedInPhone by viewModel.loggedInPhone.collectAsStateWithLifecycle()
    val showProfileDialog by viewModel.showProfileDialog.collectAsStateWithLifecycle()
    var showRekapPiutangDialog by remember { mutableStateOf(false) }

    // Cloud status
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncTime by viewModel.lastSyncTime.collectAsStateWithLifecycle()
    val driveAuthStatus by viewModel.driveAuthStatus.collectAsStateWithLifecycle()
    val isDriveLoading by viewModel.isDriveLoading.collectAsStateWithLifecycle()

    // Request Notification permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    // SAF Launchers for genuine Google Drive & Device Storage file backup
    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackupToUri(uri)
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackupFromUri(uri)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Perhitungan Tanggal Jatuh Tempo
    val todayEndMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    // Menemukan data nasabah jika login sebagai Customer
    val currentLoggedInCustomer = remember(isLoggedIn, loggedInUserType, loggedInPhone, selectedCustomerId, nasabahList) {
        if (isLoggedIn && loggedInUserType == "NASABAH") {
            nasabahList.find { it.id == selectedCustomerId || it.whatsappNumber == loggedInPhone }
        } else {
            null
        }
    }

    // Filter tagihan jatuh tempo sesuai tipe akun pengguna (Nasabah hanya miliknya, Admin semua data)
    val activeDueInstallments = remember(installmentList, todayEndMillis, loggedInUserType, currentLoggedInCustomer, isLoggedIn) {
        if (!isLoggedIn) {
            emptyList()
        } else if (loggedInUserType == "NASABAH") {
            val custId = currentLoggedInCustomer?.id
            if (custId != null) {
                installmentList.filter { it.customerId == custId && it.status != "LUNAS" && it.tanggalJatuhTempo <= todayEndMillis }
            } else {
                emptyList()
            }
        } else if (loggedInUserType == "ADMIN") {
            installmentList.filter { it.status != "LUNAS" && it.tanggalJatuhTempo <= todayEndMillis }
        } else {
            emptyList()
        }
    }

    val activeDueTopups = remember(topupList, todayEndMillis, loggedInUserType, isLoggedIn) {
        if (isLoggedIn && loggedInUserType == "ADMIN") {
            topupList.filter { !it.isLunas && it.tanggalJatuhTempo <= todayEndMillis }
        } else {
            emptyList()
        }
    }

    val dueInstallmentsCount = activeDueInstallments.size
    val dueTopupCount = activeDueTopups.size
    val totalDueBillsCount = dueInstallmentsCount + dueTopupCount

    // Memicu notifikasi sistem di status bar HP saat ada tagihan jatuh tempo (100% dipisahkan antara Nasabah & Admin)
    LaunchedEffect(totalDueBillsCount, activeDueInstallments, activeDueTopups, isLoggedIn, loggedInUserType) {
        if (isLoggedIn && totalDueBillsCount > 0) {
            val summaries = mutableListOf<String>()
            val kreditMap = kreditList.associateBy { it.id }

            if (loggedInUserType == "NASABAH") {
                // Notifikasi khusus Nasabah: HANYA menampilkan rincian angsuran pribadinya sendiri
                activeDueInstallments.take(3).forEach { inst ->
                    val namaBarang = kreditMap[inst.kreditId]?.itemName ?: "Angsuran"
                    summaries.add("Angsuran ke-${inst.angsuranKe} ($namaBarang): ${FormatUtils.formatRupiah(inst.nominalRupiah)}")
                }
                NotificationHelper.showDueBillsNotification(
                    context = context,
                    dueCount = totalDueBillsCount,
                    billSummaries = summaries,
                    title = "⚠️ Pengingat: $totalDueBillsCount Tagihan Angsuran Anda Jatuh Tempo"
                )
            } else if (loggedInUserType == "ADMIN") {
                // Notifikasi Admin: rekap seluruh nasabah dan layanan topup
                val nasabahMap = nasabahList.associateBy { it.id }
                activeDueInstallments.take(3).forEach { inst ->
                    val nama = nasabahMap[inst.customerId]?.name ?: "Nasabah #${inst.customerId}"
                    summaries.add("Kredit $nama: ${FormatUtils.formatRupiah(inst.nominalRupiah)}")
                }

                activeDueTopups.take(3).forEach { t ->
                    summaries.add("Topup ${t.nama}: ${FormatUtils.formatRupiah(t.nominal)}")
                }

                NotificationHelper.showDueBillsNotification(
                    context = context,
                    dueCount = totalDueBillsCount,
                    billSummaries = summaries,
                    title = "⚠️ $totalDueBillsCount Tagihan Jatuh Tempo!"
                )
            }
        } else {
            NotificationHelper.cancelDueBillsNotification(context)
        }
    }

    // Financial calculations
    val totalHutang = hutangList.sumOf { it.sisaHutang }
    val totalPiutang = kreditList.sumOf { it.totalPiutang - it.totalTerbayar }
    val totalTopup = topupList.filter { !it.isLunas }.sumOf { it.nominal }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF8FAFC),
        contentColor = Slate900
    ) {
        if (!isLoggedIn) {
            // Halaman Login Utama dengan Nomor WhatsApp dan PIN
            LoginScreen(
                onLoginSubmit = { phone, pin ->
                    viewModel.login(phone, pin)
                },
                onDownloadCloudData = { onComplete ->
                    viewModel.downloadDataFromCloud(onComplete)
                },
                isSyncing = isSyncing,
                defaultAdminPhone = adminPhone,
                defaultAdminPin = adminPin
            )
        } else if (loggedInUserType == "SUPER_ADMIN") {
            // Halaman Khusus Super Admin: Pemulihan / Timpa Akun Admin
            SuperAdminScreen(
                currentAdminPhone = adminPhone,
                currentAdminPin = adminPin,
                onUpdateAdminCredentials = { newPhone, newPin ->
                    viewModel.updateAdminCredentials(newPhone, newPin)
                },
                onGoToDashboard = {
                    viewModel.switchSuperAdminToAdmin()
                },
                onLogout = {
                    viewModel.logout()
                }
            )
        } else if (loggedInUserType == "NASABAH") {
            // Halaman Khusus Customer: Hanya bisa melihat detail piutang masing-masing
            // Tidak ada tombol kembali atau akses menuju dashboard admin (privasi admin terjamin)
            val currentCustomer = currentLoggedInCustomer ?: nasabahList.find { it.id == selectedCustomerId || it.whatsappNumber == loggedInPhone }
            if (currentCustomer != null) {
                val customerKredits = kreditList.filter { it.customerId == currentCustomer.id }
                val customerInsts = installmentList.filter { it.customerId == currentCustomer.id }

                DetailNasabahScreen(
                    nasabah = currentCustomer,
                    kreditList = customerKredits,
                    installments = customerInsts,
                    isCustomerView = true,
                    dueBillsCount = totalDueBillsCount,
                    onNotificationClick = { viewModel.setShowDueBillsDialog(true) },
                    onBack = { viewModel.logout() },
                    onLogout = { viewModel.logout() },
                    onKwitansiClick = { inst, kredit ->
                        viewModel.openKwitansi(inst, currentCustomer, kredit)
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    viewModel.logout()
                }
            }
        } else if (selectedCustomerId != null) {
            // Admin viewing customer detail
            val selectedCustomer = nasabahList.find { it.id == selectedCustomerId }
            if (selectedCustomer != null) {
                val customerKredits = kreditList.filter { it.customerId == selectedCustomer.id }
                val customerInsts = installmentList.filter { it.customerId == selectedCustomer.id }

                DetailNasabahScreen(
                    nasabah = selectedCustomer,
                    kreditList = customerKredits,
                    installments = customerInsts,
                    isCustomerView = false,
                    onBack = { viewModel.closeCustomerDetail() },
                    onLogout = { viewModel.logout() },
                    onEditNasabahClick = {
                        viewModel.openEditNasabah(selectedCustomer)
                    },
                    onKwitansiClick = { inst, kredit ->
                        viewModel.openKwitansi(inst, selectedCustomer, kredit)
                    }
                )
            } else {
                viewModel.closeCustomerDetail()
            }
        } else if (showDaftarNasabah) {
            DaftarNasabahScreen(
                nasabahList = nasabahList,
                kreditList = kreditList,
                installmentList = installmentList,
                onBack = { viewModel.setShowDaftarNasabah(false) },
                onCustomerDetailClick = { customerId ->
                    viewModel.openCustomerDetail(customerId)
                },
                onEditNasabahClick = { nasabah ->
                    viewModel.openEditNasabah(nasabah)
                },
                onKwitansiClick = { inst, kredit, nasabah ->
                    viewModel.openKwitansi(inst, nasabah, kredit)
                }
            )
        } else {
            // Main Dashboard Screen
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color(0xFFF8FAFC),
                contentColor = Slate900,
                topBar = {
                    DashboardHeader(
                        appName = appName,
                        isSyncing = isSyncing,
                        lastSyncTime = lastSyncTime,
                        dueBillsCount = totalDueBillsCount,
                        onNotificationClick = { viewModel.setShowDueBillsDialog(true) },
                        onSyncClick = { viewModel.triggerFirestoreSync() },
                        onBackupClick = { viewModel.setShowBackupDialog(true) },
                        onProfileClick = { viewModel.setShowProfileDialog(true) },
                        onLogoutClick = { viewModel.logout() }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Dashboard Menu Utama: 3 Menu Buttons (Piutang, Hutang, Topup)
                    DashboardMenuTabs(
                        currentTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) },
                        piutangCount = kreditList.size,
                        hutangCount = hutangList.count { it.sisaHutang > 0 },
                        topupCount = topupList.count { !it.isLunas }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tab Content
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentTab) {
                            0 -> {
                                MenuPiutangScreen(
                                    kreditList = kreditList,
                                    installmentList = installmentList,
                                    onListClick = { viewModel.openKreditList(it) },
                                    onCustomerClick = { customerId -> viewModel.openCustomerDetail(customerId) },
                                    onAddClick = { viewModel.setShowFormKredit(true) },
                                    onRekapClick = { showRekapPiutangDialog = true }
                                )
                            }
                            1 -> {
                                MenuHutangScreen(
                                    hutangList = hutangList,
                                    paymentList = hutangPaymentList,
                                    onPayClick = { viewModel.openPayHutang(it) },
                                    onAddHutangClick = { viewModel.setShowAddHutang(true) }
                                )
                            }
                            2 -> {
                                MenuTopupScreen(
                                    topupList = topupList,
                                    onMarkLunasClick = { viewModel.markTopupLunas(it) },
                                    onAddTopupClick = { viewModel.setShowAddTopup(true) },
                                    onPayTopupClick = { id, nom, isFull ->
                                        viewModel.payTopup(id, nom, isFull)
                                    }
                                )
                            }
                        }
                    }
                }

                // Menu Admin (Aksi Navigasi) di KIRI BAWAH - HANYA berada di menu Piutang (currentTab == 0)
                if (currentTab == 0) {
                    AdminActionMenu(
                        onNewNasabahClick = { viewModel.setShowFormNasabah(true) },
                        onNewKreditClick = { viewModel.setShowFormKredit(true) },
                        onDaftarNasabahClick = { viewModel.setShowDaftarNasabah(true) },
                        onRekapPiutangClick = { showRekapPiutangDialog = true },
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }
        }
    }

    // Modal Dialogs: Hanya ditampilkan jika pengguna telah berhasil login
    if (isLoggedIn) {
        // Autocomplete suggestion lists dari data yang pernah diinput
        val topupNameSuggestions = remember(topupList, nasabahList) {
            (topupList.map { it.nama } + nasabahList.map { it.name }).distinct().filter { it.isNotBlank() }
        }
        val kreditItemSuggestions = remember(kreditList) {
            kreditList.map { it.itemName }.distinct().filter { it.isNotBlank() }
        }
        val hutangSumberSuggestions = remember(hutangList) {
            hutangList.map { it.namaSumber }.distinct().filter { it.isNotBlank() }
        }

        // 1. Installment List Sheet (saat tombol [LIST] ditekan)
        activeKreditForList?.let { kredit ->
            val relevantInsts = installmentList.filter { it.kreditId == kredit.id }
            InstallmentListSheet(
                kredit = kredit,
                installments = relevantInsts,
                onDismiss = { viewModel.closeKreditList() },
                onPayClick = { inst -> viewModel.openPayInstallment(inst) }
            )
        }

        // 2. Pay Installment Choice Dialog (pilihan [TUNAI] / [NON TUNAI] -> [OK])
        activePayingInstallment?.let { inst ->
            PayInstallmentChoiceDialog(
                installment = inst,
                onDismiss = { viewModel.closePayInstallment() },
                onConfirmOk = { metode -> viewModel.processPaymentInstallment(metode) }
            )
        }

        // 3. Struk Receipt Dialog (setelah bayar [OK] -> struk -> [Kirim ke WhatsApp] PDF)
        activeStrukReceipt?.let { strukData ->
            StrukReceiptDialog(
                data = strukData,
                onDismiss = { viewModel.closeStrukReceipt() }
            )
        }

        // 4. Kwitansi Dialog (dari Detail Nasabah)
        activeKwitansi?.let { kwitansiData ->
            KwitansiDialog(
                data = kwitansiData,
                onDismiss = { viewModel.closeKwitansi() }
            )
        }

        // 5. Pay Hutang Dialog (input manual nominal & tanggal)
        activeHutangForPay?.let { hutang ->
            PayHutangDialog(
                hutang = hutang,
                onDismiss = { viewModel.closePayHutang() },
                onConfirmPay = { nominal, tgl -> viewModel.payHutangManual(nominal, tgl) }
            )
        }

        // 6. Add Hutang Dialog
        if (showAddHutangDialog) {
            AddHutangDialog(
                existingSumberList = hutangSumberSuggestions,
                onDismiss = { viewModel.setShowAddHutang(false) },
                onSave = { nama, nom, ket -> viewModel.addHutang(nama, nom, ket) }
            )
        }

        // 7. Add Topup Dialog (dengan tanggal input, keuntungan, dan pilihan status lunas / nanti)
        if (showAddTopupDialog) {
            AddTopupDialog(
                existingNames = topupNameSuggestions,
                onDismiss = { viewModel.setShowAddTopup(false) },
                onSave = { nama, nom, untung, tgl, tempoMillis, isLunas ->
                    viewModel.addTopup(nama, nom, untung, tgl, tempoMillis, isLunas)
                }
            )
        }

        // 8. Profile & Kalkulasi Keuangan Dialog
        if (showProfileDialog) {
            ProfileSettingsDialog(
                currentAppName = appName,
                currentAdminName = loggedInUserName,
                currentAdminBio = adminBio,
                currentAdminPhone = adminPhone,
                currentAdminPin = adminPin,
                kreditList = kreditList,
                hutangList = hutangList,
                topupList = topupList,
                onDismiss = { viewModel.setShowProfileDialog(false) },
                onSaveAppName = { viewModel.updateAppName(it) },
                onSaveBiodata = { name, bio -> viewModel.updateAdminBiodata(name, bio) },
                onSaveCredentials = { phone, pin -> viewModel.updateAdminCredentials(phone, pin) },
                onResetDatabase = { viewModel.resetDatabase() },
                onLogout = {
                    viewModel.setShowProfileDialog(false)
                    viewModel.logout()
                }
            )
        }

        // 9. Form Nasabah Baru
        if (showFormNasabah) {
            val nextCode = "cust-${(nasabahList.size + 1).toString().padStart(3, '0')}"
            FormNasabahDialog(
                suggestedCode = nextCode,
                onDismiss = { viewModel.setShowFormNasabah(false) },
                onSave = { name, code, phone, pin -> viewModel.createNasabah(name, code, phone, pin) }
            )
        }

        // 10. Form Kredit Baru
        if (showFormKredit) {
            FormKreditDialog(
                nasabahList = nasabahList,
                existingItemNames = kreditItemSuggestions,
                onDismiss = { viewModel.setShowFormKredit(false) },
                onSave = { custId, akad, item, pokok, persen, setoran, tenor, unit, hari, tgl ->
                    viewModel.createKredit(custId, akad, item, pokok, persen, setoran, tenor, unit, hari, tgl)
                }
            )
        }

        // 11. Backup & Restore Dialog (Cloud Server, Google Drive / SAF, XLS to Email)
        if (showBackupDialog) {
            BackupRestoreDialog(
                driveAuthStatus = driveAuthStatus,
                isDriveLoading = isDriveLoading,
                nasabahList = nasabahList,
                kreditList = kreditList,
                installmentList = installmentList,
                hutangList = hutangList,
                topupList = topupList,
                hutangPaymentList = hutangPaymentList,
                onDismiss = { viewModel.setShowBackupDialog(false) },
                onBackupToDrive = { email -> viewModel.triggerGoogleDriveBackup(email) },
                onRestoreFromDrive = { email -> viewModel.triggerGoogleDriveRestore(email) },
                onSaveJsonFile = {
                    exportJsonLauncher.launch("Backup_Manajemen_Hutang_Piutang.json")
                },
                onOpenJsonFile = {
                    importJsonLauncher.launch(arrayOf("application/json", "*/*"))
                },
                onConnectDrive = { email -> viewModel.connectGoogleDriveAccount(email) },
                onDisconnectDrive = { viewModel.disconnectGoogleDriveAccount() }
            )
        }

        // 13. Tagihan Jatuh Tempo Dialog (Pemberitahuan & Pembayaran Langsung)
        if (showDueBillsDialog) {
            val isCustomer = (loggedInUserType == "NASABAH")
            val dialogInstallments = if (isCustomer) {
                val custId = currentLoggedInCustomer?.id
                if (custId != null) installmentList.filter { it.customerId == custId } else emptyList()
            } else {
                installmentList
            }
            val dialogKreditList = if (isCustomer) {
                val custId = currentLoggedInCustomer?.id
                if (custId != null) kreditList.filter { it.customerId == custId } else emptyList()
            } else {
                kreditList
            }
            val dialogTopupList = if (isCustomer) emptyList() else topupList

            DueBillsDialog(
                installments = dialogInstallments,
                kreditList = dialogKreditList,
                nasabahList = nasabahList,
                topupList = dialogTopupList,
                isCustomerView = isCustomer,
                onPayInstallmentClick = { inst ->
                    if (!isCustomer) {
                        viewModel.openPayInstallment(inst)
                    }
                },
                onPayTopupClick = { topup ->
                    if (!isCustomer) {
                        viewModel.openPayTopup(topup)
                    }
                },
                onDismiss = { viewModel.setShowDueBillsDialog(false) }
            )
        }

        // 14. Rekap Piutang Dialog
        if (showRekapPiutangDialog) {
            RekapPiutangDialog(
                kreditList = kreditList,
                installmentList = installmentList,
                onDismiss = { showRekapPiutangDialog = false }
            )
        }

        // 15. Edit Nasabah & Restrukturisasi Tenor Dialog
        activeEditNasabah?.let { nasabah ->
            val nasabahKredits = kreditList.filter { it.customerId == nasabah.id }
            EditNasabahDialog(
                nasabah = nasabah,
                kredits = nasabahKredits,
                onDismiss = { viewModel.closeEditNasabah() },
                onSave = { id, name, phone, pin, kId, bunga, unit, hari, tgl, setoran, tenor ->
                    viewModel.updateNasabahAndRestructure(
                        nasabahId = id,
                        newName = name,
                        newPhone = phone,
                        newPin = pin,
                        kreditIdToRestructure = kId,
                        tambahanBunga = bunga,
                        newTenorUnit = unit,
                        newHariPenagihan = hari,
                        newTanggalPenagihan = tgl,
                        newJumlahSetoran = setoran,
                        newDurasiTenor = tenor
                    )
                }
            )
        }

        // 16. Bayar Topup Dialog (Pilihan Lunas & Sebagian)
        activeTopupForPay?.let { topup ->
            BayarTopupDialog(
                topup = topup,
                onDismiss = { viewModel.closePayTopup() },
                onConfirmPayment = { topupId, nominalBayar, isFullLunas ->
                    viewModel.payTopup(topupId, nominalBayar, isFullLunas)
                    viewModel.closePayTopup()
                }
            )
        }
    }
}

/**
 * Dashboard Menu Utama: Menampilkan 3 menu:
 * 1. Menu Piutang
 * 2. Menu Hutang
 * 3. Menu Topup
 */
@Composable
private fun DashboardMenuTabs(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    piutangCount: Int,
    hutangCount: Int,
    topupCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("dashboard_menu_tabs"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate900
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MenuTabItem(
                title = "Piutang",
                icon = Icons.Default.ReceiptLong,
                count = piutangCount,
                isSelected = currentTab == 0,
                activeColor = Emerald800,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            MenuTabItem(
                title = "Hutang",
                icon = Icons.Default.MoneyOff,
                count = hutangCount,
                isSelected = currentTab == 1,
                activeColor = Red700,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )

            MenuTabItem(
                title = "Topup",
                icon = Icons.Default.ElectricBolt,
                count = topupCount,
                isSelected = currentTab == 2,
                activeColor = Color(0xFFB45309), // Amber 700
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MenuTabItem(
    title: String,
    icon: ImageVector,
    count: Int,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) activeColor else Color(0xFFF1F5F9)
    val contentTextColor = if (isSelected) Color.White else Slate900
    val iconColor = if (isSelected) Color.White else Slate800

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .testTag("tab_${title.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = contentTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    lineHeight = 13.sp
                )
            )
            if (count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color.White else Slate900,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            lineHeight = 11.sp
                        )
                    )
                }
            }
        }
    }
}
