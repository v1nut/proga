package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val calories: Double, // kcal per 100g
    val protein: Double,  // g per 100g
    val fat: Double,      // g per 100g
    val carbs: Double,    // g per 100g
    val barcode: String? = null,
    val isCustom: Boolean = false
) {
    fun calculateForWeight(grams: Double): MacroCalculations {
        val multiplier = grams / 100.0
        return MacroCalculations(
            calories = calories * multiplier,
            protein = protein * multiplier,
            fat = fat * multiplier,
            carbs = carbs * multiplier
        )
    }
}

data class MacroCalculations(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
