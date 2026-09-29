package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketCategory
import com.example.model.MarketItem
import com.example.model.Timeframe
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
import com.example.viewmodel.ChartEngine
import com.example.viewmodel.TradingViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: TradingViewModel,
    onNavigateToSignals: () -> Unit,
    onNavigateToAutoTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedMarket by viewModel.selectedMarket.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val chartEngine by viewModel.chartEngine.collectAsState()
    val markets by viewModel.markets.collectAsState()
    val candles by viewModel.candles.collectAsState()
    val signals by viewModel.signals.collectAsState()
    val stats by viewModel.stats.collectAsState()

    var showOrderSheet by remember { mutableStateOf(false) }
    var orderAmount by remember { mutableDoubleStateOf(250.0) }

    val categoryMarkets = markets.filter { it.category == selectedCategory }
    val isPositive = selectedMarket.change24h >= 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // Market Category Tabs (Crypto, Stocks, Forex, Commodities, Indices)
        MarketCategoryTabs(
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectCategory(it) },
            modifier = Modifier.testTag("market_category_tabs")
        )

        // Horizontal Symbol Selector for selected category
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryMarkets.forEach { item ->
                val isSelected = item.id == selectedMarket.id
                val border = if (isSelected) BorderStroke(1.5.dp, CyanAccent) else BorderStroke(1.dp, CardBorder)
                val bg = if (isSelected) SurfaceDark else SurfaceDarkVariant.copy(alpha = 0.6f)

                Card(
                    modifier = Modifier
                        .clickable { viewModel.selectMarket(item) }
                        .testTag("symbol_chip_${item.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = bg),
                    border = border
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = item.ticker,
                            color = if (isSelected) CyanAccent else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${if (item.change24h >= 0) "+" else ""}${String.format(Locale.US, "%.1f", item.change24h)}%",
                            color = if (item.change24h >= 0) GreenBullish else RedBearish,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Chart Section Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .testTag("chart_section_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header inside chart: Title & Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedMarket.name} - Live Chart",
                            color = CyanAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (selectedMarket.decimals == 4)
                                    String.format(Locale.US, "%.4f", selectedMarket.currentPrice)
                                else
                                    String.format(Locale.US, "%,.2f", selectedMarket.currentPrice),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isPositive) GreenBullish.copy(alpha = 0.2f) else RedBearish.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPositive) GreenBullish else RedBearish,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.2f", selectedMarket.change24h)}%",
                                        color = if (isPositive) GreenBullish else RedBearish,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Engine switch toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .clickable { viewModel.toggleChartEngine() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("toggle_chart_engine")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (chartEngine == ChartEngine.TRADING_VIEW) Icons.Default.Public else Icons.Default.CandlestickChart,
                                contentDescription = "Switch Engine",
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (chartEngine == ChartEngine.TRADING_VIEW) "TradingView" else "Native Pro",
                                color = CyanAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Timeframe controls (1m, 5m, 15m, 1h, 4h, 1D, 1W)
                TimeframeControls(
                    selectedTimeframe = selectedTimeframe,
                    onTimeframeSelected = { viewModel.setTimeframe(it) },
                    modifier = Modifier.testTag("timeframe_controls")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chart Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                        .background(BackgroundDark)
                ) {
                    if (chartEngine == ChartEngine.TRADING_VIEW) {
                        TradingViewWidget(
                            symbol = selectedMarket.symbol,
                            interval = selectedTimeframe.tvCode,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        NativeCandleChart(
                            candles = candles,
                            currentPrice = selectedMarket.currentPrice,
                            decimals = selectedMarket.decimals,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Quick Order Actions Bar
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.placeManualTrade(isBuy = true, amount = orderAmount)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_buy_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenBullish)
                    ) {
                        Text(
                            text = "BUY / LONG",
                            color = Color(0xFF0F0F0F),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.placeManualTrade(isBuy = false, amount = orderAmount)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("quick_sell_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedBearish)
                    ) {
                        Text(
                            text = "SELL / SHORT",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = { showOrderSheet = !showOrderSheet },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(BorderStroke(1.dp, CardBorder), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.CompareArrows,
                            contentDescription = "Order Sizing",
                            tint = CyanAccent
                        )
                    }
                }

                // Expandable Order Sizing Slider
                AnimatedVisibility(visible = showOrderSheet) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(SurfaceDark.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Position Size:", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "$${orderAmount.toInt()}",
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Slider(
                            value = orderAmount.toFloat(),
                            onValueChange = { orderAmount = it.toDouble() },
                            valueRange = 50f..2000f,
                            steps = 38,
                            colors = SliderDefaults.colors(
                                thumbColor = CyanAccent,
                                activeTrackColor = CyanAccent,
                                inactiveTrackColor = CardBorder
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Signal Card Preview
        val matchedSignal = signals.find { it.symbol == selectedMarket.symbol } ?: signals.first()
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clickable { onNavigateToSignals() }
                .testTag("home_ai_signal_preview"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🤖", fontSize = 18.sp)
                        Text(
                            text = "AI Signal: ${matchedSignal.name}",
                            color = CyanAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    SignalBadge(signal = matchedSignal.signal)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = matchedSignal.analysisText,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RSI: ${matchedSignal.rsi}", color = TextMuted, fontSize = 11.sp)
                    Text("MACD: ${matchedSignal.macd}", color = TextMuted, fontSize = 11.sp)
                    Text(
                        "Tap for full analysis →",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto Trading Quick Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clickable { onNavigateToAutoTrade() }
                .testTag("home_auto_trade_banner"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
            border = BorderStroke(1.5.dp, CyanAccent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("⚡", fontSize = 18.sp)
                        Text(
                            text = "Auto Trading (Demo)",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (stats.isTradingActive) "🟢 Active • Win Rate: ${stats.winRatePercent}%" else "● System Ready to run",
                        color = if (stats.isTradingActive) GreenBullish else CyanAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onNavigateToAutoTrade,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stats.isTradingActive) RedBearish else GreenBullish
                    )
                ) {
                    Text(
                        text = if (stats.isTradingActive) "Manage" else "Start Demo",
                        color = if (stats.isTradingActive) Color.White else Color(0xFF0F0F0F),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
