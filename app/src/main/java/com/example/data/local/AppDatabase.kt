package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.MealEntry
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.model.WaterEntry
import com.example.data.model.WeightEntry

@Database(
    entities = [Product::class, MealEntry::class, UserProfile::class, WaterEntry::class, WeightEntry::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kbzhu_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
