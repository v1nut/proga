package com.example.data.repository

import com.example.data.calculator.KbzhuCalculator
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.KbzhuBackupData
import com.example.data.model.MealEntry
import com.example.data.model.Product
import com.example.data.model.SavedMeal
import com.example.data.model.UserProfile
import com.example.data.model.WaterEntry
import com.example.data.model.WeightEntry
import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class KbzhuRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val mealEntryDao = database.mealEntryDao()
    private val userProfileDao = database.userProfileDao()
    private val waterEntryDao = database.waterEntryDao()
    private val weightEntryDao = database.weightEntryDao()
    private val savedMealDao = database.savedMealDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val recentProducts: Flow<List<Product>> = productDao.getRecentProducts()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val allWeightEntries: Flow<List<WeightEntry>> = weightEntryDao.getAllWeightEntries()
    val allSavedMeals: Flow<List<SavedMeal>> = savedMealDao.getAllSavedMeals()

    private val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
    private val savedMealItemListAdapter = moshi.adapter<List<com.example.data.model.SavedMealItem>>(
        com.squareup.moshi.Types.newParameterizedType(List::class.java, com.example.data.model.SavedMealItem::class.java)
    )

    suspend fun setProductFavorite(productId: Long, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        productDao.setFavorite(productId, isFavorite)
    }

    suspend fun saveMealAsDish(name: String, entries: List<MealEntry>) = withContext(Dispatchers.IO) {
        val items = entries.map {
            com.example.data.model.SavedMealItem(
                productId = it.productId,
                productName = it.productName,
                weightGrams = it.weightGrams,
                calories = it.calories,
                protein = it.protein,
                fat = it.fat,
                carbs = it.carbs
            )
        }
        val json = savedMealItemListAdapter.toJson(items)
        val savedMeal = SavedMeal(name = name.trim(), itemsJson = json)
        savedMealDao.insertSavedMeal(savedMeal)
    }

    suspend fun deleteSavedMeal(meal: SavedMeal) = withContext(Dispatchers.IO) {
        savedMealDao.deleteSavedMeal(meal)
    }

    fun parseSavedMealItems(itemsJson: String): List<com.example.data.model.SavedMealItem> {
        return try {
            savedMealItemListAdapter.fromJson(itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun cleanInvalidDateEntries(): Int = withContext(Dispatchers.IO) {
        val globPattern = "[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]"
        var deletedCount = 0

        database.withTransaction {
            try {
                deletedCount += database.compileStatement(
                    "DELETE FROM meal_entries WHERE dateString NOT GLOB '$globPattern'"
                ).executeUpdateDelete()

                deletedCount += database.compileStatement(
                    "DELETE FROM water_entries WHERE dateString NOT GLOB '$globPattern'"
                ).executeUpdateDelete()

                deletedCount += database.compileStatement(
                    "DELETE FROM weight_entries WHERE dateString NOT GLOB '$globPattern'"
                ).executeUpdateDelete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        deletedCount
    }

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        cleanInvalidDateEntries()

        val count = productDao.getCount()
        if (count == 0) {
            productDao.insertAll(InitialData.predefinedProducts)
        } else {
            val existingList = productDao.getAllProducts().first()
            val existingNames = existingList.map { it.name.lowercase() }.toSet()
            val missing = InitialData.predefinedProducts.filter { !existingNames.contains(it.name.lowercase()) }
            if (missing.isNotEmpty()) {
                productDao.insertAll(missing)
            }
        }

        val existingProfile = userProfileDao.getProfileSync()
        if (existingProfile == null) {
            val calc = KbzhuCalculator.calculate(
                gender = Gender.MALE,
                age = 26,
                heightCm = 178.0,
                weightKg = 75.0,
                activityLevel = ActivityLevel.MODERATE,
                goal = FitnessGoal.MAINTENANCE
            )
            val defaultProfile = UserProfile(
                id = 1,
                age = 26,
                gender = Gender.MALE.name,
                heightCm = 178.0,
                weightKg = 75.0,
                activityLevel = ActivityLevel.MODERATE.name,
                goal = FitnessGoal.MAINTENANCE.name,
                targetCalories = calc.targetCalories,
                targetProtein = calc.targetProtein,
                targetFat = calc.targetFat,
                targetCarbs = calc.targetCarbs,
                isManualTarget = false
            )
            userProfileDao.insertOrUpdate(defaultProfile)
        }

        if (weightEntryDao.getAllWeightEntries().first().isEmpty()) {
            val profile = userProfileDao.getProfileSync() ?: UserProfile()
            val today = java.time.LocalDate.now().toString()
            weightEntryDao.insertOrUpdateWeight(WeightEntry(dateString = today, weightKg = profile.weightKg))
        }
    }

    fun getEntriesForDate(dateString: String): Flow<List<MealEntry>> {
        return mealEntryDao.getEntriesForDate(dateString)
    }

    fun getEntriesForDates(dates: List<String>): Flow<List<MealEntry>> {
        return mealEntryDao.getEntriesForDates(dates)
    }

    suspend fun addMealEntry(entry: MealEntry) = withContext(Dispatchers.IO) {
        mealEntryDao.insertEntry(entry)
    }

    suspend fun addMealEntries(entries: List<MealEntry>): List<Long> = withContext(Dispatchers.IO) {
        database.withTransaction {
            mealEntryDao.insertEntries(entries)
        }
    }

    suspend fun updateMealEntry(entry: MealEntry) = withContext(Dispatchers.IO) {
        mealEntryDao.updateEntry(entry)
    }

    suspend fun deleteMealEntry(entry: MealEntry) = withContext(Dispatchers.IO) {
        mealEntryDao.deleteEntry(entry)
    }

    suspend fun deleteMealEntryById(id: Long) = withContext(Dispatchers.IO) {
        mealEntryDao.deleteEntryById(id)
    }

    suspend fun deleteMealEntriesByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        if (ids.isNotEmpty()) {
            database.withTransaction {
                mealEntryDao.deleteByIds(ids)
            }
        }
    }

    fun getTotalWaterForDate(dateString: String): Flow<Int?> {
        return waterEntryDao.getTotalWaterForDate(dateString)
    }

    suspend fun addWater(dateString: String, amountMl: Int) = withContext(Dispatchers.IO) {
        waterEntryDao.insertEntry(WaterEntry(dateString = dateString, amountMl = amountMl))
    }

    suspend fun removeLastWater(dateString: String) = withContext(Dispatchers.IO) {
        waterEntryDao.deleteLastEntryForDate(dateString)
    }

    suspend fun removeLastWaterWithReturn(dateString: String): WaterEntry? = withContext(Dispatchers.IO) {
        val last = waterEntryDao.getLastEntryForDate(dateString)
        if (last != null) {
            waterEntryDao.deleteEntry(last)
        }
        last
    }

    suspend fun restoreWaterEntry(entry: WaterEntry) = withContext(Dispatchers.IO) {
        waterEntryDao.insertEntry(entry)
    }

    suspend fun findProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        productDao.findProductByBarcode(barcode.trim())
    }

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.insertProduct(product)
    }

    suspend fun createCustomProduct(
        name: String,
        category: String,
        calories: Double,
        protein: Double,
        fat: Double,
        carbs: Double,
        barcode: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val product = Product(
            name = name.trim(),
            category = category.trim().ifEmpty { "Мои продукты" },
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            barcode = barcode?.trim()?.ifEmpty { null },
            isCustom = true
        )
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun saveProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        userProfileDao.insertOrUpdate(profile)
    }

    suspend fun logWeight(dateString: String, weightKg: Double, note: String = "") = withContext(Dispatchers.IO) {
        weightEntryDao.insertOrUpdateWeight(WeightEntry(dateString = dateString, weightKg = weightKg, note = note))
        if (dateString == java.time.LocalDate.now().toString()) {
            val profile = userProfileDao.getProfileSync() ?: UserProfile()
            val updatedProfile = if (!profile.isManualTarget) {
                val genderEnum = try { Gender.valueOf(profile.gender) } catch (e: Exception) { Gender.MALE }
                val activityEnum = try { ActivityLevel.valueOf(profile.activityLevel) } catch (e: Exception) { ActivityLevel.MODERATE }
                val goalEnum = try { FitnessGoal.valueOf(profile.goal) } catch (e: Exception) { FitnessGoal.MAINTENANCE }
                val calc = KbzhuCalculator.calculate(
                    gender = genderEnum,
                    age = profile.age,
                    heightCm = profile.heightCm,
                    weightKg = weightKg,
                    activityLevel = activityEnum,
                    goal = goalEnum
                )
                profile.copy(
                    weightKg = weightKg,
                    targetCalories = calc.targetCalories,
                    targetProtein = calc.targetProtein,
                    targetFat = calc.targetFat,
                    targetCarbs = calc.targetCarbs
                )
            } else {
                profile.copy(weightKg = weightKg)
            }
            userProfileDao.insertOrUpdate(updatedProfile)
        }
    }

    suspend fun deleteWeightForDate(dateString: String) = withContext(Dispatchers.IO) {
        weightEntryDao.deleteWeightForDate(dateString)
    }

    suspend fun createBackupData(prefs: android.content.SharedPreferences): KbzhuBackupData = withContext(Dispatchers.IO) {
        val profile = userProfileDao.getProfileSync()
        val products = productDao.getAllProducts().first()
        val meals = mealEntryDao.getAllEntries().first()
        val water = waterEntryDao.getAllEntries().first()
        val weight = weightEntryDao.getAllWeightEntries().first()
        val savedMeals = savedMealDao.getAllSavedMeals().first()

        val settings = com.example.data.model.BackupSettings(
            isManualWaterTarget = prefs.getBoolean("is_manual_water_target", false),
            customTargetWaterMl = prefs.getInt("custom_target_water_ml", 2000),
            appThemeMode = prefs.getString("app_theme_mode", "dark") ?: "dark",
            appLanguage = prefs.getString("app_language", "ru") ?: "ru",
            notificationsEnabled = prefs.getBoolean(com.example.notification.InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true),
            intervalHours = prefs.getInt(com.example.notification.InactivityReminderManager.KEY_INTERVAL_HOURS, 4),
            tierListMode = prefs.getString("tier_list_mode", "auto") ?: "auto",
            autoSaveAiProducts = prefs.getBoolean("auto_save_ai_products", false)
        )

        KbzhuBackupData(
            format = "kbzhu-backup",
            versionScheme = 6,
            exportDate = java.time.LocalDate.now().toString(),
            userProfile = profile,
            products = products,
            mealEntries = meals,
            waterEntries = water,
            weightEntries = weight,
            savedMeals = savedMeals,
            settings = settings
        )
    }

    suspend fun restoreBackupData(data: KbzhuBackupData, prefs: android.content.SharedPreferences) = withContext(Dispatchers.IO) {
        database.withTransaction {
            if (data.userProfile != null) {
                userProfileDao.insertOrUpdate(data.userProfile)
            }
            if (data.products != null) {
                database.openHelper.writableDatabase.compileStatement("DELETE FROM products").execute()
                if (data.products.isNotEmpty()) {
                    productDao.insertAll(data.products)
                }
            }
            if (data.mealEntries != null) {
                database.openHelper.writableDatabase.compileStatement("DELETE FROM meal_entries").execute()
                if (data.mealEntries.isNotEmpty()) {
                    mealEntryDao.insertEntries(data.mealEntries)
                }
            }
            if (data.waterEntries != null) {
                database.openHelper.writableDatabase.compileStatement("DELETE FROM water_entries").execute()
                if (data.waterEntries.isNotEmpty()) {
                    waterEntryDao.insertEntries(data.waterEntries)
                }
            }
            if (data.weightEntries != null) {
                database.openHelper.writableDatabase.compileStatement("DELETE FROM weight_entries").execute()
                if (data.weightEntries.isNotEmpty()) {
                    weightEntryDao.insertEntries(data.weightEntries)
                }
            }
            if (data.savedMeals != null) {
                database.openHelper.writableDatabase.compileStatement("DELETE FROM saved_meals").execute()
                if (data.savedMeals.isNotEmpty()) {
                    for (meal in data.savedMeals) {
                        savedMealDao.insertSavedMeal(meal)
                    }
                }
            }
        }

        if (data.settings != null) {
            prefs.edit()
                .putBoolean("is_manual_water_target", data.settings.isManualWaterTarget)
                .putInt("custom_target_water_ml", data.settings.customTargetWaterMl)
                .putString("app_theme_mode", data.settings.appThemeMode)
                .putString("app_language", data.settings.appLanguage)
                .putBoolean(com.example.notification.InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, data.settings.notificationsEnabled)
                .putInt(com.example.notification.InactivityReminderManager.KEY_INTERVAL_HOURS, data.settings.intervalHours)
                .putString("tier_list_mode", data.settings.tierListMode)
                .putBoolean("auto_save_ai_products", data.settings.autoSaveAiProducts)
                .apply()
        }
    }
}
