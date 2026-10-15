package com.example

import android.os.Bundle
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricLime
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
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KbzhuApp()
            }
        }
    }
}

@Composable
fun KbzhuApp(viewModel: MainViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val currentDate by viewModel.currentDate.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val entries by viewModel.currentDayEntries.collectAsStateWithLifecycle()

    val filteredProducts by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val activePortionProduct by viewModel.activePortionProduct.collectAsStateWithLifecycle()
    val activePortionMeal by viewModel.activePortionMeal.collectAsStateWithLifecycle()

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
    val googleFitData by viewModel.googleFitData.collectAsStateWithLifecycle()
    val userAuthState by viewModel.authState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var isAiPhotoDialogOpen by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        viewModel.googleFitManager.registerStepSensor()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        InactivityReminderManager.scheduleReminder(context)
    }

    // Handle system back navigation: return to Dashboard tab if on secondary tab
    BackHandler(enabled = selectedTab != AppTab.DASHBOARD) {
        viewModel.selectTab(AppTab.DASHBOARD)
    }

    // Auto-dismiss status message after 2.5 seconds
    LaunchedEffect(scannedStatusMessage) {
        if (scannedStatusMessage != null) {
            delay(2500)
            viewModel.clearStatusMessage()
        }
    }

    val displayDate = viewModel.formatDisplayDate(currentDate)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        containerColor = DarkBackground,
        bottomBar = {
            FitnessBottomBar(
                selectedTab = selectedTab,
                onTabSelect = { tab -> viewModel.selectTab(tab) }
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
                            dailyCaloriesMap = weekCaloriesMap,
                            weeklyTrendData = weeklyTrendData,
                            currentWaterMl = currentWaterMl,
                            targetWaterMl = targetWaterMl,
                            weightEntries = weightEntries,
                            onLogWeight = { w, d -> viewModel.logWeight(w, d) },
                            onSelectDate = { date -> viewModel.setDate(date) },
                            onAddWater = { amount -> viewModel.addWater(amount) },
                            onRemoveLastWater = { viewModel.removeLastWater() },
                            onOpenCatalogForMeal = { mealType ->
                                viewModel.setTargetMeal(mealType)
                                viewModel.selectTab(AppTab.CATALOG)
                            },
                            onOpenScanner = {
                                viewModel.openScanner(activePortionMeal)
                            },
                            onNavigateToTab = { targetTab -> viewModel.selectTab(targetTab) },
                            onAddAiMeal = { dishName, grams, calories, p, f, c, mealType ->
                                viewModel.addAiMealEntry(dishName, grams, calories, p, f, c, mealType)
                            },
                            onOpenCameraPhoto = {
                                isAiPhotoDialogOpen = true
                            }
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
                                viewModel.selectTab(AppTab.CATALOG)
                            },
                            onDeleteEntry = { entry ->
                                viewModel.deleteMealEntry(entry)
                            }
                        )
                    }

                    AppTab.CATALOG -> {
                        CatalogScreen(
                            products = filteredProducts,
                            searchQuery = searchQuery,
                            selectedCategory = selectedCategory,
                            targetMealType = activePortionMeal,
                            onSearchQueryChange = { viewModel.searchQuery.value = it },
                            onCategoryChange = { viewModel.selectedCategory.value = it },
                            onSelectTargetMeal = { meal -> viewModel.setTargetMeal(meal) },
                            onProductClick = { product ->
                                viewModel.openPortionDialog(product, activePortionMeal)
                            },
                            onCreateProductClick = {
                                viewModel.openCreateProduct()
                            },
                            onOpenScanner = {
                                viewModel.openScanner(activePortionMeal)
                            },
                            onDeleteProduct = { product ->
                                viewModel.deleteProduct(product)
                            }
                        )
                    }

                    AppTab.PROFILE -> {
                        ProfileScreen(
                            userProfile = userProfile,
                            onSaveProfile = { age, gender, height, weight, activity, goal, isManual, cal, p, f, c ->
                                viewModel.saveProfile(
                                    age = age,
                                    gender = gender,
                                    heightCm = height,
                                    weightKg = weight,
                                    activityLevel = activity,
                                    goal = goal,
                                    isManual = isManual,
                                    customCal = cal,
                                    customP = p,
                                    customF = f,
                                    customC = c
                                )
                            }
                        )
                    }

                    AppTab.SETTINGS -> {
                        SettingsScreen(
                            userProfile = userProfile,
                            googleFitData = googleFitData,
                            userAuthState = userAuthState,
                            onSyncGoogleFit = { viewModel.syncGoogleFit() },
                            onToggleGoogleFitAutoSync = { enabled -> viewModel.toggleGoogleFitAutoSync(enabled) },
                            onLoginWithGoogle = { email -> viewModel.loginWithGoogle(email) },
                            onLoginWithEmail = { email -> viewModel.loginWithEmail(email) },
                            onLogoutUser = { viewModel.logoutUser() },
                            onNavigateToProfile = { viewModel.selectTab(AppTab.PROFILE) },
                            onNavigateToCatalog = { viewModel.selectTab(AppTab.CATALOG) }
                        )
                    }

                    AppTab.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = { email, name ->
                                viewModel.loginWithGoogle(email, name)
                                viewModel.selectTab(AppTab.DASHBOARD)
                            },
                            onSkipAsGuest = {
                                viewModel.selectTab(AppTab.DASHBOARD)
                            }
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

    // Modal Camera Barcode Scanner
    if (isScannerOpen) {
        BarcodeScannerDialog(
            onDismiss = { viewModel.closeScanner() },
            onBarcodeScanned = { barcode ->
                viewModel.processScannedBarcode(barcode)
            }
        )
    }

    // Barcode Not Found Dialog
    notFoundBarcode?.let { code ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissNotFoundBarcode() },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Штрих-код не найден",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Продукт со штрих-кодом $code отсутствует в базе. Желаете создать новый продукт с этим штрих-кодом?",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentNotFound = code
                        viewModel.dismissNotFoundBarcode()
                        viewModel.closeScanner()
                        viewModel.openCreateProduct(currentNotFound)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelon,
                        contentColor = Color(0xFF101014)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_scanned_product_button")
                ) {
                    Text("Создать продукт", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissNotFoundBarcode() }) {
                    Text("Отмена", color = TextTertiary)
                }
            }
        )
    }

    // Modal Portion Dialog
    activePortionProduct?.let { product ->
        PortionDialog(
            product = product,
            initialMealType = activePortionMeal,
            onDismiss = { viewModel.closePortionDialog() },
            onConfirm = { mealType, grams ->
                viewModel.addPortion(mealType, grams)
            }
        )
    }

    // Modal Create Custom Product Dialog
    if (isCreateProductOpen) {
        CreateProductDialog(
            initialBarcode = createProductInitialBarcode,
            onDismiss = { viewModel.closeCreateProduct() },
            onSaveProduct = { name, category, cal, p, f, c, barcode ->
                viewModel.saveCustomProduct(name, category, cal, p, f, c, barcode)
            }
        )
    }

    // Modal AI Food Photo Analysis Dialog
    if (isAiPhotoDialogOpen) {
        AiFoodPhotoDialog(
            onDismiss = { isAiPhotoDialogOpen = false },
            onAddMeal = { dishName, grams, calories, p, f, c, mealType ->
                viewModel.addAiMealEntry(dishName, grams, calories, p, f, c, mealType)
            }
        )
    }
}

