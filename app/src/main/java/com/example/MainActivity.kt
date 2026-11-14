package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.LiveSmsAlertBanner
import com.example.ui.screens.AccountActivationSheet
import com.example.ui.screens.AddPayoutMethodSheet
import com.example.ui.screens.AdminControlPanelScreen
import com.example.ui.screens.AdminPasskeyDialog
import com.example.ui.screens.BiometricLockScreen
import com.example.ui.screens.GmailOtpAuthScreen
import com.example.ui.screens.HomePanelScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.LuxurySplashScreen
import com.example.ui.screens.NotificationsSheet
import com.example.ui.screens.PaymentGatewaySheet
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.WithdrawSheet
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.JioPanalTheme
import com.example.ui.viewmodel.JioPanalViewModel
import com.example.util.BiometricHelper
import com.example.util.stringsFor

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JioPanalApp(activity = this)
        }
    }
}

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JioPanalApp(
    activity: FragmentActivity,
    viewModel: JioPanalViewModel = viewModel()
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()
    val strings = remember(language) { stringsFor(language) }

    val splashFinished by viewModel.splashFinished.collectAsStateWithLifecycle()
    val loggedInEmail by viewModel.loggedInEmail.collectAsStateWithLifecycle()
    val biometricEnabled by viewModel.biometricEnabled.collectAsStateWithLifecycle()
    val biometricUnlocked by viewModel.biometricSessionUnlocked.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()

    val pendingOtpEmail by viewModel.pendingOtpEmail.collectAsStateWithLifecycle()
    val activeOtpCode by viewModel.activeOtpCode.collectAsStateWithLifecycle()
    val otpCountdown by viewModel.otpCountdown.collectAsStateWithLifecycle()
    val liveSmsBanner by viewModel.liveSmsBanner.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    val partner by viewModel.currentPartner.collectAsStateWithLifecycle()
    val transactions by viewModel.userTransactions.collectAsStateWithLifecycle()
    val payoutMethods by viewModel.payoutMethods.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val supportMessages by viewModel.supportMessages.collectAsStateWithLifecycle()
    val referrals by viewModel.referrals.collectAsStateWithLifecycle()
    val allPartners by viewModel.allPartners.collectAsStateWithLifecycle()
    val pendingTransactions by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val allSupportMessages by viewModel.allSupportMessages.collectAsStateWithLifecycle()
    val commissionRate by viewModel.globalCommissionRate.collectAsStateWithLifecycle()

    var showAdminDialog by remember { mutableStateOf(false) }
    var showActivationSheet by remember { mutableStateOf(false) }
    var showGatewaySheet by remember { mutableStateOf(false) }
    var showWithdrawSheet by remember { mutableStateOf(false) }
    var showAddPayoutSheet by remember { mutableStateOf(false) }
    var showNotificationsSheet by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(loggedInEmail) {
        if (loggedInEmail != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearToast()
        }
    }

    JioPanalTheme(darkTheme = isDarkMode) {
        if (!splashFinished) {
            LuxurySplashScreen(strings = strings)
        } else if (isAdminUnlocked) {
            BackHandler { viewModel.lockAdmin() }
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LiveSmsAlertBanner(
                        alert = liveSmsBanner,
                        strings = strings,
                        onDismiss = viewModel::dismissLiveSmsBanner
                    )
                    AdminControlPanelScreen(
                        pendingTransactions = pendingTransactions,
                        allPartners = allPartners,
                        allSupportMessages = allSupportMessages,
                        commissionRate = commissionRate,
                        strings = strings,
                        onApproveTx = viewModel::adminApproveTransaction,
                        onRejectTx = viewModel::adminRejectTransaction,
                        onSetPartnerStatus = viewModel::adminSetPartnerStatus,
                        onCreditPartner = viewModel::adminCreditPartner,
                        onUpdateCommissionRate = viewModel::adminUpdateCommissionRate,
                        onUpdatePasskey = viewModel::adminUpdatePasskey,
                        onReplySupport = viewModel::adminReplyToUser,
                        onBroadcastAlert = viewModel::adminBroadcastAlert,
                        onLockAdmin = viewModel::lockAdmin
                    )
                }
            }
        } else {
            val email = loggedInEmail
            if (email == null) {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    GmailOtpAuthScreen(
                        strings = strings,
                        currentLanguage = language,
                        pendingOtpEmail = pendingOtpEmail,
                        activeOtpCode = activeOtpCode,
                        otpCountdown = otpCountdown,
                        onSelectLanguage = viewModel::setLanguage,
                        onSendOtp = viewModel::sendGmailOtp,
                        onResendOtp = viewModel::resendGmailOtp,
                        onVerifyOtp = viewModel::verifyGmailOtp,
                        onChangeEmail = viewModel::cancelOtpStep,
                        onOpenAdminGate = { showAdminDialog = true }
                    )
                }
            }
        } else if (biometricEnabled && !biometricUnlocked) {
            BiometricLockScreen(
                email = email,
                strings = strings,
                onTriggerBiometric = {
                    BiometricHelper.showBiometricPrompt(
                        activity = activity,
                        title = strings.biometricTitle,
                        subtitle = strings.biometricSubtitle,
                        onSuccess = { viewModel.unlockBiometricSession() },
                        onFallbackOrError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onQuickUnlock = { viewModel.unlockBiometricSession() },
                onSignOut = viewModel::signOut
            )
        } else {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "panel"
            val unreadCount = notifications.count { !it.isRead }

            val destinations = listOf(
                NavDestination("panel", strings.navHome, Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_panel"),
                NavDestination("ledger", strings.navHistory, Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, "nav_ledger"),
                NavDestination("refer", strings.navNetwork, Icons.Filled.CardGiftcard, Icons.Outlined.CardGiftcard, "nav_refer"),
                NavDestination("support", strings.navSupport, Icons.Filled.SupportAgent, Icons.Outlined.SupportAgent, "nav_support"),
                NavDestination("profile", strings.navProfile, Icons.Filled.Person, Icons.Outlined.Person, "nav_profile")
            )

            val navigateTab: (String) -> Unit = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useRail = maxWidth >= 600.dp

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        painter = painterResource(id = R.drawable.jio_panal_icon_1794621620253),
                                        contentDescription = "Jio Panal Emblem",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .border(1.dp, ChampagneGold, RoundedCornerShape(10.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = strings.appTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${commissionRate.toInt()}% Partner Vault",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { showNotificationsSheet = true },
                                    modifier = Modifier.testTag("top_notifications_button")
                                ) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge { Text(unreadCount.toString()) }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Real-time notifications"
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { showAdminDialog = true },
                                    modifier = Modifier.testTag("top_admin_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = "Admin Vault",
                                        tint = ChampagneGold
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        if (!useRail) {
                            NavigationBar(
                                windowInsets = WindowInsets.navigationBars,
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                destinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { navigateTab(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label, maxLines = 1) },
                                        modifier = Modifier.testTag(dest.tag)
                                    )
                                }
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useRail) {
                            NavigationRail(
                                modifier = Modifier.fillMaxHeight(),
                                containerColor = MaterialTheme.colorScheme.surface
                            ) {
                                destinations.forEach { dest ->
                                    val selected = currentRoute == dest.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { navigateTab(dest.route) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.label
                                            )
                                        },
                                        label = { Text(dest.label) },
                                        modifier = Modifier.testTag(dest.tag)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            LiveSmsAlertBanner(
                                alert = liveSmsBanner,
                                strings = strings,
                                onDismiss = viewModel::dismissLiveSmsBanner
                            )
                            NavHost(
                                navController = navController,
                                startDestination = "panel",
                                modifier = Modifier.weight(1f)
                            ) {
                                composable("panel") {
                                    HomePanelScreen(
                                        partner = partner,
                                        transactions = transactions,
                                        notifications = notifications,
                                        strings = strings,
                                        onOpenActivate = { showActivationSheet = true },
                                        onOpenDepositGateway = { showGatewaySheet = true },
                                        onOpenWithdraw = { showWithdrawSheet = true },
                                        onOpenAddPayoutAccount = { showAddPayoutSheet = true },
                                        onOpenLedger = { navigateTab("ledger") }
                                    )
                                }
                                composable("ledger") {
                                    LedgerScreen(transactions = transactions, strings = strings)
                                }
                                composable("refer") {
                                    ReferralScreen(
                                        partner = partner,
                                        referrals = referrals,
                                        strings = strings,
                                        onSimulateInvite = viewModel::addDemoReferral
                                    )
                                }
                                composable("support") {
                                    SupportScreen(
                                        messages = supportMessages,
                                        strings = strings,
                                        onSendMessage = viewModel::sendSupportMessage
                                    )
                                }
                                composable("profile") {
                                    ProfileScreen(
                                        partner = partner,
                                        payoutMethods = payoutMethods,
                                        isDarkMode = isDarkMode,
                                        biometricEnabled = biometricEnabled,
                                        currentLanguage = language,
                                        strings = strings,
                                        onUpdateProfile = viewModel::updatePersonalDetails,
                                        onOpenAddPayout = { showAddPayoutSheet = true },
                                        onSetPrimaryPayout = viewModel::setPrimaryPayoutMethod,
                                        onDeletePayout = viewModel::deletePayoutMethod,
                                        onToggleDarkMode = viewModel::setDarkMode,
                                        onToggleBiometric = viewModel::setBiometricEnabled,
                                        onSelectLanguage = viewModel::setLanguage,
                                        onOpenAdminVault = { showAdminDialog = true },
                                        onSignOut = viewModel::signOut
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showActivationSheet) {
                AccountActivationSheet(
                    partner = partner,
                    strings = strings,
                    onDismiss = { showActivationSheet = false },
                    onSubmitUtr = viewModel::submitActivationUtr
                )
            }
            if (showGatewaySheet) {
                PaymentGatewaySheet(
                    commissionRate = commissionRate,
                    onDismiss = { showGatewaySheet = false },
                    onCompleteDeposit = viewModel::processDeposit
                )
            }
            if (showAddPayoutSheet) {
                AddPayoutMethodSheet(
                    onDismiss = { showAddPayoutSheet = false },
                    onSaveMethod = viewModel::addPayoutMethod
                )
            }
            if (showWithdrawSheet) {
                WithdrawSheet(
                    availableBalance = partner?.walletBalance ?: 0.0,
                    payoutMethods = payoutMethods,
                    onDismiss = { showWithdrawSheet = false },
                    onOpenAddPayout = { showAddPayoutSheet = true },
                    onRequestWithdraw = viewModel::requestWithdrawal
                )
            }
            if (showNotificationsSheet) {
                NotificationsSheet(
                    notifications = notifications,
                    onDismiss = { showNotificationsSheet = false },
                    onMarkAllRead = viewModel::markNotificationsRead
                )
            }
        }
        }

        if (showAdminDialog) {
            AdminPasskeyDialog(
                onDismiss = { showAdminDialog = false },
                onUnlock = viewModel::unlockAdmin
            )
        }
    }
}
