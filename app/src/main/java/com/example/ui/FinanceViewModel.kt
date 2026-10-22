package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.Hutang
import com.example.data.model.HutangPayment
import com.example.data.model.KreditPiutang
import com.example.data.model.Nasabah
import com.example.data.model.PiutangInstallment
import com.example.data.model.TopupTransaction
import com.example.data.remote.DriveAuthStatus
import com.example.data.remote.FirebaseSyncManager
import com.example.data.remote.GoogleDriveManager
import com.example.data.repository.FinanceRepository
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StrukReceiptData(
    val installment: PiutangInstallment,
    val kredit: KreditPiutang?,
    val nasabah: Nasabah?
)

data class KwitansiData(
    val installment: PiutangInstallment,
    val kredit: KreditPiutang?,
    val nasabah: Nasabah
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val firebaseSyncManager = FirebaseSyncManager(application)
    val googleDriveManager = GoogleDriveManager(application)
    val repository = FinanceRepository(database, firebaseSyncManager, googleDriveManager)

    // Authentication Preferences
    private val authPrefs = application.getSharedPreferences("app_auth_prefs", Context.MODE_PRIVATE)

    private val _adminPhone = MutableStateFlow(authPrefs.getString("admin_phone", "081234567890") ?: "081234567890")
    val adminPhone: StateFlow<String> = _adminPhone.asStateFlow()

    private val _adminPin = MutableStateFlow(authPrefs.getString("admin_pin", "1234") ?: "1234")
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    private val _appName = MutableStateFlow(authPrefs.getString("app_name", "Manajemen Hutang Piutang") ?: "Manajemen Hutang Piutang")
    val appName: StateFlow<String> = _appName.asStateFlow()

    private val _adminBio = MutableStateFlow(authPrefs.getString("admin_bio", "Pengelola Catatan Usaha & Keuangan") ?: "Pengelola Catatan Usaha & Keuangan")
    val adminBio: StateFlow<String> = _adminBio.asStateFlow()

    private val _showProfileDialog = MutableStateFlow(false)
    val showProfileDialog: StateFlow<Boolean> = _showProfileDialog.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loggedInUserType = MutableStateFlow(authPrefs.getString("logged_in_type", "ADMIN") ?: "ADMIN")
    val loggedInUserType: StateFlow<String> = _loggedInUserType.asStateFlow()

    private val _loggedInUserName = MutableStateFlow(authPrefs.getString("logged_in_name", "Administrator") ?: "Administrator")
    val loggedInUserName: StateFlow<String> = _loggedInUserName.asStateFlow()

    private val _loggedInPhone = MutableStateFlow(authPrefs.getString("logged_in_phone", "081234567890") ?: "081234567890")
    val loggedInPhone: StateFlow<String> = _loggedInPhone.asStateFlow()

    // Data streams
    val allNasabah: StateFlow<List<Nasabah>> = repository.allNasabah.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allKredit: StateFlow<List<KreditPiutang>> = repository.allKredit.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allInstallments: StateFlow<List<PiutangInstallment>> = repository.allInstallments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allHutang: StateFlow<List<Hutang>> = repository.allHutang.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allTopup: StateFlow<List<TopupTransaction>> = repository.allTopup.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allHutangPayments: StateFlow<List<HutangPayment>> = repository.allHutangPayments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // UI Tab: 0 = Piutang, 1 = Hutang, 2 = Topup
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Navigation: Detail Nasabah
    private val _selectedCustomerId = MutableStateFlow<Long?>(null)
    val selectedCustomerId: StateFlow<Long?> = _selectedCustomerId.asStateFlow()

    // Modal dialogs
    private val _showFormNasabah = MutableStateFlow(false)
    val showFormNasabah: StateFlow<Boolean> = _showFormNasabah.asStateFlow()

    private val _showDaftarNasabah = MutableStateFlow(false)
    val showDaftarNasabah: StateFlow<Boolean> = _showDaftarNasabah.asStateFlow()

    private val _showFormKredit = MutableStateFlow(false)
    val showFormKredit: StateFlow<Boolean> = _showFormKredit.asStateFlow()

    private val _showBackupDialog = MutableStateFlow(false)
    val showBackupDialog: StateFlow<Boolean> = _showBackupDialog.asStateFlow()

    private val _showFirstLaunchGoogleAuth = MutableStateFlow(false)
    val showFirstLaunchGoogleAuth: StateFlow<Boolean> = _showFirstLaunchGoogleAuth.asStateFlow()

    // Due Bills Notification Dialog
    private val _showDueBillsDialog = MutableStateFlow(false)
    val showDueBillsDialog: StateFlow<Boolean> = _showDueBillsDialog.asStateFlow()

    fun setShowDueBillsDialog(show: Boolean) {
        _showDueBillsDialog.value = show
    }

    // Piutang [LIST] dialog
    private val _activeKreditForList = MutableStateFlow<KreditPiutang?>(null)
    val activeKreditForList: StateFlow<KreditPiutang?> = _activeKreditForList.asStateFlow()

    // Piutang [BAYAR] dialog (TUNAI / NON TUNAI)
    private val _activePayingInstallment = MutableStateFlow<PiutangInstallment?>(null)
    val activePayingInstallment: StateFlow<PiutangInstallment?> = _activePayingInstallment.asStateFlow()

    // Struk UI dialog
    private val _activeStrukReceipt = MutableStateFlow<StrukReceiptData?>(null)
    val activeStrukReceipt: StateFlow<StrukReceiptData?> = _activeStrukReceipt.asStateFlow()

    // Kwitansi UI dialog
    private val _activeKwitansi = MutableStateFlow<KwitansiData?>(null)
    val activeKwitansi: StateFlow<KwitansiData?> = _activeKwitansi.asStateFlow()

    // Hutang Bayar dialog
    private val _activeHutangForPay = MutableStateFlow<Hutang?>(null)
    val activeHutangForPay: StateFlow<Hutang?> = _activeHutangForPay.asStateFlow()

    private val _showAddHutangDialog = MutableStateFlow(false)
    val showAddHutangDialog: StateFlow<Boolean> = _showAddHutangDialog.asStateFlow()

    private val _showAddTopupDialog = MutableStateFlow(false)
    val showAddTopupDialog: StateFlow<Boolean> = _showAddTopupDialog.asStateFlow()

    // Dialog Edit Nasabah & Restrukturisasi Tenor
    private val _activeEditNasabah = MutableStateFlow<Nasabah?>(null)
    val activeEditNasabah: StateFlow<Nasabah?> = _activeEditNasabah.asStateFlow()

    fun openEditNasabah(nasabah: Nasabah) {
        _activeEditNasabah.value = nasabah
    }

    fun closeEditNasabah() {
        _activeEditNasabah.value = null
    }

    // Dialog Bayar Topup (Pilihan Lunas / Sebagian)
    private val _activeTopupForPay = MutableStateFlow<TopupTransaction?>(null)
    val activeTopupForPay: StateFlow<TopupTransaction?> = _activeTopupForPay.asStateFlow()

    fun openPayTopup(topup: TopupTransaction) {
        _activeTopupForPay.value = topup
    }

    fun closePayTopup() {
        _activeTopupForPay.value = null
    }

    // Sync state
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(repository.getLastFirestoreSync())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()

    // Drive status
    private val _driveAuthStatus = MutableStateFlow(googleDriveManager.getAuthStatus())
    val driveAuthStatus: StateFlow<DriveAuthStatus> = _driveAuthStatus.asStateFlow()

    private val _isDriveLoading = MutableStateFlow(false)
    val isDriveLoading: StateFlow<Boolean> = _isDriveLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            _lastSyncTime.value = repository.getLastFirestoreSync()
        }
    }

    fun selectTab(tab: Int) {
        _currentTab.value = tab
    }

    fun openCustomerDetail(customerId: Long) {
        _selectedCustomerId.value = customerId
    }

    fun closeCustomerDetail() {
        _selectedCustomerId.value = null
    }

    fun setShowFormNasabah(show: Boolean) {
        _showFormNasabah.value = show
    }

    fun setShowDaftarNasabah(show: Boolean) {
        _showDaftarNasabah.value = show
    }

    fun setShowFormKredit(show: Boolean) {
        _showFormKredit.value = show
    }

    fun setShowBackupDialog(show: Boolean) {
        _showBackupDialog.value = show
    }

    fun closeFirstLaunchGoogleAuth(authorized: Boolean, email: String? = null) {
        googleDriveManager.setFirstLaunchPrompted()
        if (authorized && !email.isNullOrBlank()) {
            googleDriveManager.connectAccount(email)
            _driveAuthStatus.value = googleDriveManager.getAuthStatus()
        }
        _showFirstLaunchGoogleAuth.value = false
    }

    fun openKreditList(kredit: KreditPiutang) {
        _activeKreditForList.value = kredit
    }

    fun closeKreditList() {
        _activeKreditForList.value = null
    }

    fun openPayInstallment(installment: PiutangInstallment) {
        _activePayingInstallment.value = installment
    }

    fun closePayInstallment() {
        _activePayingInstallment.value = null
    }

    fun closeStrukReceipt() {
        _activeStrukReceipt.value = null
    }

    fun openKwitansi(installment: PiutangInstallment, nasabah: Nasabah, kredit: KreditPiutang?) {
        _activeKwitansi.value = KwitansiData(installment, kredit, nasabah)
    }

    fun closeKwitansi() {
        _activeKwitansi.value = null
    }

    fun openPayHutang(hutang: Hutang) {
        _activeHutangForPay.value = hutang
    }

    fun closePayHutang() {
        _activeHutangForPay.value = null
    }

    fun setShowAddHutang(show: Boolean) {
        _showAddHutangDialog.value = show
    }

    fun setShowAddTopup(show: Boolean) {
        _showAddTopupDialog.value = show
    }

    // Actions
    fun createNasabah(name: String, code: String, phone: String, pin: String) {
        viewModelScope.launch {
            val nasabah = Nasabah(
                customerCode = code.ifBlank { "cust-${(allNasabah.value.size + 1).toString().padStart(3, '0')}" },
                name = name.trim(),
                whatsappNumber = phone.trim(),
                pinLogin = pin.trim().ifBlank { "1234" }
            )
            repository.insertNasabah(nasabah)
            _showFormNasabah.value = false
            _toastEvent.emit("Nasabah ${nasabah.name} berhasil ditambahkan!")
        }
    }

    fun createKredit(
        customerId: Long,
        akadType: String,
        itemName: String,
        nominalPokok: Double,
        persenBunga: Double,
        jumlahSetoran: Double,
        durasiTenor: Int,
        tenorUnit: String,
        hariPenagihan: String? = null,
        tanggalPenagihan: Int? = null
    ) {
        viewModelScope.launch {
            val customer = repository.getNasabahByIdSync(customerId) ?: return@launch
            val totalPiutang = nominalPokok + (nominalPokok * (persenBunga / 100.0))
            val finalTenor = if (durasiTenor <= 0 && jumlahSetoran > 0) {
                Math.ceil(totalPiutang / jumlahSetoran).toInt().coerceAtLeast(1)
            } else {
                durasiTenor.coerceAtLeast(1)
            }

            val kredit = KreditPiutang(
                customerId = customerId,
                customerCode = customer.customerCode,
                customerName = customer.name,
                akadType = akadType,
                itemName = itemName.trim(),
                nominalPokok = nominalPokok,
                persentaseBunga = persenBunga,
                totalPiutang = totalPiutang,
                jumlahSetoran = if (jumlahSetoran > 0) jumlahSetoran else (totalPiutang / finalTenor),
                durasiTenor = finalTenor,
                tenorUnit = tenorUnit,
                hariPenagihan = hariPenagihan?.trim()?.ifBlank { null },
                tanggalPenagihan = tanggalPenagihan
            )
            repository.insertKreditWithInstallments(kredit)
            _showFormKredit.value = false
            _toastEvent.emit("Kredit baru berhasil dibuat dengan $finalTenor jadwal angsuran!")
        }
    }

    fun processPaymentInstallment(metode: String) {
        val target = _activePayingInstallment.value ?: return
        viewModelScope.launch {
            val updated = repository.payInstallment(target.id, metode)
            if (updated != null) {
                val kredit = repository.getKreditByIdSync(updated.kreditId)
                val nasabah = repository.getNasabahByIdSync(updated.customerId)
                _activePayingInstallment.value = null
                // Show Struk Pembayaran
                _activeStrukReceipt.value = StrukReceiptData(updated, kredit, nasabah)
                _toastEvent.emit("Pembayaran ${updated.nominalRupiah} berhasil tercatat!")
            }
        }
    }

    fun payHutangManual(nominal: Double, tanggal: String) {
        val target = _activeHutangForPay.value ?: return
        viewModelScope.launch {
            val dateMillis = try {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                sdf.parse(tanggal.trim())?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            repository.payHutang(target.id, nominal, dateMillis)
            _activeHutangForPay.value = null
            _toastEvent.emit("Pembayaran hutang Rp ${com.example.util.FormatUtils.formatThousandDots(nominal.toLong().toString())} berhasil dicatat!")
        }
    }

    fun addHutang(namaSumber: String, nominal: Double, keterangan: String?) {
        viewModelScope.launch {
            val h = Hutang(
                namaSumber = namaSumber.trim(),
                nominalHutang = nominal,
                hutangDibayar = 0.0,
                sisaHutang = nominal,
                catatan = keterangan
            )
            repository.insertHutang(h)
            _showAddHutangDialog.value = false
            _toastEvent.emit("Hutang baru berhasil dicatat!")
        }
    }

    fun addTopup(
        nama: String,
        nominal: Double,
        keuntungan: Double,
        tanggalTransaksi: Long,
        tanggalJatuhTempo: Long,
        isLunas: Boolean = false
    ) {
        viewModelScope.launch {
            val t = TopupTransaction(
                nama = nama.trim(),
                nominal = nominal,
                keuntungan = keuntungan,
                tanggalTransaksi = tanggalTransaksi,
                tanggalJatuhTempo = tanggalJatuhTempo,
                isLunas = isLunas,
                tanggalBayar = if (isLunas) tanggalTransaksi else null
            )
            repository.insertTopup(t)
            _showAddTopupDialog.value = false
            val msg = if (isLunas) "Transaksi topup lunas berhasil dicatat!" else "Transaksi layanan topup berhasil ditambahkan!"
            _toastEvent.emit(msg)
        }
    }

    fun addTopup(
        nama: String,
        nominal: Double,
        keuntungan: Double,
        tanggalTransaksi: Long,
        jatuhTempoDays: Int
    ) {
        val jt = tanggalTransaksi + (jatuhTempoDays * 86400000L)
        addTopup(nama, nominal, keuntungan, tanggalTransaksi, jt, false)
    }

    fun markTopupLunas(topupId: Long) {
        payTopup(topupId, 0.0, true)
    }

    fun payTopup(topupId: Long, nominalBayar: Double = 0.0, isFullLunas: Boolean = true) {
        viewModelScope.launch {
            val success = repository.payTopup(topupId, nominalBayar, isFullLunas)
            if (success) {
                _activeTopupForPay.value = null
                val msg = if (isFullLunas) "Transaksi layanan topup berhasil dilunasi!" else "Pembayaran sebagian ${FormatUtils.formatRupiah(nominalBayar)} berhasil dicatat!"
                _toastEvent.emit(msg)
            }
        }
    }

    fun updateNasabahAndRestructure(
        nasabahId: Long,
        newName: String,
        newPhone: String,
        newPin: String,
        kreditIdToRestructure: Long? = null,
        tambahanBunga: Double = 0.0,
        newTenorUnit: String = "Bulan",
        newHariPenagihan: String? = null,
        newTanggalPenagihan: Int? = null,
        newJumlahSetoran: Double = 0.0,
        newDurasiTenor: Int = 1
    ) {
        viewModelScope.launch {
            val success = repository.updateNasabahAndRestructure(
                nasabahId = nasabahId,
                newName = newName,
                newPhone = newPhone,
                newPin = newPin,
                kreditIdToRestructure = kreditIdToRestructure,
                tambahanBunga = tambahanBunga,
                newTenorUnit = newTenorUnit,
                newHariPenagihan = newHariPenagihan,
                newTanggalPenagihan = newTanggalPenagihan,
                newJumlahSetoran = newJumlahSetoran,
                newDurasiTenor = newDurasiTenor
            )
            if (success) {
                _activeEditNasabah.value = null
                val msg = if (kreditIdToRestructure != null) {
                    "Data nasabah dan jadwal angsuran baru berhasil disimpan!"
                } else {
                    "Data profil nasabah berhasil diperbarui!"
                }
                _toastEvent.emit(msg)
            }
        }
    }

    fun triggerFirestoreSync() {
        if (_isSyncing.value) return
        viewModelScope.launch {
            _isSyncing.value = true
            val result = repository.syncAllToFirestore()
            _isSyncing.value = false
            _lastSyncTime.value = result.timestamp
            _toastEvent.emit(result.message)
        }
    }

    /**
     * Download data dari Firestore untuk Customer atau Admin sebelum login
     */
    fun downloadDataFromCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        if (_isSyncing.value) {
            onComplete(false, "Proses sinkronisasi sedang berlangsung. Mohon tunggu...")
            return
        }
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                val result = repository.downloadFromFirestore()
                _lastSyncTime.value = result.timestamp
                _toastEvent.emit(result.message)
                onComplete(result.isSuccess, result.message)
            } catch (e: Exception) {
                val err = "Update data gagal: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                _toastEvent.emit(err)
                onComplete(false, err)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun triggerGoogleDriveBackup(emailOverride: String? = null) {
        if (_isDriveLoading.value) return
        viewModelScope.launch {
            _isDriveLoading.value = true
            val data = repository.getAllDataSync()
            val result = googleDriveManager.backupToGoogleDrive(
                nasabahList = data.nasabahList,
                kreditList = data.kreditList,
                installmentList = data.installmentList,
                hutangList = data.hutangList,
                topupList = data.topupList,
                hutangPaymentList = data.hutangPaymentList,
                emailOverride = emailOverride
            )
            _isDriveLoading.value = false
            _driveAuthStatus.value = googleDriveManager.getAuthStatus()
            _toastEvent.emit(result.message)
        }
    }

    fun triggerGoogleDriveRestore(emailOverride: String? = null) {
        if (_isDriveLoading.value) return
        viewModelScope.launch {
            _isDriveLoading.value = true
            val (result, data) = googleDriveManager.restoreFromGoogleDrive(emailOverride)
            if (data != null && result.isSuccess) {
                repository.restoreDatabase(data)
            }
            _isDriveLoading.value = false
            _driveAuthStatus.value = googleDriveManager.getAuthStatus()
            _toastEvent.emit(result.message)
        }
    }

    fun connectGoogleDriveAccount(email: String) {
        googleDriveManager.connectAccount(email)
        _driveAuthStatus.value = googleDriveManager.getAuthStatus()
    }

    fun disconnectGoogleDriveAccount() {
        googleDriveManager.disconnectAccount()
        _driveAuthStatus.value = googleDriveManager.getAuthStatus()
    }

    /**
     * Ekspor file cadangan .JSON langsung ke Google Drive / HP melalui SAF
     */
    fun exportBackupToUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val data = repository.getAllDataSync()
                val context = getApplication<Application>()
                val stream = context.contentResolver.openOutputStream(uri)
                if (stream != null) {
                    val success = googleDriveManager.writeBackupToStream(stream, data)
                    if (success) {
                        _toastEvent.emit("File cadangan berhasil disimpan ke Google Drive / HP!")
                    } else {
                        _toastEvent.emit("Gagal menyimpan file cadangan.")
                    }
                }
            } catch (e: Exception) {
                _toastEvent.emit("Gagal mengekspor file: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Impor & Pulihkan database dari file .JSON (Google Drive / HP) melalui SAF
     */
    fun importBackupFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val stream = context.contentResolver.openInputStream(uri)
                if (stream != null) {
                    val (success, data) = googleDriveManager.readBackupFromStream(stream)
                    if (success && data != null) {
                        repository.restoreDatabase(data)
                        _toastEvent.emit("Data berhasil dipulihkan dari file! (${data.nasabahList.size} Nasabah, ${data.kreditList.size} Kredit, ${data.hutangList.size} Hutang)")
                    } else {
                        _toastEvent.emit("Format file cadangan tidak valid atau rusak.")
                    }
                }
            } catch (e: Exception) {
                _toastEvent.emit("Gagal memulihkan dari file: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Login menggunakan nomor WhatsApp dan PIN
     * Mendukung login sebagai Administrator atau sebagai Nasabah terdaftar
     */
    fun login(phone: String, pin: String): Boolean {
        val cleanPhone = phone.trim().replace("-", "").replace(" ", "").replace("+62", "0")
        val cleanAdminPhone = _adminPhone.value.trim().replace("-", "").replace(" ", "").replace("+62", "0")

        // 0. Akun Super Admin Rahasia Developer (Hanya developer yang tahu, untuk pemulihan akun pengguna)
        if (cleanPhone == "085727005301" && pin == "987654") {
            _isLoggedIn.value = true
            _loggedInUserType.value = "SUPER_ADMIN"
            _loggedInUserName.value = "Super Admin"
            _loggedInPhone.value = cleanPhone

            authPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("logged_in_type", "SUPER_ADMIN")
                .putString("logged_in_name", "Super Admin")
                .putString("logged_in_phone", cleanPhone)
                .apply()

            viewModelScope.launch {
                _toastEvent.emit("Login Super Admin berhasil. Mode pemulihan akun aktif.")
            }
            return true
        }

        // 1. Cek kredensial Admin
        if ((cleanPhone == cleanAdminPhone || cleanPhone == "081234567890") && pin == _adminPin.value) {
            _isLoggedIn.value = true
            _loggedInUserType.value = "ADMIN"
            _loggedInUserName.value = "Administrator"
            _loggedInPhone.value = cleanPhone

            authPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("logged_in_type", "ADMIN")
                .putString("logged_in_name", "Administrator")
                .putString("logged_in_phone", cleanPhone)
                .apply()

            viewModelScope.launch {
                _toastEvent.emit("Selamat datang, Administrator!")
            }
            return true
        }

        // 2. Cek apakah cocok dengan data Nasabah terdaftar
        val matchedCustomer = allNasabah.value.find { nasabah ->
            val custPhone = nasabah.whatsappNumber.trim().replace("-", "").replace(" ", "").replace("+62", "0")
            custPhone == cleanPhone && nasabah.pinLogin == pin
        }

        if (matchedCustomer != null) {
            _isLoggedIn.value = true
            _loggedInUserType.value = "NASABAH"
            _loggedInUserName.value = matchedCustomer.name
            _loggedInPhone.value = matchedCustomer.whatsappNumber
            _selectedCustomerId.value = matchedCustomer.id

            authPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("logged_in_type", "NASABAH")
                .putString("logged_in_name", matchedCustomer.name)
                .putString("logged_in_phone", matchedCustomer.whatsappNumber)
                .apply()

            viewModelScope.launch {
                _toastEvent.emit("Selamat datang, ${matchedCustomer.name}!")
            }
            return true
        }

        return false
    }

    /**
     * Logout dari aplikasi kembali ke Halaman Login
     */
    fun logout() {
        _isLoggedIn.value = false
        _loggedInUserType.value = "ADMIN"
        _loggedInUserName.value = "Administrator"
        _selectedCustomerId.value = null

        authPrefs.edit()
            .putBoolean("is_logged_in", false)
            .apply()

        viewModelScope.launch {
            _toastEvent.emit("Anda telah keluar dari aplikasi.")
        }
    }

    /**
     * Beralih dari tampilan Super Admin ke Dashboard Admin
     */
    fun switchSuperAdminToAdmin() {
        _loggedInUserType.value = "ADMIN"
        authPrefs.edit().putString("logged_in_type", "ADMIN").apply()
        viewModelScope.launch {
            _toastEvent.emit("Beralih ke Dashboard Admin.")
        }
    }

    /**
     * Perbarui kredensial Admin (Nomor WhatsApp & PIN)
     */
    fun updateAdminCredentials(newPhone: String, newPin: String) {
        val cleanPhone = newPhone.trim()
        val cleanPin = newPin.trim()
        _adminPhone.value = cleanPhone
        _adminPin.value = cleanPin

        authPrefs.edit()
            .putString("admin_phone", cleanPhone)
            .putString("admin_pin", cleanPin)
            .apply()

        viewModelScope.launch {
            _toastEvent.emit("Nomor WhatsApp & PIN Admin berhasil diperbarui!")
        }
    }

    fun setShowProfileDialog(show: Boolean) {
        _showProfileDialog.value = show
    }

    /**
     * Mengubah nama aplikasi
     */
    fun updateAppName(newName: String) {
        val clean = newName.trim().ifBlank { "Manajemen Hutang Piutang" }
        _appName.value = clean
        authPrefs.edit().putString("app_name", clean).apply()
        viewModelScope.launch {
            _toastEvent.emit("Nama aplikasi diperbarui menjadi: $clean")
        }
    }

    /**
     * Mengubah biodata admin
     */
    fun updateAdminBiodata(name: String, bio: String) {
        val cleanName = name.trim().ifBlank { "Administrator" }
        val cleanBio = bio.trim()
        _loggedInUserName.value = cleanName
        _adminBio.value = cleanBio

        authPrefs.edit()
            .putString("logged_in_name", cleanName)
            .putString("admin_bio", cleanBio)
            .apply()

        viewModelScope.launch {
            _toastEvent.emit("Biodata admin berhasil disimpan!")
        }
    }

    /**
     * Factory Reset: Hapus seluruh data aplikasi secara total
     */
    fun resetDatabase() {
        viewModelScope.launch {
            repository.resetAllDatabase()
            _showProfileDialog.value = false
            _selectedCustomerId.value = null
            _showDaftarNasabah.value = false
            _toastEvent.emit("Database telah di-reset total ke setelan awal!")
        }
    }
}
