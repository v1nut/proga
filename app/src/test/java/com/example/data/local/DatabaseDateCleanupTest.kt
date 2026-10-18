package com.example.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.WaterEntry
import com.example.data.model.WeightEntry
import com.example.data.repository.KbzhuRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseDateCleanupTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: KbzhuRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = KbzhuRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testCleanInvalidDateEntriesDeletesGarbageDates() = runBlocking {
        // Insert valid ISO 8601 entries
        database.mealEntryDao().insertEntry(
            MealEntry(dateString = "2026-10-16", mealType = MealType.BREAKFAST.name, productName = "Овсянка", weightGrams = 100.0, calories = 150.0, protein = 5.0, fat = 2.0, carbs = 25.0)
        )
        database.waterEntryDao().insertEntry(WaterEntry(dateString = "2026-10-16", amountMl = 250))
        database.weightEntryDao().insertOrUpdateWeight(WeightEntry(dateString = "2026-10-16", weightKg = 75.0))

        // Insert invalid / garbage date entries
        database.mealEntryDao().insertEntry(
            MealEntry(dateString = "invalid_date_123", mealType = MealType.LUNCH.name, productName = "Мусорный обед", weightGrams = 200.0, calories = 300.0, protein = 10.0, fat = 5.0, carbs = 40.0)
        )
        database.waterEntryDao().insertEntry(WaterEntry(dateString = "bad-date-format", amountMl = 500))
        database.weightEntryDao().insertOrUpdateWeight(WeightEntry(dateString = "2026/10/16", weightKg = 80.0))

        // Execute cleanup
        val deletedCount = repository.cleanInvalidDateEntries()

        // 3 invalid entries should be deleted
        assertEquals(3, deletedCount)

        // Verify valid entries remain
        val meals = database.mealEntryDao().getEntriesForDate("2026-10-16").first()
        assertEquals(1, meals.size)
        assertEquals("Овсянка", meals[0].productName)

        val invalidMeals = database.mealEntryDao().getEntriesForDate("invalid_date_123").first()
        assertEquals(0, invalidMeals.size)

        val water = database.waterEntryDao().getTotalWaterForDate("2026-10-16").first()
        assertEquals(250, water)

        val weights = database.weightEntryDao().getAllWeightEntries().first()
        assertEquals(1, weights.size)
        assertEquals("2026-10-16", weights[0].dateString)
    }
}
