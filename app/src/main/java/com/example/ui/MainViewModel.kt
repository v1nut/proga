package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calculator.KbzhuCalculator
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.repository.KbzhuRepository
import com.example.ui.components.DayTrendData
import com.example.ui.util.SmartFoodSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

enum class AppTab(val titleRu: String) {
    DASHBOARD("Главная"),
    DIARY("Дневник"),
    CATALOG("Продукты"),
    PROFILE("Профиль"),
    SETTINGS("Настройки"),
    LOGIN("Вход")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KbzhuRepository(AppDatabase.getInstance(application))
    private val geminiFoodService = com.example.data.service.GeminiFoodService()
    val googleFitManager = com.example.data.manager.GoogleFitManager(application)
    val healthConnectManager = com.example.data.manager.HealthConnectManager(application)
    val authManager = com.example.data.manager.AuthManager(application)

    val googleFitData: StateFlow<com.example.data.manager.GoogleFitActivityData> = googleFitManager.fitData
    val healthStepData: StateFlow<com.example.data.manager.HealthStepData> = healthConnectManager.stepData
    val authState: StateFlow<com.example.data.manager.UserAuthState> = authManager.authState

    private val _selectedTab = MutableStateFlow(AppTab.DASHBOARD)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _currentDate = MutableStateFlow(LocalDate.now())
    val currentDate: StateFlow<LocalDate> = _currentDate.asStateFlow()

    // Active dialogs
    private val _activePortionProduct = MutableStateFlow<Product?>(null)
    val activePortionProduct: StateFlow<Product?> = _activePortionProduct.asStateFlow()

    private val _activePortionMeal = MutableStateFlow(MealType.BREAKFAST)
    val activePortionMeal: StateFlow<MealType> = _activePortionMeal.asStateFlow()

    private val _isScannerOpen = MutableStateFlow(false)
    val isScannerOpen: StateFlow<Boolean> = _isScannerOpen.asStateFlow()

    private val _scannedStatusMessage = MutableStateFlow<String?>(null)
    val scannedStatusMessage: StateFlow<String?> = _scannedStatusMessage.asStateFlow()

    private val _notFoundBarcode = MutableStateFlow<String?>(null)
    val notFoundBarcode: StateFlow<String?> = _notFoundBarcode.asStateFlow()

    private val _isCreateProductOpen = MutableStateFlow(false)
    val isCreateProductOpen: StateFlow<Boolean> = _isCreateProductOpen.asStateFlow()

    private val _createProductInitialBarcode = MutableStateFlow<String?>(null)
    val createProductInitialBarcode: StateFlow<String?> = _createProductInitialBarcode.asStateFlow()

    // Catalog filtering
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("Все")

    // User profile
    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weightEntries: StateFlow<List<com.example.data.model.WeightEntry>> = repository.allWeightEntries
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        searchQuery,
        selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesQuery = SmartFoodSearch.matches(product, query)

