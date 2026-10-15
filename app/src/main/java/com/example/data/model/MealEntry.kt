package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val titleRu: String) {
    BREAKFAST("Завтрак"),
    LUNCH("Обед"),
    DINNER("Ужин"),
    SNACK("Перекусы");

    companion object {
        fun fromString(value: String): MealType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: BREAKFAST
        }
    }
}

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val mealType: String,   // BREAKFAST, LUNCH, DINNER, SNACK
    val productId: Long? = null,
    val productName: String,
    val weightGrams: Double,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val timestamp: Long = System.currentTimeMillis()
)
