package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.WaterEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE dateString = :dateString ORDER BY timestamp ASC")
    fun getEntriesForDate(dateString: String): Flow<List<WaterEntry>>

    @Query("SELECT SUM(amountMl) FROM water_entries WHERE dateString = :dateString")
    fun getTotalWaterForDate(dateString: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WaterEntry): Long

    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    @Query("DELETE FROM water_entries WHERE id = (SELECT id FROM water_entries WHERE dateString = :dateString ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLastEntryForDate(dateString: String)
}
