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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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

@Composable
fun WeeklyTrendsCard(
    weeklyData: List<DayTrendData>,
    targetCalories: Int,
    targetProtein: Int,
    targetFat: Int,
    targetCarbs: Int,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(TrendMode.CALORIES) }
    var selectedPointIndex by remember { mutableIntStateOf(-1) }

    val avgCalories = remember(weeklyData) {
        if (weeklyData.isEmpty()) 0 else (weeklyData.sumOf { it.calories } / weeklyData.size).roundToInt()
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
                    text = "Динамика за 7 дней",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "В среднем: $avgCalories ккал/день",
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
                            text = mode.title,
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
            val dateLabel = DateTimeFormatter.ofPattern("d MMM (EEE)", Locale("ru")).format(point.date)
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
                    Text(
                        text = "${point.calories.roundToInt()} ккал (цель: $targetCalories)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon
                    )
                } else {
                    Text(
                        text = String.format(Locale.US, "Б: %.0fг • Ж: %.0fг • У: %.0fг", point.protein, point.fat, point.carbs),
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
                    LegendItem(label = "Калории", color = WildWatermelon)
                    LegendItem(label = "Цель ($targetCalories)", color = TextTertiary, isDashed = true)
                } else {
                    LegendItem(label = "Белки", color = ProteinColor)
                    LegendItem(label = "Жиры", color = FatColor)
                    LegendItem(label = "Углеводы", color = CarbsColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Line Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .pointerInput(weeklyData, selectedMode) {
                        detectTapGestures { offset ->
                            val count = weeklyData.size
                            if (count > 0) {
                                val stepX = size.width / (count - 1).coerceAtLeast(1)
                                val index = ((offset.x + stepX / 2) / stepX).toInt().coerceIn(0, count - 1)
                                selectedPointIndex = if (selectedPointIndex == index) -1 else index
                            }
                        }
                    }
            ) {
                if (weeklyData.isEmpty()) return@Canvas

                val count = weeklyData.size
                val w = size.width
                val h = size.height - 24.dp.toPx() // leave space for bottom date labels
                val stepX = w / (count - 1).coerceAtLeast(1)

                if (selectedMode == TrendMode.CALORIES) {
                    val maxVal = maxOf(targetCalories.toDouble(), weeklyData.maxOfOrNull { it.calories } ?: 2000.0) * 1.15
                    val minVal = 0.0

                    fun getY(cal: Double): Float {
                        val ratio = ((cal - minVal) / (maxVal - minVal)).toFloat().coerceIn(0f, 1f)
                        return h * (1f - ratio)
                    }

                    // Target dashed guideline
                    val targetY = getY(targetCalories.toDouble())
                    drawLine(
                        color = Color(0xFF4A4A58),
                        start = Offset(0f, targetY),
                        end = Offset(w, targetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )

                    // Path for curve and gradient fill
                    val linePath = Path()
                    val fillPath = Path()

                    val points = weeklyData.mapIndexed { i, d ->
                        Offset(i * stepX, getY(d.calories))
                    }

                    if (points.isNotEmpty()) {
                        linePath.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, h)
                        fillPath.lineTo(points[0].x, points[0].y)

                        for (i in 1 until points.size) {
                            val pPrev = points[i - 1]
                            val pCurr = points[i]
                            val cx1 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                            val cy1 = pPrev.y
                            val cx2 = pPrev.x + (pCurr.x - pPrev.x) / 2f
                            val cy2 = pCurr.y
                            linePath.cubicTo(cx1, cy1, cx2, cy2, pCurr.x, pCurr.y)
                            fillPath.cubicTo(cx1, cy1, cx2, cy2, pCurr.x, pCurr.y)
                        }

                        fillPath.lineTo(points.last().x, h)
                        fillPath.close()

                        // Draw area gradient fill
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(WildWatermelon.copy(alpha = 0.28f), WildWatermelon.copy(alpha = 0.02f)),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Draw smooth line
                        drawPath(
                            path = linePath,
                            color = WildWatermelon,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw data point circles
                        points.forEachIndexed { i, pt ->
                            val isSelected = i == selectedPointIndex
                            drawCircle(
                                color = if (isSelected) Color.White else WildWatermelon,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = pt
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = WildWatermelon,
                                    radius = 3.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                } else {
                    // Macro Mode: 3 lines for Protein, Fat, Carbs
                    val maxVal = maxOf(
                        weeklyData.maxOfOrNull { maxOf(it.protein, it.fat, it.carbs) } ?: 100.0,
                        maxOf(targetProtein, targetFat, targetCarbs).toDouble()
                    ) * 1.15

                    fun getY(g: Double): Float {
                        val ratio = (g / maxVal).toFloat().coerceIn(0f, 1f)
                        return h * (1f - ratio)
                    }

                    fun drawMacroLine(color: Color, selector: (DayTrendData) -> Double) {
                        val pts = weeklyData.mapIndexed { i, d -> Offset(i * stepX, getY(selector(d))) }
                        if (pts.isEmpty()) return
                        val path = Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            for (i in 1 until pts.size) {
                                val pPrev = pts[i - 1]
                                val pCurr = pts[i]
                                val cx = pPrev.x + (pCurr.x - pPrev.x) / 2f
                                cubicTo(cx, pPrev.y, cx, pCurr.y, pCurr.x, pCurr.y)
                            }
                        }
                        drawPath(path = path, color = color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                        pts.forEach { drawCircle(color = color, radius = 3.dp.toPx(), center = it) }
                    }

                    drawMacroLine(ProteinColor) { it.protein }
                    drawMacroLine(FatColor) { it.fat }
                    drawMacroLine(CarbsColor) { it.carbs }
                }
            }
        }

        // Bottom Days Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            weeklyData.forEachIndexed { i, d ->
                val dayOfWeekRu = when (d.date.dayOfWeek.value) {
                    1 -> "Пн"; 2 -> "Вт"; 3 -> "Ср"; 4 -> "Чт"; 5 -> "Пт"; 6 -> "Сб"; else -> "Вс"
                }
                val isSelected = i == selectedPointIndex
                Text(
                    text = dayOfWeekRu,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) WildWatermelon else TextTertiary
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
