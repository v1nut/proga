package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FitnessGoal
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.model.WeightEntry
import com.example.ui.AppTab
import com.example.ui.components.CalorieRing
import com.example.ui.components.DayTrendData
import com.example.ui.components.QuickAiMealBar
import com.example.ui.components.WaterTrackerCard
import com.example.ui.components.WeekCalendarBar
import com.example.ui.components.WeeklyTrendsCard
import com.example.ui.components.WeightTrendChartCard
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentDate: LocalDate,
    displayDateText: String,
    userProfile: UserProfile,
    entries: List<MealEntry>,
    yesterdayEntries: List<MealEntry> = emptyList(),
    dailyCaloriesMap: Map<String, Double>,
    weeklyTrendData: List<DayTrendData>,
    currentWaterMl: Int,
    targetWaterMl: Int,
    weightEntries: List<WeightEntry> = emptyList(),
    onLogWeight: (weightKg: Double, dateString: String) -> Unit = { _, _ -> },
    onDeleteWeightEntry: (dateString: String) -> Unit = {},
    onRepeatYesterdayMeal: (MealType) -> Unit = {},
    onSelectDate: (LocalDate) -> Unit,
    onAddWater: (amountMl: Int) -> Unit,
    onRemoveLastWater: () -> Unit,
    onOpenCatalogForMeal: (MealType) -> Unit,
    onOpenScanner: () -> Unit,
    onNavigateToTab: (AppTab) -> Unit,
    onEditEntry: (MealEntry) -> Unit = {},
    onSaveAiMealEntries: (List<MealEntry>) -> Unit = {},
    onOpenCameraPhoto: () -> Unit = {},
    effectiveTierGoal: FitnessGoal = FitnessGoal.MAINTENANCE,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.sumOf { it.protein }
    val totalFat = entries.sumOf { it.fat }
    val totalCarbs = entries.sumOf { it.carbs }

    val dailyAvgTier = remember(entries, effectiveTierGoal) {
        com.example.ui.util.FoodTierSystem.calculateAverageTier(entries, effectiveTierGoal)
    }

    val isToday = currentDate == LocalDate.now()

    var isProgressExpanded by rememberSaveable { mutableStateOf(false) }
    var isAddMenuOpen by rememberSaveable { mutableStateOf(false) }

    val currentHour = LocalTime.now().hour
    val currentMealType = when (currentHour) {
        in 5..11 -> MealType.BREAKFAST
        in 12..16 -> MealType.LUNCH
        in 17..21 -> MealType.DINNER
        else -> MealType.SNACK
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Week Calendar Buttons Header
            WeekCalendarBar(
                selectedDate = currentDate,
                dailyCaloriesMap = dailyCaloriesMap,
                targetCalories = userProfile.targetCalories,
                onSelectDate = onSelectDate,
                appLanguage = appLanguage,
                modifier = Modifier.testTag("dashboard_week_calendar_bar")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Compact Express AI Input Bar
            QuickAiMealBar(
                onSaveMealEntries = onSaveAiMealEntries,
                onOpenCatalog = { onNavigateToTab(AppTab.CATALOG) },
                onOpenCameraPhoto = onOpenCameraPhoto,
                currentDateString = currentDate.toString(),
                effectiveTierGoal = effectiveTierGoal,
                appLanguage = appLanguage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Calorie & Macros Summary Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage == "en") "Daily Calorie Balance" else "Дневной баланс калорий",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (!isToday) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkCardElevated)
                                .clickable { onSelectDate(LocalDate.now()) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (appLanguage == "en") "To Today" else "К сегодня",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = WildWatermelon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Macros Column
                    Column(
                        modifier = Modifier.weight(1.1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroCompactBar(label = if (appLanguage == "en") "Protein (P)" else "Белки (Б)", consumed = totalProtein, target = userProfile.targetProtein, color = ProteinColor, appLanguage = appLanguage)
                        MacroCompactBar(label = if (appLanguage == "en") "Fat (F)" else "Жиры (Ж)", consumed = totalFat, target = userProfile.targetFat, color = FatColor, appLanguage = appLanguage)
                        MacroCompactBar(label = if (appLanguage == "en") "Carbs (C)" else "Углеводы (У)", consumed = totalCarbs, target = userProfile.targetCarbs, color = CarbsColor, appLanguage = appLanguage)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Calorie Ring
                    Box(
                        modifier = Modifier.weight(0.9f),
                        contentAlignment = Alignment.Center
                    ) {
                        CalorieRing(
                            consumedCalories = totalCalories,
                            targetCalories = userProfile.targetCalories,
                            appLanguage = appLanguage,
                            ringSize = 125.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Over-consumption Warning vs Remaining Metrics Row
                val rem = userProfile.targetCalories - totalCalories.roundToInt()
                val unitKcal = if (appLanguage == "en") "kcal" else "ккал"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (rem < 0) Color(0xFF2A1517) else DarkCardElevated)
                        .border(1.dp, if (rem < 0) Color(0xFFF87171) else DarkBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatColumn(label = if (appLanguage == "en") "Consumed" else "Употреблено", value = "${totalCalories.roundToInt()} $unitKcal", color = WildWatermelon)
                    StatDivider()
                    StatColumn(label = if (appLanguage == "en") "Target" else "Норма", value = "${userProfile.targetCalories} $unitKcal", color = TextPrimary)
                    StatDivider()

                    // Remaining or Over-limit display
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (rem >= 0) {
                                if (appLanguage == "en") "Remaining" else "Остаток"
                            } else {
                                if (appLanguage == "en") "Over Goal" else "Сверх нормы"
                            },
                            fontSize = 11.sp,
                            color = if (rem >= 0) TextTertiary else Color(0xFFF87171)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (rem < 0) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                            }
                            Text(
                                text = if (rem >= 0) "$rem $unitKcal" else "${-rem} $unitKcal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rem >= 0) TextSecondary else Color(0xFFF87171)
                            )
                        }
                    }
                }

                if (dailyAvgTier != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == "en") "Daily Diet Rank:" else "Ранг рациона за день:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.example.ui.util.TierSquareBadge(tier = dailyAvgTier, size = 22.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (appLanguage == "en") dailyAvgTier.titleEn else dailyAvgTier.titleRu,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = dailyAvgTier.color
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Interactive Meal Cards (Expandable on Dashboard)
            val meals = listOf(
                MealType.BREAKFAST to Icons.Default.FreeBreakfast,
                MealType.LUNCH to Icons.Default.LunchDining,
                MealType.DINNER to Icons.Default.DinnerDining,
                MealType.SNACK to Icons.Default.Cookie
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                meals.forEach { (type, icon) ->
                    val mealEntries = entries.filter { it.mealType == type.name }
                    val mealKcal = mealEntries.sumOf { it.calories }.roundToInt()
                    val mealAvgTier = remember(mealEntries, effectiveTierGoal) {
                        com.example.ui.util.FoodTierSystem.calculateAverageTier(mealEntries, effectiveTierGoal)
                    }
                    var isExpanded by rememberSaveable(type.name) { mutableStateOf(false) }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    ) {
                        // Header Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isExpanded = !isExpanded }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                com.example.ui.components.FlatMealTypeIcon(
                                    mealType = type,
                                    size = 28.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = type.title(appLanguage),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = if (mealEntries.isNotEmpty()) {
                                            if (appLanguage == "en") "${mealEntries.size} items • $mealKcal kcal" else "${mealEntries.size} поз. • $mealKcal ккал"
                                        } else {
                                            if (appLanguage == "en") "Empty" else "Не заполнено"
                                        },
                                        fontSize = 11.sp,
                                        color = if (mealEntries.isNotEmpty()) TextSecondary else TextTertiary
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (mealAvgTier != null) {
                                    com.example.ui.util.TierSquareBadge(tier = mealAvgTier, size = 20.dp)
                                }

                                if (mealKcal > 0) {
                                    Text(
                                        text = if (appLanguage == "en") "$mealKcal kcal" else "$mealKcal ккал",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WildWatermelon
                                    )
                                }

                                // 48dp Touch Target Plus Button
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .clickable { onOpenCatalogForMeal(type) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(DarkCardElevated),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            tint = WildWatermelon,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (mealEntries.isEmpty()) {
                            val yEntries = yesterdayEntries.filter { it.mealType == type.name }
                            if (yEntries.isNotEmpty()) {
                                val yKcal = yEntries.sumOf { it.calories }.roundToInt()
                                val yNames = yEntries.take(3).joinToString(", ") { it.productName }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp)
                                        .padding(bottom = 10.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(DarkCardElevated)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (appLanguage == "en") "Like yesterday: $yNames ($yKcal kcal)" else "Как вчера: $yNames ($yKcal ккал)",
                                        fontSize = 11.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    TextButton(
                                        onClick = { onRepeatYesterdayMeal(type) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                    ) {
                                        Text(if (appLanguage == "en") "Repeat" else "Повторить", fontSize = 11.sp, color = WildWatermelon, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Expanded Products List
                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .padding(bottom = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (mealEntries.isEmpty()) {
                                    Text(
                                        text = if (appLanguage == "en") "Tap \"+\" to add food" else "Нажмите «+» чтобы добавить продукт",
                                        fontSize = 12.sp,
                                        color = TextTertiary,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                } else {
                                    mealEntries.forEach { entry ->
                                        val factor = if (entry.weightGrams > 0) 100.0 / entry.weightGrams else 1.0
                                        val (entryTier, _) = remember(entry, effectiveTierGoal, appLanguage) {
                                            com.example.ui.util.FoodTierSystem.evaluateProductTier(
                                                name = entry.productName,
                                                category = "",
                                                calories = entry.calories * factor,
                                                protein = entry.protein * factor,
                                                fat = entry.fat * factor,
                                                carbs = entry.carbs * factor,
                                                goal = effectiveTierGoal,
                                                lang = appLanguage
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(DarkCardElevated)
                                                .clickable { onEditEntry(entry) }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            com.example.ui.util.TierSquareBadge(tier = entryTier, size = 20.dp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            com.example.ui.components.FlatFoodIcon(
                                                productName = entry.productName,
                                                category = "",
                                                size = 28.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.productName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                                Text(
                                                    text = "${entry.weightGrams.roundToInt()} ${if (appLanguage == "en") "g" else "г"}",
                                                    fontSize = 10.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                            Text(
                                                text = "${entry.calories.roundToInt()} ${if (appLanguage == "en") "kcal" else "ккал"}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = WildWatermelon
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Water Tracker Card
            WaterTrackerCard(
                currentWaterMl = currentWaterMl,
                targetWaterMl = targetWaterMl,
                onAddWater = onAddWater,
                onRemoveLastWater = onRemoveLastWater,
                appLanguage = appLanguage,
                modifier = Modifier.testTag("dashboard_water_card")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Expandable Progress Section (Weekly Trends & Weight Charts)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkCard)
                    .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isProgressExpanded = !isProgressExpanded }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = WildWatermelon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (appLanguage == "en") "Progress & Charts" else "Прогресс и графики",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = if (isProgressExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Раскрыть прогресс",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isProgressExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        WeeklyTrendsCard(
                            weeklyData = weeklyTrendData,
                            targetCalories = userProfile.targetCalories,
                            targetProtein = userProfile.targetProtein,
                            targetFat = userProfile.targetFat,
                            targetCarbs = userProfile.targetCarbs,
                            appLanguage = appLanguage
                        )

                        val currentGoal = try { com.example.data.model.FitnessGoal.valueOf(userProfile.goal) } catch (e: Exception) { com.example.data.model.FitnessGoal.WEIGHT_LOSS }

                        WeightTrendChartCard(
                            weightEntries = weightEntries,
                            targetWeightKg = if (userProfile.targetWeightKg > 0.0) userProfile.targetWeightKg else userProfile.weightKg,
                            goal = currentGoal,
                            onLogWeight = onLogWeight,
                            onDeleteWeightEntry = onDeleteWeightEntry,
                            appLanguage = appLanguage
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MacroCompactBar(
    label: String,
    consumed: Double,
    target: Int,
    color: Color,
    appLanguage: String = "ru"
) {
    val progress = if (target > 0) (consumed.toFloat() / target.toFloat()).coerceIn(0f, 1f) else 0f
    val unitStr = if (appLanguage == "en") "g" else "г"

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = "${consumed.roundToInt()} / $target$unitStr",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkCardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextTertiary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(22.dp)
            .background(DarkBorder)
    )
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Dashboard Large Font Scale 2x",
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFF101014
)
@Composable
fun DashboardScreenPreview() {
    com.example.ui.theme.MyApplicationTheme {
        DashboardScreen(
            currentDate = java.time.LocalDate.now(),
            displayDateText = "Сегодня, 15 октября",
            userProfile = UserProfile(),
            entries = emptyList(),
            dailyCaloriesMap = emptyMap(),
            weeklyTrendData = emptyList(),
            currentWaterMl = 1250,
            targetWaterMl = 2130,
            onSelectDate = {},
            onAddWater = {},
            onRemoveLastWater = {},
            onOpenCatalogForMeal = {},
            onOpenScanner = {},
            onNavigateToTab = {}
        )
    }
}
