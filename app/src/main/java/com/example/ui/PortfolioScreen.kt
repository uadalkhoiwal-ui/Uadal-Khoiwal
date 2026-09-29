package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PortfolioSnapshotEntity
import com.example.model.MarketCategory
import com.example.model.TradePosition
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ChartBackground
import com.example.ui.theme.ChartGridLine
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
import kotlin.math.max
import kotlin.math.min

/**
 * PortfolioScreen composable that fetches and displays the list of active simulated positions
 * and calculates the total portfolio balance using the data persisted in the Room database.
 */
@Composable
fun PortfolioScreen(
    modifier: Modifier = Modifier,
    viewModel: TradingViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val settings by viewModel.settings.collectAsState()
    // Reactive Room DB flows
    val openTrades by viewModel.openTrades.collectAsState()
    val closedTrades by viewModel.closedTrades.collectAsState()
    val snapshots by viewModel.portfolioSnapshots.collectAsState()
    val totalRealizedPnl by viewModel.totalRealizedPnl.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var filterType by remember { mutableIntStateOf(0) } // 0: All, 1: Profit, 2: Loss

    // Real-time calculations derived directly from Room DB position entities
    val totalUnrealizedPnl = openTrades.sumOf { it.pnl }
    val totalInvestedMargin = openTrades.sumOf { it.investedAmount }
    val cashBalance = stats.currentBalance
    val totalPortfolioBalance = cashBalance + totalUnrealizedPnl // Total Net Equity
    val netPnlFromStart = (totalPortfolioBalance - settings.demoStartingBalance)
    val netPnlPercent = if (settings.demoStartingBalance > 0) {
        (netPnlFromStart / settings.demoStartingBalance) * 100.0
    } else 0.0
    val isNetProfit = netPnlFromStart >= 0

    // Filter active trades if requested
    val filteredOpenTrades = when (filterType) {
        1 -> openTrades.filter { it.pnl >= 0 }
        2 -> openTrades.filter { it.pnl < 0 }
        else -> openTrades
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 14.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Screen Header with Room DB Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💼", fontSize = 20.sp)
                    Text(
                        text = "Simulated Portfolio",
                        color = CyanAccent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = "Room DB",
                        tint = GreenBullish,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Room DB: ${openTrades.size} Active Positions Persisted",
                        color = GreenBullish,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Add demo funds button
                IconButton(
                    onClick = { showDepositDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                        .border(BorderStroke(1.dp, CyanAccent.copy(alpha = 0.6f)), RoundedCornerShape(8.dp))
                        .size(36.dp)
                        .testTag("portfolio_deposit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Demo Funds",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Reset portfolio button
                IconButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceDark)
                        .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                        .size(36.dp)
                        .testTag("portfolio_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Portfolio",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Portfolio Balance & Calculation Breakdown Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("portfolio_hero_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
            border = BorderStroke(1.5.dp, CyanAccent)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "TOTAL PORTFOLIO BALANCE",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$${String.format(Locale.US, "%,.2f", totalPortfolioBalance)}",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Return badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isNetProfit) GreenBullish.copy(alpha = 0.2f) else RedBearish.copy(alpha = 0.2f))
                            .border(
                                BorderStroke(1.dp, if (isNetProfit) GreenBullish else RedBearish),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isNetProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = if (isNetProfit) GreenBullish else RedBearish,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${if (isNetProfit) "+" else ""}${String.format(Locale.US, "%.2f", netPnlPercent)}%",
                                color = if (isNetProfit) GreenBullish else RedBearish,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Room DB Balance Formula Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BackgroundDark.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cash ($${String.format(Locale.US, "%,.0f", cashBalance)}) + Floating PnL (${if (totalUnrealizedPnl >= 0) "+" else ""}$${String.format(Locale.US, "%.1f", totalUnrealizedPnl)})",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "= Balance",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Breakdown Strip (Cash, Margin, Floating, Realized)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPill(
                        label = "Free Margin",
                        value = "$${String.format(Locale.US, "%,.0f", max(0.0, cashBalance - totalInvestedMargin))}",
                        valueColor = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Invested",
                        value = "$${String.format(Locale.US, "%,.0f", totalInvestedMargin)}",
                        valueColor = AmberWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Floating P&L",
                        value = "${if (totalUnrealizedPnl >= 0) "+" else ""}$${String.format(Locale.US, "%.1f", totalUnrealizedPnl)}",
                        valueColor = if (totalUnrealizedPnl >= 0) GreenBullish else RedBearish,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Realized",
                        value = "${if (totalRealizedPnl >= 0) "+" else ""}$${String.format(Locale.US, "%.1f", totalRealizedPnl)}",
                        valueColor = if (totalRealizedPnl >= 0) GreenBullish else RedBearish,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (openTrades.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.closeAllOpenPositions() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .testTag("flatten_positions_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedBearish.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, RedBearish.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = RedBearish,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Close All ${openTrades.size} Open Positions",
                                color = RedBearish,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // P&L Equity Performance Curve
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("portfolio_pnl_chart_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📈 Equity Growth Curve (P&L)",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Base: $${settings.demoStartingBalance.toInt()}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                PortfolioPnlChart(
                    snapshots = snapshots,
                    currentEquity = totalPortfolioBalance,
                    startingBalance = settings.demoStartingBalance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: Active Positions vs Closed History
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
                        "Active Positions (${openTrades.size})",
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
                        "P&L History (${closedTrades.size})",
                        color = if (selectedTab == 1) CyanAccent else TextSecondary,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            )
        }

        // Filter chips for Active Positions
        if (selectedTab == 0 && openTrades.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterTabChip("All (${openTrades.size})", filterType == 0) { filterType = 0 }
                FilterTabChip("In Profit (${openTrades.count { it.pnl >= 0 }})", filterType == 1) { filterType = 1 }
                FilterTabChip("In Loss (${openTrades.count { it.pnl < 0 }})", filterType == 2) { filterType = 2 }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display List of Active Simulated Positions / Closed Trades
        val displayedTrades = if (selectedTab == 0) filteredOpenTrades else closedTrades
        if (displayedTrades.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💼", fontSize = 34.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (selectedTab == 0) "No active simulated positions in Room" else "No closed trades recorded",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (selectedTab == 0) "Open positions on Markets tab or run Auto Trade" else "Closed positions will appear here",
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
                    .testTag("portfolio_positions_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp)
            ) {
                items(displayedTrades, key = { it.id }) { trade ->
                    PortfolioTradeCard(
                        trade = trade,
                        isOpen = selectedTab == 0,
                        onClose = { viewModel.closeTradeManually(trade.id) }
                    )
                }
            }
        }
    }

    // Deposit Dialog
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("💵 Add Demo Capital", color = CyanAccent, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Deposit +$5,000 in simulated demo funds to test higher leverage or larger position sizes without risk.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addDemoFunds(5000.0)
                        showDepositDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("Deposit $5,000", color = BackgroundDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("⚠️ Reset Demo Account?", color = RedBearish, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will clear all open and closed trade history from Room database and reset your portfolio balance back to $10,000.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetDemoAccount()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedBearish)
                ) {
                    Text("Reset Everything", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun FilterTabChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else SurfaceDark)
            .border(BorderStroke(1.dp, if (isSelected) CyanAccent else CardBorder), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) CyanAccent else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(BackgroundDark)
            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = TextMuted, fontSize = 9.sp)
            Spacer(modifier = Modifier.height(2.dp))
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

@Composable
private fun PortfolioPnlChart(
    snapshots: List<PortfolioSnapshotEntity>,
    currentEquity: Double,
    startingBalance: Double,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    val equityPoints = remember(snapshots, currentEquity) {
        val list = snapshots.map { it.totalEquity }.toMutableList()
        list.add(currentEquity)
        if (list.size < 2) {
            listOf(startingBalance, currentEquity)
        } else {
            list
        }
    }

    val minVal = (equityPoints.minOrNull() ?: startingBalance).toFloat() * 0.99f
    val maxVal = (equityPoints.maxOrNull() ?: startingBalance).toFloat() * 1.01f
    val range = max(1f, maxVal - minVal)

    val isProfitable = currentEquity >= startingBalance
    val lineColor = if (isProfitable) GreenBullish else RedBearish

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ChartBackground)
    ) {
        val width = size.width
        val height = size.height

        // Horizontal Grid Line for starting balance
        val baselineY = height - ((startingBalance.toFloat() - minVal) / range) * height
        if (baselineY in 0f..height) {
            drawLine(
                color = CardBorder,
                start = Offset(0f, baselineY),
                end = Offset(width, baselineY),
                strokeWidth = 1f
            )
            drawText(
                textMeasurer = textMeasurer,
                text = "Start: $${startingBalance.toInt()}",
                topLeft = Offset(8f, baselineY - 14f),
                style = TextStyle(color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
            )
        }

        // Draw curved line and filled gradient
        val count = equityPoints.size
        val stepX = width / (count - 1)
        val linePath = Path()
        val fillPath = Path()

        val firstY = height - ((equityPoints.first().toFloat() - minVal) / range) * height
        linePath.moveTo(0f, firstY)
        fillPath.moveTo(0f, height)
        fillPath.lineTo(0f, firstY)

        for (i in 1 until count) {
            val x = i * stepX
            val y = height - ((equityPoints[i].toFloat() - minVal) / range) * height
            linePath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        // Draw gradient area
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY = height
            )
        )

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // End Point Marker
        val lastX = width
        val lastY = height - ((currentEquity.toFloat() - minVal) / range) * height
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}

@Composable
private fun PortfolioTradeCard(
    trade: TradePosition,
    isOpen: Boolean,
    onClose: () -> Unit
) {
    val isProfit = trade.pnl >= 0
    val pnlColor = if (isProfit) GreenBullish else RedBearish
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portfolio_trade_${trade.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Row: Direction, Symbol, PnL
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
                        fontSize = 13.sp
                    )
                }

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
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Margin: $${String.format(Locale.US, "%.0f", trade.investedAmount)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Entry: $${String.format(Locale.US, "%.2f", trade.entryPrice)}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Text(
                    text = "Mark: $${String.format(Locale.US, "%.2f", trade.currentPrice)}",
                    color = CyanAccent,
                    fontSize = 11.sp
                )
            }

            // Close button or closed metadata
            if (isOpen) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Opened: ${dateFormat.format(Date(trade.openTime))}",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Button(
                        onClick = onClose,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedBearish.copy(alpha = 0.2f)),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("portfolio_close_button_${trade.id}")
                    ) {
                        Text(
                            text = "Close Position",
                            color = RedBearish,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (trade.closeTime != null) "Closed: ${dateFormat.format(Date(trade.closeTime))}" else "",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Trigger: ${trade.closeReason}",
                        color = AmberWarning,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
