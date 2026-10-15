package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey
    val dateString: String, // YYYY-MM-DD
    val weightKg: Double,
    val note: String = ""
)
