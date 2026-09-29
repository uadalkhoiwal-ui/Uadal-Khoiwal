package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MarketDataProvider
import com.example.data.PortfolioRepository
import com.example.data.PortfolioSnapshotEntity
import com.example.model.AutoTradeSettings
import com.example.model.CandlePoint
import com.example.model.MarketCategory
import com.example.model.MarketItem
import com.example.model.NewsImpact
import com.example.model.NewsItem
import com.example.model.SignalType
import com.example.model.TechnicalSignal
import com.example.model.Timeframe
import com.example.model.TradePosition
import com.example.model.TradingSystemStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

enum class ChartEngine {
    TRADING_VIEW,
    NATIVE_PRO
}

class TradingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val portfolioRepository = PortfolioRepository(db.tradeDao(), db.portfolioSnapshotDao())

    private val _selectedCategory = MutableStateFlow(MarketCategory.CRYPTO)
    val selectedCategory: StateFlow<MarketCategory> = _selectedCategory.asStateFlow()

    private val _markets = MutableStateFlow(MarketDataProvider.defaultMarkets)
    val markets: StateFlow<List<MarketItem>> = _markets.asStateFlow()

    private val _selectedMarket = MutableStateFlow(MarketDataProvider.defaultMarkets.first())
    val selectedMarket: StateFlow<MarketItem> = _selectedMarket.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow(Timeframe.D1)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    private val _chartEngine = MutableStateFlow(ChartEngine.TRADING_VIEW)
    val chartEngine: StateFlow<ChartEngine> = _chartEngine.asStateFlow()

    private val _candles = MutableStateFlow<List<CandlePoint>>(emptyList())
    val candles: StateFlow<List<CandlePoint>> = _candles.asStateFlow()

    private val _signals = MutableStateFlow(MarketDataProvider.sampleSignals)
    val signals: StateFlow<List<TechnicalSignal>> = _signals.asStateFlow()

    private val _news = MutableStateFlow(MarketDataProvider.initialNews)
    val news: StateFlow<List<NewsItem>> = _news.asStateFlow()

    private val _selectedNewsCategory = MutableStateFlow<MarketCategory?>(null)
    val selectedNewsCategory: StateFlow<MarketCategory?> = _selectedNewsCategory.asStateFlow()

    private val _activeNewsDetail = MutableStateFlow<NewsItem?>(null)
    val activeNewsDetail: StateFlow<NewsItem?> = _activeNewsDetail.asStateFlow()

    private val _stats = MutableStateFlow(TradingSystemStats())
    val stats: StateFlow<TradingSystemStats> = _stats.asStateFlow()

    private val _settings = MutableStateFlow(AutoTradeSettings())
    val settings: StateFlow<AutoTradeSettings> = _settings.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Room Database persistence flows via Repository
    val openTrades: StateFlow<List<TradePosition>> = portfolioRepository.openTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val closedTrades: StateFlow<List<TradePosition>> = portfolioRepository.closedTrades
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val portfolioSnapshots: StateFlow<List<PortfolioSnapshotEntity>> = portfolioRepository.snapshots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRealizedPnl: StateFlow<Double> = portfolioRepository.totalRealizedPnl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private var autoTradeJob: Job? = null
    private var priceTickJob: Job? = null

    init {
        loadCandlesForCurrentSelection()
        startPriceTickSimulator()
        seedInitialPortfolioSnapshotIfNeeded()
    }

    private fun seedInitialPortfolioSnapshotIfNeeded() {
        viewModelScope.launch {
            delay(500)
            if (portfolioSnapshots.value.isEmpty()) {
                val now = System.currentTimeMillis()
                val base = _settings.value.demoStartingBalance
                // Seed 5 historical points for realistic initial curve
                val points = listOf(
                    base * 0.98,
                    base * 0.995,
                    base * 1.01,
                    base * 1.018,
                    base * 1.024
                )
                points.forEachIndexed { i, balanceVal ->
                    portfolioRepository.recordSnapshot(
                        totalBalance = balanceVal,
                        totalEquity = balanceVal,
                        unrealizedPnl = 0.0,
                        realizedPnl = balanceVal - base,
                        openCount = 0
                    )
                }
            }
        }
    }

    private fun loadCandlesForCurrentSelection() {
        val market = _selectedMarket.value
        val tf = _selectedTimeframe.value
        val generated = MarketDataProvider.generateCandles(market.currentPrice, tf)
        _candles.value = generated
    }

    fun selectCategory(category: MarketCategory) {
        _selectedCategory.value = category
        val market = _markets.value.firstOrNull { it.category == category }
            ?: _markets.value.first()
        _selectedMarket.value = market
        loadCandlesForCurrentSelection()
    }

    fun selectMarket(market: MarketItem) {
        _selectedMarket.value = market
        _selectedCategory.value = market.category
        loadCandlesForCurrentSelection()
    }

    fun setTimeframe(tf: Timeframe) {
        _selectedTimeframe.value = tf
        loadCandlesForCurrentSelection()
    }

    fun setChartEngine(engine: ChartEngine) {
        _chartEngine.value = engine
    }

    fun toggleChartEngine() {
        _chartEngine.update { current ->
            if (current == ChartEngine.TRADING_VIEW) ChartEngine.NATIVE_PRO else ChartEngine.TRADING_VIEW
        }
    }

    fun selectNewsCategory(category: MarketCategory?) {
        _selectedNewsCategory.value = category
    }

    fun showNewsDetail(newsItem: NewsItem?) {
        _activeNewsDetail.value = newsItem
    }

    fun dismissMessage() {
        _userMessage.value = null
    }

    fun updateSettings(newSettings: AutoTradeSettings) {
        _settings.value = newSettings
        _userMessage.value = "Settings updated successfully"
    }

    fun addDemoFunds(amount: Double) {
        _stats.update { current ->
            current.copy(currentBalance = current.currentBalance + amount)
        }
        viewModelScope.launch {
            recordSnapshot()
        }
        _userMessage.value = "Added +$${amount.toInt()} demo capital to portfolio"
    }

    fun resetDemoAccount() {
        viewModelScope.launch {
            portfolioRepository.clearPortfolio()
            _stats.update {
                it.copy(
                    isTradingActive = false,
                    totalReturnPercent = 0.0,
                    tradesToday = 0,
                    winRatePercent = 0,
                    profit24h = 0.0,
                    currentBalance = _settings.value.demoStartingBalance
                )
            }
            autoTradeJob?.cancel()
            autoTradeJob = null
            recordSnapshot()
            _userMessage.value = "Demo account reset to \$${_settings.value.demoStartingBalance.toInt()}"
        }
    }

    fun startAutoTrade() {
        if (_stats.value.isTradingActive) {
            _userMessage.value = "Trading system is already active"
            return
        }

        _stats.update { it.copy(isTradingActive = true) }
        _userMessage.value = "🚀 Auto Trading Started! Simulating AI-driven executions."

        autoTradeJob?.cancel()
        autoTradeJob = viewModelScope.launch {
            while (isActive) {
                delay(3500)
                executeSimulatedAutoCycle()
            }
        }
    }

    fun stopAutoTrade() {
        if (!_stats.value.isTradingActive) {
            _userMessage.value = "Trading system is already stopped"
            return
        }
        autoTradeJob?.cancel()
        autoTradeJob = null
        _stats.update { it.copy(isTradingActive = false) }
        _userMessage.value = "⏹️ Auto Trading Paused. Open positions remain monitored."
    }

    private suspend fun executeSimulatedAutoCycle() {
        val activeMarkets = _markets.value.filter {
            when (it.category) {
                MarketCategory.CRYPTO -> _settings.value.enableCrypto
                MarketCategory.STOCKS -> _settings.value.enableStocks
                MarketCategory.FOREX -> _settings.value.enableForex
                MarketCategory.COMMODITIES -> _settings.value.enableCommodities
                MarketCategory.INDICES -> _settings.value.enableIndices
            }
        }
        if (activeMarkets.isEmpty()) return

        val market = activeMarkets.random()
        val isBuy = Random.nextBoolean()
        val riskFraction = _settings.value.riskPerTradePercent / 100.0
        val investAmount = max(100.0, _stats.value.currentBalance * riskFraction)
        val quantity = investAmount / market.currentPrice

        val tpPercent = _settings.value.autoTakeProfitPercent / 100.0
        val slPercent = _settings.value.autoStopLossPercent / 100.0

        val takeProfit = if (isBuy) market.currentPrice * (1.0 + tpPercent) else market.currentPrice * (1.0 - tpPercent)
        val stopLoss = if (isBuy) market.currentPrice * (1.0 - slPercent) else market.currentPrice * (1.0 + slPercent)

        val newTrade = TradePosition(
            symbol = market.symbol,
            name = market.name,
            isBuy = isBuy,
            entryPrice = market.currentPrice,
            currentPrice = market.currentPrice,
            quantity = quantity,
            investedAmount = investAmount,
            takeProfit = takeProfit,
            stopLoss = stopLoss,
            pnl = 0.0,
            pnlPercent = 0.0
        )
        portfolioRepository.insertTrade(newTrade)

        // Update stats
        val randomProfitDelta = (Random.nextDouble() * 60.0) - 15.0
        _stats.update { current ->
            val newTrades = current.tradesToday + 1
            val newProfit = current.profit24h + randomProfitDelta
            val newBalance = current.currentBalance + randomProfitDelta
            val ret = ((newBalance - _settings.value.demoStartingBalance) / _settings.value.demoStartingBalance) * 100.0
            current.copy(
                tradesToday = newTrades,
                profit24h = newProfit,
                currentBalance = newBalance,
                totalReturnPercent = String.format("%.1f", ret).toDoubleOrNull() ?: ret,
                winRatePercent = (70 + Random.nextInt(8)).coerceIn(65, 85)
            )
        }
        recordSnapshot()
    }

    fun placeManualTrade(isBuy: Boolean, amount: Double) {
        viewModelScope.launch {
            val market = _selectedMarket.value
            val quantity = amount / market.currentPrice
            val tpPercent = _settings.value.autoTakeProfitPercent / 100.0
            val slPercent = _settings.value.autoStopLossPercent / 100.0
            val tp = if (isBuy) market.currentPrice * (1.0 + tpPercent) else market.currentPrice * (1.0 - tpPercent)
            val sl = if (isBuy) market.currentPrice * (1.0 - slPercent) else market.currentPrice * (1.0 + slPercent)

            val trade = TradePosition(
                symbol = market.symbol,
                name = market.name,
                isBuy = isBuy,
                entryPrice = market.currentPrice,
                currentPrice = market.currentPrice,
                quantity = quantity,
                investedAmount = amount,
                takeProfit = tp,
                stopLoss = sl,
                pnl = 0.0,
                pnlPercent = 0.0
            )
            portfolioRepository.insertTrade(trade)
            recordSnapshot()
            _userMessage.value = "${if (isBuy) "BUY" else "SELL"} order filled for ${market.ticker} at \$${market.currentPrice}"
        }
    }

    fun closeTradeManually(tradeId: Long) {
        viewModelScope.launch {
            val trade = openTrades.value.find { it.id == tradeId } ?: return@launch
            portfolioRepository.closeTrade(
                tradeId = tradeId,
                exitPrice = trade.currentPrice,
                pnl = trade.pnl,
                pnlPercent = trade.pnlPercent,
                reason = "Manual Close"
            )
            _stats.update { it.copy(currentBalance = it.currentBalance + trade.pnl) }
            recordSnapshot()
            _userMessage.value = "Closed position for ${trade.name} (P&L: \$${String.format("%.2f", trade.pnl)})"
        }
    }

    fun closeAllOpenPositions() {
        viewModelScope.launch {
            val current = openTrades.value
            var totalPnl = 0.0
            for (t in current) {
                totalPnl += t.pnl
                portfolioRepository.closeTrade(
                    tradeId = t.id,
                    exitPrice = t.currentPrice,
                    pnl = t.pnl,
                    pnlPercent = t.pnlPercent,
                    reason = "Close All Flatten"
                )
            }
            _stats.update { it.copy(currentBalance = it.currentBalance + totalPnl) }
            recordSnapshot()
            _userMessage.value = "Closed all ${current.size} positions (Net P&L: \$${String.format("%.2f", totalPnl)})"
        }
    }

    private suspend fun recordSnapshot() {
        val open = openTrades.value
        val unRealized = open.sumOf { it.pnl }
        val equity = _stats.value.currentBalance + unRealized
        val realized = totalRealizedPnl.value
        portfolioRepository.recordSnapshot(
            totalBalance = _stats.value.currentBalance,
            totalEquity = equity,
            unrealizedPnl = unRealized,
            realizedPnl = realized,
            openCount = open.size
        )
    }

    fun scanAISignals() {
        viewModelScope.launch {
            _isScanning.value = true
            delay(1200)
            val updated = _signals.value.map { sig ->
                val newRsi = (sig.rsi + (Random.nextFloat() * 4f - 2f)).coerceIn(25f, 85f)
                val newConfidence = (sig.confidence + Random.nextInt(5) - 2).coerceIn(60, 96)
                sig.copy(
                    rsi = (newRsi * 10).roundToInt() / 10f,
                    confidence = newConfidence,
                    timestamp = System.currentTimeMillis()
                )
            }
            _signals.value = updated
            _isScanning.value = false
            _userMessage.value = "AI Models updated with latest technical indicators!"
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            val additionalNews = NewsItem(
                id = "n_new_${System.currentTimeMillis()}",
                title = "Breaking: Market volatility index stabilizes as liquidity surges",
                source = "MarketWire Live",
                timeAgo = "Just now",
                category = _selectedCategory.value,
                impact = NewsImpact.BULLISH,
                summary = "Trading desks report elevated bid-ask depths with automated liquidity providers re-entering market spread corridors."
            )
            _news.update { listOf(additionalNews) + it.take(15) }
            _userMessage.value = "News feed refreshed"
        }
    }

    private fun startPriceTickSimulator() {
        priceTickJob?.cancel()
        priceTickJob = viewModelScope.launch {
            while (isActive) {
                delay(1800)
                updateMarketTicks()
            }
        }
    }

    private suspend fun updateMarketTicks() {
        val currentMarkets = _markets.value
        val updatedMarkets = currentMarkets.map { market ->
            val volatility = if (market.category == MarketCategory.CRYPTO) 0.0018 else 0.0006
            val deltaPct = (Random.nextDouble() - 0.49) * volatility
            val newPrice = max(0.0001, market.currentPrice * (1.0 + deltaPct))
            val newChange = market.change24h + (deltaPct * 100 * 0.1)

            market.copy(
                currentPrice = newPrice,
                change24h = (newChange * 100).roundToInt() / 100.0,
                high24h = max(market.high24h, newPrice),
                low24h = min(market.low24h, newPrice)
            )
        }
        _markets.value = updatedMarkets

        val currentSel = _selectedMarket.value
        val updatedSel = updatedMarkets.find { it.id == currentSel.id }
        if (updatedSel != null) {
            _selectedMarket.value = updatedSel
        }

        // Update open positions PnL
        val currentOpen = openTrades.value
        for (trade in currentOpen) {
            val market = updatedMarkets.find { it.symbol == trade.symbol }
            if (market != null) {
                val priceDiff = if (trade.isBuy) market.currentPrice - trade.entryPrice else trade.entryPrice - market.currentPrice
                val pnl = priceDiff * trade.quantity
                val pnlPct = (priceDiff / trade.entryPrice) * 100.0

                val hitTP = if (trade.isBuy) market.currentPrice >= trade.takeProfit else market.currentPrice <= trade.takeProfit
                val hitSL = if (trade.isBuy) market.currentPrice <= trade.stopLoss else market.currentPrice >= trade.stopLoss

                if (hitTP) {
                    portfolioRepository.closeTrade(trade.id, market.currentPrice, pnl, pnlPct, "Take Profit Hit")
                    _stats.update { it.copy(currentBalance = it.currentBalance + pnl) }
                } else if (hitSL) {
                    portfolioRepository.closeTrade(trade.id, market.currentPrice, pnl, pnlPct, "Stop Loss Hit")
                    _stats.update { it.copy(currentBalance = it.currentBalance + pnl) }
                } else {
                    portfolioRepository.updateTrade(
                        trade.copy(
                            currentPrice = market.currentPrice,
                            pnl = pnl,
                            pnlPercent = pnlPct
                        )
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoTradeJob?.cancel()
        priceTickJob?.cancel()
    }
}
