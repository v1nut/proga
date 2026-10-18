package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedMeal
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedMealDao {
    @Query("SELECT * FROM saved_meals ORDER BY id DESC")
    fun getAllSavedMeals(): Flow<List<SavedMeal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedMeal(meal: SavedMeal): Long

    @Delete
    suspend fun deleteSavedMeal(meal: SavedMeal)
}