            val matchesCategory = when (category) {
                "Все" -> true
                "Мои продукты", "Свои продукты" -> product.isCustom || product.category.equals("Мои продукты", ignoreCase = true) || product.category.equals("Свои продукты", ignoreCase = true)
                else -> product.category.equals(category, ignoreCase = true)
            }

            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current date entries
    val currentDayEntries: StateFlow<List<MealEntry>> = _currentDate
        .flatMapLatest { date ->
            repository.getEntriesForDate(date.toString())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Week calories map for 7-day calendar bar (Monday through Sunday)
    val weekCaloriesMap: StateFlow<Map<String, Double>> = _currentDate
        .flatMapLatest { date ->
            val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            val dates = (0..6).map { monday.plusDays(it.toLong()).toString() }
            repository.getEntriesForDates(dates).map { entries ->
                entries.groupBy { it.dateString }
                    .mapValues { (_, dayEntries) -> dayEntries.sumOf { it.calories } }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Weekly progress trend for the past 7 days up to selected date
    val weeklyTrendData: StateFlow<List<DayTrendData>> = _currentDate
        .flatMapLatest { date ->
            val past7Days = (6 downTo 0).map { date.minusDays(it.toLong()) }
            val dateStrings = past7Days.map { it.toString() }
            repository.getEntriesForDates(dateStrings).map { entries ->
                val byDate = entries.groupBy { it.dateString }
                past7Days.map { dayDate ->
                    val dayEntries = byDate[dayDate.toString()] ?: emptyList()
                    DayTrendData(
                        date = dayDate,
                        calories = dayEntries.sumOf { it.calories },
                        protein = dayEntries.sumOf { it.protein },
                        fat = dayEntries.sumOf { it.fat },
                        carbs = dayEntries.sumOf { it.carbs }
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Water intake tracking for the selected date
    val currentDayWater: StateFlow<Int> = _currentDate
        .flatMapLatest { date ->
            repository.getTotalWaterForDate(date.toString()).map { it ?: 0 }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Target water calculated automatically based on body weight (30ml per kg)
    val targetWaterMl: StateFlow<Int> = userProfile.map { profile ->
        (profile.weightKg * 30.0).roundToInt().coerceAtLeast(1500)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2100
    )

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
        }
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setTargetMeal(mealType: MealType) {
        _activePortionMeal.value = mealType
    }

    fun nextDay() {
        _currentDate.value = _currentDate.value.plusDays(1)
    }

    fun previousDay() {
        _currentDate.value = _currentDate.value.minusDays(1)
    }

    fun setDate(date: LocalDate) {
        _currentDate.value = date
    }

    fun openPortionDialog(product: Product, mealType: MealType = MealType.BREAKFAST) {
        _activePortionProduct.value = product
        _activePortionMeal.value = mealType
    }

    fun closePortionDialog() {
        _activePortionProduct.value = null
    }

    fun addPortion(mealType: MealType, grams: Double) {
        val product = _activePortionProduct.value ?: return
        val multiplier = grams / 100.0
        val entry = MealEntry(
            dateString = _currentDate.value.toString(),
            mealType = mealType.name,
            productId = product.id,
            productName = product.name,
            weightGrams = grams,
            calories = product.calories * multiplier,
            protein = product.protein * multiplier,
            fat = product.fat * multiplier,
            carbs = product.carbs * multiplier
        )
        viewModelScope.launch {
            repository.addMealEntry(entry)
            closePortionDialog()
            com.example.notification.InactivityReminderManager.onUserActivity(getApplication())
        }
    }

    fun deleteMealEntry(entry: MealEntry) {
        viewModelScope.launch {
            repository.deleteMealEntry(entry)
        }
    }

    fun logWeight(weightKg: Double, dateString: String = LocalDate.now().toString()) {
        viewModelScope.launch {
            repository.logWeight(dateString, weightKg)
        }
    }

    fun openScanner(mealType: MealType = _activePortionMeal.value) {
        _activePortionMeal.value = mealType
        _notFoundBarcode.value = null
        _isScannerOpen.value = true
    }

    fun closeScanner() {
        _isScannerOpen.value = false
    }

    fun dismissNotFoundBarcode() {
        _notFoundBarcode.value = null
    }

    fun clearStatusMessage() {
        _scannedStatusMessage.value = null
    }

    fun processScannedBarcode(barcode: String) {
        viewModelScope.launch {
            val trimmed = barcode.trim()
            if (trimmed.isBlank()) return@launch

            val localProduct = repository.findProductByBarcode(trimmed)
            if (localProduct != null) {
                _isScannerOpen.value = false
                _notFoundBarcode.value = null
                _scannedStatusMessage.value = "Найден: ${localProduct.name}"
                openPortionDialog(localProduct, _activePortionMeal.value)
            } else {
                _scannedStatusMessage.value = "Поиск штрих-кода $trimmed..."
                val remoteProduct = geminiFoodService.lookupProductByBarcode(trimmed)
                if (remoteProduct != null) {
                    repository.insertProduct(remoteProduct)
                    _isScannerOpen.value = false
                    _notFoundBarcode.value = null
                    _scannedStatusMessage.value = "Найден: ${remoteProduct.name}"
                    openPortionDialog(remoteProduct, _activePortionMeal.value)
                } else {
                    _notFoundBarcode.value = trimmed
                }
            }
        }
    }

    fun openCreateProduct(prefilledBarcode: String? = null) {
        _createProductInitialBarcode.value = prefilledBarcode
        _isCreateProductOpen.value = true
    }

    fun closeCreateProduct() {
        _isCreateProductOpen.value = false
        _createProductInitialBarcode.value = null
    }

    fun saveCustomProduct(
        name: String,
        category: String,
        calories: Double,
        protein: Double,
        fat: Double,
        carbs: Double,
        barcode: String? = null
    ) {
        viewModelScope.launch {
            val id = repository.createCustomProduct(name, category, calories, protein, fat, carbs, barcode)
            closeCreateProduct()
            val newProduct = Product(
                id = id,
                name = name,
                category = category,
                calories = calories,
                protein = protein,
                fat = fat,
                carbs = carbs,
                barcode = barcode,
                isCustom = true
            )
            _scannedStatusMessage.value = "Продукт «$name» сохранён!"
            openPortionDialog(newProduct, _activePortionMeal.value)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun saveProfile(
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        isManual: Boolean,
        customCal: Int,
        customP: Int,
        customF: Int,
        customC: Int
    ) {
        viewModelScope.launch {
            val finalCal: Int
            val finalP: Int
            val finalF: Int
            val finalC: Int

            if (isManual) {
                finalCal = customCal
                finalP = customP
                finalF = customF
                finalC = customC
            } else {
                val calc = KbzhuCalculator.calculate(
                    gender = gender,
                    age = age,
                    heightCm = heightCm,
                    weightKg = weightKg,
                    activityLevel = activityLevel,
                    goal = goal
                )
                finalCal = calc.targetCalories
                finalP = calc.targetProtein
                finalF = calc.targetFat
                finalC = calc.targetCarbs
            }

            val updated = UserProfile(
                id = 1,
                age = age,
                gender = gender.name,
                heightCm = heightCm,
                weightKg = weightKg,
                activityLevel = activityLevel.name,
                goal = goal.name,
                targetCalories = finalCal,
                targetProtein = finalP,
                targetFat = finalF,
                targetCarbs = finalC,
                isManualTarget = isManual
            )
            repository.saveProfile(updated)
        }
    }

    fun addAiMealEntry(
        dishName: String,
        grams: Int,
        calories: Int,
        protein: Double,
        fat: Double,
        carbs: Double,
        mealType: MealType
    ) {
        val entry = MealEntry(
            dateString = _currentDate.value.toString(),
            mealType = mealType.name,
            productName = dishName,
            weightGrams = grams.toDouble(),
            calories = calories.toDouble(),
            protein = protein,
            fat = fat,
            carbs = carbs
        )
        viewModelScope.launch {
            repository.addMealEntry(entry)

            // Auto-save to "Свои продукты" for future catalog reuse as requested
            val factor = if (grams > 0) 100.0 / grams else 1.0
            val per100Cal = calories.toDouble() * factor
            val per100P = protein * factor
            val per100F = fat * factor
            val per100C = carbs * factor

            repository.createCustomProduct(
                name = dishName,
                category = "Свои продукты",
                calories = per100Cal,
                protein = per100P,
                fat = per100F,
                carbs = per100C
            )

            com.example.notification.InactivityReminderManager.onUserActivity(getApplication())
        }
    }

    fun syncGoogleFit() {
        googleFitManager.syncNow()
    }

    fun toggleGoogleFitAutoSync(enabled: Boolean) {
        googleFitManager.setAutoSyncEnabled(enabled)
    }

    fun loginWithGoogle(email: String = "chistochilling@gmail.com", name: String = "Пользователь Google") {
        authManager.loginWithGoogle(email, name)
        googleFitManager.syncNow()
    }

    fun loginWithEmail(email: String) {
        authManager.loginWithEmail(email)
    }

    fun logoutUser() {
        authManager.logout()
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(_currentDate.value.toString(), amountMl)
            com.example.notification.InactivityReminderManager.onUserActivity(getApplication())
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            repository.removeLastWater(_currentDate.value.toString())
        }
    }

    fun formatDisplayDate(date: LocalDate): String {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val formatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
        val dateText = date.format(formatter)

        return when (date) {
            today -> "Сегодня, $dateText"
            yesterday -> "Вчера, $dateText"
            tomorrow -> "Завтра, $dateText"
            else -> {
                val dayOfWeek = DateTimeFormatter.ofPattern("EEE, d MMM", Locale("ru")).format(date)
                dayOfWeek.replaceFirstChar { it.uppercase() }
            }
        }
    }
}
