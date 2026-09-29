package com.example.data

import com.example.model.CandlePoint
import com.example.model.MarketCategory
import com.example.model.MarketItem
import com.example.model.NewsImpact
import com.example.model.NewsItem
import com.example.model.SignalType
import com.example.model.TechnicalSignal
import com.example.model.Timeframe
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object MarketDataProvider {

    val defaultMarkets = listOf(
        // Crypto
        MarketItem(
            id = "btc",
            symbol = "BINANCE:BTCUSDT",
            name = "Bitcoin / USD",
            ticker = "BTC/USDT",
            category = MarketCategory.CRYPTO,
            currentPrice = 68420.50,
            change24h = +3.84,
            high24h = 69200.00,
            low24h = 66100.00,
            volume24h = "$38.4B",
            decimals = 2
        ),
        MarketItem(
            id = "eth",
            symbol = "BINANCE:ETHUSDT",
            name = "Ethereum / USD",
            ticker = "ETH/USDT",
            category = MarketCategory.CRYPTO,
            currentPrice = 3524.80,
            change24h = +2.45,
            high24h = 3590.00,
            low24h = 3440.00,
            volume24h = "$18.1B",
            decimals = 2
        ),
        MarketItem(
            id = "sol",
            symbol = "BINANCE:SOLUSDT",
            name = "Solana / USD",
            ticker = "SOL/USDT",
            category = MarketCategory.CRYPTO,
            currentPrice = 176.40,
            change24h = +5.12,
            high24h = 182.00,
            low24h = 167.50,
            volume24h = "$5.7B",
            decimals = 2
        ),

        // Stocks
        MarketItem(
            id = "aapl",
            symbol = "NASDAQ:AAPL",
            name = "Apple Inc.",
            ticker = "AAPL",
            category = MarketCategory.STOCKS,
            currentPrice = 227.60,
            change24h = +1.18,
            high24h = 229.40,
            low24h = 225.10,
            volume24h = "$4.9B",
            decimals = 2
        ),
        MarketItem(
            id = "nvda",
            symbol = "NASDAQ:NVDA",
            name = "NVIDIA Corp.",
            ticker = "NVDA",
            category = MarketCategory.STOCKS,
            currentPrice = 126.40,
            change24h = +3.42,
            high24h = 128.50,
            low24h = 122.90,
            volume24h = "$12.8B",
            decimals = 2
        ),
        MarketItem(
            id = "tsla",
            symbol = "NASDAQ:TSLA",
            name = "Tesla Inc.",
            ticker = "TSLA",
            category = MarketCategory.STOCKS,
            currentPrice = 246.80,
            change24h = -0.74,
            high24h = 252.00,
            low24h = 243.20,
            volume24h = "$7.3B",
            decimals = 2
        ),

        // Forex
        MarketItem(
            id = "eurusd",
            symbol = "FX:EURUSD",
            name = "EUR / USD",
            ticker = "EUR/USD",
            category = MarketCategory.FOREX,
            currentPrice = 1.0845,
            change24h = -0.32,
            high24h = 1.0892,
            low24h = 1.0820,
            volume24h = "$85.2B",
            decimals = 4
        ),
        MarketItem(
            id = "gbpusd",
            symbol = "FX:GBPUSD",
            name = "GBP / USD",
            ticker = "GBP/USD",
            category = MarketCategory.FOREX,
            currentPrice = 1.2984,
            change24h = +0.15,
            high24h = 1.3020,
            low24h = 1.2950,
            volume24h = "$54.0B",
            decimals = 4
        ),
        MarketItem(
            id = "usdjpy",
            symbol = "FX:USDJPY",
            name = "USD / JPY",
            ticker = "USD/JPY",
            category = MarketCategory.FOREX,
            currentPrice = 152.35,
            change24h = +0.48,
            high24h = 152.90,
            low24h = 151.70,
            volume24h = "$68.7B",
            decimals = 2
        ),

        // Commodities
        MarketItem(
            id = "xauusd",
            symbol = "OANDA:XAUUSD",
            name = "Gold Spot / USD",
            ticker = "XAU/USD",
            category = MarketCategory.COMMODITIES,
            currentPrice = 2685.20,
            change24h = +1.64,
            high24h = 2694.00,
            low24h = 2648.00,
            volume24h = "$29.4B",
            decimals = 2
        ),
        MarketItem(
            id = "usoil",
            symbol = "TVC:USOIL",
            name = "WTI Crude Oil",
            ticker = "WTI/USD",
            category = MarketCategory.COMMODITIES,
            currentPrice = 71.85,
            change24h = -1.25,
            high24h = 73.40,
            low24h = 70.90,
            volume24h = "$14.6B",
            decimals = 2
        ),

        // Indices
        MarketItem(
            id = "ndx",
            symbol = "NASDAQ:NDX",
            name = "NASDAQ 100",
            ticker = "NDX",
            category = MarketCategory.INDICES,
            currentPrice = 19842.10,
            change24h = +1.42,
            high24h = 19920.00,
            low24h = 19680.00,
            volume24h = "$42.1B",
            decimals = 2
        ),
        MarketItem(
            id = "spx",
            symbol = "SP:SPX",
            name = "S&P 500",
            ticker = "SPX",
            category = MarketCategory.INDICES,
            currentPrice = 5738.90,
            change24h = +0.88,
            high24h = 5755.00,
            low24h = 5705.00,
            volume24h = "$36.8B",
            decimals = 2
        )
    )

    fun getCategoryDefaultSymbol(category: MarketCategory): String {
        return when (category) {
            MarketCategory.CRYPTO -> "BINANCE:BTCUSDT"
            MarketCategory.STOCKS -> "NASDAQ:AAPL"
            MarketCategory.FOREX -> "FX:EURUSD"
            MarketCategory.COMMODITIES -> "OANDA:XAUUSD"
            MarketCategory.INDICES -> "NASDAQ:NDX"
        }
    }

    fun getCategoryTitle(category: MarketCategory): String {
        return when (category) {
            MarketCategory.CRYPTO -> "₿ Crypto - Live Chart"
            MarketCategory.STOCKS -> "📈 Stocks - Live Chart"
            MarketCategory.FOREX -> "💱 Forex - Live Chart"
            MarketCategory.COMMODITIES -> "🛢️ Commodities - Live Chart"
            MarketCategory.INDICES -> "📊 Indices - Live Chart"
        }
    }

    fun generateCandles(basePrice: Double, timeframe: Timeframe, count: Int = 40): List<CandlePoint> {
        val candles = mutableListOf<CandlePoint>()
        var current = basePrice.toFloat() * 0.95f
        val now = System.currentTimeMillis()
        val intervalMillis = timeframe.seconds * 1000

        val volatility = when (timeframe) {
            Timeframe.M1, Timeframe.M5 -> 0.003f
            Timeframe.M15, Timeframe.H1 -> 0.007f
            Timeframe.H4, Timeframe.D1 -> 0.015f
            Timeframe.W1 -> 0.035f
        }

        for (i in count downTo 0) {
            val time = now - (i * intervalMillis)
            val open = current
            val delta = (Random.nextFloat() - 0.48f) * 2f * (open * volatility)
            val close = max(open * 0.5f, open + delta)
            val high = max(open, close) + Random.nextFloat() * (open * volatility * 0.8f)
            val low = min(open, close) - Random.nextFloat() * (open * volatility * 0.8f)
            val volume = (Random.nextFloat() * 1500f + 500f) * (open / 100f)

            candles.add(
                CandlePoint(
                    timestamp = time,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume
                )
            )
            current = close
        }
        return candles
    }

    val initialNews = listOf(
        NewsItem(
            id = "n1",
            title = "Bitcoin surges past \$68,000 amid institutional buying wave",
            source = "CryptoNews",
            timeAgo = "12m ago",
            category = MarketCategory.CRYPTO,
            impact = NewsImpact.BULLISH,
            summary = "Spot ETF inflows recorded an impressive \$480M single-day net positive as institutional desks position for macroeconomic easing and network hash rate records."
        ),
        NewsItem(
            id = "n2",
            title = "Federal Reserve signals measured rate policy stance for upcoming FOMC",
            source = "Finance Today",
            timeAgo = "34m ago",
            category = MarketCategory.FOREX,
            impact = NewsImpact.NEUTRAL,
            summary = "Federal Reserve officials highlighted steady progress on inflation while monitoring labor market metrics, leading dollar index to consolidate near 104.2."
        ),
        NewsItem(
            id = "n3",
            title = "Gold hits new record high on heightened safe-haven demand",
            source = "Commodities Live",
            timeAgo = "1h ago",
            category = MarketCategory.COMMODITIES,
            impact = NewsImpact.BULLISH,
            summary = "XAU/USD tested \$2,694 per ounce as central banks continue accumulation and sovereign bond yields adjust across major economies."
        ),
        NewsItem(
            id = "n4",
            title = "Tech stocks rally as AI semiconductor earnings beat forecast",
            source = "Market Watch",
            timeAgo = "2h ago",
            category = MarketCategory.STOCKS,
            impact = NewsImpact.BULLISH,
            summary = "Leading tech components drove NASDAQ 100 up over 200 points as cloud providers ramp up artificial intelligence capital expenditures."
        ),
        NewsItem(
            id = "n5",
            title = "Crude oil eases slightly on reports of refinery capacity resumption",
            source = "Energy Report",
            timeAgo = "3h ago",
            category = MarketCategory.COMMODITIES,
            impact = NewsImpact.BEARISH,
            summary = "WTI prices dipped toward \$71.85 as maritime shipping inventories normalized and storage facilities reported higher distillate stockpiles."
        ),
        NewsItem(
            id = "n6",
            title = "EUR/USD tests key resistance at 1.0880 ahead of ECB briefing",
            source = "FX World",
            timeAgo = "4h ago",
            category = MarketCategory.FOREX,
            impact = NewsImpact.NEUTRAL,
            summary = "Euro traders prepare for monetary policy commentary with implied volatility stabilizing near monthly lows in European session."
        ),
        NewsItem(
            id = "n7",
            title = "Global equity indices sustain momentum led by Asia-Pacific gains",
            source = "Bloomberg Sync",
            timeAgo = "5h ago",
            category = MarketCategory.INDICES,
            impact = NewsImpact.BULLISH,
            summary = "Broad indices rallied across Tokyo and Frankfurt as manufacturing purchasing manager index (PMI) readings surpassed consensus expectations."
        )
    )

    val sampleSignals = listOf(
        TechnicalSignal(
            symbol = "BINANCE:BTCUSDT",
            name = "Bitcoin / USD",
            category = MarketCategory.CRYPTO,
            signal = SignalType.STRONG_BUY,
            confidence = 89,
            rsi = 64.2f,
            macd = "Bullish Cross",
            trend = "Aggressive Uptrend",
            entryPrice = 68200.0,
            target1 = 69800.0,
            target2 = 71500.0,
            stopLoss = 66500.0,
            analysisText = "Strong bullish momentum confirmed by 20/50 EMA golden cross. RSI is in healthy expansion without terminal divergence. Key support at \$66,800 is holding firmly."
        ),
        TechnicalSignal(
            symbol = "OANDA:XAUUSD",
            name = "Gold Spot / USD",
            category = MarketCategory.COMMODITIES,
            signal = SignalType.BUY,
            confidence = 82,
            rsi = 59.8f,
            macd = "Expanding Positive",
            trend = "Ascending Channel",
            entryPrice = 2678.0,
            target1 = 2715.0,
            target2 = 2740.0,
            stopLoss = 2650.0,
            analysisText = "Consolidation resolved with upward breakout above \$2,670 resistance. Safe-haven inflows support continuation towards next psychological target at \$2,720."
        ),
        TechnicalSignal(
            symbol = "FX:EURUSD",
            name = "EUR / USD",
            category = MarketCategory.FOREX,
            signal = SignalType.SELL,
            confidence = 74,
            rsi = 41.5f,
            macd = "Bearish Histogram",
            trend = "Lower Highs Formed",
            entryPrice = 1.0850,
            target1 = 1.0805,
            target2 = 1.0770,
            stopLoss = 1.0910,
            analysisText = "Bearish divergence on 4H time frame. Price rejected 100-day moving average. Selling volume accelerating into dollar strength."
        ),
        TechnicalSignal(
            symbol = "NASDAQ:AAPL",
            name = "Apple Inc.",
            category = MarketCategory.STOCKS,
            signal = SignalType.HOLD,
            confidence = 68,
            rsi = 52.3f,
            macd = "Neutral Flat",
            trend = "Sideways Range",
            entryPrice = 227.0,
            target1 = 233.0,
            target2 = 238.0,
            stopLoss = 222.0,
            analysisText = "Price oscillating within \$224 - \$230 range. Volume is subdued. Recommend holding existing exposure or waiting for confirmed breakout."
        ),
        TechnicalSignal(
            symbol = "NASDAQ:NDX",
            name = "NASDAQ 100",
            category = MarketCategory.INDICES,
            signal = SignalType.STRONG_BUY,
            confidence = 87,
            rsi = 67.1f,
            macd = "Strong Bullish",
            trend = "All-Time High Trajectory",
            entryPrice = 19800.0,
            target1 = 20150.0,
            target2 = 20500.0,
            stopLoss = 19520.0,
            analysisText = "Mega-cap tech earnings tailwinds driving index above previous resistance. Stochastic indicators show healthy momentum with trailing stop at 19,520."
        )
    )
}
