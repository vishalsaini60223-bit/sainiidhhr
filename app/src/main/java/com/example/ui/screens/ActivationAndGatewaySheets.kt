package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.AccountStatus
import com.example.data.local.NotificationAlert
import com.example.data.local.PartnerAccount
import com.example.data.local.PayoutMethod
import com.example.data.repository.JioPanalRepository
import com.example.ui.components.StatusPillBadge
import com.example.ui.components.copyToClipboard
import com.example.ui.components.formatTimestamp
import com.example.ui.components.openExternalUrl
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.EmeraldAccent
import com.example.util.LocalizedStrings

private const val ADMIN_UPI_ID = "vishalkumar90@upi"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountActivationSheet(
    partner: PartnerAccount?,
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
    onSubmitUtr: (String) -> Unit
) {
    val context = LocalContext.current
    var utrInput by remember { mutableStateOf(partner?.activationUtr ?: "") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.activateAccountTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusPillBadge(status = partner?.status ?: AccountStatus.INACTIVE, strings = strings)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = strings.activateAccountSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User's Provided QR Code inside Crisp Frame
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(2.dp, ChampagneGold),
                modifier = Modifier.size(220.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.upi_qr_code),
                    contentDescription = "Jio Panal ₹5000 UPI QR Code",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = strings.activationAmountLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = ADMIN_UPI_ID,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    OutlinedButton(
                        onClick = { copyToClipboard(context, "UPI ID", ADMIN_UPI_ID) },
                        modifier = Modifier.testTag("copy_upi_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.copyUpiBtn, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    val upiUri = "upi://pay?pa=$ADMIN_UPI_ID&pn=Jio%20Panal&am=5000&cu=INR&tn=Jio%20Panal%20Activation"
                    openExternalUrl(context, upiUri, ADMIN_UPI_ID)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pay_upi_intent_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.payViaUpiBtn, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = utrInput,
                onValueChange = { utrInput = it.filter { ch -> ch.isLetterOrDigit() }.take(22) },
                label = { Text("UTR / Reference Number") },
                placeholder = { Text(strings.utrHint) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("activation_utr_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    onSubmitUtr(utrInput)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_activation_utr_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.submitUtrBtn, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewaySheet(
    commissionRate: Double,
    onDismiss: () -> Unit,
    onCompleteDeposit: (Double, String) -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("5000") }
    var selectedMethod by remember { mutableStateOf("UPI QR ($ADMIN_UPI_ID)") }
    val methods = listOf(
        "UPI QR ($ADMIN_UPI_ID)",
        "UPI App Intent",
        "NetBanking Instant",
        "Debit / Credit Card"
    )
    val amount = amountText.toDoubleOrNull() ?: 0.0
    val comm = (amount * commissionRate) / 100.0
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Jio Panal Payment Gateway",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Instant deposit with automatic ${commissionRate.toInt()}% partner commission credit & Live SMS alert.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("500", "1000", "5000", "10000").forEach { preset ->
                    FilterChip(
                        selected = amountText == preset,
                        onClick = { amountText = preset },
                        label = { Text("₹$preset") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() }.take(7) },
                label = { Text("Deposit Amount (₹)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gateway_amount_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${commissionRate.toInt()}% Instant Bonus",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldAccent
                    )
                    Text(
                        text = "+${JioPanalRepository.formatCurrency(comm)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Select Gateway Method", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            methods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedMethod = method }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method }
                    )
                    Icon(
                        imageVector = if ("UPI" in method) Icons.Default.QrCode2 else Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(method, style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (selectedMethod.contains("UPI App")) {
                TextButton(
                    onClick = {
                        val uri = "upi://pay?pa=$ADMIN_UPI_ID&pn=Jio%20Panal&am=${amount.toInt()}&cu=INR"
                        openExternalUrl(context, uri, ADMIN_UPI_ID)
                    }
                ) {
                    Text("Launch Installed UPI App")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    onCompleteDeposit(amount, selectedMethod)
                    onDismiss()
                },
                enabled = amount > 0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("gateway_pay_now_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Pay ${JioPanalRepository.formatCurrency(amount)} & Credit +${JioPanalRepository.formatCurrency(comm)}",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPayoutMethodSheet(
    onDismiss: () -> Unit,
    onSaveMethod: (
        methodType: String,
        label: String,
        upiId: String,
        bankName: String,
        accountHolder: String,
        accountNumber: String,
        ifscCode: String,
        makePrimary: Boolean
    ) -> Unit
) {
    var methodType by remember { mutableStateOf("UPI") }
    var label by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var accountHolder by remember { mutableStateOf("") }
    var accountNumber by remember { mutableStateOf("") }
    var ifscCode by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Add Payout UPI / Bank Account", style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = methodType == "UPI",
                    onClick = { methodType = "UPI" },
                    label = { Text("UPI ID") }
                )
                FilterChip(
                    selected = methodType == "BANK",
                    onClick = { methodType = "BANK" },
                    label = { Text("Bank Account") }
                )
            }
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Nickname (e.g. Primary HDFC / PhonePe)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("payout_label_input"),
                shape = RoundedCornerShape(12.dp)
            )
            if (methodType == "UPI") {
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Your UPI ID (e.g. name@okicici)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payout_upi_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = accountHolder,
                    onValueChange = { accountHolder = it },
                    label = { Text("Account Holder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Account Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = ifscCode,
                    onValueChange = { ifscCode = it.uppercase() },
                    label = { Text("IFSC Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Button(
                onClick = {
                    val valid = if (methodType == "UPI") upiId.contains("@") else accountNumber.length >= 6 && ifscCode.length >= 6
                    if (valid) {
                        onSaveMethod(
                            methodType,
                            label.ifBlank { if (methodType == "UPI") "My UPI" else bankName.ifBlank { "My Bank" } },
                            upiId,
                            bankName,
                            accountHolder,
                            accountNumber,
                            ifscCode,
                            true
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_payout_method_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Account", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawSheet(
    availableBalance: Double,
    payoutMethods: List<PayoutMethod>,
    onDismiss: () -> Unit,
    onOpenAddPayout: () -> Unit,
    onRequestWithdraw: (Double, PayoutMethod) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf(payoutMethods.firstOrNull { it.isPrimary }?.id ?: payoutMethods.firstOrNull()?.id) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Withdraw Funds", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "Available Balance: ${JioPanalRepository.formatCurrency(availableBalance)}",
                style = MaterialTheme.typography.titleMedium,
                color = EmeraldAccent
            )

            if (payoutMethods.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No UPI or Bank Account linked yet.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            onDismiss()
                            onOpenAddPayout()
                        }) {
                            Text("Add UPI / Bank Account")
                        }
                    }
                }
            } else {
                payoutMethods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedId = method.id }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedId == method.id,
                            onClick = { selectedId = method.id }
                        )
                        Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(method.label, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (method.methodType == "UPI") method.upiId else "${method.bankName} ••${method.accountNumber.takeLast(4)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Withdrawal Amount (₹)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                val chosen = payoutMethods.firstOrNull { it.id == selectedId }
                val amount = amountText.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        if (chosen != null && amount > 0 && amount <= availableBalance) {
                            onRequestWithdraw(amount, chosen)
                            onDismiss()
                        }
                    },
                    enabled = chosen != null && amount > 0 && amount <= availableBalance,
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("confirm_withdraw_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Submit Withdrawal Request", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    notifications: List<NotificationAlert>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = ChampagneGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Real-Time Alerts & SMS", style = MaterialTheme.typography.headlineSmall)
                }
                TextButton(onClick = onMarkAllRead) {
                    Text("Mark Read")
                }
            }
            if (notifications.isEmpty()) {
                Text(
                    "No notifications yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                notifications.forEach { alert ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isRead) MaterialTheme.colorScheme.surfaceContainer
                            else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(alert.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    formatTimestamp(alert.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(alert.smsBody, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
