package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_meals")
data class SavedMeal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val itemsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class SavedMealItem(
    val productId: Long? = null,
    val productName: String,
    val weightGrams: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
