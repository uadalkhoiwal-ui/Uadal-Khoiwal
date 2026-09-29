package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_snapshots")
data class PortfolioSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalBalance: Double,
    val totalEquity: Double,
    val unrealizedPnl: Double,
    val realizedPnl: Double,
    val openPositionsCount: Int
)
