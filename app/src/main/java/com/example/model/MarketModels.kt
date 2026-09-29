package com.example.model

enum class MarketCategory(val label: String, val icon: String) {
    CRYPTO("Crypto", "₿"),
    STOCKS("Stocks", "📈"),
    FOREX("Forex", "💱"),
    COMMODITIES("Commodities", "🛢️"),
    INDICES("Indices", "📊")
}

enum class Timeframe(val label: String, val tvCode: String, val seconds: Long) {
    M1("1m", "1", 60),
    M5("5m", "5", 300),
    M15("15m", "15", 900),
    H1("1h", "60", 3600),
    H4("4h", "240", 14400),
    D1("1D", "D", 86400),
    W1("1W", "W", 604800)
}

enum class SignalType(val label: String) {
    STRONG_BUY("STRONG BUY"),
    BUY("BUY"),
    HOLD("HOLD"),
    SELL("SELL"),
    STRONG_SELL("STRONG SELL")
}

enum class NewsImpact {
    BULLISH,
    BEARISH,
    NEUTRAL
}

data class CandlePoint(
    val timestamp: Long,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float
)

data class MarketItem(
    val id: String,
    val symbol: String, // e.g., BINANCE:BTCUSDT
    val name: String,   // e.g., Bitcoin / USD
    val ticker: String, // e.g., BTC/USD
    val category: MarketCategory,
    val currentPrice: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: String,
    val decimals: Int = 2
)

data class TechnicalSignal(
    val symbol: String,
    val name: String,
    val category: MarketCategory,
    val signal: SignalType,
    val confidence: Int, // 0 - 100
    val rsi: Float,
    val macd: String,
    val trend: String,
    val entryPrice: Double,
    val target1: Double,
    val target2: Double,
    val stopLoss: Double,
    val analysisText: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NewsItem(
    val id: String,
    val title: String,
    val source: String,
    val timeAgo: String,
    val category: MarketCategory,
    val impact: NewsImpact,
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class TradePosition(
    val id: Long = 0,
    val symbol: String,
    val name: String,
    val isBuy: Boolean,
    val entryPrice: Double,
    val currentPrice: Double,
    val quantity: Double,
    val investedAmount: Double,
    val takeProfit: Double,
    val stopLoss: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val isClosed: Boolean = false,
    val closeReason: String = "",
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null
)

data class AutoTradeSettings(
    val riskPerTradePercent: Float = 2.5f,
    val maxDailyTrades: Int = 50,
    val autoStopLossPercent: Float = 2.0f,
    val autoTakeProfitPercent: Float = 4.5f,
    val enableCrypto: Boolean = true,
    val enableStocks: Boolean = true,
    val enableForex: Boolean = true,
    val enableCommodities: Boolean = true,
    val enableIndices: Boolean = true,
    val demoStartingBalance: Double = 10000.0
)

data class TradingSystemStats(
    val isTradingActive: Boolean = false,
    val totalReturnPercent: Double = 24.7,
    val tradesToday: Int = 156,
    val winRatePercent: Int = 73,
    val profit24h: Double = 4832.0,
    val currentBalance: Double = 14832.0
)
