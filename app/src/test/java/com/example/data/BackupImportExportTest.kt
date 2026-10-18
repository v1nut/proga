package com.example.data

import com.example.data.model.BackupSettings
import com.example.data.model.KbzhuBackupData
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupImportExportTest {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(KbzhuBackupData::class.java)

    @Test
    fun testEmptyJsonParsingResultInNullFields() {
        val json = "{}"
        val parsed = adapter.fromJson(json)

        assertNotNull(parsed)
        assertNull(parsed?.format)
        assertNull(parsed?.versionScheme)
        assertNull(parsed?.userProfile)
        assertNull(parsed?.products)
        assertNull(parsed?.mealEntries)
        assertNull(parsed?.waterEntries)
        assertNull(parsed?.weightEntries)
        assertNull(parsed?.savedMeals)
    }

    @Test
    fun testValidExportFormatRoundtrip() {
        val original = KbzhuBackupData(
            format = "kbzhu-backup",
            versionScheme = 6,
            exportDate = "2026-10-17",
            userProfile = UserProfile(age = 28, weightKg = 75.0),
            products = listOf(
                Product(
                    name = "Яблоко",
                    category = "Фрукты и ягоды",
                    calories = 52.0,
                    protein = 0.3,
                    fat = 0.2,
                    carbs = 13.8
                )
            ),
            settings = BackupSettings(appLanguage = "en", tierListMode = "loss")
        )

        val json = adapter.toJson(original)
        assertTrue(json.contains("\"format\":\"kbzhu-backup\""))

        val restored = adapter.fromJson(json)
        assertNotNull(restored)
        assertEquals("kbzhu-backup", restored?.format)
        assertEquals(6, restored?.versionScheme)
        assertEquals("2026-10-17", restored?.exportDate)
        assertEquals(75.0, restored?.userProfile?.weightKg ?: 0.0, 0.01)
        assertEquals(1, restored?.safeProducts?.size)
        assertEquals("Яблоко", restored?.safeProducts?.first()?.name)
        assertEquals("en", restored?.settings?.appLanguage)
        assertEquals("loss", restored?.settings?.tierListMode)
    }
}
