package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.JioPanalDatabase
import com.example.data.local.NotificationAlert
import com.example.data.local.PartnerAccount
import com.example.data.local.PayoutMethod
import com.example.data.local.ReferralRecord
import com.example.data.local.SupportMessage
import com.example.data.local.TransactionRecord
import com.example.data.repository.JioPanalRepository
import com.example.util.AppLanguage
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class JioPanalViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext: Context = application.applicationContext
    private val prefs = AppPreferences(appContext)
    private val dao = JioPanalDatabase.getInstance(appContext).dao()
    private val repository = JioPanalRepository(dao)

    // Splash & Security Gates
    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    private val _biometricSessionUnlocked = MutableStateFlow(false)
    val biometricSessionUnlocked: StateFlow<Boolean> = _biometricSessionUnlocked.asStateFlow()

    private val _isAdminUnlocked = MutableStateFlow(false)
    val isAdminUnlocked: StateFlow<Boolean> = _isAdminUnlocked.asStateFlow()

    // OTP Login State
    private val _pendingOtpEmail = MutableStateFlow<String?>(null)
    val pendingOtpEmail: StateFlow<String?> = _pendingOtpEmail.asStateFlow()

    private val _activeOtpCode = MutableStateFlow<String?>(null)
    val activeOtpCode: StateFlow<String?> = _activeOtpCode.asStateFlow()

    private val _otpCountdown = MutableStateFlow(0)
    val otpCountdown: StateFlow<Int> = _otpCountdown.asStateFlow()

    private var otpTimerJob: Job? = null

    // Live SMS Toast Banner
    private val _liveSmsBanner = MutableStateFlow<NotificationAlert?>(null)
    val liveSmsBanner: StateFlow<NotificationAlert?> = _liveSmsBanner.asStateFlow()
    private var bannerJob: Job? = null

    // Feedback Message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Preferences Flows
    val isDarkMode: StateFlow<Boolean> = prefs.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val language: StateFlow<AppLanguage> = prefs.languageCode
        .combine(MutableStateFlow(Unit)) { code, _ -> AppLanguage.fromCode(code) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    val biometricEnabled: StateFlow<Boolean> = prefs.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val loggedInEmail: StateFlow<String?> = prefs.loggedInEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminPasskey: StateFlow<String> = prefs.adminPasskey
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SAINI@9076")

    val globalCommissionRate: StateFlow<Double> = prefs.commissionRate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15.0)

    // User-scoped Reactive Room Flows
    val currentPartner: StateFlow<PartnerAccount?> = loggedInEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(null) else repository.observePartner(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userTransactions: StateFlow<List<TransactionRecord>> = loggedInEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.observeTransactionsForUser(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payoutMethods: StateFlow<List<PayoutMethod>> = loggedInEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.observePayoutMethods(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationAlert>> = loggedInEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.observeNotifications(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supportMessages: StateFlow<List<SupportMessage>> = loggedInEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.observeSupportMessages(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referrals: StateFlow<List<ReferralRecord>> = loggedInEmail
        .flatMapLatest { email ->
            if (email.isNullOrBlank()) flowOf(emptyList()) else repository.observeReferrals(email)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin Reactive Room Flows
    val allPartners: StateFlow<List<PartnerAccount>> = repository.observeAllPartners()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<TransactionRecord>> = repository.observePendingTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionRecord>> = repository.observeAllTransactions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSupportMessages: StateFlow<List<SupportMessage>> = repository.observeAllSupportMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        NotificationHelper.ensureChannel(appContext)
        viewModelScope.launch {
            delay(1500)
            _splashFinished.value = true
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun dismissLiveSmsBanner() {
        _liveSmsBanner.value = null
    }

    private fun triggerLiveAlertBanner(alert: NotificationAlert) {
        NotificationHelper.showLiveAlert(appContext, alert.title, alert.smsBody)
        _liveSmsBanner.value = alert
        bannerJob?.cancel()
        bannerJob = viewModelScope.launch {
            delay(6500)
            _liveSmsBanner.value = null
        }
    }

    // Gmail + OTP Login
    fun sendGmailOtp(rawEmail: String): Boolean {
        val email = rawEmail.trim().lowercase()
        if (!email.endsWith("@gmail.com") || email.substringBefore("@").length < 3) {
            _toastMessage.value = "Please enter a valid @gmail.com address"
            return false
        }
        val otp = Random.nextInt(100000, 999999).toString()
        _pendingOtpEmail.value = email
        _activeOtpCode.value = otp
        startOtpTimer()
        NotificationHelper.showLiveAlert(
            appContext,
            "JIO PANAL Gmail OTP: $otp",
            "Your 6-digit login verification OTP for $email is $otp. Valid for 2 minutes."
        )
        return true
    }

    fun resendGmailOtp() {
        val email = _pendingOtpEmail.value ?: return
        sendGmailOtp(email)
    }

    fun cancelOtpStep() {
        _pendingOtpEmail.value = null
        _activeOtpCode.value = null
        otpTimerJob?.cancel()
        _otpCountdown.value = 0
    }

    private fun startOtpTimer() {
        otpTimerJob?.cancel()
        _otpCountdown.value = 60
        otpTimerJob = viewModelScope.launch {
            while (_otpCountdown.value > 0) {
                delay(1000)
                _otpCountdown.value = _otpCountdown.value - 1
            }
        }
    }

    fun verifyGmailOtp(enteredOtp: String): Boolean {
        val expected = _activeOtpCode.value
        val email = _pendingOtpEmail.value
        if (expected == null || email == null) return false
        if (enteredOtp.trim() != expected) {
            _toastMessage.value = "Invalid OTP code. Please check the 6-digit OTP."
            return false
        }
        viewModelScope.launch {
            repository.ensurePartnerInitialized(email, globalCommissionRate.value)
            prefs.setLoggedInEmail(email)
            _biometricSessionUnlocked.value = true
            _pendingOtpEmail.value = null
            _activeOtpCode.value = null
            otpTimerJob?.cancel()
            _toastMessage.value = "Verified! Welcome to JIO PANAL"
        }
        return true
    }

    fun unlockBiometricSession() {
        _biometricSessionUnlocked.value = true
    }

    fun signOut() {
        viewModelScope.launch {
            prefs.setLoggedInEmail(null)
            _biometricSessionUnlocked.value = false
            _isAdminUnlocked.value = false
        }
    }

    // Account Activation (₹5,000 + UTR)
    fun submitActivationUtr(utr: String) {
        val email = loggedInEmail.value ?: return
        val clean = utr.trim()
        if (clean.length < 6) {
            _toastMessage.value = "Please enter a valid UTR / Reference ID (min 6 characters)"
            return
        }
        viewModelScope.launch {
            val alert = repository.submitActivationRequest(email, clean)
            triggerLiveAlertBanner(alert)
            _toastMessage.value = "Activation UTR submitted! Awaiting Admin approval."
        }
    }

    // Payment Gateway Deposit
    fun processDeposit(amount: Double, methodLabel: String) {
        val email = loggedInEmail.value ?: return
        if (amount <= 0) {
            _toastMessage.value = "Enter a valid deposit amount"
            return
        }
        val utr = "JP${Random.nextInt(10000000, 99999999)}"
        viewModelScope.launch {
            val alert = repository.processGatewayDeposit(email, amount, methodLabel, utr)
            triggerLiveAlertBanner(alert)
        }
    }

    // Withdrawals & Payout Methods
    fun requestWithdrawal(amount: Double, payoutMethod: PayoutMethod) {
        val email = loggedInEmail.value ?: return
        val balance = currentPartner.value?.walletBalance ?: 0.0
        if (amount <= 0 || amount > balance) {
            _toastMessage.value = "Enter an amount within your available balance"
            return
        }
        val label = if (payoutMethod.methodType == "UPI") {
            "${payoutMethod.label} (${payoutMethod.upiId})"
        } else {
            "${payoutMethod.bankName} ••${payoutMethod.accountNumber.takeLast(4)}"
        }
        viewModelScope.launch {
            val alert = repository.requestWithdrawal(email, amount, label)
            triggerLiveAlertBanner(alert)
        }
    }

    fun addPayoutMethod(
        methodType: String,
        label: String,
        upiId: String,
        bankName: String,
        accountHolder: String,
        accountNumber: String,
        ifscCode: String,
        makePrimary: Boolean
    ) {
        val email = loggedInEmail.value ?: return
        viewModelScope.launch {
            repository.addPayoutMethod(
                email = email,
                methodType = methodType,
                label = label,
                upiId = upiId,
                bankName = bankName,
                accountHolder = accountHolder,
                accountNumber = accountNumber,
                ifscCode = ifscCode,
                makePrimary = makePrimary
            )
            _toastMessage.value = "Payout account saved offline & synced!"
        }
    }

    fun setPrimaryPayoutMethod(id: Long) {
        val email = loggedInEmail.value ?: return
        viewModelScope.launch {
            repository.setPrimaryPayoutMethod(id, email)
        }
    }

    fun deletePayoutMethod(id: Long) {
        viewModelScope.launch {
            repository.deletePayoutMethod(id)
        }
    }

    // Profile & Preferences
    fun updatePersonalDetails(fullName: String, phone: String, city: String) {
        val email = loggedInEmail.value ?: return
        viewModelScope.launch {
            repository.updatePersonalProfile(email, fullName, phone, city)
            _toastMessage.value = "Personal details updated"
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch { prefs.setLanguageCode(lang.code) }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setBiometricEnabled(enabled) }
    }

    // Notifications & Support & Referrals
    fun markNotificationsRead() {
        val email = loggedInEmail.value ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(email) }
    }

    fun sendSupportMessage(text: String) {
        val email = loggedInEmail.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendSupportMessage(email, "USER", text)
        }
    }

    fun addDemoReferral(friendName: String, friendEmail: String) {
        val email = loggedInEmail.value ?: return
        if (friendName.isBlank() || friendEmail.isBlank()) {
            _toastMessage.value = "Enter friend name and Gmail"
            return
        }
        viewModelScope.launch {
            val alert = repository.recordSimulatedReferral(email, friendName, friendEmail)
            if (alert != null) {
                triggerLiveAlertBanner(alert)
            }
        }
    }

    // Admin Secure Control Vault
    fun unlockAdmin(passkeyInput: String): Boolean {
        return if (passkeyInput == adminPasskey.value) {
            _isAdminUnlocked.value = true
            true
        } else {
            _toastMessage.value = "Incorrect Admin Passkey"
            false
        }
    }

    fun lockAdmin() {
        _isAdminUnlocked.value = false
    }

    fun adminApproveTransaction(tx: TransactionRecord) {
        viewModelScope.launch {
            val alert = repository.adminApproveTransaction(tx)
            if (alert != null) {
                triggerLiveAlertBanner(alert)
            }
            _toastMessage.value = "Approved ${tx.referenceUtr}"
        }
    }

    fun adminRejectTransaction(tx: TransactionRecord) {
        viewModelScope.launch {
            val alert = repository.adminRejectTransaction(tx)
            triggerLiveAlertBanner(alert)
            _toastMessage.value = "Declined ${tx.referenceUtr}"
        }
    }

    fun adminSetPartnerStatus(email: String, status: String) {
        viewModelScope.launch {
            repository.adminSetPartnerStatus(email, status)
            _toastMessage.value = "$email set to $status"
        }
    }

    fun adminCreditPartner(email: String, amount: Double, note: String) {
        viewModelScope.launch {
            val alert = repository.adminCreditBonus(email, amount, note.ifBlank { "Panel Bonus" })
            if (alert != null) {
                triggerLiveAlertBanner(alert)
            }
        }
    }

    fun adminUpdateCommissionRate(rate: Double) {
        viewModelScope.launch {
            prefs.setCommissionRate(rate)
            repository.updateGlobalCommissionRate(rate)
            _toastMessage.value = "Commission rate updated to ${rate.toInt()}%"
        }
    }

    fun adminUpdatePasskey(newPasskey: String) {
        if (newPasskey.length < 4) {
            _toastMessage.value = "Passkey must be at least 4 characters"
            return
        }
        viewModelScope.launch {
            prefs.setAdminPasskey(newPasskey)
            _toastMessage.value = "Admin passkey updated"
        }
    }

    fun adminReplyToUser(targetEmail: String, message: String) {
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.sendSupportMessage(targetEmail, "ADMIN", message)
            _toastMessage.value = "Reply sent to $targetEmail"
        }
    }

    fun adminBroadcastAlert(title: String, body: String) {
        if (title.isBlank() || body.isBlank()) return
        viewModelScope.launch {
            repository.broadcastAdminNotification(title, body)
            NotificationHelper.showLiveAlert(appContext, title, body)
            _toastMessage.value = "Broadcast sent to all partners"
        }
    }
}
