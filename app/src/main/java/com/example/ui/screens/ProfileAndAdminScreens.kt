package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.AccountStatus
import com.example.data.local.PartnerAccount
import com.example.data.local.PayoutMethod
import com.example.data.local.SupportMessage
import com.example.data.local.TransactionRecord
import com.example.data.repository.JioPanalRepository
import com.example.ui.components.LanguageSelectorRow
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.CrimsonError
import com.example.ui.theme.EmeraldAccent
import com.example.util.AppLanguage
import com.example.util.LocalizedStrings

@Composable
fun ProfileScreen(
    partner: PartnerAccount?,
    payoutMethods: List<PayoutMethod>,
    isDarkMode: Boolean,
    biometricEnabled: Boolean,
    currentLanguage: AppLanguage,
    strings: LocalizedStrings,
    onUpdateProfile: (String, String, String) -> Unit,
    onOpenAddPayout: () -> Unit,
    onSetPrimaryPayout: (Long) -> Unit,
    onDeletePayout: (Long) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onOpenAdminVault: () -> Unit,
    onSignOut: () -> Unit
) {
    var name by remember(partner?.fullName) { mutableStateOf(partner?.fullName ?: "") }
    var phone by remember(partner?.phone) { mutableStateOf(partner?.phone ?: "") }
    var city by remember(partner?.city) { mutableStateOf(partner?.city ?: "") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Header
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = partner?.fullName ?: "Jio Partner",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = partner?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusPillBadge(status = partner?.status ?: AccountStatus.INACTIVE, strings = strings)
                        }
                    }
                }
            }

            // Personal Details Editor
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(strings.personalDetailsLabel, style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Mobile Number") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("profile_phone_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City / State") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("profile_city_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { onUpdateProfile(name, phone, city) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_profile_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(strings.saveProfileBtn, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Linked Payout Accounts (Add UPI / Bank)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Linked UPI & Bank Accounts", style = MaterialTheme.typography.titleMedium)
                            OutlinedButton(
                                onClick = onOpenAddPayout,
                                modifier = Modifier.testTag("profile_add_payout_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }

                        if (payoutMethods.isEmpty()) {
                            Text(
                                text = "No payout accounts added yet. Add your UPI ID or Bank Account for withdrawals.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            payoutMethods.forEach { method ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        if (method.isPrimary) EmeraldAccent else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = if (method.isPrimary) EmeraldAccent else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = method.label + if (method.isPrimary) " (Primary)" else "",
                                                style = MaterialTheme.typography.titleMedium
                                            )
                                            Text(
                                                text = if (method.methodType == "UPI") method.upiId
                                                else "${method.bankName} • ${method.accountNumber} (${method.ifscCode})",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (!method.isPrimary) {
                                            IconButton(onClick = { onSetPrimaryPayout(method.id) }) {
                                                Icon(Icons.Default.Star, contentDescription = "Set Primary", tint = ChampagneGold)
                                            }
                                        }
                                        IconButton(onClick = { onDeletePayout(method.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonError)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Preferences: Dark Mode, Biometric, Multi-Language
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DarkMode, contentDescription = null, tint = ChampagneGold)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(strings.darkModeLabel, style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = onToggleDarkMode,
                                modifier = Modifier.testTag("dark_mode_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = EmeraldAccent)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(strings.biometricLockLabel, style = MaterialTheme.typography.bodyLarge)
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = onToggleBiometric,
                                modifier = Modifier.testTag("biometric_switch")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(strings.languageLabel, style = MaterialTheme.typography.labelLarge)
                            LanguageSelectorRow(
                                currentLanguage = currentLanguage,
                                onSelectLanguage = onSelectLanguage
                            )
                        }
                    }
                }
            }

            // Admin Vault Entry & Sign Out
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpenAdminVault,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("profile_admin_vault_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Admin Vault", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("sign_out_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = CrimsonError)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.signOutBtn, color = CrimsonError)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun AdminPasskeyDialog(
    onDismiss: () -> Unit,
    onUnlock: (String) -> Boolean
) {
    var passkey by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Key, contentDescription = null, tint = ChampagneGold) },
        title = { Text("Admin Secure Key Pass") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Enter the Master Admin Passkey to control partner activations, manual approvals, commission rates, and live broadcasts.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = passkey,
                    onValueChange = { passkey = it },
                    label = { Text("Admin Passkey") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_passkey_input"),
                    shape = RoundedCornerShape(12.dp)
                )
                Text(
                    "Default Owner Key: SAINI@9076 (changeable inside Admin Settings)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onUnlock(passkey)) {
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("unlock_admin_confirm_button")
            ) {
                Text("Unlock Vault")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AdminControlPanelScreen(
    pendingTransactions: List<TransactionRecord>,
    allPartners: List<PartnerAccount>,
    allSupportMessages: List<SupportMessage>,
    commissionRate: Double,
    strings: LocalizedStrings,
    onApproveTx: (TransactionRecord) -> Unit,
    onRejectTx: (TransactionRecord) -> Unit,
    onSetPartnerStatus: (String, String) -> Unit,
    onCreditPartner: (String, Double, String) -> Unit,
    onUpdateCommissionRate: (Double) -> Unit,
    onUpdatePasskey: (String) -> Unit,
    onReplySupport: (String, String) -> Unit,
    onBroadcastAlert: (String, String) -> Unit,
    onLockAdmin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("APPROVALS") }
    var rateInput by remember(commissionRate) { mutableStateOf(commissionRate.toInt().toString()) }
    var newPasskeyInput by remember { mutableStateOf("") }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastBody by remember { mutableStateOf("") }
    var creditTargetEmail by remember { mutableStateOf<String?>(null) }
    var creditAmountText by remember { mutableStateOf("5000") }
    var creditNoteText by remember { mutableStateOf("Admin Panel Credit") }
    var replyTargetEmail by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ChampagneGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.adminVaultTitle, style = MaterialTheme.typography.headlineSmall)
                }
                OutlinedButton(
                    onClick = onLockAdmin,
                    modifier = Modifier.testTag("lock_admin_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Exit Admin")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTab == "APPROVALS",
                    onClick = { selectedTab = "APPROVALS" },
                    label = { Text("Approvals (${pendingTransactions.size})") }
                )
                FilterChip(
                    selected = selectedTab == "PARTNERS",
                    onClick = { selectedTab = "PARTNERS" },
                    label = { Text("Partners (${allPartners.size})") }
                )
                FilterChip(
                    selected = selectedTab == "CONTROLS",
                    onClick = { selectedTab = "CONTROLS" },
                    label = { Text("Controls") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    "APPROVALS" -> {
                        if (pendingTransactions.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                                ) {
                                    Text(
                                        text = "No pending manual approvals right now.",
                                        modifier = Modifier.padding(20.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(pendingTransactions, key = { it.id }) { tx ->
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                    border = BorderStroke(1.dp, AmberWarning.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(tx.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(
                                                JioPanalRepository.formatCurrency(tx.amount),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = ChampagneGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Partner: ${tx.userEmail}", style = MaterialTheme.typography.bodyMedium)
                                        Text("UTR: ${tx.referenceUtr} • ${tx.paymentMethod}", style = MaterialTheme.typography.labelSmall)
                                        Text(formatTimestamp(tx.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(
                                                onClick = { onApproveTx(tx) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                                    .testTag("admin_approve_${tx.id}"),
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent, contentColor = Color.Black),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(strings.approveBtn, fontWeight = FontWeight.Bold)
                                            }
                                            OutlinedButton(
                                                onClick = { onRejectTx(tx) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(46.dp)
                                                    .testTag("admin_reject_${tx.id}"),
                                                border = BorderStroke(1.dp, CrimsonError),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, tint = CrimsonError, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(strings.rejectBtn, color = CrimsonError)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "PARTNERS" -> {
                        items(allPartners, key = { it.email }) { p ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(p.fullName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                            Text(p.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        StatusPillBadge(status = p.status, strings = strings)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Bal: ${JioPanalRepository.formatCurrency(p.walletBalance)} • 15% Comm: ${JioPanalRepository.formatCurrency(p.commissionEarned)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = EmeraldAccent
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = {
                                                val next = if (p.status == AccountStatus.ACTIVE) AccountStatus.SUSPENDED else AccountStatus.ACTIVE
                                                onSetPartnerStatus(p.email, next)
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text(if (p.status == AccountStatus.ACTIVE) "Suspend" else "Activate")
                                        }
                                        Button(
                                            onClick = { creditTargetEmail = p.email },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Credit +15%")
                                        }
                                        OutlinedButton(
                                            onClick = { replyTargetEmail = p.email },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Chat Reply")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "CONTROLS" -> {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text("Global Commission & Passkey", style = MaterialTheme.typography.titleMedium)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = rateInput,
                                            onValueChange = { rateInput = it.filter { ch -> ch.isDigit() }.take(2) },
                                            label = { Text("Commission %") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(onClick = {
                                            val r = rateInput.toDoubleOrNull() ?: 15.0
                                            onUpdateCommissionRate(r.coerceIn(1.0, 50.0))
                                        }) {
                                            Text("Update %")
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = newPasskeyInput,
                                            onValueChange = { newPasskeyInput = it },
                                            label = { Text("New Admin Passkey") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(onClick = {
                                            onUpdatePasskey(newPasskeyInput)
                                            newPasskeyInput = ""
                                        }) {
                                            Text("Save Key")
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = EmeraldAccent)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Broadcast Live Alert to All Partners", style = MaterialTheme.typography.titleMedium)
                                    }
                                    OutlinedTextField(
                                        value = broadcastTitle,
                                        onValueChange = { broadcastTitle = it },
                                        label = { Text("Alert Title") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = broadcastBody,
                                        onValueChange = { broadcastBody = it },
                                        label = { Text("SMS / Notification Message") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Button(
                                        onClick = {
                                            onBroadcastAlert(broadcastTitle, broadcastBody)
                                            broadcastTitle = ""
                                            broadcastBody = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Send Live Broadcast")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    creditTargetEmail?.let { email ->
        AlertDialog(
            onDismissRequest = { creditTargetEmail = null },
            title = { Text("Credit Partner ($email)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = creditAmountText,
                        onValueChange = { creditAmountText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Deposit / Credit Amount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = creditNoteText,
                        onValueChange = { creditNoteText = it },
                        label = { Text("Note") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = creditAmountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onCreditPartner(email, amt, creditNoteText)
                    }
                    creditTargetEmail = null
                }) { Text("Credit & Send SMS") }
            },
            dismissButton = {
                TextButton(onClick = { creditTargetEmail = null }) { Text("Cancel") }
            }
        )
    }

    replyTargetEmail?.let { email ->
        val recentUserMsgs = allSupportMessages.filter { it.userEmail == email }.take(3)
        AlertDialog(
            onDismissRequest = { replyTargetEmail = null },
            title = { Text("Reply to $email") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    recentUserMsgs.forEach { msg ->
                        Text("${msg.sender}: ${msg.message}", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        label = { Text("Admin Message") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onReplySupport(email, replyText)
                    replyText = ""
                    replyTargetEmail = null
                }) { Text("Send Reply") }
            },
            dismissButton = {
                TextButton(onClick = { replyTargetEmail = null }) { Text("Cancel") }
            }
        )
    }
}
