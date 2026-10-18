package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupSettings(
    val isManualWaterTarget: Boolean = false,
    val customTargetWaterMl: Int = 2000,
    val appThemeMode: String = "dark",
    val appLanguage: String = "ru",
    val notificationsEnabled: Boolean = true,
    val intervalHours: Int = 4,
    val tierListMode: String = "auto",
    val autoSaveAiProducts: Boolean = false
)

@JsonClass(generateAdapter = true)
data class KbzhuBackupData(
    val format: String? = null,
    val versionScheme: Int? = null,
    val exportDate: String? = null,
    val userProfile: UserProfile? = null,
    val products: List<Product>? = null,
    val mealEntries: List<MealEntry>? = null,
    val waterEntries: List<WaterEntry>? = null,
    val weightEntries: List<WeightEntry>? = null,
    val savedMeals: List<SavedMeal>? = null,
    val settings: BackupSettings? = null
) {
    val safeProducts: List<Product> get() = products ?: emptyList()
    val safeMealEntries: List<MealEntry> get() = mealEntries ?: emptyList()
    val safeWaterEntries: List<WaterEntry> get() = waterEntries ?: emptyList()
    val safeWeightEntries: List<WeightEntry> get() = weightEntries ?: emptyList()
    val safeSavedMeals: List<SavedMeal> get() = savedMeals ?: emptyList()
}
