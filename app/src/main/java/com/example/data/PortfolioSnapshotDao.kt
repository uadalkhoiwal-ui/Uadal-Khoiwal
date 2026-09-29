package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioSnapshotDao {
    @Query("SELECT * FROM portfolio_snapshots ORDER BY timestamp ASC")
    fun getAllSnapshots(): Flow<List<PortfolioSnapshotEntity>>

    @Query("SELECT * FROM portfolio_snapshots ORDER BY timestamp DESC LIMIT 100")
    fun getRecentSnapshots(): Flow<List<PortfolioSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: PortfolioSnapshotEntity): Long

    @Query("DELETE FROM portfolio_snapshots")
    suspend fun clearSnapshots()
}
