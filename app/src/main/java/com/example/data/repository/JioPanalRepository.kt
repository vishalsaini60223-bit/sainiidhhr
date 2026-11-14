package com.example.data.repository

import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalDao
import com.example.data.local.NotificationAlert
import com.example.data.local.PartnerAccount
import com.example.data.local.PayoutMethod
import com.example.data.local.ReferralRecord
import com.example.data.local.SupportMessage
import com.example.data.local.TransactionRecord
import com.example.data.local.TransactionStatus
import com.example.data.local.TransactionType
import kotlinx.coroutines.flow.Flow
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

class JioPanalRepository(private val dao: JioPanalDao) {

    fun observePartner(email: String): Flow<PartnerAccount?> = dao.observePartner(email)
    fun observeAllPartners(): Flow<List<PartnerAccount>> = dao.observeAllPartners()
    fun observeTransactionsForUser(email: String): Flow<List<TransactionRecord>> =
        dao.observeTransactionsForUser(email)

    fun observeAllTransactions(): Flow<List<TransactionRecord>> = dao.observeAllTransactions()
    fun observePendingTransactions(): Flow<List<TransactionRecord>> = dao.observePendingTransactions()
    fun observePayoutMethods(email: String): Flow<List<PayoutMethod>> = dao.observePayoutMethods(email)
    fun observeNotifications(email: String): Flow<List<NotificationAlert>> = dao.observeNotifications(email)
    fun observeSupportMessages(email: String): Flow<List<SupportMessage>> = dao.observeSupportMessages(email)
    fun observeAllSupportMessages(): Flow<List<SupportMessage>> = dao.observeAllSupportMessages()
    fun observeReferrals(email: String): Flow<List<ReferralRecord>> = dao.observeReferrals(email)

