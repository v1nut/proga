package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import com.example.ui.theme.DarkSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import com.example.ui.components.MonthCalendarDialog
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
    currentDate: LocalDate,
    displayDateText: String,
    entries: List<MealEntry>,
    dailyCaloriesMap: Map<String, Double> = emptyMap(),
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onTodayClick: () -> Unit,
    onSelectDate: (LocalDate) -> Unit = {},
    onAddProductClick: (MealType) -> Unit,
    onEditEntry: (MealEntry) -> Unit = {},
    onDeleteEntry: (MealEntry) -> Unit,
    onCopyMealToToday: (MealType) -> Unit = {},
    onCopyMealToDate: (MealType, String) -> Unit = { _, _ -> },
    onSaveMealAsDish: (String, List<MealEntry>) -> Unit = { _, _ -> },
    effectiveTierGoal: com.example.data.model.FitnessGoal = com.example.data.model.FitnessGoal.MAINTENANCE,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val totalCalories = entries.sumOf { it.calories }
    val totalProtein = entries.sumOf { it.protein }
    val totalFat = entries.sumOf { it.fat }
    val totalCarbs = entries.sumOf { it.carbs }

    var isMonthCalendarOpen by remember { mutableStateOf(false) }

    if (isMonthCalendarOpen) {
        MonthCalendarDialog(
            selectedDate = currentDate,
            dailyCaloriesMap = dailyCaloriesMap,
            onDateSelected = { date ->
                onSelectDate(date)
                isMonthCalendarOpen = false
            },
            onDismiss = { isMonthCalendarOpen = false },
            appLanguage = appLanguage
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        // Date Switcher Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCard)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousDay,
                modifier = Modifier.testTag("diary_prev_day")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Предыдущий день",
                    tint = TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isMonthCalendarOpen = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Выбрать дату в календаре",
                        tint = WildWatermelon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = displayDateText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }

                if (currentDate != LocalDate.now()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WildWatermelonContainer)
                            .clickable { onTodayClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (appLanguage == "en") "Today" else "Сегодня",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WildWatermelon
                        )
                    }
                }
            }

            IconButton(
                onClick = onNextDay,
                modifier = Modifier.testTag("diary_next_day")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Следующий день",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Daily Summary Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (appLanguage == "en") "Daily Total:" else "Всего за день:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${totalCalories.roundToInt()} ккал",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WildWatermelon
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MacroMiniBadge(label = if (appLanguage == "en") "P" else "Б", value = totalProtein, color = ProteinColor)
                MacroMiniBadge(label = if (appLanguage == "en") "F" else "Ж", value = totalFat, color = FatColor)
                MacroMiniBadge(label = if (appLanguage == "en") "C" else "У", value = totalCarbs, color = CarbsColor)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4 Meal Sections
        MealSection(
            currentDate = currentDate,
            mealType = MealType.BREAKFAST,
            icon = Icons.Default.FreeBreakfast,
            entries = entries.filter { it.mealType == MealType.BREAKFAST.name },
            onAddClick = { onAddProductClick(MealType.BREAKFAST) },
            onEditEntry = onEditEntry,
            onDeleteEntry = onDeleteEntry,
            onCopyMealToToday = onCopyMealToToday,
            onCopyMealToDate = onCopyMealToDate,
            onSaveMealAsDish = onSaveMealAsDish,
            effectiveTierGoal = effectiveTierGoal,
            appLanguage = appLanguage
        )

        Spacer(modifier = Modifier.height(14.dp))

        MealSection(
            currentDate = currentDate,
            mealType = MealType.LUNCH,
            icon = Icons.Default.LunchDining,
            entries = entries.filter { it.mealType == MealType.LUNCH.name },
            onAddClick = { onAddProductClick(MealType.LUNCH) },
            onEditEntry = onEditEntry,
            onDeleteEntry = onDeleteEntry,
            onCopyMealToToday = onCopyMealToToday,
            onCopyMealToDate = onCopyMealToDate,
            onSaveMealAsDish = onSaveMealAsDish,
            effectiveTierGoal = effectiveTierGoal,
            appLanguage = appLanguage
        )

        Spacer(modifier = Modifier.height(14.dp))

        MealSection(
            currentDate = currentDate,
            mealType = MealType.DINNER,
            icon = Icons.Default.DinnerDining,
            entries = entries.filter { it.mealType == MealType.DINNER.name },
            onAddClick = { onAddProductClick(MealType.DINNER) },
            onEditEntry = onEditEntry,
            onDeleteEntry = onDeleteEntry,
            onCopyMealToToday = onCopyMealToToday,
            onCopyMealToDate = onCopyMealToDate,
            onSaveMealAsDish = onSaveMealAsDish,
            effectiveTierGoal = effectiveTierGoal,
            appLanguage = appLanguage
        )

        Spacer(modifier = Modifier.height(14.dp))

        MealSection(
            currentDate = currentDate,
            mealType = MealType.SNACK,
            icon = Icons.Default.Cookie,
            entries = entries.filter { it.mealType == MealType.SNACK.name },
            onAddClick = { onAddProductClick(MealType.SNACK) },
            onEditEntry = onEditEntry,
            onDeleteEntry = onDeleteEntry,
            onCopyMealToToday = onCopyMealToToday,
            onCopyMealToDate = onCopyMealToDate,
            onSaveMealAsDish = onSaveMealAsDish,
            effectiveTierGoal = effectiveTierGoal,
            appLanguage = appLanguage
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MealSection(
    currentDate: LocalDate,
    mealType: MealType,
    icon: ImageVector,
    entries: List<MealEntry>,
    onAddClick: () -> Unit,
    onEditEntry: (MealEntry) -> Unit = {},
    onDeleteEntry: (MealEntry) -> Unit,
    onCopyMealToToday: (MealType) -> Unit = {},
    onCopyMealToDate: (MealType, String) -> Unit = { _, _ -> },
    onSaveMealAsDish: (String, List<MealEntry>) -> Unit = { _, _ -> },
    effectiveTierGoal: com.example.data.model.FitnessGoal = com.example.data.model.FitnessGoal.MAINTENANCE,
    appLanguage: String = "ru"
) {
    val mealKcal = entries.sumOf { it.calories }.roundToInt()
    val mealProtein = entries.sumOf { it.protein }
    val mealFat = entries.sumOf { it.fat }
    val mealCarbs = entries.sumOf { it.carbs }

    val mealAvgTier = remember(entries, effectiveTierGoal) {
        com.example.ui.util.FoodTierSystem.calculateAverageTier(entries, effectiveTierGoal)
    }

    var isMenuOpen by rememberSaveable { mutableStateOf(false) }
    var isCopyDateDialogOpen by rememberSaveable { mutableStateOf(false) }
    var isSaveDishDialogOpen by rememberSaveable { mutableStateOf(false) }
    var dishNameInput by rememberSaveable { mutableStateOf(mealType.title(appLanguage)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.example.ui.components.FlatMealTypeIcon(
                    mealType = mealType,
                    size = 28.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = mealType.title(appLanguage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (entries.isNotEmpty()) {
                        val fmt = if (appLanguage == "en") "P: %.1fg  F: %.1fg  C: %.1fg" else "Б: %.1fг  Ж: %.1fг  У: %.1fг"
                        Text(
                            text = String.format(Locale.forLanguageTag("ru"), fmt, mealProtein, mealFat, mealCarbs),
                            fontSize = 10.5.sp,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                if (mealAvgTier != null) {
                    com.example.ui.util.TierSquareBadge(tier = mealAvgTier, size = 22.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (mealKcal > 0) {
                    Text(
                        text = "$mealKcal ккал",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onAddClick() },
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
                            contentDescription = "Добавить",
                            tint = WildWatermelon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (entries.isNotEmpty()) {
                    Box {
                        IconButton(
                            onClick = { isMenuOpen = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.MoreVert,
                                contentDescription = "Меню приёма пищи",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuOpen,
                            onDismissRequest = { isMenuOpen = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (appLanguage == "en") "Copy to Today" else "Копировать на сегодня", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    isMenuOpen = false
                                    onCopyMealToToday(mealType)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (appLanguage == "en") "Copy to Date…" else "Копировать на дату…", color = TextPrimary, fontSize = 13.sp) },
                                onClick = {
                                    isMenuOpen = false
                                    isCopyDateDialogOpen = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (appLanguage == "en") "Save as Recipe" else "Сохранить как блюдо", color = WildWatermelon, fontSize = 13.sp) },
                                onClick = {
                                    isMenuOpen = false
                                    isSaveDishDialogOpen = true
                                }
                            )
                        }
                    }
                }
            }
        }

        if (isCopyDateDialogOpen) {
            val initialTomorrowMillis = remember(currentDate) {
                java.time.LocalDate.now().plusDays(1)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            }

            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = initialTomorrowMillis
            )

            val selectedDateMillis = datePickerState.selectedDateMillis
            val selectedLocalDate = if (selectedDateMillis != null) {
                java.time.Instant.ofEpochMilli(selectedDateMillis)
                    .atZone(java.time.ZoneOffset.UTC)
                    .toLocalDate()
            } else null

            val isSameDate = selectedLocalDate != null && selectedLocalDate == currentDate
            val isCopyValid = selectedLocalDate != null && !isSameDate

            DatePickerDialog(
                onDismissRequest = { isCopyDateDialogOpen = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedLocalDate != null && isCopyValid) {
                                onCopyMealToDate(mealType, selectedLocalDate.toString())
                                isCopyDateDialogOpen = false
                            }
                        },
                        enabled = isCopyValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014)
                        )
                    ) {
                        Text(
                            text = if (appLanguage == "en") "Copy" else "Копировать",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isCopyDateDialogOpen = false }) {
                        Text(
                            text = if (appLanguage == "en") "Cancel" else "Отмена",
                            color = TextTertiary
                        )
                    }
                }
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                    if (isSameDate) {
                        Text(
                            text = if (appLanguage == "en") "Cannot copy to the same date" else "Нельзя копировать на ту же дату",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp)
                        )
                    }
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            selectedDayContainerColor = WildWatermelon,
                            selectedDayContentColor = Color(0xFF101014),
                            todayDateBorderColor = WildWatermelon,
                            todayContentColor = WildWatermelon
                        )
                    )
                }
            }
        }

        if (isSaveDishDialogOpen) {
            AlertDialog(
                onDismissRequest = { isSaveDishDialogOpen = false },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(20.dp),
                title = { Text(if (appLanguage == "en") "Save as Recipe" else "Сохранить как блюдо", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary) },
                text = {
                    Column {
                        Text(if (appLanguage == "en") "Enter a name for this recipe / dish:" else "Введите название сложного блюда или рецепта:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = dishNameInput,
                            onValueChange = { dishNameInput = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (dishNameInput.isNotBlank()) {
                                onSaveMealAsDish(dishNameInput.trim(), entries)
                                isSaveDishDialogOpen = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = WildWatermelon, contentColor = Color(0xFF101014)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (appLanguage == "en") "Save" else "Сохранить", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isSaveDishDialogOpen = false }) {
                        Text(if (appLanguage == "en") "Cancel" else "Отмена", color = TextTertiary)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCardElevated)
                    .clickable { onAddClick() }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (appLanguage == "en") "+ Add food to ${mealType.titleEn.lowercase()}" else "+ Добавить продукт в ${mealType.titleRu.lowercase()}",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.forEach { entry ->
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
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardElevated)
                            .clickable { onEditEntry(entry) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                maxLines = 2,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val macroFmt = if (appLanguage == "en") "%.0fg • P: %.1f  F: %.1f  C: %.1f" else "%.0fг • Б: %.1f  Ж: %.1f  У: %.1f"
                            Text(
                                text = String.format(
                                    Locale.forLanguageTag("ru"),
                                    macroFmt,
                                    entry.weightGrams,
                                    entry.protein,
                                    entry.fat,
                                    entry.carbs
                                ),
                                fontSize = 10.5.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${entry.calories.roundToInt()} ${if (appLanguage == "en") "kcal" else "ккал"}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable { onDeleteEntry(entry) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Удалить запись",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroMiniBadge(
    label: String,
    value: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = String.format(Locale.forLanguageTag("ru"), "%.0fг", value),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
