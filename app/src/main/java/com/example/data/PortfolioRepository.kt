package com.example.data

import com.example.model.TradePosition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PortfolioRepository(
    private val tradeDao: TradeDao,
    private val snapshotDao: PortfolioSnapshotDao
) {
    val openTrades: Flow<List<TradePosition>> = tradeDao.getOpenTrades()
        .map { list -> list.map { it.toDomain() } }

    val closedTrades: Flow<List<TradePosition>> = tradeDao.getClosedTrades()
        .map { list -> list.map { it.toDomain() } }

    val allTrades: Flow<List<TradePosition>> = tradeDao.getAllTrades()
        .map { list -> list.map { it.toDomain() } }

    val totalRealizedPnl: Flow<Double> = tradeDao.getTotalRealizedPnl()
        .map { it ?: 0.0 }

    val snapshots: Flow<List<PortfolioSnapshotEntity>> = snapshotDao.getAllSnapshots()

    suspend fun insertTrade(trade: TradePosition): Long {
        return tradeDao.insertTrade(TradeEntity.fromDomain(trade))
    }

    suspend fun updateTrade(trade: TradePosition) {
        tradeDao.updateTrade(TradeEntity.fromDomain(trade))
    }

    suspend fun closeTrade(
        tradeId: Long,
        exitPrice: Double,
        pnl: Double,
        pnlPercent: Double,
        reason: String
    ) {
        tradeDao.closeTrade(
            id = tradeId,
            exitPrice = exitPrice,
            pnl = pnl,
            pnlPercent = pnlPercent,
            reason = reason,
            closeTime = System.currentTimeMillis()
        )
    }

    suspend fun recordSnapshot(
        totalBalance: Double,
        totalEquity: Double,
        unrealizedPnl: Double,
        realizedPnl: Double,
        openCount: Int
    ) {
        snapshotDao.insertSnapshot(
            PortfolioSnapshotEntity(
                totalBalance = totalBalance,
                totalEquity = totalEquity,
                unrealizedPnl = unrealizedPnl,
                realizedPnl = realizedPnl,
                openPositionsCount = openCount
            )
        )
    }

    suspend fun clearPortfolio() {
        tradeDao.clearAllTrades()
        snapshotDao.clearSnapshots()
    }
}
