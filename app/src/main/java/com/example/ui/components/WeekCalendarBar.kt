package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

@Composable
fun WeekCalendarBar(
    selectedDate: LocalDate,
    dailyCaloriesMap: Map<String, Double>, // dateString -> consumed calories
    targetCalories: Int,
    onSelectDate: (LocalDate) -> Unit,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var isMonthExpanded by remember { mutableStateOf(false) }

    val monthFormatter = remember(appLanguage) {
        DateTimeFormatter.ofPattern("LLLL yyyy", if (appLanguage == "en") Locale.ENGLISH else Locale("ru"))
    }
    val monthTitle = remember(selectedDate, appLanguage) {
        selectedDate.format(monthFormatter).replaceFirstChar { it.uppercase() }
    }

    // Compute the 7 days of the week (Monday to Sunday) containing selectedDate
    val weekDays = remember(selectedDate) {
        val monday = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        (0..6).map { monday.plusDays(it.toLong()) }
    }

    // Compute full month grid days (1..30/31)
    val firstDayOfMonth = remember(selectedDate) { selectedDate.withDayOfMonth(1) }
    val lengthOfMonth = remember(selectedDate) { selectedDate.lengthOfMonth() }
    val startDayOfWeek = remember(firstDayOfMonth) { firstDayOfMonth.dayOfWeek.value } // 1 (Mon) .. 7 (Sun)

    val monthGridDays = remember(firstDayOfMonth, lengthOfMonth, startDayOfWeek) {
        val list = mutableListOf<LocalDate?>()
        // Leading blank slots
        for (i in 1 until startDayOfWeek) {
            list.add(null)
        }
        // Month days
        for (day in 1..lengthOfMonth) {
            list.add(firstDayOfMonth.withDayOfMonth(day))
        }
        list
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        // Month Header & Navigation Arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isMonthExpanded) {
                        onSelectDate(selectedDate.minusMonths(1).withDayOfMonth(1))
                    } else {
                        onSelectDate(selectedDate.minusWeeks(1))
                    }
                },
                modifier = Modifier.size(40.dp).testTag("week_prev_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkCardElevated.copy(alpha = 0.6f))
                        .clickable { isMonthExpanded = !isMonthExpanded }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = monthTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isMonthExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Переключить вид месяца",
                        tint = WildWatermelon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (selectedDate != today) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCardElevated)
                            .clickable {
                                onSelectDate(today)
                                isMonthExpanded = false
                            }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
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
                onClick = {
                    if (isMonthExpanded) {
                        onSelectDate(selectedDate.plusMonths(1).withDayOfMonth(1))
                    } else {
                        onSelectDate(selectedDate.plusWeeks(1))
                    }
                },
                modifier = Modifier.size(40.dp).testTag("week_next_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Вперёд",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!isMonthExpanded) {
            // 7-Day Week Row
            Column(modifier = Modifier.fillMaxWidth()) {
                // Day of Week Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dayNames = if (appLanguage == "en") listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    dayNames.forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDays.forEachIndexed { index, date ->
                        val isSelected = date == selectedDate
                        val isCurrentToday = date == today
                        val dateString = date.toString()
                        val dayCalories = dailyCaloriesMap[dateString] ?: 0.0
                        val progressRatio = (dayCalories / targetCalories.coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)

                        DayButton(
                            dayOfWeekName = "",
                            dayOfMonth = date.dayOfMonth,
                            isSelected = isSelected,
                            isToday = isCurrentToday,
                            progress = progressRatio,
                            hasEntries = dayCalories > 0,
                            onClick = { onSelectDate(date) },
                            modifier = Modifier.weight(1f)
                        )

                        if (index < 6) {
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                    }
                }
            }
        } else {
            // Expanded 30-Day Month Drawer Grid
            Column(modifier = Modifier.fillMaxWidth()) {
                // Day of Week Header Row
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val dayNames = if (appLanguage == "en") listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun") else listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                    dayNames.forEach { dayName ->
                        Text(
                            text = dayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Chunk month grid into rows of 7 days
                val rows = monthGridDays.chunked(7)
                rows.forEach { rowDays ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 7) {
                            val date = rowDays.getOrNull(i)
                            if (date != null) {
                                val isSelected = date == selectedDate
                                val isCurrentToday = date == today
                                val dateString = date.toString()
                                val dayCalories = dailyCaloriesMap[dateString] ?: 0.0
                                val progressRatio = (dayCalories / targetCalories.coerceAtLeast(1)).toFloat().coerceIn(0f, 1f)

                                DayButton(
                                    dayOfWeekName = "",
                                    dayOfMonth = date.dayOfMonth,
                                    isSelected = isSelected,
                                    isToday = isCurrentToday,
                                    progress = progressRatio,
                                    hasEntries = dayCalories > 0,
                                    onClick = {
                                        onSelectDate(date)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }

                            if (i < 6) {
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayButton(
    dayOfWeekName: String,
    dayOfMonth: Int,
    isSelected: Boolean,
    isToday: Boolean,
    progress: Float,
    hasEntries: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) WildWatermelon else DarkCardElevated
    val textColor = if (isSelected) Color(0xFF101014) else TextPrimary
    val subTextColor = if (isSelected) Color(0xFF350B13) else TextSecondary
    val ringTrackColor = if (isSelected) Color(0x33101014) else Color(0xFF262634)
    val ringProgressColor = if (isSelected) Color(0xFF101014) else WildWatermelon

    val btnHeight = if (dayOfWeekName.isNotEmpty()) 64.dp else 46.dp

    Box(
        modifier = modifier
            .height(btnHeight)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.5.dp, WildWatermelon, RoundedCornerShape(14.dp))
                } else if (!isSelected) {
                    Modifier.border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (dayOfWeekName.isNotEmpty()) {
                Text(
                    text = dayOfWeekName,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = subTextColor
                )
            }

            Text(
                text = "$dayOfMonth",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            // Mini circular progress ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(13.dp)
            ) {
                Canvas(modifier = Modifier.size(12.dp)) {
                    val strokeW = 2.dp.toPx()
                    // Track
                    drawArc(
                        color = ringTrackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeW)
                    )
                    // Progress
                    if (progress > 0f) {
                        drawArc(
                            color = ringProgressColor,
                            startAngle = -90f,
                            sweepAngle = progress * 360f,
                            useCenter = false,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
