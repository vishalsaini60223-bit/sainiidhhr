package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object AccountStatus {
    const val INACTIVE = "INACTIVE"
    const val PENDING = "PENDING"
    const val ACTIVE = "ACTIVE"
    const val SUSPENDED = "SUSPENDED"
}

object TransactionType {
    const val DEPOSIT = "DEPOSIT"
    const val COMMISSION = "COMMISSION"
    const val WITHDRAWAL = "WITHDRAWAL"
    const val ACTIVATION = "ACTIVATION"
    const val REFERRAL = "REFERRAL"
}

object TransactionStatus {
    const val PENDING = "PENDING"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
    const val COMPLETED = "COMPLETED"
}

@Entity(tableName = "partner_accounts")
data class PartnerAccount(
    @PrimaryKey val email: String,
    val fullName: String,
    val phone: String = "",
    val city: String = "",
    val status: String = AccountStatus.INACTIVE,
    val walletBalance: Double = 0.0,
    val commissionEarned: Double = 0.0,
    val commissionRate: Double = 15.0,
    val referralCode: String = "",
    val referredBy: String = "",
    val activationUtr: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val type: String,
    val amount: Double,
    val commissionAmount: Double = 0.0,
    val status: String,
    val title: String,
    val referenceUtr: String,
    val paymentMethod: String,
    val smsReceipt: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "payout_methods")
data class PayoutMethod(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val methodType: String, // "UPI" or "BANK"
    val label: String,
    val upiId: String = "",
    val bankName: String = "",
    val accountHolder: String = "",
    val accountNumber: String = "",
    val ifscCode: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val title: String,
    val smsBody: String,
    val amount: Double = 0.0,
    val alertType: String, // "CREDIT", "DEPOSIT", "ACTIVATION", "ADMIN", "OTP"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_messages")
data class SupportMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val sender: String, // "USER", "ADMIN", "SYSTEM"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "referrals")
data class ReferralRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val referrerEmail: String,
    val friendName: String,
    val friendEmail: String,
    val bonusEarned: Double,
    val status: String, // "ACTIVE", "PENDING"
    val joinedAt: Long = System.currentTimeMillis()
)
