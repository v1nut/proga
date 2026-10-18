package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

data class DayTrendData(
    val date: LocalDate,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

enum class TrendMode(val title: String) {
    CALORIES("Калории"),
    MACROS("БЖУ")
}

private fun formatDaysWord(count: Int): String {
    val rem10 = count % 10
    val rem100 = count % 100
    return when {
        rem100 in 11..19 -> "дней"
        rem10 == 1 -> "день"
        rem10 in 2..4 -> "дня"
        else -> "дней"
    }
}

@Composable
fun WeeklyTrendsCard(
    weeklyData: List<DayTrendData>,
    targetCalories: Int,
    targetProtein: Int,
    targetFat: Int,
    targetCarbs: Int,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(TrendMode.CALORIES) }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    val today = LocalDate.now()

    // Completed days (excluding today and excluding zero days)
    val completedDaysWithEntries = remember(weeklyData) {
        weeklyData.filter { it.date < today && it.calories > 0 }
    }

    val completedCount = completedDaysWithEntries.size
    val avgCalories = if (completedCount > 0) {
        (completedDaysWithEntries.sumOf { it.calories } / completedCount).roundToInt()
    } else 0

    val ruLocale = remember { Locale("ru") }

    val subtitleText = if (completedCount > 0) {
        if (appLanguage == "en") {
            String.format(ruLocale, "Avg %,d kcal/day over %d logged %s", avgCalories, completedCount, if (completedCount == 1) "day" else "days")
        } else {
            String.format(ruLocale, "В среднем %,d ккал/день за %d %s с записями", avgCalories, completedCount, formatDaysWord(completedCount))
        }
    } else {
        if (appLanguage == "en") "No completed logged days yet" else "Нет завершённых дней с записями"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        // Header & Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (appLanguage == "en") "7-Day Activity" else "Динамика за 7 дней",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitleText,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1
                )
            }

            // Mode Tabs
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCardElevated)
                    .padding(3.dp)
            ) {
                TrendMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val modeTitle = if (appLanguage == "en") {
                        if (mode == TrendMode.CALORIES) "Calories" else "Macros"
                    } else mode.title
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) WildWatermelon else Color.Transparent)
                            .clickable {
                                selectedMode = mode
                                selectedPointIndex = -1
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = modeTitle,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tooltip or Legend
        if (selectedPointIndex in weeklyData.indices) {
            val point = weeklyData[selectedPointIndex]
            val isTodayDate = point.date == today
            val dateLocale = if (appLanguage == "en") Locale.ENGLISH else ruLocale
            val todaySuffix = if (isTodayDate) (if (appLanguage == "en") " • Today (in progress)" else " • Сегодня (в процессе)") else ""
            val dateLabel = DateTimeFormatter.ofPattern("d MMM (EEE)", dateLocale).format(point.date) + todaySuffix

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkCardElevated)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                if (selectedMode == TrendMode.CALORIES) {
                    val calFmt = if (appLanguage == "en") "%,d kcal (target: %,d)" else "%,d ккал (цель: %,d)"
                    Text(
                        text = String.format(dateLocale, calFmt, point.calories.roundToInt(), targetCalories),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon
                    )
                } else {
                    val macroFmt = if (appLanguage == "en") "P: %.1fg • F: %.1fg • C: %.1fg" else "Б: %.1fг • Ж: %.1fг • У: %.1fг"
                    Text(
                        text = String.format(dateLocale, macroFmt, point.protein, point.fat, point.carbs),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        } else {
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (selectedMode == TrendMode.CALORIES) Arrangement.SpaceBetween else Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedMode == TrendMode.CALORIES) {
                    LegendItem(label = if (appLanguage == "en") "Calories" else "Калории (столбцы)", color = WildWatermelon)
                    LegendItem(label = if (appLanguage == "en") "Target (${String.format(ruLocale, "%,d", targetCalories)})" else "Норма (${String.format(ruLocale, "%,d", targetCalories)})", color = TextTertiary, isDashed = true)
                } else {
                    LegendItem(label = if (appLanguage == "en") "Protein" else "Белки", color = ProteinColor)
                    LegendItem(label = if (appLanguage == "en") "Fat" else "Жиры", color = FatColor)
                    LegendItem(label = if (appLanguage == "en") "Carbs" else "Углеводы", color = CarbsColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val trendDesc = String.format(ruLocale, "Столбчатый график калорий за 7 дней. В среднем %,d ккал в день за %d дней с записями. Норма %,d ккал.", avgCalories, completedCount, targetCalories)

        // Bar Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .semantics { contentDescription = trendDesc }
                    .pointerInput(weeklyData, selectedMode) {
                        detectTapGestures { offset ->
                            val count = weeklyData.size
                            if (count > 0) {
                                val barWidthSlot = size.width / count
                                val index = (offset.x / barWidthSlot).toInt().coerceIn(0, count - 1)
                                selectedPointIndex = if (selectedPointIndex == index) -1 else index
                            }
                        }
                    }
            ) {
                if (weeklyData.isEmpty()) return@Canvas

                val count = weeklyData.size
                val w = size.width
                val h = size.height - 22.dp.toPx() // leave space for bottom date labels
                val slotWidth = w / count
                val barWidth = (slotWidth * 0.48f).coerceAtMost(28.dp.toPx())

                val maxVal = maxOf(
                    targetCalories.toDouble(),
                    weeklyData.maxOfOrNull { it.calories } ?: 2000.0
                ) * 1.18

                fun getY(cal: Double): Float {
                    val ratio = (cal / maxVal).toFloat().coerceIn(0f, 1f)
                    return h * (1f - ratio)
                }

                // Target dashed guideline
                val targetY = getY(targetCalories.toDouble())
                drawLine(
                    color = Color(0xFF5A5A6E),
                    start = Offset(0f, targetY),
                    end = Offset(w, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                if (selectedMode == TrendMode.CALORIES) {
                    // Draw Bars for Calories
                    weeklyData.forEachIndexed { i, dayData ->
                        if (dayData.calories > 0) {
                            val isTodayDate = dayData.date == today
                            val isSelected = i == selectedPointIndex
                            val barTopY = getY(dayData.calories)
                            val barHeight = h - barTopY
                            val centerX = (i * slotWidth) + (slotWidth / 2f)
                            val barLeft = centerX - (barWidth / 2f)

                            val barColor = when {
                                isSelected -> Color.White
                                isTodayDate -> WildWatermelon.copy(alpha = 0.55f)
                                dayData.calories > targetCalories -> Color(0xFFF87171)
                                else -> WildWatermelon
                            }

                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(barLeft, barTopY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                } else {
                    // Draw Macro Stacked Bars
                    val maxMacroVal = maxOf(
                        weeklyData.maxOfOrNull { it.protein + it.fat + it.carbs } ?: 100.0,
                        (targetProtein + targetFat + targetCarbs).toDouble()
                    ) * 1.18

                    weeklyData.forEachIndexed { i, dayData ->
                        if (dayData.protein > 0 || dayData.fat > 0 || dayData.carbs > 0) {
                            val centerX = (i * slotWidth) + (slotWidth / 2f)
                            val barLeft = centerX - (barWidth / 2f)

                            var currentY = h

                            listOf(
                                dayData.protein to ProteinColor,
                                dayData.fat to FatColor,
                                dayData.carbs to CarbsColor
                            ).forEach { (grams, color) ->
                                if (grams > 0) {
                                    val segHeight = (h * (grams / maxMacroVal)).toFloat()
                                    val segTopY = currentY - segHeight
                                    drawRoundRect(
                                        color = color,
                                        topLeft = Offset(barLeft, segTopY),
                                        size = Size(barWidth, segHeight),
                                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                    )
                                    currentY = segTopY
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Days Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyData.forEachIndexed { i, d ->
                val dayOfWeekLabel = if (appLanguage == "en") {
                    when (d.date.dayOfWeek.value) {
                        1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; else -> "Sun"
                    }
                } else {
                    when (d.date.dayOfWeek.value) {
                        1 -> "Пн"; 2 -> "Вт"; 3 -> "Ср"; 4 -> "Чт"; 5 -> "Пт"; 6 -> "Сб"; else -> "Вс"
                    }
                }
                val isTodayDate = d.date == today
                val isSelected = i == selectedPointIndex
                Text(
                    text = dayOfWeekLabel + if (isTodayDate) "*" else "",
                    fontSize = 11.sp,
                    fontWeight = if (isSelected || isTodayDate) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) WildWatermelon else if (isTodayDate) TextPrimary else TextTertiary
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    isDashed: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isDashed) {
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(color)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}