@Composable
fun FitnessBottomBar(
    selectedTab: AppTab,
    onTabSelect: (AppTab) -> Unit,
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
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Главная
                NavTabButton(
                    title = "Главная",
                    icon = Icons.Default.DonutLarge,
                    isSelected = selectedTab == AppTab.DASHBOARD,
                    onClick = { onTabSelect(AppTab.DASHBOARD) },
                    tag = "tab_dashboard"
                )

                // 2. Дневник
                NavTabButton(
                    title = "Дневник",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    isSelected = selectedTab == AppTab.DIARY,
                    onClick = { onTabSelect(AppTab.DIARY) },
                    tag = "tab_diary"
                )

                // 3. Выделенная кнопка поиска (Лупа, без текста "Продукты", больше остальных)
                SearchTabButton(
                    isSelected = selectedTab == AppTab.CATALOG,
                    onClick = { onTabSelect(AppTab.CATALOG) },
                    tag = "tab_catalog"
                )

                // 4. Профиль
                NavTabButton(
                    title = "Профиль",
                    icon = Icons.Default.Person,
                    isSelected = selectedTab == AppTab.PROFILE,
                    onClick = { onTabSelect(AppTab.PROFILE) },
                    tag = "tab_profile"
                )

                // 5. Настройки
                NavTabButton(
                    title = "Настройки",
                    icon = Icons.Default.Settings,
                    isSelected = selectedTab == AppTab.SETTINGS,
                    onClick = { onTabSelect(AppTab.SETTINGS) },
                    tag = "tab_settings"
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
    tag: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
            maxLines = 1
        )
    }
}

@Composable
private fun SearchTabButton(
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .size(52.dp)
            .shadow(
                elevation = if (isSelected) 10.dp else 6.dp,
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
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, Color.White, CircleShape)
                } else {
                    Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                }
            )
            .clickable(onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Поиск продуктов",
            modifier = Modifier.size(28.dp),
            tint = Color.White
        )
    }
}
