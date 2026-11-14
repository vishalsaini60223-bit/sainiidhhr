package com.example

import android.content.Context
import android.os.Looper
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AccountStatus
import com.example.data.local.JioPanalDatabase
import com.example.data.local.TransactionStatus
import com.example.data.repository.JioPanalRepository
import com.example.ui.components.LiveTickerGenerator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private lateinit var db: JioPanalDatabase
    private lateinit var repository: JioPanalRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, JioPanalDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = JioPanalRepository(db.dao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `main activity launches renders splash gmail otp dashboard activation qr and admin vault`() {
        // Advance main looper past 1500ms splash delay
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(2000))
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("gmail_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("gmail_input").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("gmail_input").performTextInput("testpartner@gmail.com")
        composeRule.onNodeWithTag("send_otp_button").performScrollTo().performClick()

        // Allow AnimatedContent transition to settle and locate OTP field
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("otp_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("otp_input").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Auto-Fill").performScrollTo().performClick()
        composeRule.onNodeWithTag("verify_otp_button").performScrollTo().performClick()

        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("hero_balance_card").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("live_marquee_ticker").assertIsDisplayed()
        composeRule.onNodeWithTag("hero_balance_card").assertIsDisplayed()

        // Scroll to Live ₹10K+ Deposit & Withdrawal Feed Card and verify it renders
        composeRule.onNodeWithTag("home_panel_list").performScrollToNode(hasTestTag("live_orders_feed_card"))
        composeRule.onNodeWithTag("live_orders_feed_card").assertIsDisplayed()

        // Verify Account Activation ₹5,000 + QR modal opens cleanly
        composeRule.onNodeWithTag("home_panel_list").performScrollToNode(hasTestTag("activation_banner_card"))
        composeRule.onNodeWithTag("activation_banner_card").performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("activation_utr_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("activation_utr_input").performScrollTo().performTextInput("431855667788")
        composeRule.onNodeWithTag("submit_activation_utr_button").performScrollTo().performClick()

        // Verify Admin Vault Passkey Unlock
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("top_admin_button").performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("admin_passkey_input").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("admin_passkey_input").performTextInput("SAINI@9076")
        composeRule.onNodeWithTag("unlock_admin_confirm_button").performClick()
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithTag("lock_admin_button").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("lock_admin_button").assertIsDisplayed()
    }

    @Test
    fun `live ticker generator always produces deposit and withdrawal orders above 10000`() {
        val orders = (1L..100L).map { id ->
            LiveTickerGenerator.createOrder(id = id, commissionRate = 15.0)
        }
        assertTrue(orders.all { it.amount > 10000.0 })
        assertTrue(orders.any { it.isDeposit && it.commission > 1500.0 })
        assertTrue(orders.any { !it.isDeposit && it.commission == 0.0 })
    }

    @Test
    fun `activation utr submission and admin manual approval unlocks active status and 15 percent commission`() = runBlocking {
        val email = "partner@gmail.com"
        val initial = repository.ensurePartnerInitialized(email, 15.0)
        assertEquals(AccountStatus.INACTIVE, initial.status)

        repository.submitActivationRequest(email, "431899887766")
        val pendingPartner = repository.observePartner(email).first()
        assertEquals(AccountStatus.PENDING, pendingPartner?.status)

        val pendingTx = repository.observePendingTransactions().first()
            .first { it.userEmail == email }
        assertEquals(5000.0, pendingTx.amount, 0.01)

        val alert = repository.adminApproveTransaction(pendingTx)
        assertTrue(alert?.smsBody?.contains("ACTIVE") == true)

        val activePartner = repository.observePartner(email).first()
        assertEquals(AccountStatus.ACTIVE, activePartner?.status)
        assertEquals(5750.0, activePartner?.walletBalance ?: 0.0, 0.01)
        assertEquals(750.0, activePartner?.commissionEarned ?: 0.0, 0.01)

        val userTxs = repository.observeTransactionsForUser(email).first()
        assertTrue(userTxs.any { it.status == TransactionStatus.APPROVED })
    }
}
