package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.model.WeightEntry
import com.example.ui.AppTab
import com.example.ui.components.CalorieRing
import com.example.ui.components.DayTrendData
import com.example.ui.components.MacroOverviewSection
import com.example.ui.components.QuickAiMealBar
import com.example.ui.components.WaterTrackerCard
import com.example.ui.components.WeekCalendarBar
import com.example.ui.components.WeeklyTrendsCard
import com.example.ui.components.WeightTrendChartCard
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    currentDate: LocalDate,
    displayDateText: String,
    userProfile: UserProfile,
    entries: List<MealEntry>,
    dailyCaloriesMap: Map<String, Double>,
    weeklyTrendData: List<DayTrendData>,
    currentWaterMl: Int,
    targetWaterMl: Int,
    weightEntries: List<WeightEntry> = emptyList(),
    onLogWeight: (weightKg: Double, dateString: String) -> Unit = { _, _ -> },
    onSelectDate: (LocalDate) -> Unit,
    onAddWater: (amountMl: Int) -> Unit,
    onRemoveLastWater: () -> Unit,
    onOpenCatalogForMeal: (MealType) -> Unit,
    onOpenScanner: () -> Unit,
    onNavigateToTab: (AppTab) -> Unit,
    onAddAiMeal: (dishName: String, grams: Int, calories: Int, protein: Double, fat: Double, carbs: Double, mealType: MealType) -> Unit = { _, _, _, _, _, _, _ -> },
    onOpenCameraPhoto: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.sumOf { it.protein }
    val totalFat = entries.sumOf { it.fat }
    val totalCarbs = entries.sumOf { it.carbs }

    val isToday = currentDate == LocalDate.now()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // 7-Day Week Calendar Buttons Header with micro progress bars
        WeekCalendarBar(
            selectedDate = currentDate,
            dailyCaloriesMap = dailyCaloriesMap,
            targetCalories = userProfile.targetCalories,
            onSelectDate = onSelectDate,
            modifier = Modifier.testTag("dashboard_week_calendar_bar")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Express AI Chat Input Bar
        QuickAiMealBar(
            onAddMeal = onAddAiMeal,
            onOpenCameraPhoto = onOpenCameraPhoto,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Calorie Balance Card (Macros on Left, Calorie Ring on Right)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Дневной баланс калорий",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
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
                            text = "К сегодня",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = WildWatermelon
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Balance Row: Macros on LEFT, CalorieRing on RIGHT
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column: Proteins, Fats, Carbs
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacroCompactBar(
                        label = "Белки (Б)",
                        consumed = totalProtein,
                        target = userProfile.targetProtein,
                        color = com.example.ui.theme.ProteinColor
                    )
                    MacroCompactBar(
                        label = "Жиры (Ж)",
                        consumed = totalFat,
                        target = userProfile.targetFat,
                        color = com.example.ui.theme.FatColor
                    )
                    MacroCompactBar(
                        label = "Углеводы (У)",
                        consumed = totalCarbs,
                        target = userProfile.targetCarbs,
                        color = com.example.ui.theme.CarbsColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right Column: 1 Big Calorie Circle
                Box(
                    modifier = Modifier.weight(0.9f),
                    contentAlignment = Alignment.Center
                ) {
                    CalorieRing(
                        consumedCalories = totalCalories,
                        targetCalories = userProfile.targetCalories,
                        ringSize = 135.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub-metrics row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkCardElevated)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatColumn(
                    label = "Употреблено",
                    value = "${totalCalories.roundToInt()} ккал",
                    color = WildWatermelon
                )
                StatDivider()
                StatColumn(
                    label = "Норма",
                    value = "${userProfile.targetCalories} ккал",
                    color = TextPrimary
                )
                StatDivider()
                val rem = userProfile.targetCalories - totalCalories.roundToInt()
                StatColumn(
                    label = "Остаток",
                    value = if (rem >= 0) "$rem ккал" else "+${-rem} ккал",
                    color = if (rem >= 0) TextSecondary else Color(0xFFFB7185)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Water Intake Tracker Card
        WaterTrackerCard(
            currentWaterMl = currentWaterMl,
            targetWaterMl = targetWaterMl,
            onAddWater = onAddWater,
            onRemoveLastWater = onRemoveLastWater,
            modifier = Modifier.testTag("dashboard_water_card")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Weekly Progress Line Chart
        WeeklyTrendsCard(
            weeklyData = weeklyTrendData,
            targetCalories = userProfile.targetCalories,
            targetProtein = userProfile.targetProtein,
            targetFat = userProfile.targetFat,
            targetCarbs = userProfile.targetCarbs,
            modifier = Modifier.testTag("dashboard_weekly_trends_card")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 30-Day Weight Progress Recharts Chart Card
        WeightTrendChartCard(
            weightEntries = weightEntries,
            targetWeightKg = if (userProfile.goal == com.example.data.model.FitnessGoal.WEIGHT_LOSS.name) userProfile.weightKg - 4.0 else if (userProfile.goal == com.example.data.model.FitnessGoal.WEIGHT_GAIN.name) userProfile.weightKg + 4.0 else userProfile.weightKg,
            onLogWeight = onLogWeight,
            modifier = Modifier.testTag("dashboard_weight_chart_card")
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Meals Overview Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Приёмы пищи",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Открыть дневник >",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = WildWatermelon,
                modifier = Modifier.clickable { onNavigateToTab(AppTab.DIARY) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Meal Summary Cards
        val meals = listOf(
            MealType.BREAKFAST to "🍳",
            MealType.LUNCH to "🥗",
            MealType.DINNER to "🥩",
            MealType.SNACK to "🍎"
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            meals.forEach { (type, emoji) ->
                val mealEntries = entries.filter { it.mealType == type.name }
                val mealKcal = mealEntries.sumOf { it.calories }.roundToInt()
                val itemCount = mealEntries.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .clickable { onOpenCatalogForMeal(type) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = emoji,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = type.titleRu,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (itemCount > 0) "$itemCount поз. • $mealKcal ккал" else "Не заполнено",
                                fontSize = 12.sp,
                                color = if (itemCount > 0) TextSecondary else TextTertiary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (mealKcal > 0) {
                            Text(
                                text = "$mealKcal ккал",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DarkCardElevated)
                                .clickable { onOpenCatalogForMeal(type) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Добавить в ${type.titleRu}",
                                tint = WildWatermelon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons Row: Add Food & Scan Barcode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { onOpenCatalogForMeal(MealType.BREAKFAST) },
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WildWatermelon,
                    contentColor = Color(0xFF101014)
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("dashboard_add_food_button")
            ) {
                Icon(imageVector = Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Добавить",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }

            OutlinedButton(
                onClick = onOpenScanner,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WildWatermelon),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WildWatermelon
                ),
                modifier = Modifier
                    .height(50.dp)
                    .testTag("dashboard_scan_barcode_button")
            ) {
                Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Штрих-код",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StatColumn(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextTertiary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(DarkBorder)
    )
}

@Composable
private fun MacroCompactBar(
    label: String,
    consumed: Double,
    target: Int,
    color: Color
) {
    val progress = (consumed / target.coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Text(
                text = "${consumed.roundToInt()} / $target г",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
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
