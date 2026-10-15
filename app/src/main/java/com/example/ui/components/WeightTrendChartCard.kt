package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WeightEntry
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeightTrendChartCard(
    weightEntries: List<WeightEntry>,
    targetWeightKg: Double = 68.0,
    onLogWeight: (weightKg: Double, dateString: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isLogDialogOpen by remember { mutableStateOf(false) }

    // Sort entries chronologically
    val sortedEntries = remember(weightEntries) {
        weightEntries.sortedBy { it.dateString }
    }

    val firstWeight = sortedEntries.firstOrNull()?.weightKg ?: 74.5
    val currentWeight = sortedEntries.lastOrNull()?.weightKg ?: 72.0
    val weightDiff = currentWeight - firstWeight
    val isLoss = weightDiff <= 0

    // Touch interactive point state
    var selectedIndex by remember(sortedEntries) {
        mutableStateOf(if (sortedEntries.isNotEmpty()) sortedEntries.lastIndex else -1)
    }

    val selectedEntry = if (selectedIndex in sortedEntries.indices) sortedEntries[selectedIndex] else sortedEntries.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("weight_trend_chart_card")
    ) {
        // Header Row: Title & + Log Weight Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(WildWatermelon.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = WildWatermelon,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Динамика веса (30 дней)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "График изменения тела и прогресс",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = { isLogDialogOpen = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WildWatermelon,
                    contentColor = Color.White
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("log_weight_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Записать",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Metrics Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCardElevated)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "30д назад", fontSize = 10.sp, color = TextSecondary)
                Text(text = "%.1f кг".format(Locale.US, firstWeight), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(DarkBorder)
            )

            Column {
                Text(text = "Текущий", fontSize = 10.sp, color = TextSecondary)
                Text(text = "%.1f кг".format(Locale.US, currentWeight), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(DarkBorder)
            )

            Column {
                Text(text = "Изменение", fontSize = 10.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLoss) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isLoss) ElectricLime else Color(0xFFFB7185),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "%+.1f кг".format(Locale.US, weightDiff),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLoss) ElectricLime else Color(0xFFFB7185)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(DarkBorder)
            )

            Column {
                Text(text = "Цель", fontSize = 10.sp, color = TextSecondary)
                Text(text = "%.1f кг".format(Locale.US, targetWeightKg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ElectricLime)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active Recharts Tooltip Box
        if (selectedEntry != null) {
            val dateFormatted = try {
                val d = LocalDate.parse(selectedEntry.dateString)
                d.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ru")))
            } catch (e: Exception) {
                selectedEntry.dateString
            }
            val pointDiff = selectedEntry.weightKg - firstWeight

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, WildWatermelon.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
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
                                .background(WildWatermelon)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateFormatted, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%.1f кг".format(Locale.US, selectedEntry.weightKg),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(%+.1f кг)".format(Locale.US, pointDiff),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pointDiff <= 0) ElectricLime else Color(0xFFFB7185)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Recharts Interactive Bezier Line Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(sortedEntries) {
                        detectTapGestures { offset ->
                            if (sortedEntries.isNotEmpty()) {
                                val width = size.width
                                val step = width / (sortedEntries.size - 1).coerceAtLeast(1)
                                val index = (offset.x / step).roundToInt().coerceIn(0, sortedEntries.lastIndex)
                                selectedIndex = index
                            }
                        }
                    }
            ) {
                if (sortedEntries.isEmpty()) return@Canvas

                val w = size.width
                val h = size.height

                // Y scale calculation with margins
                val minVal = (sortedEntries.minOf { it.weightKg }.coerceAtMost(targetWeightKg) - 1.0)
                val maxVal = (sortedEntries.maxOf { it.weightKg }.coerceAtLeast(targetWeightKg) + 1.0)
                val range = (maxVal - minVal).coerceAtLeast(1.0)

                fun getY(valKg: Double): Float {
                    val ratio = (valKg - minVal) / range
                    return (h - (ratio * h * 0.82f) - (h * 0.08f)).toFloat()
                }

                fun getX(index: Int): Float {
                    return if (sortedEntries.size <= 1) w / 2f
                    else index * (w / (sortedEntries.size - 1))
                }

                // Dotted Horizontal Line for Target Weight
                val targetY = getY(targetWeightKg)
                drawLine(
                    color = ElectricLime.copy(alpha = 0.5f),
                    start = Offset(0f, targetY),
                    end = Offset(w, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Build Smoothed Bezier Path
                val strokePath = Path()
                val fillPath = Path()

                val points = sortedEntries.mapIndexed { idx, entry ->
                    Offset(getX(idx), getY(entry.weightKg))
                }

                if (points.isNotEmpty()) {
                    strokePath.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, h)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val control1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                        val control2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                        strokePath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                        fillPath.cubicTo(control1.x, control1.y, control2.x, control2.y, p2.x, p2.y)
                    }

                    fillPath.lineTo(points.last().x, h)
                    fillPath.close()

                    // Fill Gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                WildWatermelon.copy(alpha = 0.30f),
                                WildWatermelon.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Smooth Chart Line
                    drawPath(
                        path = strokePath,
                        color = WildWatermelon,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Active Node Selection
                    if (selectedIndex in points.indices) {
                        val activePoint = points[selectedIndex]

                        // Vertical reference line
                        drawLine(
                            color = TextSecondary.copy(alpha = 0.4f),
                            start = Offset(activePoint.x, 0f),
                            end = Offset(activePoint.x, h),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )

                        // Outer halo
                        drawCircle(
                            color = WildWatermelon.copy(alpha = 0.25f),
                            radius = 12.dp.toPx(),
                            center = activePoint
                        )
                        // Inner circle
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = activePoint
                        )
                        drawCircle(
                            color = WildWatermelon,
                            radius = 3.dp.toPx(),
                            center = activePoint
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Date Labels Row
        if (sortedEntries.size >= 5) {
            val labelIndices = listOf(
                0,
                sortedEntries.size / 4,
                sortedEntries.size / 2,
                (sortedEntries.size * 3) / 4,
                sortedEntries.lastIndex
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labelIndices.forEach { idx ->
                    val entry = sortedEntries[idx]
                    val label = try {
                        val d = LocalDate.parse(entry.dateString)
                        d.format(DateTimeFormatter.ofPattern("dd MMM", Locale("ru")))
                    } catch (e: Exception) {
                        entry.dateString.takeLast(5)
                    }
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (idx == selectedIndex) WildWatermelon else TextTertiary,
                        fontWeight = if (idx == selectedIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }

    // Modal Dialog to Log Weight
    if (isLogDialogOpen) {
        var inputWeight by remember {
            mutableStateOf("%.1f".format(Locale.US, currentWeight))
        }

        Dialog(
            onDismissRequest = { isLogDialogOpen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
                    .testTag("log_weight_dialog"),
                color = DarkSurface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Запись веса",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        IconButton(onClick = { isLogDialogOpen = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Внесите текущие показания взвешивания (сегодня):",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text("Вес (кг)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Step Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(-0.5, -0.1, +0.1, +0.5).forEach { step ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkCardElevated)
                                    .clickable {
                                        val cur = inputWeight.toDoubleOrNull() ?: currentWeight
                                        inputWeight = "%.1f".format(Locale.US, (cur + step).coerceIn(30.0, 300.0))
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "%+.1f".format(Locale.US, step),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WildWatermelon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val doubleVal = inputWeight.replace(',', '.').toDoubleOrNull()
                            if (doubleVal != null && doubleVal in 30.0..300.0) {
                                onLogWeight(doubleVal, LocalDate.now().toString())
                                isLogDialogOpen = false
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Сохранить вес", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
