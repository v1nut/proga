package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.example.data.calculator.KbzhuCalculator
import com.example.data.local.AppDatabase
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.KbzhuBackupData
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.Product
import com.example.data.model.UserProfile
import com.example.data.repository.KbzhuRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.ui.components.DayTrendData
import com.example.ui.util.SmartFoodSearch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToInt

enum class AppTab(val titleRu: String, val titleEn: String) {
    DASHBOARD("Главная", "Main"),
    DIARY("Дневник", "Diary"),
    CATALOG("Продукты", "Catalog"),
    PROFILE("Профиль", "Profile"),
    SETTINGS("Настройки", "Settings");

    fun title(lang: String = "ru"): String = if (lang == "en") titleEn else titleRu
}

sealed class UiEvent {
    data class ShowUndoSnackbar(
        val message: String,
        val onUndo: () -> Unit
    ) : UiEvent()
}

sealed class ScannerState {
    object Idle : ScannerState()
    data class Searching(val barcode: String) : ScannerState()
    data class Found(val product: Product) : ScannerState()
    data class NotFound(val barcode: String) : ScannerState()
}

data class PrefilledProductData(
    val name: String = "",
    val barcode: String = "",
    val category: String = "Мои продукты",
    val cal: Double = 0.0,
    val p: Double = 0.0,
    val f: Double = 0.0,
    val c: Double = 0.0
)

data class AiPhotoItemUi(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val gramsText: String,
    val caloriesText: String,
    val proteinText: String,
    val fatText: String,
    val carbsText: String,
    val baseCalPer100g: Double,
    val basePPer100g: Double,
    val baseFPer100g: Double,
    val baseCPer100g: Double
)

