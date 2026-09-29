package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TradeDao {
    @Query("SELECT * FROM trade_positions WHERE isClosed = 0 ORDER BY openTime DESC")
    fun getOpenTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trade_positions WHERE isClosed = 1 ORDER BY closeTime DESC LIMIT 100")
    fun getClosedTrades(): Flow<List<TradeEntity>>

    @Query("SELECT * FROM trade_positions ORDER BY openTime DESC LIMIT 150")
    fun getAllTrades(): Flow<List<TradeEntity>>

    @Query("SELECT SUM(pnl) FROM trade_positions WHERE isClosed = 1")
    fun getTotalRealizedPnl(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM trade_positions WHERE isClosed = 1 AND pnl > 0")
    fun getWinTradesCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM trade_positions WHERE isClosed = 1")
    fun getClosedTradesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrade(trade: TradeEntity): Long

    @Update
    suspend fun updateTrade(trade: TradeEntity)

    @Query("UPDATE trade_positions SET isClosed = 1, closeTime = :closeTime, closeReason = :reason, currentPrice = :exitPrice, pnl = :pnl, pnlPercent = :pnlPercent WHERE id = :id")
    suspend fun closeTrade(id: Long, exitPrice: Double, pnl: Double, pnlPercent: Double, reason: String, closeTime: Long)

    @Query("DELETE FROM trade_positions")
    suspend fun clearAllTrades()
}
