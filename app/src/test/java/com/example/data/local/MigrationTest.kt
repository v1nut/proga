package com.example.data.local

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    @Test
    fun testMigration1To2CreatesWeightEntriesTable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("test_migration_1_2.db")
        dbFile.delete()

        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration_1_2.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS user_profile (
                            id INTEGER PRIMARY KEY NOT NULL,
                            age INTEGER NOT NULL,
                            gender TEXT NOT NULL,
                            heightCm INTEGER NOT NULL,
                            weightKg REAL NOT NULL,
                            activityLevel TEXT NOT NULL,
                            goal TEXT NOT NULL,
                            targetCalories INTEGER NOT NULL,
                            targetProtein Int NOT NULL,
                            targetFat Int NOT NULL,
                            targetCarbs Int NOT NULL,
                            isManualTarget INTEGER NOT NULL
                        )
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase

        AppDatabase.MIGRATION_1_2.migrate(db)

        val cursor = db.query("SELECT count(*) FROM weight_entries")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()
        db.close()
    }

    @Test
    fun testMigration2To3CreatesSavedMealsTable() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("test_migration_2_3.db")
        dbFile.delete()

        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration_2_3.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(2) {
                override fun onCreate(db: SupportSQLiteDatabase) {}
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase

        AppDatabase.MIGRATION_2_3.migrate(db)

        val cursor = db.query("SELECT count(*) FROM saved_meals")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()
        db.close()
    }

    @Test
    fun testMigration3To4AddsBarcodeAndIsCustomToProducts() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("test_migration_3_4.db")
        dbFile.delete()

        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration_3_4.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(3) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            category TEXT NOT NULL,
                            calories REAL NOT NULL,
                            protein REAL NOT NULL,
                            fat REAL NOT NULL,
                            carbs REAL NOT NULL
                        )
                    """.trimIndent())
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase

        AppDatabase.MIGRATION_3_4.migrate(db)

        val cursor = db.query("PRAGMA table_info(products)")
        var hasBarcode = false
        var hasIsCustom = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == "barcode") hasBarcode = true
            if (name == "isCustom") hasIsCustom = true
        }
        cursor.close()
        assertTrue(hasBarcode)
        assertTrue(hasIsCustom)
        db.close()
    }

    @Test
    fun testMigration4To5AddsTargetWeightToUserProfile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("test_migration_4_5.db")
        dbFile.delete()

        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration_4_5.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(4) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS user_profile (
                            id INTEGER PRIMARY KEY NOT NULL,
                            age INTEGER NOT NULL,
                            gender TEXT NOT NULL,
                            heightCm INTEGER NOT NULL,
                            weightKg REAL NOT NULL,
                            activityLevel TEXT NOT NULL,
                            goal TEXT NOT NULL,
                            targetCalories INTEGER NOT NULL,
                            targetProtein Int NOT NULL,
                            targetFat Int NOT NULL,
                            targetCarbs Int NOT NULL,
                            isManualTarget INTEGER NOT NULL
                        )
                    """.trimIndent())
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase

        AppDatabase.MIGRATION_4_5.migrate(db)

        val cursor = db.query("PRAGMA table_info(user_profile)")
        var hasTargetWeight = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            if (name == "targetWeightKg") hasTargetWeight = true
        }
        cursor.close()
        assertTrue(hasTargetWeight)
        db.close()
    }

    @Test
    fun testMigration5To6ExecutesSuccessfully() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("test_migration_5_6.db")
        dbFile.delete()

        val configuration = androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
            .name("test_migration_5_6.db")
            .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS products (
                            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            name TEXT NOT NULL,
                            category TEXT NOT NULL,
                            calories REAL NOT NULL,
                            protein REAL NOT NULL,
                            fat REAL NOT NULL,
                            carbs REAL NOT NULL,
                            barcode TEXT,
                            isCustom INTEGER NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL("""
                        INSERT INTO products (id, name, category, calories, protein, fat, carbs, isCustom)
                        VALUES (1, 'Яблоко', 'Фрукты', 52.0, 0.3, 0.2, 13.8, 0)
                    """.trimIndent())
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
        val db = helper.writableDatabase

        AppDatabase.MIGRATION_5_6.migrate(db)

        val cursor = db.query("SELECT isFavorite FROM products WHERE id = 1")
        assertTrue(cursor.moveToFirst())
        assertEquals(0, cursor.getInt(0))
        cursor.close()

        val cursorMeals = db.query("SELECT count(*) FROM saved_meals")
        assertTrue(cursorMeals.moveToFirst())
        assertEquals(0, cursorMeals.getInt(0))
        cursorMeals.close()

        db.close()
    }
}
