package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val amountMl: Int,
    val timestamp: Long = System.currentTimeMillis()
)
