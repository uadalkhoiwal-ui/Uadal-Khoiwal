package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoTradeSettings
import com.example.model.TradePosition
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutoTradeScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val openTrades by viewModel.openTrades.collectAsState()
    val closedTrades by viewModel.closedTrades.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Main Auto Trading Container Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auto_trading_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
            border = BorderStroke(1.5.dp, CyanAccent)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with live status dot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚡", fontSize = 22.sp)
                        Text(
                            text = "Auto Trading (Demo)",
                            color = CyanAccent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (stats.isTradingActive) GreenBullish else AmberWarning)
                        )
                        Text(
                            text = if (stats.isTradingActive) "Trading Active" else "System Ready",
                            color = if (stats.isTradingActive) GreenBullish else AmberWarning,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4 Stats Grid (Total Return, Trades Today, Win Rate, Profit 24h)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        label = "Total Return",
                        value = "${if (stats.totalReturnPercent >= 0) "+" else ""}${String.format(Locale.US, "%.1f", stats.totalReturnPercent)}%",
                        valueColor = if (stats.totalReturnPercent >= 0) GreenBullish else RedBearish,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Trades Today",
                        value = "${stats.tradesToday}",
                        valueColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        label = "Win Rate",
                        value = "${stats.winRatePercent}%",
                        valueColor = GreenBullish,
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        label = "Profit (24h)",
                        value = "$${String.format(Locale.US, "%,.0f", stats.profit24h)}",
                        valueColor = CyanAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Account Balance strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(BackgroundDark)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Demo Capital Balance:", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", stats.currentBalance)}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Controls: Start, Stop, Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!stats.isTradingActive) {
                        Button(
                            onClick = { viewModel.startAutoTrade() },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("start_auto_trade_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenBullish)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🚀", fontSize = 16.sp)
                                Text(
                                    text = "Start Demo",
                                    color = Color(0xFF0F0F0F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { viewModel.stopAutoTrade() },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(46.dp)
                                .testTag("stop_auto_trade_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedBearish)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("⏹️", fontSize = 16.sp)
                                Text(
                                    text = "Stop",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("open_settings_button"),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CyanAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanAccent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Open Positions vs Execution History
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CyanAccent
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Open Positions (${openTrades.size})",
                        color = if (selectedTab == 0) CyanAccent else TextSecondary,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        "Trade History (${closedTrades.size})",
                        color = if (selectedTab == 1) CyanAccent else TextSecondary,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Trades List
        val currentTrades = if (selectedTab == 0) openTrades else closedTrades
        if (currentTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📊", fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedTab == 0) "No active simulated positions" else "No trade history yet",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (selectedTab == 0) "Click 'Start Demo' to begin automated trading" else "Executed trades will appear here",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("positions_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
            ) {
                items(currentTrades, key = { it.id }) { trade ->
                    TradePositionItem(
                        trade = trade,
                        isOpen = selectedTab == 0,
                        onClosePosition = { viewModel.closeTradeManually(trade.id) }
                    )
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AutoTradeSettingsDialog(
            currentSettings = settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { updated ->
                viewModel.updateSettings(updated)
                showSettingsDialog = false
            },
            onResetAccount = {
                viewModel.resetDemoAccount()
                showSettingsDialog = false
            }
        )
    }
}

@Composable
private fun TradePositionItem(
    trade: TradePosition,
    isOpen: Boolean,
    onClosePosition: () -> Unit
) {
    val isProfit = trade.pnl >= 0
    val pnlColor = if (isProfit) GreenBullish else RedBearish

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("trade_item_${trade.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (trade.isBuy) GreenBullish.copy(alpha = 0.2f) else RedBearish.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (trade.isBuy) "BUY" else "SELL",
                            color = if (trade.isBuy) GreenBullish else RedBearish,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = trade.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // PnL display
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isProfit) "+" else ""}$${String.format(Locale.US, "%.2f", trade.pnl)}",
                        color = pnlColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "(${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", trade.pnlPercent)}%)",
                        color = pnlColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Entry: $${String.format(Locale.US, "%.2f", trade.entryPrice)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Mark: $${String.format(Locale.US, "%.2f", trade.currentPrice)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "TP: $${String.format(Locale.US, "%.2f", trade.takeProfit)}",
                    color = GreenBullish.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }

            if (isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onClosePosition,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedBearish.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("close_trade_${trade.id}")
                    ) {
                        Text(
                            text = "Close Position",
                            color = RedBearish,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (trade.closeReason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Closed: ${trade.closeReason}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun AutoTradeSettingsDialog(
    currentSettings: AutoTradeSettings,
    onDismiss: () -> Unit,
    onSave: (AutoTradeSettings) -> Unit,
    onResetAccount: () -> Unit
) {
    var risk by remember { mutableStateOf(currentSettings.riskPerTradePercent) }
    var maxTrades by remember { mutableStateOf(currentSettings.maxDailyTrades) }
    var stopLoss by remember { mutableStateOf(currentSettings.autoStopLossPercent) }
    var takeProfit by remember { mutableStateOf(currentSettings.autoTakeProfitPercent) }
    var enableCrypto by remember { mutableStateOf(currentSettings.enableCrypto) }
    var enableStocks by remember { mutableStateOf(currentSettings.enableStocks) }
    var enableForex by remember { mutableStateOf(currentSettings.enableForex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚙️ Auto Trading Settings", color = CyanAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Risk per trade
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Risk per Trade:", color = TextSecondary, fontSize = 12.sp)
                    Text("${String.format(Locale.US, "%.1f", risk)}%", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Slider(
                    value = risk,
                    onValueChange = { risk = it },
                    valueRange = 0.5f..10.0f,
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = CyanAccent, activeTrackColor = CyanAccent)
                )

                // Stop loss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Stop Loss:", color = TextSecondary, fontSize = 12.sp)
                    Text("${String.format(Locale.US, "%.1f", stopLoss)}%", color = RedBearish, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Slider(
                    value = stopLoss,
                    onValueChange = { stopLoss = it },
                    valueRange = 0.5f..5.0f,
                    colors = SliderDefaults.colors(thumbColor = RedBearish, activeTrackColor = RedBearish)
                )

                // Take Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Take Profit:", color = TextSecondary, fontSize = 12.sp)
                    Text("${String.format(Locale.US, "%.1f", takeProfit)}%", color = GreenBullish, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Slider(
                    value = takeProfit,
                    onValueChange = { takeProfit = it },
                    valueRange = 1.0f..10.0f,
                    colors = SliderDefaults.colors(thumbColor = GreenBullish, activeTrackColor = GreenBullish)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("Eligible Markets:", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enableCrypto,
                        onCheckedChange = { enableCrypto = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Text("Crypto (BTC, ETH, SOL)", color = TextSecondary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enableStocks,
                        onCheckedChange = { enableStocks = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Text("Stocks (AAPL, NVDA, TSLA)", color = TextSecondary, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = enableForex,
                        onCheckedChange = { enableForex = it },
                        colors = CheckboxDefaults.colors(checkedColor = CyanAccent)
                    )
                    Text("Forex (EUR/USD, GBP/USD)", color = TextSecondary, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onResetAccount,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, RedBearish),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RedBearish)
                ) {
                    Text("Reset Demo Account Balance", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentSettings.copy(
                            riskPerTradePercent = risk,
                            autoStopLossPercent = stopLoss,
                            autoTakeProfitPercent = takeProfit,
                            enableCrypto = enableCrypto,
                            enableStocks = enableStocks,
                            enableForex = enableForex
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("Save Settings", color = BackgroundDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