    suspend fun ensurePartnerInitialized(email: String, defaultCommissionRate: Double): PartnerAccount {
        val normalized = email.trim().lowercase()
        val existing = dao.getPartner(normalized)
        if (existing != null) return existing

        val namePart = normalized.substringBefore("@")
            .split(".", "_", "-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
            .ifBlank { "Jio Partner" }
        val refCode = "JIO-${normalized.take(3).uppercase()}${Random.nextInt(100, 999)}"
        val now = System.currentTimeMillis()
        val account = PartnerAccount(
            email = normalized,
            fullName = namePart,
            phone = "",
            city = "",
            status = AccountStatus.INACTIVE,
            walletBalance = 0.0,
            commissionEarned = 0.0,
            commissionRate = defaultCommissionRate,
            referralCode = refCode,
            createdAt = now,
            updatedAt = now
        )
        dao.upsertPartner(account)
        dao.insertSupportMessage(
            SupportMessage(
                userEmail = normalized,
                sender = "SYSTEM",
                message = "Welcome to JIO PANAL Priority Support! Ask any question here or tap Telegram @Sainishaab9076 for instant desk assistance.",
                timestamp = now
            )
        )
        dao.insertNotification(
            NotificationAlert(
                userEmail = normalized,
                title = "Welcome to JIO PANAL",
                smsBody = "JIO PANAL: Your Gmail ($normalized) is verified. Complete ₹5,000 activation via vishalkumar90@upi to unlock 15% live commission.",
                amount = 0.0,
                alertType = "ACTIVATION",
                timestamp = now
            )
        )
        return account
    }

    suspend fun updatePersonalProfile(email: String, fullName: String, phone: String, city: String) {
        val partner = dao.getPartner(email) ?: return
        dao.upsertPartner(
            partner.copy(
                fullName = fullName.trim(),
                phone = phone.trim(),
                city = city.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun submitActivationRequest(email: String, utr: String): NotificationAlert {
        val cleanUtr = utr.trim()
        val partner = dao.getPartner(email) ?: throw IllegalStateException("Partner not found")
        val now = System.currentTimeMillis()
        dao.upsertPartner(
            partner.copy(
                status = AccountStatus.PENDING,
                activationUtr = cleanUtr,
                updatedAt = now
            )
        )
        val sms = "JIO PANAL Alert: ₹5,000.00 Activation request (UTR: $cleanUtr) submitted to vishalkumar90@upi. Pending Admin approval."
        dao.insertTransaction(
            TransactionRecord(
                userEmail = email,
                type = TransactionType.ACTIVATION,
                amount = 5000.0,
                commissionAmount = 0.0,
                status = TransactionStatus.PENDING,
                title = "Account Activation (₹5,000)",
                referenceUtr = cleanUtr,
                paymentMethod = "UPI (vishalkumar90@upi)",
                smsReceipt = sms,
                timestamp = now
            )
        )
        val alert = NotificationAlert(
            userEmail = email,
            title = "Activation Request Submitted",
            smsBody = sms,
            amount = 5000.0,
            alertType = "ACTIVATION",
            timestamp = now
        )
        dao.insertNotification(alert)
        return alert
    }

    suspend fun processGatewayDeposit(
        email: String,
        amount: Double,
        methodLabel: String,
        utr: String
    ): NotificationAlert {
        val partner = dao.getPartner(email) ?: throw IllegalStateException("Partner not found")
        val rate = partner.commissionRate
        val comm = (amount * rate) / 100.0
        val now = System.currentTimeMillis()
        val formattedAmt = formatCurrency(amount)
        val formattedComm = formatCurrency(comm)

        // Update wallet & commission
        val newBalance = partner.walletBalance + amount + comm
        val newCommEarned = partner.commissionEarned + comm
        val newStatus = if (amount >= 5000.0 && partner.status == AccountStatus.INACTIVE) {
            AccountStatus.ACTIVE
        } else {
            partner.status
        }
        dao.upsertPartner(
            partner.copy(
                status = newStatus,
                walletBalance = newBalance,
                commissionEarned = newCommEarned,
                updatedAt = now
            )
        )

        val sms = "JIO PANAL Credit SMS: A/C credited $formattedAmt + $formattedComm (${rate.toInt()}% Comm). Bal: ${formatCurrency(newBalance)}. Ref: $utr"
        dao.insertTransaction(
            TransactionRecord(
                userEmail = email,
                type = TransactionType.DEPOSIT,
                amount = amount,
                commissionAmount = comm,
                status = TransactionStatus.COMPLETED,
                title = "Deposit via $methodLabel",
                referenceUtr = utr,
                paymentMethod = methodLabel,
                smsReceipt = sms,
                timestamp = now
            )
        )
        dao.insertTransaction(
            TransactionRecord(
                userEmail = email,
                type = TransactionType.COMMISSION,
                amount = comm,
                commissionAmount = comm,
                status = TransactionStatus.COMPLETED,
                title = "${rate.toInt()}% Commission Bonus",
                referenceUtr = "COM-$utr",
                paymentMethod = "Auto 15% Engine",
                smsReceipt = "JIO PANAL Credit SMS: ${rate.toInt()}% Commission of $formattedComm credited instantly! Ref: COM-$utr",
                timestamp = now + 1L
            )
        )
        val alert = NotificationAlert(
            userEmail = email,
            title = "Live Deposit & Credit SMS",
            smsBody = sms,
            amount = amount + comm,
            alertType = "CREDIT",
            timestamp = now
        )
        dao.insertNotification(alert)
        return alert
    }

    suspend fun requestWithdrawal(
        email: String,
        amount: Double,
        payoutLabel: String
    ): NotificationAlert {
        val partner = dao.getPartner(email) ?: throw IllegalStateException("Partner not found")
        require(amount > 0 && amount <= partner.walletBalance) { "Insufficient wallet balance" }
        val now = System.currentTimeMillis()
        val ref = "WD${Random.nextInt(100000, 999999)}"
        val newBalance = partner.walletBalance - amount
        dao.upsertPartner(
            partner.copy(
                walletBalance = newBalance,
                updatedAt = now
            )
        )
        val sms = "JIO PANAL Debit SMS: Withdrawal of ${formatCurrency(amount)} to $payoutLabel submitted (Ref: $ref). Pending Admin payout release."
        dao.insertTransaction(
            TransactionRecord(
                userEmail = email,
                type = TransactionType.WITHDRAWAL,
                amount = amount,
                commissionAmount = 0.0,
                status = TransactionStatus.PENDING,
                title = "Payout to $payoutLabel",
                referenceUtr = ref,
                paymentMethod = payoutLabel,
                smsReceipt = sms,
                timestamp = now
            )
        )
        val alert = NotificationAlert(
            userEmail = email,
            title = "Withdrawal Requested",
            smsBody = sms,
            amount = amount,
            alertType = "DEPOSIT",
            timestamp = now
        )
        dao.insertNotification(alert)
        return alert
    }

    suspend fun adminApproveTransaction(tx: TransactionRecord): NotificationAlert? {
        val now = System.currentTimeMillis()
        dao.updateTransaction(tx.copy(status = TransactionStatus.APPROVED))
        val partner = dao.getPartner(tx.userEmail) ?: return null

        return when (tx.type) {
            TransactionType.ACTIVATION -> {
                val bonus = (tx.amount * partner.commissionRate) / 100.0
                val updatedBalance = partner.walletBalance + tx.amount + bonus
                val updatedComm = partner.commissionEarned + bonus
                dao.upsertPartner(
                    partner.copy(
                        status = AccountStatus.ACTIVE,
                        walletBalance = updatedBalance,
                        commissionEarned = updatedComm,
                        updatedAt = now
                    )
                )
                val sms = "JIO PANAL Credit SMS: Your Account is NOW ACTIVE! ₹5,000.00 + ${formatCurrency(bonus)} (${partner.commissionRate.toInt()}% Comm) credited. UTR: ${tx.referenceUtr}"
                dao.insertTransaction(
                    TransactionRecord(
                        userEmail = tx.userEmail,
                        type = TransactionType.COMMISSION,
                        amount = bonus,
                        commissionAmount = bonus,
                        status = TransactionStatus.COMPLETED,
                        title = "Activation ${partner.commissionRate.toInt()}% Bonus",
                        referenceUtr = "ACT-${tx.referenceUtr}",
                        paymentMethod = "Admin Approved",
                        smsReceipt = sms,
                        timestamp = now
                    )
                )
                val alert = NotificationAlert(
                    userEmail = tx.userEmail,
                    title = "Account Activated & Credited!",
                    smsBody = sms,
                    amount = tx.amount + bonus,
                    alertType = "CREDIT",
                    timestamp = now
                )
                dao.insertNotification(alert)
                alert
            }
            TransactionType.WITHDRAWAL -> {
                val sms = "JIO PANAL Payout SMS: Withdrawal of ${formatCurrency(tx.amount)} (Ref: ${tx.referenceUtr}) has been APPROVED & Settled to your account."
                val alert = NotificationAlert(
                    userEmail = tx.userEmail,
                    title = "Withdrawal Approved",
                    smsBody = sms,
                    amount = tx.amount,
                    alertType = "ADMIN",
                    timestamp = now
                )
                dao.insertNotification(alert)
                alert
            }
            else -> {
                val sms = "JIO PANAL Credit SMS: Transaction ${tx.referenceUtr} for ${formatCurrency(tx.amount)} approved by Admin."
                val alert = NotificationAlert(
                    userEmail = tx.userEmail,
                    title = "Transaction Approved",
                    smsBody = sms,
                    amount = tx.amount,
                    alertType = "ADMIN",
                    timestamp = now
                )
                dao.insertNotification(alert)
                alert
            }
        }
    }

    suspend fun adminRejectTransaction(tx: TransactionRecord): NotificationAlert {
        val now = System.currentTimeMillis()
        dao.updateTransaction(tx.copy(status = TransactionStatus.REJECTED))
        val partner = dao.getPartner(tx.userEmail)
        if (partner != null) {
            if (tx.type == TransactionType.ACTIVATION && partner.status == AccountStatus.PENDING) {
                dao.upsertPartner(partner.copy(status = AccountStatus.INACTIVE, updatedAt = now))
            } else if (tx.type == TransactionType.WITHDRAWAL) {
                dao.upsertPartner(
                    partner.copy(
                        walletBalance = partner.walletBalance + tx.amount,
                        updatedAt = now
                    )
                )
            }
        }
        val sms = "JIO PANAL Alert: Transaction ${tx.referenceUtr} (${formatCurrency(tx.amount)}) was declined by Admin. Contact @Sainishaab9076 for details."
        val alert = NotificationAlert(
            userEmail = tx.userEmail,
            title = "Transaction Declined",
            smsBody = sms,
            amount = tx.amount,
            alertType = "ADMIN",
            timestamp = now
        )
        dao.insertNotification(alert)
        return alert
    }

    suspend fun adminSetPartnerStatus(email: String, newStatus: String) {
        dao.updatePartnerStatus(email, newStatus)
        dao.insertNotification(
            NotificationAlert(
                userEmail = email,
                title = "Account Status Updated",
                smsBody = "JIO PANAL Admin Update: Your partner account status is now $newStatus.",
                alertType = "ADMIN",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun adminCreditBonus(email: String, amount: Double, note: String): NotificationAlert? {
        val partner = dao.getPartner(email) ?: return null
        val now = System.currentTimeMillis()
        val ref = "ADM${Random.nextInt(100000, 999999)}"
        val comm = (amount * partner.commissionRate) / 100.0
        val total = amount + comm
        dao.upsertPartner(
            partner.copy(
                walletBalance = partner.walletBalance + total,
                commissionEarned = partner.commissionEarned + comm,
                status = if (partner.status == AccountStatus.INACTIVE && amount >= 5000.0) AccountStatus.ACTIVE else partner.status,
                updatedAt = now
            )
        )
        val sms = "JIO PANAL Credit SMS: Admin credited ${formatCurrency(amount)} + ${formatCurrency(comm)} (${partner.commissionRate.toInt()}% Comm). Note: $note. Ref: $ref"
        dao.insertTransaction(
            TransactionRecord(
                userEmail = email,
                type = TransactionType.COMMISSION,
                amount = total,
                commissionAmount = comm,
                status = TransactionStatus.COMPLETED,
                title = "Admin Credit: $note",
                referenceUtr = ref,
                paymentMethod = "Admin Direct Credit",
                smsReceipt = sms,
                timestamp = now
            )
        )
        val alert = NotificationAlert(
            userEmail = email,
            title = "Live Credit SMS Received",
            smsBody = sms,
            amount = total,
            alertType = "CREDIT",
            timestamp = now
        )
        dao.insertNotification(alert)
        return alert
    }

    suspend fun updateGlobalCommissionRate(rate: Double) {
        dao.updateAllCommissionRates(rate)
    }

    suspend fun addPayoutMethod(
        email: String,
        methodType: String,
        label: String,
        upiId: String,
        bankName: String,
        accountHolder: String,
        accountNumber: String,
        ifscCode: String,
        makePrimary: Boolean
    ) {
        if (makePrimary) {
            dao.clearPrimaryPayoutMethods(email)
        }
        dao.insertPayoutMethod(
            PayoutMethod(
                userEmail = email,
                methodType = methodType,
                label = label.trim(),
                upiId = upiId.trim(),
                bankName = bankName.trim(),
                accountHolder = accountHolder.trim(),
                accountNumber = accountNumber.trim(),
                ifscCode = ifscCode.trim().uppercase(),
                isPrimary = makePrimary
            )
        )
    }

    suspend fun setPrimaryPayoutMethod(id: Long, email: String) {
        dao.clearPrimaryPayoutMethods(email)
        dao.setPrimaryPayoutMethod(id, email)
    }

    suspend fun deletePayoutMethod(id: Long) {
        dao.deletePayoutMethod(id)
    }

    suspend fun sendSupportMessage(email: String, sender: String, text: String) {
        val now = System.currentTimeMillis()
        dao.insertSupportMessage(
            SupportMessage(
                userEmail = email,
                sender = sender,
                message = text.trim(),
                timestamp = now
            )
        )
        if (sender == "USER") {
            val lower = text.lowercase()
            val autoReply = when {
                "5000" in lower || "activ" in lower || "qr" in lower || "upi" in lower ->
                    "24/7 Desk: To activate your account, pay ₹5,000 to UPI vishalkumar90@upi (or scan the QR in Activate tab) and submit your 12-digit UTR. Admin approves requests quickly. Need urgent help? Tap Telegram @Sainishaab9076."
                "commission" in lower || "15" in lower ->
                    "24/7 Desk: Active partners earn a flat 15% commission automatically on deposits and panel settlements! Track every rupee in the Ledger tab."
                "withdraw" in lower || "payout" in lower || "bank" in lower ->
                    "24/7 Desk: Add your UPI ID or Bank Account in Profile/Payouts, then tap Withdraw on the Home Panel. Payouts are settled promptly after Admin verification."
                else ->
                    "24/7 Support Desk received your message. Our executive team (and Admin @Sainishaab9076 on Telegram) will reply shortly."
            }
            dao.insertSupportMessage(
                SupportMessage(
                    userEmail = email,
                    sender = "SYSTEM",
                    message = autoReply,
                    timestamp = now + 200L
                )
            )
        }
    }

    suspend fun recordSimulatedReferral(referrerEmail: String, friendName: String, friendEmail: String): NotificationAlert? {
        val partner = dao.getPartner(referrerEmail) ?: return null
        val bonus = 750.0 // 15% of ₹5,000 activation
        val now = System.currentTimeMillis()
        dao.insertReferral(
            ReferralRecord(
                referrerEmail = referrerEmail,
                friendName = friendName.trim(),
                friendEmail = friendEmail.trim().lowercase(),
                bonusEarned = bonus,
                status = "ACTIVE",
                joinedAt = now
            )
        )
        dao.upsertPartner(
            partner.copy(
                walletBalance = partner.walletBalance + bonus,
                commissionEarned = partner.commissionEarned + bonus,
                updatedAt = now
            )
        )
        val ref = "REF${Random.nextInt(100000, 999999)}"
        val sms = "JIO PANAL Referral SMS: ${friendName.trim()} activated via your invite! 15% Referral Bonus ${formatCurrency(bonus)} credited. Ref: $ref"
        dao.insertTransaction(
            TransactionRecord(
                userEmail = referrerEmail,
                type = TransactionType.REFERRAL,
                amount = bonus,
                commissionAmount = bonus,
                status = TransactionStatus.COMPLETED,
                title = "Referral Bonus (${friendName.trim()})",
                referenceUtr = ref,
                paymentMethod = "15% Referral Engine",
                smsReceipt = sms,
                timestamp = now
            )
        )
        val alert = NotificationAlert(
            userEmail = referrerEmail,
            title = "Referral Bonus Credited!",
            smsBody = sms,
            amount = bonus,
            alertType = "CREDIT",
            timestamp = now
        )
        dao.insertNotification(alert)
        return alert
    }

    suspend fun markAllNotificationsRead(email: String) {
        dao.markAllNotificationsRead(email)
    }

    suspend fun broadcastAdminNotification(title: String, body: String) {
        dao.insertNotification(
            NotificationAlert(
                userEmail = "ALL",
                title = title.trim(),
                smsBody = body.trim(),
                alertType = "ADMIN",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            return format.format(amount)
        }
    }
}
