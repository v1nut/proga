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

    @Query("SELECT * FROM water_entries ORDER BY timestamp ASC")
    fun getAllEntries(): Flow<List<WaterEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: WaterEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<WaterEntry>): List<Long>

    @Delete
    suspend fun deleteEntry(entry: WaterEntry)

    @Query("DELETE FROM water_entries WHERE id = (SELECT id FROM water_entries WHERE dateString = :dateString ORDER BY timestamp DESC LIMIT 1)")
    suspend fun deleteLastEntryForDate(dateString: String)

    @Query("SELECT * FROM water_entries WHERE dateString = :dateString ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEntryForDate(dateString: String): WaterEntry?
}
