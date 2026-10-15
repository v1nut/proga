package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entries ORDER BY dateString ASC")
    fun getAllWeightEntries(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE dateString >= :startDate ORDER BY dateString ASC")
    fun getWeightEntriesSince(startDate: String): Flow<List<WeightEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWeight(entry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE dateString = :dateString")
    suspend fun deleteWeightForDate(dateString: String)
}