data class AiPhotoState(
    val photoUri: Uri? = null,
    val selectedBitmap: android.graphics.Bitmap? = null,
    val isAnalyzing: Boolean = false,
    val analysisDone: Boolean = false,
    val errorMessage: String? = null,
    val dishName: String = "",
    val items: List<AiPhotoItemUi> = emptyList(),
    val selectedMealType: MealType = MealType.LUNCH,
    val analysisNotes: String = ""
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = KbzhuRepository(AppDatabase.getInstance(application))
    private val geminiFoodService = com.example.data.service.GeminiFoodService(application)
    val healthConnectManager = com.example.data.manager.HealthConnectManager(application)
    val authManager = com.example.data.manager.AuthManager(application)

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

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

    val isSaving = MutableStateFlow(false)

    private val openFoodFactsService = com.example.data.service.OpenFoodFactsService()

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState.asStateFlow()

    private val _prefilledProductData = MutableStateFlow<PrefilledProductData?>(null)
    val prefilledProductData: StateFlow<PrefilledProductData?> = _prefilledProductData.asStateFlow()

    val isAiPhotoDialogOpen = MutableStateFlow(false)
    val isAddMenuOpen = MutableStateFlow(false)

    val allSavedMeals: StateFlow<List<com.example.data.model.SavedMeal>> = repository.allSavedMeals
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleFavoriteProduct(productId: Long, isFavorite: Boolean) {
        if (productId <= 0L) return
        viewModelScope.launch {
            repository.setProductFavorite(productId, isFavorite)
        }
    }

    fun saveMealAsDish(name: String, entries: List<MealEntry>) {
        if (entries.isEmpty() || name.isBlank()) return
        viewModelScope.launch {
            repository.saveMealAsDish(name, entries)
            _scannedStatusMessage.value = "Сохранено блюдо «$name»!"
        }
    }

    fun deleteSavedMeal(meal: com.example.data.model.SavedMeal) {
        viewModelScope.launch {
            repository.deleteSavedMeal(meal)
        }
    }

    fun addSavedMealToDiary(meal: com.example.data.model.SavedMeal, targetMealType: MealType, dateString: String = _currentDate.value.toString()) {
        if (isSaving.value) return
        isSaving.value = true

        viewModelScope.launch {
            try {
                val items = repository.parseSavedMealItems(meal.itemsJson)
                if (items.isNotEmpty()) {
                    val entries = items.map { item ->
                        MealEntry(
                            dateString = dateString,
                            mealType = targetMealType.name,
                            productId = item.productId,
                            productName = item.productName,
                            weightGrams = item.weightGrams,
                            calories = item.calories,
                            protein = item.protein,
                            fat = item.fat,
                            carbs = item.carbs
                        )
                    }
                    val ids = repository.addMealEntries(entries)
                    val totalKcal = entries.sumOf { it.calories }.roundToInt()
                    val mealTitle = targetMealType.title(appLanguage.value)

                    val message = if (appLanguage.value == "en") {
                        "Added recipe \"${meal.name}\" to $mealTitle ($totalKcal kcal)"
                    } else {
                        "Добавлено блюдо «${meal.name}» в $mealTitle ($totalKcal ккал)"
                    }

                    _uiEvent.emit(
                        UiEvent.ShowUndoSnackbar(
                            message = message,
                            onUndo = {
                                viewModelScope.launch {
                                    repository.deleteMealEntriesByIds(ids)
                                }
                            }
                        )
                    )
                }
            } finally {
                isSaving.value = false
            }
        }
    }

    fun copyMealSectionToDate(sourceDateString: String, targetDateString: String, mealType: MealType) {
        if (isSaving.value) return

        val sourceDate = try {
            LocalDate.parse(sourceDateString.trim())
        } catch (e: Exception) {
            _scannedStatusMessage.value = if (appLanguage.value == "en") "Invalid source date: $sourceDateString" else "Некорректная исходная дата: $sourceDateString"
            return
        }

        val targetDate = try {
            LocalDate.parse(targetDateString.trim())
        } catch (e: Exception) {
            _scannedStatusMessage.value = if (appLanguage.value == "en") "Invalid target date: $targetDateString" else "Некорректная дата назначения: $targetDateString"
            return
        }

        if (sourceDate == targetDate) {
            _scannedStatusMessage.value = if (appLanguage.value == "en") "Cannot copy to the same date" else "Нельзя копировать на ту же дату!"
            return
        }

        isSaving.value = true

        viewModelScope.launch {
            try {
                val sourceEntries = repository.getEntriesForDate(sourceDate.toString()).first()
                    .filter { it.mealType == mealType.name }

                if (sourceEntries.isEmpty()) {
                    _scannedStatusMessage.value = if (appLanguage.value == "en") "No entries to copy" else "Нет записей для копирования"
                    return@launch
                }

                val copiedEntries = sourceEntries.map { original ->
                    original.copy(id = 0, dateString = targetDate.toString())
                }
                val ids = repository.addMealEntries(copiedEntries)
                val totalKcal = copiedEntries.sumOf { it.calories }.roundToInt()
                val mealTitle = mealType.title(appLanguage.value)

                _uiEvent.emit(
                    UiEvent.ShowUndoSnackbar(
                        message = if (appLanguage.value == "en") {
                            "Copied to $mealTitle for $targetDate: $totalKcal kcal"
                        } else {
                            "Скопировано в $mealTitle за $targetDate: $totalKcal ккал"
                        },
                        onUndo = {
                            viewModelScope.launch {
                                repository.deleteMealEntriesByIds(ids)
                            }
                        }
                    )
                )
            } finally {
                isSaving.value = false
            }
        }
    }

    fun repeatYesterdayMeal(mealType: MealType) {
        val todayStr = _currentDate.value.toString()
        val yesterdayStr = _currentDate.value.minusDays(1).toString()
        copyMealSectionToDate(yesterdayStr, todayStr, mealType)
    }

    private val _aiPhotoState = MutableStateFlow(AiPhotoState())
    val aiPhotoState: StateFlow<AiPhotoState> = _aiPhotoState.asStateFlow()

    fun createCameraTempUri(context: Context): Uri {
        val file = java.io.File(context.cacheDir, "temp_ai_camera_photo.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        _aiPhotoState.value = _aiPhotoState.value.copy(photoUri = uri)
        return uri
    }

    fun processAiPhotoUri(context: Context, uri: Uri) {
        _aiPhotoState.value = _aiPhotoState.value.copy(isAnalyzing = true, errorMessage = null)

        viewModelScope.launch {
            val bitmap = com.example.ui.util.ImageDecoderUtil.decodeAndDownscale(context, uri, 1024)
            if (bitmap == null) {
                _aiPhotoState.value = _aiPhotoState.value.copy(
                    isAnalyzing = false,
                    errorMessage = "Не удалось загрузить или декодировать фото"
                )
                return@launch
            }

            _aiPhotoState.value = _aiPhotoState.value.copy(selectedBitmap = bitmap)

            val result = geminiFoodService.analyzeMealPhoto(bitmap)
            result.onSuccess { meal ->
                val locale = if (appLanguage.value == "en") Locale.ENGLISH else Locale.forLanguageTag("ru")
                val uiItems = if (meal.items.isNotEmpty()) {
                    meal.items.map { item ->
                        val g = item.grams.coerceAtLeast(1)
                        val factor = 100.0 / g.toDouble()
                        AiPhotoItemUi(
                            name = item.name,
                            gramsText = g.toString(),
                            caloriesText = item.calories.toString(),
                            proteinText = String.format(locale, "%.1f", item.protein),
                            fatText = String.format(locale, "%.1f", item.fat),
                            carbsText = String.format(locale, "%.1f", item.carbs),
                            baseCalPer100g = item.calories * factor,
                            basePPer100g = item.protein * factor,
                            baseFPer100g = item.fat * factor,
                            baseCPer100g = item.carbs * factor
                        )
                    }
                } else {
                    val g = meal.estimatedGrams.coerceAtLeast(1)
                    val factor = 100.0 / g.toDouble()
                    listOf(
                        AiPhotoItemUi(
                            name = meal.dishName.ifBlank { "Блюдо" },
                            gramsText = g.toString(),
                            caloriesText = meal.calories.toString(),
                            proteinText = String.format(locale, "%.1f", meal.protein),
                            fatText = String.format(locale, "%.1f", meal.fat),
                            carbsText = String.format(locale, "%.1f", meal.carbs),
                            baseCalPer100g = meal.calories * factor,
                            basePPer100g = meal.protein * factor,
                            baseFPer100g = meal.fat * factor,
                            baseCPer100g = meal.carbs * factor
                        )
                    )
                }

                _aiPhotoState.value = _aiPhotoState.value.copy(
                    isAnalyzing = false,
                    analysisDone = true,
                    dishName = meal.dishName,
                    items = uiItems,
                    selectedMealType = meal.mealType,
                    analysisNotes = meal.description
                )
            }.onFailure { error ->
                _aiPhotoState.value = _aiPhotoState.value.copy(
                    isAnalyzing = false,
                    errorMessage = error.message ?: "Не удалось распознать фото"
                )
            }
        }
    }

    fun updateAiPhotoDishName(name: String) {
        _aiPhotoState.value = _aiPhotoState.value.copy(dishName = name)
    }

    fun updateAiPhotoItemName(id: String, name: String) {
        val state = _aiPhotoState.value
        val updated = state.items.map { item ->
            if (item.id == id) item.copy(name = name) else item
        }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun updateAiPhotoItemGrams(id: String, text: String) {
        val clean = text.filter { it.isDigit() }
        val state = _aiPhotoState.value
        val g = clean.toDoubleOrNull()
        val locale = if (appLanguage.value == "en") Locale.ENGLISH else Locale.forLanguageTag("ru")

        val updated = state.items.map { item ->
            if (item.id == id) {
                if (g != null && g > 0) {
                    val cal = (item.baseCalPer100g * g / 100.0).roundToInt()
                    val p = item.basePPer100g * g / 100.0
                    val f = item.baseFPer100g * g / 100.0
                    val c = item.baseCPer100g * g / 100.0

                    item.copy(
                        gramsText = clean,
                        caloriesText = cal.toString(),
                        proteinText = String.format(locale, "%.1f", p),
                        fatText = String.format(locale, "%.1f", f),
                        carbsText = String.format(locale, "%.1f", c)
                    )
                } else {
                    item.copy(gramsText = clean)
                }
            } else item
        }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun updateAiPhotoItemCalories(id: String, text: String) {
        val clean = text.filter { it.isDigit() }
        val state = _aiPhotoState.value
        val updated = state.items.map { item ->
            if (item.id == id) {
                val g = (item.gramsText.toDoubleOrNull() ?: 100.0).coerceAtLeast(1.0)
                val newCal = clean.toDoubleOrNull() ?: 0.0
                item.copy(caloriesText = clean, baseCalPer100g = newCal * 100.0 / g)
            } else item
        }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun updateAiPhotoItemProtein(id: String, text: String) {
        val norm = text.replace(',', '.')
        val state = _aiPhotoState.value
        val updated = state.items.map { item ->
            if (item.id == id) {
                val g = (item.gramsText.toDoubleOrNull() ?: 100.0).coerceAtLeast(1.0)
                val newP = norm.toDoubleOrNull() ?: 0.0
                item.copy(proteinText = norm, basePPer100g = newP * 100.0 / g)
            } else item
        }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun updateAiPhotoItemFat(id: String, text: String) {
        val norm = text.replace(',', '.')
        val state = _aiPhotoState.value
        val updated = state.items.map { item ->
            if (item.id == id) {
                val g = (item.gramsText.toDoubleOrNull() ?: 100.0).coerceAtLeast(1.0)
                val newF = norm.toDoubleOrNull() ?: 0.0
                item.copy(fatText = norm, baseFPer100g = newF * 100.0 / g)
            } else item
        }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun updateAiPhotoItemCarbs(id: String, text: String) {
        val norm = text.replace(',', '.')
        val state = _aiPhotoState.value
        val updated = state.items.map { item ->
            if (item.id == id) {
                val g = (item.gramsText.toDoubleOrNull() ?: 100.0).coerceAtLeast(1.0)
                val newC = norm.toDoubleOrNull() ?: 0.0
                item.copy(carbsText = norm, baseCPer100g = newC * 100.0 / g)
            } else item
        }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun removeAiPhotoItem(id: String) {
        val state = _aiPhotoState.value
        val updated = state.items.filterNot { it.id == id }
        _aiPhotoState.value = state.copy(items = updated)
    }

    fun addManualAiPhotoItem() {
        val state = _aiPhotoState.value
        val newItem = AiPhotoItemUi(
            name = "Новое блюдо",
            gramsText = "100",
            caloriesText = "150",
            proteinText = "10.0",
            fatText = "5.0",
            carbsText = "15.0",
            baseCalPer100g = 150.0,
            basePPer100g = 10.0,
            baseFPer100g = 5.0,
            baseCPer100g = 15.0
        )
        _aiPhotoState.value = state.copy(items = state.items + newItem)
    }

    fun updateAiPhotoMealType(type: MealType) {
        _aiPhotoState.value = _aiPhotoState.value.copy(selectedMealType = type)
    }

    fun clearAiPhotoState() {
        _aiPhotoState.value = AiPhotoState()
    }

    private val _activeProductForInfo = MutableStateFlow<Product?>(null)
    val activeProductForInfo: StateFlow<Product?> = _activeProductForInfo.asStateFlow()

    fun openProductInfo(product: Product) {
        _activeProductForInfo.value = product
    }

    fun closeProductInfo() {
        _activeProductForInfo.value = null
    }

    private val productInfoCache = object : LinkedHashMap<String, com.example.data.service.ProductScienceInfo>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, com.example.data.service.ProductScienceInfo>?): Boolean {
            return size > 50
        }
    }

    fun fetchProductScienceInfo(product: Product, onResult: (Result<com.example.data.service.ProductScienceInfo>) -> Unit) {
        val cacheKey = product.name.lowercase().trim()
        val cached = productInfoCache[cacheKey]
        if (cached != null) {
            onResult(Result.success(cached))
            return
        }

        viewModelScope.launch {
            val result = geminiFoodService.getNutritionalScienceAdvice(product)
            result.onSuccess { info ->
                if (cacheKey.isNotBlank()) {
                    productInfoCache[cacheKey] = info
                }
            }
            onResult(result)
        }
    }

    fun resetScannerState() {
        _scannerState.value = ScannerState.Idle
    }

    private val _isCreateProductOpen = MutableStateFlow(false)
    val isCreateProductOpen: StateFlow<Boolean> = _isCreateProductOpen.asStateFlow()

    private val _createProductInitialBarcode = MutableStateFlow<String?>(null)
    val createProductInitialBarcode: StateFlow<String?> = _createProductInitialBarcode.asStateFlow()

    private val _isChangelogOpen = MutableStateFlow(false)
    val isChangelogOpen: StateFlow<Boolean> = _isChangelogOpen.asStateFlow()

    private val _appLanguage = MutableStateFlow(com.example.data.manager.AppLanguageManager.getLanguage(application))
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _appThemeMode = MutableStateFlow(
        getApplication<Application>().getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)
            .getString("app_theme_mode", "dark") ?: "dark"
    )
    val appThemeMode: StateFlow<String> = _appThemeMode.asStateFlow()

    fun setAppThemeMode(mode: String) {
        getApplication<Application>().getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)
            .edit().putString("app_theme_mode", mode).apply()
        _appThemeMode.value = mode
    }

    fun setAppLanguage(langCode: String) {
        com.example.data.manager.AppLanguageManager.setLanguage(getApplication(), langCode)
        _appLanguage.value = langCode
        selectedCategory.value = if (langCode == "en") "All" else "Все"
    }

    fun openChangelog() {
        _isChangelogOpen.value = true
    }

    fun closeChangelog() {
        _isChangelogOpen.value = false
    }

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

    val recentProducts: StateFlow<List<Product>> = repository.recentProducts
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

    val onlineSearchResults = MutableStateFlow<List<Product>>(emptyList())
    val isOnlineSearching = MutableStateFlow(false)

    private val _tierMode = MutableStateFlow(
        getApplication<Application>().getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)
            .getString("tier_list_mode", "auto") ?: "auto"
    )
    val tierMode: StateFlow<String> = _tierMode.asStateFlow()

    fun setTierMode(mode: String) {
        getApplication<Application>().getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)
            .edit().putString("tier_list_mode", mode).apply()
        _tierMode.value = mode
    }

    val effectiveTierGoal: StateFlow<FitnessGoal> = combine(tierMode, userProfile) { mode, profile ->
        val goalEnum = try { FitnessGoal.valueOf(profile.goal) } catch (e: Exception) { FitnessGoal.MAINTENANCE }
        when (mode) {
            "loss" -> FitnessGoal.WEIGHT_LOSS
            "gain" -> FitnessGoal.WEIGHT_GAIN
            else -> goalEnum
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FitnessGoal.MAINTENANCE
    )

    val selectedTierFilter = MutableStateFlow<com.example.ui.util.FoodTier?>(null)

    fun setSelectedTierFilter(tier: com.example.ui.util.FoodTier?) {
        selectedTierFilter.value = tier
    }

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        recentProducts,
        searchQuery,
        selectedCategory,
        selectedTierFilter,
        effectiveTierGoal
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val products = flows[0] as List<Product>
        @Suppress("UNCHECKED_CAST")
        val recent = flows[1] as List<Product>
        val query = flows[2] as String
        val category = flows[3] as String
        val tierFilter = flows[4] as com.example.ui.util.FoodTier?
        val goal = flows[5] as FitnessGoal

        val sourceList = when (category) {
            "Все", "All" -> products
            "Избранное", "Favorites" -> products.filter { it.isFavorite }
            "Недавние", "Recent" -> recent
            "Сканированное", "Scanned" -> products.filter {
                it.category.equals("Сканированное", ignoreCase = true) ||
                it.category.equals("Импорт (Open Food Facts)", ignoreCase = true) ||
                (!it.barcode.isNullOrBlank() && !it.isCustom)
            }
            "Мои продукты", "Свои продукты", "My Foods" -> products.filter {
                it.isCustom && !it.category.equals("Сканированное", ignoreCase = true)
            }
            "Мясо и птица", "Meat & Poultry" -> products.filter {
                it.category.equals("Мясо и птица", ignoreCase = true) || it.category.equals("Meat & Poultry", ignoreCase = true)
            }
            "Рыба и морепродукты", "Fish & Seafood" -> products.filter {
                it.category.equals("Рыба и морепродукты", ignoreCase = true) || it.category.equals("Fish & Seafood", ignoreCase = true)
            }
            "Крупы и гарниры", "Grains & Sides" -> products.filter {
                it.category.equals("Крупы и гарниры", ignoreCase = true) || it.category.equals("Grains & Sides", ignoreCase = true)
            }
            "Молочные продукты", "Dairy & Eggs" -> products.filter {
                it.category.equals("Молочные продукты", ignoreCase = true) || it.category.equals("Dairy & Eggs", ignoreCase = true)
            }
            "Овощи и зелень", "Vegetables & Greens" -> products.filter {
                it.category.equals("Овощи и зелень", ignoreCase = true) || it.category.equals("Vegetables & Greens", ignoreCase = true)
            }
            "Фрукты и ягоды", "Fruits & Berries" -> products.filter {
                it.category.equals("Фрукты и ягоды", ignoreCase = true) || it.category.equals("Fruits & Berries", ignoreCase = true)
            }
            "Орехи и масла", "Nuts & Oils" -> products.filter {
                it.category.equals("Орехи и масла", ignoreCase = true) || it.category.equals("Nuts & Oils", ignoreCase = true)
            }
            "Хлеб и выпечка", "Bread & Bakery" -> products.filter {
                it.category.equals("Хлеб и выпечка", ignoreCase = true) || it.category.equals("Bread & Bakery", ignoreCase = true)
            }
            "Snacks & Desserts", "Перекусы и десерты" -> products.filter {
                it.category.contains("перекус", ignoreCase = true) ||
                it.category.contains("десерт", ignoreCase = true) ||
                it.category.contains("сладости", ignoreCase = true) ||
                it.category.equals("Snacks & Desserts", ignoreCase = true)
            }
            else -> products.filter { it.category.equals(category, ignoreCase = true) }
        }

        val tierFiltered = if (tierFilter != null) {
            sourceList.filter { p ->
                val (t, _) = com.example.ui.util.FoodTierSystem.evaluateProductTier(
                    name = p.name,
                    category = p.category,
                    calories = p.calories,
                    protein = p.protein,
                    fat = p.fat,
                    carbs = p.carbs,
                    goal = goal
                )
                t == tierFilter
            }
        } else {
            sourceList
        }

        SmartFoodSearch.filterAndSort(tierFiltered, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    fun openEditCustomProduct(product: Product) {
        openCreateProductPrefilled(
            name = product.name,
            barcode = product.barcode ?: "",
            category = product.category,
            cal = product.calories,
            p = product.protein,
            f = product.fat,
            c = product.carbs
        )
        _editingProduct.value = product
    }

    fun updateCustomProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            closeCreateProduct()
            _scannedStatusMessage.value = "Продукт «${product.name}» обновлён!"
        }
    }

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

    val yesterdayEntries: StateFlow<List<MealEntry>> = _currentDate
        .flatMapLatest { date ->
            repository.getEntriesForDate(date.minusDays(1).toString())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Week calories map for calendar bar and month dialog
    val weekCaloriesMap: StateFlow<Map<String, Double>> = _currentDate
        .flatMapLatest { date ->
            val start = date.minusMonths(6).withDayOfMonth(1)
            val end = date.plusMonths(2).let { it.withDayOfMonth(it.lengthOfMonth()) }
            val dates = mutableListOf<String>()
            var curr = start
            while (!curr.isAfter(end)) {
                dates.add(curr.toString())
                curr = curr.plusDays(1)
            }
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

    private val _waterGoalTrigger = MutableStateFlow(0)

    val targetWaterMl: StateFlow<Int> = combine(userProfile, _waterGoalTrigger) { profile: UserProfile, _: Int ->
        val weight = profile.weightKg
        val isManual = prefs.getBoolean("is_manual_water_target", false)
        val customVal = prefs.getInt("custom_target_water_ml", 2000)
        KbzhuCalculator.calculateTargetWaterMl(weight, isManual, customVal)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2100
    )

    fun setWaterGoalSetting(isManual: Boolean, customTargetMl: Int) {
        prefs.edit()
            .putBoolean("is_manual_water_target", isManual)
            .putInt("custom_target_water_ml", customTargetMl.coerceIn(500, 6000))
            .apply()
        _waterGoalTrigger.value += 1
    }

    private val prefs = getApplication<Application>().getSharedPreferences("kbzhu_notification_prefs", Context.MODE_PRIVATE)

    private val _isOnboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
            if (!_isOnboardingCompleted.value) {
                val todayEntries = repository.getEntriesForDate(LocalDate.now().toString()).first()
                val profile = repository.userProfile.first()
                val isExistingUser = todayEntries.isNotEmpty() || (profile != null && (profile.isManualTarget || profile.age != 26 || profile.weightKg != 75.0))

                if (isExistingUser) {
                    prefs.edit().putBoolean("onboarding_completed", true).apply()
                    _isOnboardingCompleted.value = true
                }
            }
        }

        viewModelScope.launch {
            searchQuery
                .debounce(500L)
                .collectLatest { query ->
                    val q = query.trim()
                    if (q.length >= 3 && filteredProducts.value.size < 5) {
                        isOnlineSearching.value = true
                        val online = openFoodFactsService.searchProductsOnline(q)
                        onlineSearchResults.value = online
                        isOnlineSearching.value = false
                    } else {
                        onlineSearchResults.value = emptyList()
                        isOnlineSearching.value = false
                    }
                }
        }
    }

    fun completeOnboarding(
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        enableNotifications: Boolean
    ) {
        saveProfile(
            age = age,
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg,
            targetWeightKg = targetWeightKg,
            activityLevel = activityLevel,
            goal = goal,
            isManual = false,
            customCal = 0,
            customP = 0,
            customF = 0,
            customC = 0
        )

        prefs.edit()
            .putBoolean("onboarding_completed", true)
            .putBoolean(com.example.notification.InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, enableNotifications)
            .apply()

        if (enableNotifications) {
            com.example.notification.InactivityReminderManager.scheduleReminder(getApplication())
        }

        _isOnboardingCompleted.value = true
    }

    fun selectTab(tab: AppTab) {
        shouldReturnAfterPortion = false
        _selectedTab.value = tab
    }

    suspend fun addAiMealEntries(entries: List<MealEntry>): List<Long> {
        if (entries.isEmpty()) return emptyList()
        val ids = repository.addMealEntries(entries)
        com.example.notification.InactivityReminderManager.onUserActivity(getApplication())

        val shouldSaveAiProduct = prefs.getBoolean("auto_save_ai_products", false)
        if (shouldSaveAiProduct) {
            for (entry in entries) {
                if (entry.weightGrams > 0 && entry.productName.isNotBlank()) {
                    val exists = allProducts.value.any { it.name.equals(entry.productName.trim(), ignoreCase = true) }
                    if (!exists) {
                        val factor = 100.0 / entry.weightGrams
                        val cat = if (appLanguage.value == "en") "My Foods" else "Мои продукты"
                        repository.createCustomProduct(
                            name = entry.productName.trim(),
                            category = cat,
                            calories = entry.calories * factor,
                            protein = entry.protein * factor,
                            fat = entry.fat * factor,
                            carbs = entry.carbs * factor
                        )
                    }
                }
            }
        }

        val count = entries.size
        val totalKcal = entries.sumOf { it.calories }.roundToInt()
        val countStr = if (appLanguage.value == "en") {
            if (count == 1) "1 item" else "$count items"
        } else {
            if (count == 1) "1 позиция" else if (count in 2..4) "$count позиции" else "$count позиций"
        }
        val message = if (appLanguage.value == "en") "Added: $countStr, $totalKcal kcal" else "Добавлено: $countStr, $totalKcal ккал"
        _uiEvent.emit(
            UiEvent.ShowUndoSnackbar(
                message = message,
                onUndo = {
                    undoEntries(ids)
                }
            )
        )

        return ids
    }

    fun undoEntries(ids: List<Long>) {
        viewModelScope.launch {
            repository.deleteMealEntriesByIds(ids)
        }
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

    private val _editingMealEntry = MutableStateFlow<MealEntry?>(null)
    val editingMealEntry: StateFlow<MealEntry?> = _editingMealEntry.asStateFlow()

    fun openEditMealEntry(entry: MealEntry) {
        shouldReturnAfterPortion = false
        _editingMealEntry.value = entry
        _activePortionMeal.value = MealType.fromString(entry.mealType)
        val foundProduct = allProducts.value.find { it.id == entry.productId }
        val product = if (foundProduct != null) {
            foundProduct
        } else {
            val factor = if (entry.weightGrams > 0) 100.0 / entry.weightGrams else 1.0
            Product(
                id = entry.productId ?: 0L,
                name = entry.productName,
                category = "Запись",
                calories = entry.calories * factor,
                protein = entry.protein * factor,
                fat = entry.fat * factor,
                carbs = entry.carbs * factor
            )
        }
        _activePortionProduct.value = product
    }

    fun closePortionDialog() {
        _activePortionProduct.value = null
        _editingMealEntry.value = null
    }

    private var previousTab: AppTab = AppTab.DASHBOARD
    private var shouldReturnAfterPortion = false

    fun openCatalogForMeal(mealType: MealType) {
        previousTab = _selectedTab.value
        shouldReturnAfterPortion = true
        _activePortionMeal.value = mealType
        _selectedTab.value = AppTab.CATALOG
    }

    fun addPortion(mealType: MealType, grams: Double) {
        val product = _activePortionProduct.value ?: return
        val multiplier = grams / 100.0
        val caloriesVal = (product.calories * multiplier).roundToInt()
        val existing = _editingMealEntry.value

        viewModelScope.launch {
            if (existing != null) {
                repository.updateMealEntry(
                    existing.copy(
                        mealType = mealType.name,
                        weightGrams = grams,
                        calories = product.calories * multiplier,
                        protein = product.protein * multiplier,
                        fat = product.fat * multiplier,
                        carbs = product.carbs * multiplier
                    )
                )
            } else {
                val targetProductId = if (product.id == 0L) {
                    repository.insertProduct(product)
                } else {
                    product.id
                }

                val entry = MealEntry(
                    dateString = _currentDate.value.toString(),
                    mealType = mealType.name,
                    productId = targetProductId,
                    productName = product.name,
                    weightGrams = grams,
                    calories = product.calories * multiplier,
                    protein = product.protein * multiplier,
                    fat = product.fat * multiplier,
                    carbs = product.carbs * multiplier
                )
                val newId = repository.addMealEntry(entry)
                val savedEntry = entry.copy(id = newId)

                val mealTitle = mealType.title(appLanguage.value)
                val message = if (appLanguage.value == "en") {
                    "Added to $mealTitle: ${product.name} ($caloriesVal kcal)"
                } else {
                    "Добавлено в $mealTitle: ${product.name} ($caloriesVal ккал)"
                }

                _uiEvent.emit(
                    UiEvent.ShowUndoSnackbar(
                        message = message,
                        onUndo = {
                            viewModelScope.launch {
                                repository.deleteMealEntry(savedEntry)
                            }
                        }
                    )
                )
            }
            closePortionDialog()

            if (shouldReturnAfterPortion && previousTab != AppTab.CATALOG) {
                _selectedTab.value = previousTab
            }
            shouldReturnAfterPortion = false

            com.example.notification.InactivityReminderManager.onUserActivity(getApplication())
        }
    }

    fun deleteMealEntry(entry: MealEntry) {
        viewModelScope.launch {
            repository.deleteMealEntry(entry)
            val message = if (appLanguage.value == "en") "Deleted: ${entry.productName}" else "Удалено: ${entry.productName}"
            _uiEvent.emit(
                UiEvent.ShowUndoSnackbar(
                    message = message,
                    onUndo = {
                        viewModelScope.launch {
                            repository.addMealEntry(entry)
                        }
                    }
                )
            )
        }
    }

    fun logWeight(weightKg: Double, dateString: String = LocalDate.now().toString()) {
        val parsedDate = try {
            val d = LocalDate.parse(dateString.trim())
            if (!d.isAfter(LocalDate.now())) d else null
        } catch (e: Exception) { null }

        if (parsedDate == null) {
            _scannedStatusMessage.value = if (appLanguage.value == "en") "Invalid weight date: $dateString" else "Некорректная дата веса: $dateString"
            return
        }

        viewModelScope.launch {
            repository.logWeight(parsedDate.toString(), weightKg)
        }
    }

    fun deleteWeightEntry(dateString: String) {
        viewModelScope.launch {
            repository.deleteWeightForDate(dateString)
            _scannedStatusMessage.value = "Запись веса за $dateString удалена"
        }
    }

    fun openScanner(mealType: MealType = _activePortionMeal.value) {
        shouldReturnAfterPortion = false
        _activePortionMeal.value = mealType
        _notFoundBarcode.value = null
        _scannerState.value = ScannerState.Idle
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
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return

        _scannerState.value = ScannerState.Searching(trimmed)

        viewModelScope.launch {
            val localProduct = repository.findProductByBarcode(trimmed)
            if (localProduct != null) {
                _scannerState.value = ScannerState.Found(localProduct)
                closeScanner()
                openPortionDialog(localProduct, _activePortionMeal.value)
                return@launch
            }

            when (val result = openFoodFactsService.fetchByBarcode(trimmed)) {
                is com.example.data.service.OpenFoodFactsResult.Complete -> {
                    val newId = repository.insertProduct(result.product)
                    val savedProduct = result.product.copy(id = newId)
                    _scannerState.value = ScannerState.Found(savedProduct)
                    closeScanner()
                    openPortionDialog(savedProduct, _activePortionMeal.value)
                }
                is com.example.data.service.OpenFoodFactsResult.Incomplete -> {
                    _scannerState.value = ScannerState.Idle
                    closeScanner()
                    openCreateProductPrefilled(
                        name = result.name,
                        barcode = result.barcode,
                        cal = result.calories,
                        p = result.protein,
                        f = result.fat,
                        c = result.carbs
                    )
                }
                is com.example.data.service.OpenFoodFactsResult.NotFound -> {
                    _scannerState.value = ScannerState.NotFound(trimmed)
                }
            }
        }
    }

    fun openCreateProduct(prefilledBarcode: String? = null) {
        openCreateProductPrefilled(barcode = prefilledBarcode ?: "")
    }

    fun openCreateProductPrefilled(
        name: String = "",
        barcode: String = "",
        category: String = "Мои продукты",
        cal: Double = 0.0,
        p: Double = 0.0,
        f: Double = 0.0,
        c: Double = 0.0
    ) {
        _editingProduct.value = null
        _prefilledProductData.value = PrefilledProductData(name, barcode, category, cal, p, f, c)
        _createProductInitialBarcode.value = barcode.ifBlank { null }
        _isCreateProductOpen.value = true
    }

    fun closeCreateProduct() {
        _isCreateProductOpen.value = false
        _createProductInitialBarcode.value = null
        _prefilledProductData.value = null
        _editingProduct.value = null
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
            _scannedStatusMessage.value = if (appLanguage.value == "en") "Product \"$name\" saved!" else "Продукт «$name» сохранён!"
            openPortionDialog(newProduct, _activePortionMeal.value)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            val message = if (appLanguage.value == "en") "Deleted product: ${product.name}" else "Удалён продукт: ${product.name}"
            _uiEvent.emit(
                UiEvent.ShowUndoSnackbar(
                    message = message,
                    onUndo = {
                        viewModelScope.launch {
                            repository.insertProduct(product)
                        }
                    }
                )
            )
        }
    }

    fun saveProfile(
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
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
                targetWeightKg = targetWeightKg,
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

    fun syncHealthConnect(onResult: (String) -> Unit = {}) {
        healthConnectManager.syncNow(onResult)
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(_currentDate.value.toString(), amountMl)
            com.example.notification.InactivityReminderManager.onUserActivity(getApplication())
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            val deleted = repository.removeLastWaterWithReturn(_currentDate.value.toString())
            if (deleted != null) {
                val message = if (appLanguage.value == "en") "Removed water portion (${deleted.amountMl} ml)" else "Удалена порция воды (${deleted.amountMl} мл)"
                _uiEvent.emit(
                    UiEvent.ShowUndoSnackbar(
                        message = message,
                        onUndo = {
                            viewModelScope.launch {
                                repository.restoreWaterEntry(deleted)
                            }
                        }
                    )
                )
            }
        }
    }

    fun formatDisplayDate(date: LocalDate): String {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val locale = if (appLanguage.value == "en") Locale.ENGLISH else Locale("ru")
        val formatter = DateTimeFormatter.ofPattern("d MMMM", locale)
        val dateText = date.format(formatter)

        return when (date) {
            today -> if (appLanguage.value == "en") "Today, $dateText" else "Сегодня, $dateText"
            yesterday -> if (appLanguage.value == "en") "Yesterday, $dateText" else "Вчера, $dateText"
            tomorrow -> if (appLanguage.value == "en") "Tomorrow, $dateText" else "Завтра, $dateText"
            else -> {
                val dayOfWeek = DateTimeFormatter.ofPattern("EEE, d MMM", locale).format(date)
                dayOfWeek.replaceFirstChar { it.uppercase() }
            }
        }
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(KbzhuBackupData::class.java)

    companion object {
        const val CURRENT_VERSION_SCHEME = 6
    }

    fun exportBackupToUri(context: Context, uri: Uri, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = repository.createBackupData(prefs)
                val jsonString = backupAdapter.toJson(backupData)
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(jsonString.toByteArray(Charsets.UTF_8))
                    }
                }
                onComplete(true, if (appLanguage.value == "en") "Backup successfully exported!" else "Резервная копия успешно экспортирована!")
            } catch (e: Exception) {
                onComplete(false, if (appLanguage.value == "en") "Export error: ${e.localizedMessage}" else "Ошибка экспорта: ${e.localizedMessage ?: "ошибка записи"}")
            }
        }
    }

    suspend fun parseBackupFromUri(context: Context, uri: Uri): Pair<KbzhuBackupData?, String?> = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader().readText()
            } ?: return@withContext Pair(null, if (appLanguage.value == "en") "Unable to read backup file" else "Не удалось прочитать файл резервной копии")

            val backup = backupAdapter.fromJson(jsonString)
            if (backup == null) {
                return@withContext Pair(null, if (appLanguage.value == "en") "Invalid backup file format" else "Некорректный формат файла резервной копии")
            }

            // Check non-empty data
            val isEmptyData = backup.userProfile == null &&
                    backup.safeProducts.isEmpty() &&
                    backup.safeMealEntries.isEmpty() &&
                    backup.safeWaterEntries.isEmpty() &&
                    backup.safeWeightEntries.isEmpty() &&
                    backup.safeSavedMeals.isEmpty()

            if (isEmptyData) {
                return@withContext Pair(null, if (appLanguage.value == "en") "Backup file contains no data" else "Файл резервной копии не содержит данных")
            }

            // Format check
            if (!backup.format.isNullOrBlank() && backup.format != "kbzhu-backup") {
                return@withContext Pair(null, if (appLanguage.value == "en") "Invalid backup file signature" else "Некорректная сигнатура файла резервной копии")
            }

            val scheme = backup.versionScheme ?: 0

            if (backup.format.isNullOrBlank() && scheme !in 5..CURRENT_VERSION_SCHEME) {
                return@withContext Pair(null, if (appLanguage.value == "en") "Unsupported backup version Scheme v$scheme" else "Неподдерживаемая версия резервной копии v$scheme")
            }

            if (scheme > CURRENT_VERSION_SCHEME) {
                return@withContext Pair(null, if (appLanguage.value == "en") "Backup was created in a newer app version (v$scheme). Please update the app." else "Резервная копия создана в более новой версии приложения (схема v$scheme). Пожалуйста, обновите приложение.")
            }

            Pair(backup, null)
        } catch (e: Exception) {
            Pair(null, e.localizedMessage ?: "Ошибка чтения файла")
        }
    }

    fun restoreBackup(backupData: KbzhuBackupData, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                repository.restoreBackupData(backupData, prefs)
                _appLanguage.value = prefs.getString("app_language", "ru") ?: "ru"
                _appThemeMode.value = prefs.getString("app_theme_mode", "dark") ?: "dark"
                _tierMode.value = prefs.getString("tier_list_mode", "auto") ?: "auto"
                _waterGoalTrigger.value += 1
                com.example.data.manager.AppLanguageManager.setLanguage(getApplication(), _appLanguage.value)
                com.example.notification.InactivityReminderManager.scheduleReminder(getApplication())
                onComplete(true, if (appLanguage.value == "en") "Data successfully restored from backup!" else "Данные успешно восстановлены из резервной копии!")
            } catch (e: Exception) {
                onComplete(false, if (appLanguage.value == "en") "Import error: ${e.localizedMessage}" else "Ошибка импорта: ${e.localizedMessage ?: "ошибка чтения"}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        productInfoCache.clear()
        healthConnectManager.cleanup()
    }
}
