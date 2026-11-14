package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PartnerAccount::class,
        TransactionRecord::class,
        PayoutMethod::class,
        NotificationAlert::class,
        SupportMessage::class,
        ReferralRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JioPanalDatabase : RoomDatabase() {
    abstract fun dao(): JioPanalDao

    companion object {
        @Volatile
        private var INSTANCE: JioPanalDatabase? = null

        fun getInstance(context: Context): JioPanalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JioPanalDatabase::class.java,
                    "jio_panal_vault.db"
                )
                    .addCallback(SeedCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.dao()?.let { dao ->
                    val now = System.currentTimeMillis()
                    // Seed sample partner network entries so Admin Panel has live queue items immediately
                    dao.upsertPartner(
                        PartnerAccount(
                            email = "rohit.sharma99@gmail.com",
                            fullName = "Rohit Sharma",
                            phone = "+91 98765 43210",
                            city = "Jaipur",
                            status = AccountStatus.PENDING,
                            walletBalance = 0.0,
                            commissionEarned = 0.0,
                            commissionRate = 15.0,
                            referralCode = "JIO-ROH99",
                            activationUtr = "431829047512",
                            createdAt = now - 3600_000L,
                            updatedAt = now - 1800_000L
                        )
                    )
                    dao.upsertPartner(
                        PartnerAccount(
                            email = "ankit.verma07@gmail.com",
                            fullName = "Ankit Verma",
                            phone = "+91 98110 22334",
                            city = "Delhi",
                            status = AccountStatus.ACTIVE,
                            walletBalance = 14500.0,
                            commissionEarned = 2175.0,
                            commissionRate = 15.0,
                            referralCode = "JIO-ANK07",
                            activationUtr = "431800112233",
                            createdAt = now - 86_400_000L,
                            updatedAt = now - 7200_000L
                        )
                    )
                    dao.insertTransaction(
                        TransactionRecord(
                            userEmail = "rohit.sharma99@gmail.com",
                            type = TransactionType.ACTIVATION,
                            amount = 5000.0,
                            commissionAmount = 0.0,
                            status = TransactionStatus.PENDING,
                            title = "Account Activation Deposit (₹5,000)",
                            referenceUtr = "431829047512",
                            paymentMethod = "UPI (vishalkumar90@upi)",
                            smsReceipt = "JIO PANAL: Activation request of INR 5,000.00 via UPI UTR 431829047512 is awaiting Admin approval.",
                            timestamp = now - 1800_000L
                        )
                    )
                    dao.insertTransaction(
                        TransactionRecord(
                            userEmail = "ankit.verma07@gmail.com",
                            type = TransactionType.COMMISSION,
                            amount = 10000.0,
                            commissionAmount = 1500.0,
                            status = TransactionStatus.COMPLETED,
                            title = "15% Partner Commission Credited",
                            referenceUtr = "431800998877",
                            paymentMethod = "Live Panel Settlement",
                            smsReceipt = "JIO PANAL Credit SMS: Your A/C is credited with INR 1,500.00 (15% Comm on INR 10,000). Ref: 431800998877.",
                            timestamp = now - 7200_000L
                        )
                    )
                }
            }
        }
    }
}
