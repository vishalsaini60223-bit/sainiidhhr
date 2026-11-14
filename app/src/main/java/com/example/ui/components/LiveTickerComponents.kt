package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.JioPanalRepository
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.EmeraldAccent
import kotlinx.coroutines.delay
import kotlin.random.Random

data class LiveTickerOrder(
    val id: Long,
    val partnerName: String,
    val city: String,
    val isDeposit: Boolean,
    val amount: Double,
    val commission: Double,
    val method: String,
    val timeLabel: String
)

object LiveTickerGenerator {
    private val names = listOf(
        "Rohit S.", "Ankit V.", "Vikram P.", "Sandeep Y.", "Arjun M.",
        "Neha G.", "Manish T.", "Karan B.", "Imran A.", "Deepak R.",
        "Pooja K.", "Rahul D.", "Suresh N.", "Gaurav C.", "Harsh L."
    )
    private val cities = listOf(
        "Delhi", "Mumbai", "Jaipur", "Lucknow", "Pune",
        "Surat", "Chandigarh", "Hyderabad", "Indore", "Patna",
        "Ahmedabad", "Noida", "Bhopal", "Nagpur"
    )
    private val depositMethods = listOf("UPI QR", "UPI Instant", "NetBanking", "Gateway")
    private val withdrawalMethods = listOf("IMPS Bank", "UPI Payout", "HDFC Fast", "ICICI Instant")
    private val presetHighAmounts = listOf(
        10500.0, 12400.0, 14800.0, 16500.0, 18900.0,
        21500.0, 25000.0, 28600.0, 32400.0, 38500.0,
        44000.0, 49500.0, 56000.0, 64800.0, 75000.0,
        82500.0, 94000.0
    )

    fun createOrder(id: Long, commissionRate: Double, forceDeposit: Boolean? = null): LiveTickerOrder {
        val isDeposit = forceDeposit ?: Random.nextBoolean()
        // Guarantee every single order amount is strictly > ₹10,000
        val base = presetHighAmounts.random()
        val extra = Random.nextInt(1, 9) * 100.0
        val amount = (base + extra).coerceAtLeast(10500.0)
        val commission = if (isDeposit) (amount * commissionRate) / 100.0 else 0.0
        return LiveTickerOrder(
            id = id,
            partnerName = names.random(),
            city = cities.random(),
            isDeposit = isDeposit,
            amount = amount,
            commission = commission,
            method = if (isDeposit) depositMethods.random() else withdrawalMethods.random(),
            timeLabel = "Just now"
        )
    }

    fun initialOrders(count: Int, commissionRate: Double): List<LiveTickerOrder> {
        return (0 until count).map { idx ->
            createOrder(
                id = idx.toLong() + 1L,
                commissionRate = commissionRate,
                forceDeposit = idx % 2 == 0
            ).copy(timeLabel = if (idx == 0) "Just now" else "${(idx + 1) * 4}s ago")
        }
    }
}

@Composable
fun LiveOrdersMarqueeTicker(
    orders: List<LiveTickerOrder>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val pulse = rememberInfiniteTransition(label = "live_dot")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(750), RepeatMode.Reverse),
        label = "dot_alpha"
    )

    // Continuously auto-scroll horizontally across live ₹10K+ deposit & withdrawal chips
    LaunchedEffect(orders.size) {
        if (orders.isEmpty()) return@LaunchedEffect
        var index = 0
        while (true) {
            delay(2100)
            index = (index + 1) % orders.size
            listState.animateScrollToItem(index)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_marquee_ticker"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ChampagneGold.copy(alpha = 0.12f),
                            EmeraldAccent.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
                .padding(vertical = 8.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = EmeraldAccent.copy(alpha = 0.18f),
                border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(dotAlpha)
                            .clip(CircleShape)
                            .background(EmeraldAccent)
                    )
                    Text(
                        text = "LIVE ₹10K+",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            LazyRow(
                state = listState,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(orders, key = { "marquee_${it.id}" }) { order ->
                    val accent = if (order.isDeposit) EmeraldAccent else ChampagneGold
                    val typeTag = if (order.isDeposit) "DEP" else "W/D"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (order.isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = typeTag,
                                tint = accent,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${order.partnerName} • $typeTag ${JioPanalRepository.formatCurrency(order.amount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveDepositWithdrawalFeedCard(
    title: String,
    commissionRate: Double,
    modifier: Modifier = Modifier
) {
    var orders by remember(commissionRate) {
        mutableStateOf(LiveTickerGenerator.initialOrders(count = 12, commissionRate = commissionRate))
    }
    var nextId by remember { mutableIntStateOf(13) }
    var filter by remember { mutableStateOf("ALL") }

    // Continuously push fresh ₹10,000+ deposit & withdrawal orders so the feed scrolls live
    LaunchedEffect(commissionRate) {
        while (true) {
            delay(2500)
            val updated = orders.mapIndexed { idx, old ->
                old.copy(timeLabel = "${(idx + 1) * 3}s ago")
            }
            val fresh = LiveTickerGenerator.createOrder(
                id = nextId.toLong(),
                commissionRate = commissionRate
            )
            nextId += 1
            orders = (listOf(fresh) + updated).take(14)
        }
    }

    val visibleOrders = remember(orders, filter) {
        orders.filter { order ->
            when (filter) {
                "DEPOSIT" -> order.isDeposit
                "WITHDRAW" -> !order.isDeposit
                else -> true
            }
        }.take(4)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_orders_feed_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = ChampagneGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = EmeraldAccent.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "MIN ₹10,000+",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filter == "ALL",
                    onClick = { filter = "ALL" },
                    label = { Text("All ₹10K+") }
                )
                FilterChip(
                    selected = filter == "DEPOSIT",
                    onClick = { filter = "DEPOSIT" },
                    label = { Text("Deposits") }
                )
                FilterChip(
                    selected = filter == "WITHDRAW",
                    onClick = { filter = "WITHDRAW" },
                    label = { Text("Withdrawals") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedContent(
                targetState = visibleOrders,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInVertically { -it / 6 }) togetherWith fadeOut(tween(180))
                },
                label = "live_orders_stream"
            ) { currentList ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currentList.forEach { order ->
                        val accent = if (order.isDeposit) EmeraldAccent else AmberWarning
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_order_row_${order.id}"),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, accent.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(accent.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (order.isDeposit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = if (order.isDeposit) "Deposit" else "Withdrawal",
                                        tint = accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${order.partnerName} • ${order.city}",
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (order.isDeposit) {
                                            "Deposit via ${order.method} • +${JioPanalRepository.formatCurrency(order.commission)} (${commissionRate.toInt()}%)"
                                        } else {
                                            "Withdrawal • ${order.method} Settled"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (order.isDeposit) "+" else "-"}${JioPanalRepository.formatCurrency(order.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = accent,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = order.timeLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
