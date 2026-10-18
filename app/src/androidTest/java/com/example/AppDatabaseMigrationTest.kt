package com.example

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    @Throws(IOException::class)
    fun migrate4To5() {
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                "INSERT INTO user_profile (id, age, gender, heightCm, weightKg, activityLevel, goal, targetCalories, targetProtein, targetFat, targetCarbs, isManualTarget) " +
                        "VALUES (1, 26, 'MALE', 178.0, 75.0, 'MODERATE', 'MAINTENANCE', 2200, 140.0, 70.0, 250.0, 0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5)

        val cursor = db.query("SELECT targetWeightKg FROM user_profile WHERE id = 1")
        cursor.moveToFirst()
        val targetWeight = cursor.getDouble(0)
        assertEquals(68.0, targetWeight, 0.01)
        cursor.close()
    }
}
