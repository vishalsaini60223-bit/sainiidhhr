package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JioPanalDao {

    // Partner Account Queries
    @Query("SELECT * FROM partner_accounts WHERE email = :email LIMIT 1")
    fun observePartner(email: String): Flow<PartnerAccount?>

    @Query("SELECT * FROM partner_accounts WHERE email = :email LIMIT 1")
    suspend fun getPartner(email: String): PartnerAccount?

    @Query("SELECT * FROM partner_accounts ORDER BY updatedAt DESC")
    fun observeAllPartners(): Flow<List<PartnerAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPartner(partner: PartnerAccount)

    @Query("UPDATE partner_accounts SET status = :status, updatedAt = :timestamp WHERE email = :email")
    suspend fun updatePartnerStatus(email: String, status: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE partner_accounts SET commissionRate = :rate, updatedAt = :timestamp")
    suspend fun updateAllCommissionRates(rate: Double, timestamp: Long = System.currentTimeMillis())

    // Transactions
    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY timestamp DESC")
    fun observeTransactionsForUser(email: String): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<TransactionRecord>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDING' ORDER BY timestamp DESC")
    fun observePendingTransactions(): Flow<List<TransactionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionRecord): Long

    @Update
    suspend fun updateTransaction(tx: TransactionRecord)

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionRecord?

    // Payout Methods
    @Query("SELECT * FROM payout_methods WHERE userEmail = :email ORDER BY isPrimary DESC, createdAt DESC")
    fun observePayoutMethods(email: String): Flow<List<PayoutMethod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutMethod(method: PayoutMethod): Long

    @Query("UPDATE payout_methods SET isPrimary = 0 WHERE userEmail = :email")
    suspend fun clearPrimaryPayoutMethods(email: String)

    @Query("UPDATE payout_methods SET isPrimary = 1 WHERE id = :id AND userEmail = :email")
    suspend fun setPrimaryPayoutMethod(id: Long, email: String)

    @Query("DELETE FROM payout_methods WHERE id = :id")
    suspend fun deletePayoutMethod(id: Long)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userEmail = :email OR userEmail = 'ALL' ORDER BY timestamp DESC")
    fun observeNotifications(email: String): Flow<List<NotificationAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(alert: NotificationAlert): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userEmail = :email OR userEmail = 'ALL'")
    suspend fun markAllNotificationsRead(email: String)

    // Support Chat
    @Query("SELECT * FROM support_messages WHERE userEmail = :email ORDER BY timestamp ASC")
    fun observeSupportMessages(email: String): Flow<List<SupportMessage>>

    @Query("SELECT * FROM support_messages ORDER BY timestamp DESC")
    fun observeAllSupportMessages(): Flow<List<SupportMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportMessage(message: SupportMessage): Long

    // Referrals
    @Query("SELECT * FROM referrals WHERE referrerEmail = :email ORDER BY joinedAt DESC")
    fun observeReferrals(email: String): Flow<List<ReferralRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralRecord): Long
}
