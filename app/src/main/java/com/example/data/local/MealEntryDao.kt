package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MealEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries WHERE dateString = :dateString ORDER BY timestamp ASC")
    fun getEntriesForDate(dateString: String): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE dateString IN (:dates) ORDER BY timestamp ASC")
    fun getEntriesForDates(dates: List<String>): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp ASC")
    fun getAllEntries(): Flow<List<MealEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MealEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<MealEntry>): List<Long>

    @Update
    suspend fun updateEntry(entry: MealEntry)

    @Delete
    suspend fun deleteEntry(entry: MealEntry)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM meal_entries WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
