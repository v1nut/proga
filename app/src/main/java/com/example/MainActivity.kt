package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.example.ui.UiEvent
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.data.model.MealType
import com.example.data.model.Product
import com.example.notification.InactivityReminderManager
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.components.AiFoodPhotoDialog
import com.example.ui.components.BarcodeScannerDialog
import com.example.ui.components.CreateProductDialog
import com.example.ui.components.PortionDialog
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DiaryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isNotEmpty()) {
                val firebaseAppCheck = com.google.firebase.appcheck.FirebaseAppCheck.getInstance()
                if (BuildConfig.DEBUG) {
                    firebaseAppCheck.installAppCheckProviderFactory(
                        com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory.getInstance()
                    )
                } else {
                    firebaseAppCheck.installAppCheckProviderFactory(
                        com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory.getInstance()
                    )
                }
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                android.util.Log.w("AppCheck", "Firebase App Check initialization skipped: ${e.message}")
            }
        }

        setContent {
            val viewModel: MainViewModel = viewModel()
            val appThemeMode by viewModel.appThemeMode.collectAsStateWithLifecycle()

            val isDark = when (appThemeMode) {
                "light" -> false
                "system" -> androidx.compose.foundation.isSystemInDarkTheme()
                else -> true
            }

            LaunchedEffect(isDark) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDark) {
                        androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        androidx.activity.SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }

            MyApplicationTheme(themeMode = appThemeMode) {
                KbzhuApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KbzhuApp(viewModel: MainViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDate.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val entries by viewModel.currentDayEntries.collectAsStateWithLifecycle()
    val yesterdayEntries by viewModel.yesterdayEntries.collectAsStateWithLifecycle()

    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val onlineProducts by viewModel.onlineSearchResults.collectAsStateWithLifecycle()
    val allSavedMeals by viewModel.allSavedMeals.collectAsStateWithLifecycle()
    val isOnlineSearching by viewModel.isOnlineSearching.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val activePortionProduct by viewModel.activePortionProduct.collectAsStateWithLifecycle()
    val activePortionMeal by viewModel.activePortionMeal.collectAsStateWithLifecycle()
    val editingMealEntry by viewModel.editingMealEntry.collectAsStateWithLifecycle()

    val isScannerOpen by viewModel.isScannerOpen.collectAsStateWithLifecycle()
    val notFoundBarcode by viewModel.notFoundBarcode.collectAsStateWithLifecycle()
    val scannedStatusMessage by viewModel.scannedStatusMessage.collectAsStateWithLifecycle()

    val isCreateProductOpen by viewModel.isCreateProductOpen.collectAsStateWithLifecycle()
    val createProductInitialBarcode by viewModel.createProductInitialBarcode.collectAsStateWithLifecycle()

    val weekCaloriesMap by viewModel.weekCaloriesMap.collectAsStateWithLifecycle()
    val weeklyTrendData by viewModel.weeklyTrendData.collectAsStateWithLifecycle()
    val currentWaterMl by viewModel.currentDayWater.collectAsStateWithLifecycle()
    val targetWaterMl by viewModel.targetWaterMl.collectAsStateWithLifecycle()
    val weightEntries by viewModel.weightEntries.collectAsStateWithLifecycle()
    val healthStepData by viewModel.healthStepData.collectAsStateWithLifecycle()

    val effectiveTierGoal by viewModel.effectiveTierGoal.collectAsStateWithLifecycle()
    val selectedTierFilter by viewModel.selectedTierFilter.collectAsStateWithLifecycle()
    val tierMode by viewModel.tierMode.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var isAiPhotoDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isAddMenuOpen by rememberSaveable { mutableStateOf(false) }
    val activeProductForInfo by viewModel.activeProductForInfo.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.healthConnectManager.registerStepSensor()
    }

    val healthConnectPermissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(viewModel.healthConnectManager.permissions)) {
            viewModel.syncHealthConnect { msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Разрешение Health Connect не предоставлено", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        InactivityReminderManager.scheduleReminder(context)
    }

    val isChangelogOpen by viewModel.isChangelogOpen.collectAsStateWithLifecycle()

    BackHandler(enabled = isChangelogOpen) {
        viewModel.closeChangelog()
    }

    // Handle system back navigation: return to Dashboard tab if on secondary tab
    BackHandler(enabled = !isChangelogOpen && selectedTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    // Auto-dismiss status message after 2.5 seconds
    LaunchedEffect(scannedStatusMessage) {
        if (scannedStatusMessage != null) {
            delay(2500)
            viewModel.clearStatusMessage()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowUndoSnackbar -> {
                    scope.launch {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = if (appLanguage == "en") "Undo" else "Отменить",
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            event.onUndo()
                        }
                    }
                }
            }
        }
    }

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()
    val displayDate = viewModel.formatDisplayDate(currentDate)

    LaunchedEffect(isOnboardingCompleted) {
        if (isOnboardingCompleted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                    activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            }
        }
    }

    if (!isOnboardingCompleted) {
        com.example.ui.screens.OnboardingScreen(
            onCompleteOnboarding = { age, gender, height, weight, targetWeight, activity, goal, enableNotifications ->
                viewModel.completeOnboarding(age, gender, height, weight, targetWeight, activity, goal, enableNotifications)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground),
            containerColor = DarkBackground,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                FitnessBottomBar(
                    selectedTab = selectedTab,
                    appLanguage = appLanguage,
                    onTabSelect = { tab -> viewModel.selectTab(tab) },
                    onOpenAddMenu = { isAddMenuOpen = true }
                )
            }
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = selectedTab,
                label = "screen_crossfade"
            ) { tab ->
                when (tab) {
                    AppTab.DASHBOARD -> {
                        DashboardScreen(
                            currentDate = currentDate,
                            displayDateText = displayDate,
                            userProfile = userProfile,
                            entries = entries,
                            yesterdayEntries = yesterdayEntries,
                            dailyCaloriesMap = weekCaloriesMap,
                            weeklyTrendData = weeklyTrendData,
                            currentWaterMl = currentWaterMl,
                            targetWaterMl = targetWaterMl,
                            weightEntries = weightEntries,
                            onLogWeight = { w, d -> viewModel.logWeight(w, d) },
                            onDeleteWeightEntry = { d -> viewModel.deleteWeightEntry(d) },
                            onRepeatYesterdayMeal = { mealType -> viewModel.repeatYesterdayMeal(mealType) },
                            onSelectDate = { date -> viewModel.setDate(date) },
                            onAddWater = { amount -> viewModel.addWater(amount) },
                            onRemoveLastWater = { viewModel.removeLastWater() },
                            onOpenCatalogForMeal = { mealType ->
                                viewModel.openCatalogForMeal(mealType)
                            },
                            onOpenScanner = {
                                viewModel.openScanner(activePortionMeal)
                            },
                            onNavigateToTab = { targetTab -> viewModel.selectTab(targetTab) },
                            onEditEntry = { entry ->
                                viewModel.openEditMealEntry(entry)
                            },
                            onSaveAiMealEntries = { entries ->
                                scope.launch { viewModel.addAiMealEntries(entries) }
                            },
                            onOpenCameraPhoto = {
                                isAiPhotoDialogOpen = true
                            },
                            effectiveTierGoal = effectiveTierGoal,
                            appLanguage = appLanguage
                        )
                    }

                    AppTab.DIARY -> {
                        DiaryScreen(
                            currentDate = currentDate,
                            displayDateText = displayDate,
                            entries = entries,
                            dailyCaloriesMap = weekCaloriesMap,
                            onPreviousDay = { viewModel.previousDay() },
                            onNextDay = { viewModel.nextDay() },
                            onTodayClick = { viewModel.setDate(java.time.LocalDate.now()) },
                            onSelectDate = { date -> viewModel.setDate(date) },
                            onAddProductClick = { mealType ->
                                viewModel.openCatalogForMeal(mealType)
                            },
                            onEditEntry = { entry ->
                                viewModel.openEditMealEntry(entry)
                            },
                            onDeleteEntry = { entry ->
                                viewModel.deleteMealEntry(entry)
                            },
                            onCopyMealToToday = { mealType ->
                                viewModel.copyMealSectionToDate(currentDate.toString(), java.time.LocalDate.now().toString(), mealType)
                            },
                            onCopyMealToDate = { mealType, targetDateStr ->
                                viewModel.copyMealSectionToDate(currentDate.toString(), targetDateStr, mealType)
                            },
                            onSaveMealAsDish = { name, mealEntries ->
                                viewModel.saveMealAsDish(name, mealEntries)
                            },
                            effectiveTierGoal = effectiveTierGoal,
                            appLanguage = appLanguage
                        )
                    }

                    AppTab.CATALOG -> {
                        CatalogScreen(
                            products = filteredProducts,
                            onlineProducts = onlineProducts,
                            savedMeals = allSavedMeals,
                            isOnlineSearching = isOnlineSearching,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            selectedTierFilter = selectedTierFilter,
                            effectiveTierGoal = effectiveTierGoal,
                            targetMealType = activePortionMeal,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            onCategoryChange = { viewModel.selectedCategory.value = it },
                            onSelectTierFilter = { tier -> viewModel.setSelectedTierFilter(tier) },
                            onSelectTargetMeal = { meal -> viewModel.setTargetMeal(meal) },
                            onToggleFavorite = { product -> viewModel.toggleFavoriteProduct(product.id, !product.isFavorite) },
                            onAddSavedMealClick = { savedMeal -> viewModel.addSavedMealToDiary(savedMeal, activePortionMeal) },
                            onDeleteSavedMealClick = { savedMeal -> viewModel.deleteSavedMeal(savedMeal) },
                            onProductClick = { product ->
                                viewModel.openPortionDialog(product, activePortionMeal)
                            },
                            onOpenProductInfo = { product ->
                                viewModel.openProductInfo(product)
                            },
                            onEditCustomProduct = { product ->
                                viewModel.openEditCustomProduct(product)
                            },
                            onCreateProductWithQuery = { q ->
                                viewModel.openCreateProductPrefilled(name = q)
                            },
                            onOpenAiRecognition = {
                                viewModel.selectTab(AppTab.DASHBOARD)
                            },
                            onCreateProductClick = {
                                viewModel.openCreateProduct()
                            },
                            onOpenScanner = {
                                viewModel.openScanner(activePortionMeal)
                            },
                            onDeleteProduct = { product ->
                                viewModel.deleteProduct(product)
                            },
                            appLanguage = appLanguage
                        )
                    }

                    AppTab.PROFILE -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            onSaveProfile = { age, gender, height, weight, targetWeight, activity, goal, isManual, cal, p, f, c ->
                                viewModel.saveProfile(
                                    age = age,
                                    gender = gender,
                                    heightCm = height,
                                    weightKg = weight,
                                    targetWeightKg = targetWeight,
                                    activityLevel = activity,
                                    goal = goal,
                                    isManual = isManual,
                                    customCal = cal,
                                    customP = p,
                                    customF = f,
                                    customC = c
                                )
                            },
                            appLanguage = appLanguage
                        )
                    }

                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            userProfile = userProfile,
                            healthStepData = healthStepData,
                            onRequestHealthPermissions = {
                                healthConnectPermissionLauncher.launch(viewModel.healthConnectManager.permissions)
                            },
                            onSyncHealthConnect = { onResult -> viewModel.syncHealthConnect(onResult) },
                            onSaveWaterGoal = { isManual, customMl -> viewModel.setWaterGoalSetting(isManual, customMl) },
                            onExportBackup = { uri, onComplete -> viewModel.exportBackupToUri(context, uri, onComplete) },
                            onImportBackup = { uri -> viewModel.parseBackupFromUri(context, uri) },
                            onConfirmRestoreBackup = { backupData, onComplete -> viewModel.restoreBackup(backupData, onComplete) },
                            onSetLanguage = { lang -> viewModel.setAppLanguage(lang) },
                            onSetThemeMode = { mode -> viewModel.setAppThemeMode(mode) },
                            tierMode = tierMode,
                            onSetTierMode = { mode -> viewModel.setTierMode(mode) },
                            onOpenChangelog = { viewModel.openChangelog() },
                            onNavigateToProfile = { viewModel.selectTab(AppTab.PROFILE) },
                            onNavigateToCatalog = { viewModel.selectTab(AppTab.CATALOG) },
                            appLanguage = appLanguage
                        )
                    }
                }
            }

            // Top Status Toast Notification
            AnimatedVisibility(
                visible = scannedStatusMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 20.dp, end = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, WildWatermelon, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WildWatermelon,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = scannedStatusMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }

    val scannerState by viewModel.scannerState.collectAsStateWithLifecycle()
    val prefilledProductData by viewModel.prefilledProductData.collectAsStateWithLifecycle()

    // Modal Camera Barcode Scanner
    if (isScannerOpen) {
        BarcodeScannerDialog(
            scannerState = scannerState,
            onResetScannerState = { viewModel.resetScannerState() },
            onOpenCreateProduct = { barcode -> viewModel.openCreateProduct(barcode) },
            onDismiss = { viewModel.closeScanner() },
            onBarcodeScanned = { barcode ->
                viewModel.processScannedBarcode(barcode)
            }
        )
    }

    // Modal Portion Dialog
    activePortionProduct?.let { product ->
        val currentEdit = editingMealEntry
        PortionDialog(
            product = product,
            initialMealType = activePortionMeal,
            initialGrams = currentEdit?.weightGrams ?: 100.0,
            isEditMode = currentEdit != null,
            effectiveTierGoal = effectiveTierGoal,
            onOpenProductInfo = { prod -> viewModel.openProductInfo(prod) },
            onDismiss = { viewModel.closePortionDialog() },
            onConfirm = { mealType, grams ->
                viewModel.addPortion(mealType, grams)
            },
            appLanguage = appLanguage
        )
    }

    // Modal Product Science Info BottomSheet
    activeProductForInfo?.let { product ->
        com.example.ui.components.ProductInfoBottomSheet(
            product = product,
            onFetchInfo = { p, callback ->
                viewModel.fetchProductScienceInfo(p, callback)
            },
            onDismiss = { viewModel.closeProductInfo() },
            appLanguage = appLanguage
        )
    }

    val editingProduct by viewModel.editingProduct.collectAsStateWithLifecycle()

    // Modal Create Custom Product Dialog
    if (isCreateProductOpen) {
        CreateProductDialog(
            initialBarcode = createProductInitialBarcode,
            prefilledData = prefilledProductData,
            isEditMode = editingProduct != null,
            onDismiss = { viewModel.closeCreateProduct() },
            onSaveProduct = { name, category, cal, p, f, c, barcode ->
                val currentEditing = editingProduct
                if (currentEditing != null) {
                    viewModel.updateCustomProduct(
                        currentEditing.copy(
                            name = name,
                            category = category,
                            calories = cal,
                            protein = p,
                            fat = f,
                            carbs = c,
                            barcode = barcode
                        )
                    )
                } else {
                    viewModel.saveCustomProduct(name, category, cal, p, f, c, barcode)
                }
            },
            appLanguage = appLanguage
        )
    }

    val aiPhotoState by viewModel.aiPhotoState.collectAsStateWithLifecycle()

    // Modal AI Food Photo Analysis Dialog
    if (isAiPhotoDialogOpen) {
        AiFoodPhotoDialog(
            aiPhotoState = aiPhotoState,
            onRequestCameraTempUri = { ctx -> viewModel.createCameraTempUri(ctx) },
            onProcessPhotoUri = { ctx, uri -> viewModel.processAiPhotoUri(ctx, uri) },
            onUpdateDishName = { name -> viewModel.updateAiPhotoDishName(name) },
            onUpdateItemName = { id, name -> viewModel.updateAiPhotoItemName(id, name) },
            onUpdateItemGrams = { id, text -> viewModel.updateAiPhotoItemGrams(id, text) },
            onUpdateItemCalories = { id, text -> viewModel.updateAiPhotoItemCalories(id, text) },
            onUpdateItemProtein = { id, text -> viewModel.updateAiPhotoItemProtein(id, text) },
            onUpdateItemFat = { id, text -> viewModel.updateAiPhotoItemFat(id, text) },
            onUpdateItemCarbs = { id, text -> viewModel.updateAiPhotoItemCarbs(id, text) },
            onRemoveItem = { id -> viewModel.removeAiPhotoItem(id) },
            onAddManualItem = { viewModel.addManualAiPhotoItem() },
            onUpdateMealType = { type -> viewModel.updateAiPhotoMealType(type) },
            onOpenCatalogWithSearch = { search ->
                viewModel.searchQuery.value = search
                viewModel.selectTab(AppTab.CATALOG)
            },
            currentDateString = currentDate.toString(),
            effectiveTierGoal = effectiveTierGoal,
            onDismiss = {
                viewModel.clearAiPhotoState()
                isAiPhotoDialogOpen = false
            },
            onSaveMealEntries = { entries ->
                scope.launch { viewModel.addAiMealEntries(entries) }
                viewModel.clearAiPhotoState()
            },
            appLanguage = appLanguage
        )
    }

    // Global ModalBottomSheet Action Menu (Search, Scan, Photo, AI Text)
    val currentHour = java.time.LocalTime.now().hour
    val currentMealType = when (currentHour) {
        in 5..11 -> MealType.BREAKFAST
        in 12..16 -> MealType.LUNCH
        in 17..21 -> MealType.DINNER
        else -> MealType.SNACK
    }

    if (isAddMenuOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { isAddMenuOpen = false },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage == "en") "Add to Log (${currentMealType.titleEn})" else "Добавить в дневник (${currentMealType.titleRu})",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { isAddMenuOpen = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: Search in Catalog
                    ActionMenuRow(
                        icon = Icons.Default.Search,
                        title = if (appLanguage == "en") "Search Food" else "Найти продукт",
                        subtitle = if (appLanguage == "en") "Search catalog & open food database" else "Поиск по каталогу продуктов и открытой базе",
                        onClick = {
                            isAddMenuOpen = false
                            viewModel.openCatalogForMeal(currentMealType)
                        }
                    )

                    // Option 2: Scan Barcode
                    ActionMenuRow(
                        icon = Icons.Default.QrCodeScanner,
                        title = if (appLanguage == "en") "Scan Barcode" else "Сканировать штрих-код",
                        subtitle = if (appLanguage == "en") "Scan package barcode" else "Быстрое сканирование кода на упаковке",
                        onClick = {
                            isAddMenuOpen = false
                            viewModel.openScanner(currentMealType)
                        }
                    )

                    // Option 3: Take Food Photo
                    ActionMenuRow(
                        icon = Icons.Default.CameraAlt,
                        title = if (appLanguage == "en") "Food Photo AI" else "Сфотографировать блюдо",
                        subtitle = if (appLanguage == "en") "AI recognition from photo" else "ИИ-анализ и распознавание порции по фото",
                        onClick = {
                            isAddMenuOpen = false
                            isAiPhotoDialogOpen = true
                        }
                    )

                    // Option 4: AI Express Text Entry
                    ActionMenuRow(
                        icon = Icons.Default.AutoAwesome,
                        title = if (appLanguage == "en") "Express AI Text" else "Написать, что съели",
                        subtitle = if (appLanguage == "en") "Type phrase (e.g. 2 eggs & coffee)" else "Экспресс-ввод фразой (например: 2 яйца и овсянка)",
                        onClick = {
                            isAddMenuOpen = false
                            viewModel.selectTab(AppTab.DASHBOARD)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Changelog Release Notes Screen
    if (isChangelogOpen) {
        com.example.ui.screens.ChangelogScreen(
            onBackClick = { viewModel.closeChangelog() }
        )
    }
    }
}

@Composable
fun FitnessBottomBar(
    selectedTab: AppTab,
    appLanguage: String = "ru",
    onTabSelect: (AppTab) -> Unit,
    onOpenAddMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(30.dp),
                    spotColor = WildWatermelon.copy(alpha = 0.25f)
                ),
            shape = RoundedCornerShape(30.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Главная
                NavTabButton(
                    title = AppTab.DASHBOARD.title(appLanguage),
                    icon = Icons.Default.DonutLarge,
                    isSelected = selectedTab == AppTab.DASHBOARD,
                    onClick = { onTabSelect(AppTab.DASHBOARD) },
                    tag = "tab_dashboard",
                    modifier = Modifier.weight(1f)
                )

                // 2. Дневник
                NavTabButton(
                    title = AppTab.DIARY.title(appLanguage),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    isSelected = selectedTab == AppTab.DIARY,
                    onClick = { onTabSelect(AppTab.DIARY) },
                    tag = "tab_diary",
                    modifier = Modifier.weight(1f)
                )

                // 3. Выделенная центральная кнопка Добавить (+)
                AddCentralButton(
                    onClick = onOpenAddMenu,
                    tag = "central_add_button"
                )

                // 4. Профиль
                NavTabButton(
                    title = AppTab.PROFILE.title(appLanguage),
                    icon = Icons.Default.Person,
                    isSelected = selectedTab == AppTab.PROFILE,
                    onClick = { onTabSelect(AppTab.PROFILE) },
                    tag = "tab_profile",
                    modifier = Modifier.weight(1f)
                )

                // 5. Настройки
                NavTabButton(
                    title = AppTab.SETTINGS.title(appLanguage),
                    icon = Icons.Default.Settings,
                    isSelected = selectedTab == AppTab.SETTINGS,
                    onClick = { onTabSelect(AppTab.SETTINGS) },
                    tag = "tab_settings",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NavTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(38.dp, 28.dp)
                .background(
                    color = if (isSelected) WildWatermelonContainer else Color.Transparent,
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(22.dp),
                tint = if (isSelected) WildWatermelon else TextTertiary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) WildWatermelon else TextTertiary,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun AddCentralButton(
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(52.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                spotColor = WildWatermelon
            )
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WildWatermelon,
                        Color(0xFFFF687C)
                    )
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Добавить в дневник",
            modifier = Modifier.size(28.dp),
            tint = Color(0xFF101014)
        )
    }
}

@Composable
private fun ActionMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(WildWatermelonContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = WildWatermelon, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
