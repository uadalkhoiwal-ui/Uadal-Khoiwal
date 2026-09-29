package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.TradePosition

@Entity(tableName = "trade_positions")
data class TradeEntity(
    @PrimaryKey(autoGenerate = true)
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
    val isClosed: Boolean,
    val closeReason: String,
    val openTime: Long,
    val closeTime: Long?
) {
    fun toDomain(): TradePosition = TradePosition(
        id = id,
        symbol = symbol,
        name = name,
        isBuy = isBuy,
        entryPrice = entryPrice,
        currentPrice = currentPrice,
        quantity = quantity,
        investedAmount = investedAmount,
        takeProfit = takeProfit,
        stopLoss = stopLoss,
        pnl = pnl,
        pnlPercent = pnlPercent,
        isClosed = isClosed,
        closeReason = closeReason,
        openTime = openTime,
        closeTime = closeTime
    )

    companion object {
        fun fromDomain(domain: TradePosition): TradeEntity = TradeEntity(
            id = domain.id,
            symbol = domain.symbol,
            name = domain.name,
            isBuy = domain.isBuy,
            entryPrice = domain.entryPrice,
            currentPrice = domain.currentPrice,
            quantity = domain.quantity,
            investedAmount = domain.investedAmount,
            takeProfit = domain.takeProfit,
            stopLoss = domain.stopLoss,
            pnl = domain.pnl,
            pnlPercent = domain.pnlPercent,
            isClosed = domain.isClosed,
            closeReason = domain.closeReason,
            openTime = domain.openTime,
            closeTime = domain.closeTime
        )
    }
}
