package com.example

import com.example.data.MarketDataProvider
import com.example.data.PortfolioSnapshotEntity
import com.example.data.TradeEntity
import com.example.model.MarketCategory
import com.example.model.Timeframe
import com.example.model.TradePosition
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultMarkets_containAllCategories() {
        val markets = MarketDataProvider.defaultMarkets
        assertTrue(markets.isNotEmpty())

        val categories = markets.map { it.category }.toSet()
        assertTrue(categories.contains(MarketCategory.CRYPTO))
        assertTrue(categories.contains(MarketCategory.STOCKS))
        assertTrue(categories.contains(MarketCategory.FOREX))
        assertTrue(categories.contains(MarketCategory.COMMODITIES))
        assertTrue(categories.contains(MarketCategory.INDICES))
    }

    @Test
    fun generateCandles_returnsValidCandles() {
        val candles = MarketDataProvider.generateCandles(68000.0, Timeframe.D1, count = 20)
        assertEquals(21, candles.size)
        candles.forEach { candle ->
            assertTrue("High should be >= Low", candle.high >= candle.low)
            assertTrue("High should be >= Open", candle.high >= candle.open)
            assertTrue("High should be >= Close", candle.high >= candle.close)
            assertTrue("Low should be <= Open", candle.low <= candle.open)
            assertTrue("Low should be <= Close", candle.low <= candle.close)
        }
    }

    @Test
    fun sampleSignals_haveNonEmptyData() {
        val signals = MarketDataProvider.sampleSignals
        assertTrue(signals.isNotEmpty())
        signals.forEach { signal ->
            assertNotNull(signal.symbol)
            assertTrue(signal.confidence in 0..100)
            assertTrue(signal.rsi in 0f..100f)
            assertTrue(signal.target1 > 0)
        }
    }

    @Test
    fun tradePosition_entityConversion_isLossless() {
        val original = TradePosition(
            id = 42L,
            symbol = "BINANCE:BTCUSDT",
            name = "Bitcoin / USD",
            isBuy = true,
            entryPrice = 65000.0,
            currentPrice = 68000.0,
            quantity = 0.5,
            investedAmount = 32500.0,
            takeProfit = 70000.0,
            stopLoss = 63000.0,
            pnl = 1500.0,
            pnlPercent = 4.61,
            isClosed = false,
            closeReason = "",
            openTime = 1000L,
            closeTime = null
        )

        val entity = TradeEntity.fromDomain(original)
        val restored = entity.toDomain()

        assertEquals(original.id, restored.id)
        assertEquals(original.symbol, restored.symbol)
        assertEquals(original.isBuy, restored.isBuy)
        assertEquals(original.entryPrice, restored.entryPrice, 0.001)
        assertEquals(original.pnl, restored.pnl, 0.001)
        assertEquals(original.pnlPercent, restored.pnlPercent, 0.001)
    }

    @Test
    fun portfolioBalanceCalculation_isAccurate() {
        val cashBalance = 10000.0
        val activeTrades = listOf(
            TradePosition(
                id = 1L,
                symbol = "BINANCE:BTCUSDT",
                name = "Bitcoin / USD",
                isBuy = true,
                entryPrice = 68000.0,
                currentPrice = 69000.0,
                quantity = 0.1,
                investedAmount = 6800.0,
                takeProfit = 70000.0,
                stopLoss = 66000.0,
                pnl = 100.0,
                pnlPercent = 1.47,
                isClosed = false
            ),
            TradePosition(
                id = 2L,
                symbol = "NASDAQ:AAPL",
                name = "Apple Inc.",
                isBuy = false,
                entryPrice = 230.0,
                currentPrice = 228.0,
                quantity = 5.0,
                investedAmount = 1150.0,
                takeProfit = 220.0,
                stopLoss = 235.0,
                pnl = 10.0,
                pnlPercent = 0.87,
                isClosed = false
            )
        )

        val totalUnrealizedPnl = activeTrades.sumOf { it.pnl }
        val totalPortfolioBalance = cashBalance + totalUnrealizedPnl
        val totalMargin = activeTrades.sumOf { it.investedAmount }

        assertEquals(110.0, totalUnrealizedPnl, 0.001)
        assertEquals(10110.0, totalPortfolioBalance, 0.001)
        assertEquals(7950.0, totalMargin, 0.001)
        assertEquals(2, activeTrades.size)
    }

    @Test
    fun portfolioSnapshotEntity_holdsValidEquity() {
        val snapshot = PortfolioSnapshotEntity(
            id = 1L,
            totalBalance = 10500.0,
            totalEquity = 11200.0,
            unrealizedPnl = 700.0,
            realizedPnl = 500.0,
            openPositionsCount = 3
        )
        assertEquals(11200.0, snapshot.totalEquity, 0.001)
        assertEquals(700.0, snapshot.unrealizedPnl, 0.001)
        assertEquals(3, snapshot.openPositionsCount)
    }
}
