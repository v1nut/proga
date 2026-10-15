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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onDeleteEntry: (MealEntry) -> Unit,
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
            onDismiss = { isMonthCalendarOpen = false }
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
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isMonthCalendarOpen = true }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
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
                    text = "Всего за день:",
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
                MacroMiniBadge(label = "Б", value = totalProtein, color = ProteinColor)
                MacroMiniBadge(label = "Ж", value = totalFat, color = FatColor)
                MacroMiniBadge(label = "У", value = totalCarbs, color = CarbsColor)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 4 Meal Sections
        MealSection(
            mealType = MealType.BREAKFAST,
            emoji = "🍳",
            entries = entries.filter { it.mealType == MealType.BREAKFAST.name },
            onAddClick = { onAddProductClick(MealType.BREAKFAST) },
            onDeleteEntry = onDeleteEntry
        )

        Spacer(modifier = Modifier.height(14.dp))

        MealSection(
            mealType = MealType.LUNCH,
            emoji = "🥗",
            entries = entries.filter { it.mealType == MealType.LUNCH.name },
            onAddClick = { onAddProductClick(MealType.LUNCH) },
            onDeleteEntry = onDeleteEntry
        )

        Spacer(modifier = Modifier.height(14.dp))

        MealSection(
            mealType = MealType.DINNER,
            emoji = "🥩",
            entries = entries.filter { it.mealType == MealType.DINNER.name },
            onAddClick = { onAddProductClick(MealType.DINNER) },
            onDeleteEntry = onDeleteEntry
        )

        Spacer(modifier = Modifier.height(14.dp))

        MealSection(
            mealType = MealType.SNACK,
            emoji = "🍎",
            entries = entries.filter { it.mealType == MealType.SNACK.name },
            onAddClick = { onAddProductClick(MealType.SNACK) },
            onDeleteEntry = onDeleteEntry
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MealSection(
    mealType: MealType,
    emoji: String,
    entries: List<MealEntry>,
    onAddClick: () -> Unit,
    onDeleteEntry: (MealEntry) -> Unit
) {
    val mealKcal = entries.sumOf { it.calories }.roundToInt()
    val mealProtein = entries.sumOf { it.protein }
    val mealFat = entries.sumOf { it.fat }
    val mealCarbs = entries.sumOf { it.carbs }

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
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = mealType.titleRu,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (entries.isNotEmpty()) {
                        Text(
                            text = String.format(Locale.US, "Б: %.1fг • Ж: %.1fг • У: %.1fг", mealProtein, mealFat, mealCarbs),
                            fontSize = 11.sp,
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
                if (mealKcal > 0) {
                    Text(
                        text = "$mealKcal ккал",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                        .clickable { onAddClick() }
                        .testTag("add_to_${mealType.name.lowercase()}"),
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
                    text = "+ Добавить продукт в ${mealType.titleRu.lowercase()}",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkCardElevated)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%.0f г • Б: %.1f | Ж: %.1f | У: %.1f",
                                    entry.weightGrams,
                                    entry.protein,
                                    entry.fat,
                                    entry.carbs
                                ),
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${entry.calories.roundToInt()} ккал",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            IconButton(
                                onClick = { onDeleteEntry(entry) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Удалить запись",
                                    tint = TextTertiary,
                                    modifier = Modifier.size(18.dp)
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
            text = String.format(Locale.US, "%.0fг", value),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}
