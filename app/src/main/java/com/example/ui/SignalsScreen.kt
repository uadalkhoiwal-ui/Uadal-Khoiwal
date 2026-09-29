package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketCategory
import com.example.model.SignalType
import com.example.model.TechnicalSignal
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenBullish
import com.example.ui.theme.RedBearish
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun SignalsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val signals by viewModel.signals.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    var selectedFilter by remember { mutableStateOf<MarketCategory?>(null) }

    val filteredSignals = if (selectedFilter == null) {
        signals
    } else {
        signals.filter { it.category == selectedFilter }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header with AI Scanner Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🤖 AI Chart Analysis",
                    color = CyanAccent,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Algorithmic RSI, MACD & Multi-Timeframe Patterns",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = { viewModel.scanAISignals() },
                enabled = !isScanning,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                modifier = Modifier.testTag("run_ai_scanner_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = BackgroundDark,
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (isScanning) Modifier.rotate(rotation) else Modifier)
                    )
                    Text(
                        text = if (isScanning) "Scanning..." else "Scan Now",
                        color = BackgroundDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipItem(
                label = "All Markets",
                isSelected = selectedFilter == null,
                onClick = { selectedFilter = null }
            )
            MarketCategory.entries.forEach { cat ->
                FilterChipItem(
                    label = "${cat.icon} ${cat.label}",
                    isSelected = selectedFilter == cat,
                    onClick = { selectedFilter = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Signal Cards List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("ai_signals_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
        ) {
            items(filteredSignals, key = { it.symbol }) { item ->
                SignalCardItem(
                    signal = item,
                    onExecuteTrade = {
                        val isBuy = item.signal == SignalType.BUY || item.signal == SignalType.STRONG_BUY
                        viewModel.placeManualTrade(isBuy = isBuy, amount = 300.0)
                    }
                )
            }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                BorderStroke(1.dp, if (isSelected) CyanAccent else CardBorder),
                RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyanAccent else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun SignalCardItem(
    signal: TechnicalSignal,
    onExecuteTrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("signal_card_${signal.symbol}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Symbol, Name and Signal Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(signal.category.icon, fontSize = 20.sp)
                    Column {
                        Text(
                            text = signal.name,
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = signal.trend,
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                SignalBadge(signal = signal.signal)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // AI Confidence Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI Confidence Score:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "${signal.confidence}%",
                    color = CyanAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { signal.confidence / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = CyanAccent,
                trackColor = SurfaceDarkVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Analysis narrative
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BackgroundDark.copy(alpha = 0.6f))
                    .padding(10.dp)
            ) {
                Text(
                    text = signal.analysisText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Key Technical Targets Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(
                    label = "RSI (14)",
                    value = String.format(Locale.US, "%.1f", signal.rsi),
                    valueColor = if (signal.rsi > 70) RedBearish else if (signal.rsi < 30) GreenBullish else AmberWarning,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Target 1",
                    value = formatPrice(signal.target1),
                    valueColor = GreenBullish,
                    modifier = Modifier.weight(1f)
                )
                MetricPill(
                    label = "Stop Loss",
                    value = formatPrice(signal.stopLoss),
                    valueColor = RedBearish,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Execution Action
            if (signal.signal != SignalType.HOLD) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onExecuteTrade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("execute_signal_${signal.symbol}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (signal.signal == SignalType.BUY || signal.signal == SignalType.STRONG_BUY)
                            GreenBullish else RedBearish
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = if (signal.signal == SignalType.BUY || signal.signal == SignalType.STRONG_BUY)
                                Color(0xFF0F0F0F) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Auto-Fill ${signal.signal.label} Order (\$300 Demo)",
                            color = if (signal.signal == SignalType.BUY || signal.signal == SignalType.STRONG_BUY)
                                Color(0xFF0F0F0F) else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceDarkVariant)
            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(6.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextMuted, fontSize = 9.sp)
            Text(
                value,
                color = valueColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1000) String.format(Locale.US, "%,.1f", price)
    else if (price < 10) String.format(Locale.US, "%.4f", price)
    else String.format(Locale.US, "%.2f", price)
}
