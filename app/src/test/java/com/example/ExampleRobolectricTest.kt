package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.Product
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun `read app_name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("КБЖУ Рацион", appName)
    }

    @Test
    fun `insert and retrieve product from database`() = runBlocking {
        val product = Product(
            name = "Творог 5%",
            category = "Молочные продукты",
            calories = 121.0,
            protein = 17.2,
            fat = 5.0,
            carbs = 1.8,
            barcode = "4605432109876",
            isCustom = false
        )

        val id = database.productDao().insertProduct(product)
        val products = database.productDao().getAllProducts().first()
        assertEquals(1, products.size)
        assertEquals("Творог 5%", products[0].name)
        assertEquals("4605432109876", products[0].barcode)
    }

    @Test
    fun `insert and find product by barcode`() = runBlocking {
        val product = Product(
            name = "Греческий йогурт 2%",
            category = "Молочные продукты",
            calories = 67.0,
            protein = 9.0,
            fat = 2.0,
            carbs = 3.5,
            barcode = "4601234000028"
        )
        database.productDao().insertProduct(product)

        val found = database.productDao().findProductByBarcode("4601234000028")
        assertNotNull(found)
        assertEquals("Греческий йогурт 2%", found?.name)
        assertEquals(67.0, found?.calories ?: 0.0, 0.01)

        val notFound = database.productDao().findProductByBarcode("9999999999999")
        assertNull(notFound)
    }

    @Test
    fun `insert and retrieve meal entry for date`() = runBlocking {
        val entry = MealEntry(
            dateString = "2026-10-14",
            mealType = MealType.BREAKFAST.name,
            productName = "Овсяные хлопья",
            weightGrams = 60.0,
            calories = 211.2,
            protein = 7.38,
            fat = 3.72,
            carbs = 37.08
        )

        database.mealEntryDao().insertEntry(entry)
        val entries = database.mealEntryDao().getEntriesForDate("2026-10-14").first()
        assertEquals(1, entries.size)
        assertEquals("Овсяные хлопья", entries[0].productName)
        assertEquals(211.2, entries[0].calories, 0.01)
    }

    @Test
    fun `inactivity reminder updates last activity timestamp`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        com.example.notification.InactivityReminderManager.onUserActivity(context)

        val prefs = context.getSharedPreferences(
            com.example.notification.InactivityReminderManager.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        val timestamp = prefs.getLong(
            com.example.notification.InactivityReminderManager.KEY_LAST_ACTIVITY_TIME,
            0L
        )
        org.junit.Assert.assertTrue("Timestamp should be greater than 0", timestamp > 0)
    }

    @Test
    fun `gemini food service returns error when api key is not configured`() = runBlocking {
        val service = com.example.data.service.GeminiFoodService()
        val result = service.parseQuickMealText("неизвестный продукт абракадабра")
        org.junit.Assert.assertTrue(result.isFailure)
        org.junit.Assert.assertNotNull(result.exceptionOrNull()?.message)
    }
}
