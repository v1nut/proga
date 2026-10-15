package com.example.data.repository

import com.example.data.calculator.KbzhuCalculator
import com.example.data.local.AppDatabase
import com.example.data.local.InitialData
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.MealEntry
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.model.WaterEntry
import com.example.data.model.WeightEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class KbzhuRepository(private val database: AppDatabase) {

    private val productDao = database.productDao()
    private val mealEntryDao = database.mealEntryDao()
    private val userProfileDao = database.userProfileDao()
    private val waterEntryDao = database.waterEntryDao()
    private val weightEntryDao = database.weightEntryDao()

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val userProfile: Flow<UserProfile?> = userProfileDao.getUserProfile()
    val allWeightEntries: Flow<List<WeightEntry>> = weightEntryDao.getAllWeightEntries()

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val count = productDao.getCount()
        if (count == 0) {
            productDao.insertAll(InitialData.predefinedProducts)
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

        // Initialize 30 days of weight records if empty
        val today = java.time.LocalDate.now()
        val start30 = today.minusDays(29)
        // Seed 30-day weight history with realistic fluctuations
        val profile = userProfileDao.getProfileSync() ?: UserProfile()
        val currentKg = profile.weightKg
        val entries = (0..29).map { i ->
            val date = start30.plusDays(i.toLong()).toString()
            // Trend smoothly from +2.4 kg 30 days ago to currentKg today with slight daily realistic jitter
            val progressRatio = i / 29.0
            val noise = ((i * 7) % 5 - 2) * 0.12 // -0.24 to +0.24 kg
            val baseWeight = (currentKg + 2.4) - (progressRatio * 2.4) + noise
            val rounded = (baseWeight * 10).roundToInt() / 10.0
            com.example.data.model.WeightEntry(dateString = date, weightKg = rounded)
        }
        entries.forEach { entry ->
            weightEntryDao.insertOrUpdateWeight(entry)
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

    suspend fun updateMealEntry(entry: MealEntry) = withContext(Dispatchers.IO) {
        mealEntryDao.updateEntry(entry)
    }

    suspend fun deleteMealEntry(entry: MealEntry) = withContext(Dispatchers.IO) {
        mealEntryDao.deleteEntry(entry)
    }

    suspend fun deleteMealEntryById(id: Long) = withContext(Dispatchers.IO) {
        mealEntryDao.deleteEntryById(id)
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

    suspend fun findProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        productDao.findProductByBarcode(barcode.trim())
    }

    suspend fun insertProduct(product: Product): Long = withContext(Dispatchers.IO) {
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
        weightEntryDao.insertOrUpdateWeight(com.example.data.model.WeightEntry(dateString = dateString, weightKg = weightKg, note = note))
        val profile = userProfileDao.getProfileSync() ?: UserProfile()
        userProfileDao.insertOrUpdate(profile.copy(weightKg = weightKg))
    }
}
