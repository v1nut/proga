package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FitnessGoal
import com.example.data.model.WeightEntry
import com.example.ui.theme.DangerColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.OutlineStrong
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.SuccessColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningColor
import com.example.ui.theme.WildWatermelon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WeightTrendChartCard(
    weightEntries: List<WeightEntry>,
    targetWeightKg: Double = 68.0,
    goal: FitnessGoal = FitnessGoal.WEIGHT_LOSS,
    onLogWeight: (weightKg: Double, dateString: String) -> Unit,
    onDeleteWeightEntry: (dateString: String) -> Unit = {},
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val ruLocale = remember { Locale("ru") }
    val dateLocale = remember(appLanguage) { if (appLanguage == "en") Locale.ENGLISH else ruLocale }
    val unitKg = if (appLanguage == "en") "kg" else "кг"
    var isLogDialogOpen by rememberSaveable { mutableStateOf(false) }
    var entryToDeleteDate by rememberSaveable { mutableStateOf<String?>(null) }

    val today = LocalDate.now()
    val startDate30 = today.minusDays(29)

    // Filter strict 30-day window relative to today
    val periodEntries = remember(weightEntries) {
        weightEntries.mapNotNull { entry ->
            try {
                val date = LocalDate.parse(entry.dateString)
                if (!date.isBefore(startDate30) && !date.isAfter(today)) {
                    entry to date
                } else null
            } catch (e: Exception) { null }
        }.sortedBy { it.second.toString() }
    }

    val entryToDelete = remember(entryToDeleteDate, periodEntries) {
        if (entryToDeleteDate != null) periodEntries.find { it.first.dateString == entryToDeleteDate }?.first else null
    }

    val firstWeight = periodEntries.firstOrNull()?.first?.weightKg ?: targetWeightKg
    val currentWeight = periodEntries.lastOrNull()?.first?.weightKg ?: targetWeightKg
    val weightDiff = currentWeight - firstWeight

    val changeColor = when (goal) {
        FitnessGoal.WEIGHT_LOSS -> if (weightDiff <= 0) SuccessColor else DangerColor
        FitnessGoal.WEIGHT_GAIN -> if (weightDiff >= 0) SuccessColor else DangerColor
        FitnessGoal.MAINTENANCE -> if (kotlin.math.abs(weightDiff) <= 1.0) SuccessColor else WarningColor
    }

    var selectedIndex by remember(periodEntries) {
        mutableStateOf(if (periodEntries.isNotEmpty()) periodEntries.lastIndex else -1)
    }

    val selectedPair = if (selectedIndex in periodEntries.indices) periodEntries[selectedIndex] else periodEntries.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
            .testTag("weight_trend_chart_card")
    ) {
        // Header Row
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
                        text = if (appLanguage == "en") "Weight Trend (30 Days)" else "Динамика веса (30 дней)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = if (appLanguage == "en") "Weight records & 7-day average" else "График взвешиваний и 7-дневное среднее",
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
                    contentColor = Color(0xFF101014)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("log_weight_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (appLanguage == "en") "Log Weight" else "Записать", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Metrics Bar
        val unitKg = if (appLanguage == "en") "kg" else "кг"
        val dateLocale = if (appLanguage == "en") Locale.ENGLISH else ruLocale

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCardElevated)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = if (appLanguage == "en") "30d ago" else "30д назад", fontSize = 10.sp, color = TextSecondary)
                Text(text = String.format(dateLocale, "%.1f %s", firstWeight, unitKg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Box(modifier = Modifier.width(1.dp).height(26.dp).background(DarkBorder))

            Column {
                Text(text = if (appLanguage == "en") "Current" else "Текущий", fontSize = 10.sp, color = TextSecondary)
                Text(text = String.format(dateLocale, "%.1f %s", currentWeight, unitKg), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
            }

            Box(modifier = Modifier.width(1.dp).height(26.dp).background(DarkBorder))

            Column {
                Text(text = if (appLanguage == "en") "In 30 days" else "За 30 дней", fontSize = 10.sp, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (weightDiff <= 0) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = changeColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format(dateLocale, "%+.1f %s", weightDiff, unitKg),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = changeColor
                    )
                }
            }

            Box(modifier = Modifier.width(1.dp).height(26.dp).background(DarkBorder))

            Column {
                Text(text = if (appLanguage == "en") "Goal" else "Цель", fontSize = 10.sp, color = TextSecondary)
                Text(text = String.format(dateLocale, "%.1f %s", targetWeightKg, unitKg), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessColor)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Point Tooltip Box
        if (selectedPair != null) {
            val (entry, entryDate) = selectedPair
            val dateFormatted = DateTimeFormatter.ofPattern("d MMMM yyyy", dateLocale).format(entryDate)
            val pointDiff = entry.weightKg - firstWeight

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
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(WildWatermelon))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = dateFormatted, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(dateLocale, "%.1f %s", entry.weightKg, unitKg),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = String.format(dateLocale, "(%+.1f %s)", pointDiff, unitKg),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = changeColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { entryToDeleteDate = entry.dateString },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить взвешивание", tint = DangerColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        val weightDesc = String.format(ruLocale, "График динамики веса за последние 30 дней. Начальный %.1f кг, текущий %.1f кг, изменение %+.1f кг, целевой %.1f кг.", firstWeight, currentWeight, weightDiff, targetWeightKg)

        // Proportional Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .semantics { contentDescription = weightDesc }
                    .pointerInput(periodEntries) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (periodEntries.isNotEmpty()) {
                                    val w = size.width
                                    val closestIndex = periodEntries.indices.minByOrNull { idx ->
                                        val entryDate = periodEntries[idx].second
                                        val daysFromStart = ChronoUnit.DAYS.between(startDate30, entryDate).toFloat().coerceIn(0f, 29f)
                                        val x = (daysFromStart / 29f) * w
                                        kotlin.math.abs(x - offset.x)
                                    } ?: -1
                                    selectedIndex = closestIndex
                                }
                            },
                            onLongPress = { offset ->
                                if (periodEntries.isNotEmpty()) {
                                    val w = size.width
                                    val closestIndex = periodEntries.indices.minByOrNull { idx ->
                                        val entryDate = periodEntries[idx].second
                                        val daysFromStart = ChronoUnit.DAYS.between(startDate30, entryDate).toFloat().coerceIn(0f, 29f)
                                        val x = (daysFromStart / 29f) * w
                                        kotlin.math.abs(x - offset.x)
                                    } ?: -1
                                    if (closestIndex in periodEntries.indices) {
                                        entryToDelete = periodEntries[closestIndex].first
                                    }
                                }
                            }
                        )
                    }
            ) {
                if (periodEntries.isEmpty()) return@Canvas

                val w = size.width
                val h = size.height

                val allValues = periodEntries.map { it.first.weightKg } + targetWeightKg
                val minVal = (allValues.minOrNull() ?: 60.0) - 1.0
                val maxVal = (allValues.maxOrNull() ?: 80.0) + 1.0
                val range = (maxVal - minVal).coerceAtLeast(1.0)

                fun getY(valKg: Double): Float {
                    val ratio = (valKg - minVal) / range
                    return (h - (ratio * h * 0.82f) - (h * 0.08f)).toFloat()
                }

                fun getX(date: LocalDate): Float {
                    val daysFromStart = ChronoUnit.DAYS.between(startDate30, date).toFloat().coerceIn(0f, 29f)
                    return (daysFromStart / 29f) * w
                }

                // Dotted Target Line
                val targetY = getY(targetWeightKg)
                drawLine(
                    color = SuccessColor.copy(alpha = 0.5f),
                    start = Offset(0f, targetY),
                    end = Offset(w, targetY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // 7-day moving average calculation
                val points = periodEntries.map { (entry, date) ->
                    val x = getX(date)
                    val y = getY(entry.weightKg)
                    Offset(x, y)
                }

                val movingAvgPoints = periodEntries.map { (_, date) ->
                    val windowStart = date.minusDays(6)
                    val window = periodEntries.filter { (_, d) -> !d.isBefore(windowStart) && !d.isAfter(date) }
                    val avgWeight = if (window.isNotEmpty()) window.sumOf { it.first.weightKg } / window.size else 70.0
                    Offset(getX(date), getY(avgWeight))
                }

                // Draw 7-day Moving Average Line (dashed ProteinColor)
                if (movingAvgPoints.size >= 2) {
                    val avgPath = Path().apply {
                        moveTo(movingAvgPoints.first().x, movingAvgPoints.first().y)
                        for (i in 1 until movingAvgPoints.size) {
                            val p1 = movingAvgPoints[i - 1]
                            val p2 = movingAvgPoints[i]
                            val cx = p1.x + (p2.x - p1.x) / 2f
                            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                    }
                    drawPath(
                        path = avgPath,
                        color = ProteinColor.copy(alpha = 0.8f),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                    )
                }

                // Draw Main Weight Curve
                if (points.isNotEmpty()) {
                    val strokePath = Path()
                    val fillPath = Path()

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

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(WildWatermelon.copy(alpha = 0.28f), WildWatermelon.copy(alpha = 0.02f))
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = WildWatermelon,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Data Points
                    points.forEachIndexed { i, pt ->
                        val isSelected = i == selectedIndex
                        drawCircle(
                            color = if (isSelected) Color.White else WildWatermelon,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = pt
                        )
                        if (isSelected) {
                            drawCircle(color = WildWatermelon, radius = 3.dp.toPx(), center = pt)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // X-Axis Date Range Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dateStartLabel = DateTimeFormatter.ofPattern("d MMM", ruLocale).format(startDate30)
            val dateMidLabel = DateTimeFormatter.ofPattern("d MMM", ruLocale).format(startDate30.plusDays(15))
            val dateEndLabel = DateTimeFormatter.ofPattern("d MMM", ruLocale).format(today)

            Text(text = dateStartLabel, fontSize = 10.sp, color = TextTertiary)
            Text(text = dateMidLabel, fontSize = 10.sp, color = TextTertiary)
            Text(text = dateEndLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
        }
    }

    // Modal Confirmation Dialog to Delete Weight Entry
    if (entryToDelete != null) {
        val entry = entryToDelete!!
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            title = { Text(if (appLanguage == "en") "Delete Weight Entry" else "Удалить запись веса?", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary) },
            text = {
                val formattedW = String.format(dateLocale, "%.1f %s", entry.weightKg, unitKg)
                val textMsg = if (appLanguage == "en")
                    "Are you sure you want to delete weight record $formattedW for ${entry.dateString}?"
                else
                    "Вы уверены, что хотите удалить запись веса $formattedW за ${entry.dateString}?"
                Text(text = textMsg, fontSize = 13.sp, color = TextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWeightEntry(entry.dateString)
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DangerColor, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (appLanguage == "en") "Delete" else "Удалить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(if (appLanguage == "en") "Cancel" else "Отмена", color = TextTertiary)
                }
            }
        )
    }

    // Modal Dialog to Log Weight for Any Past Date
    if (isLogDialogOpen) {
        var inputWeight by remember { mutableStateOf(String.format(ruLocale, "%.1f", currentWeight)) }
        var inputDateText by remember { mutableStateOf(LocalDate.now().toString()) }

        val parsedDate = try {
            val d = LocalDate.parse(inputDateText.trim())
            if (!d.isAfter(LocalDate.now())) d else null
        } catch (e: Exception) { null }

        val parsedWeight = inputWeight.replace(',', '.').toDoubleOrNull()
        val isWeightValid = parsedWeight != null && parsedWeight in 25.0..300.0
        val isDateError = inputDateText.isNotBlank() && parsedDate == null

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
                        Text(if (appLanguage == "en") "Log Weight" else "Запись веса", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        IconButton(onClick = { isLogDialogOpen = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Input Field (YYYY-MM-DD)
                    OutlinedTextField(
                        value = inputDateText,
                        onValueChange = { inputDateText = it },
                        label = { Text(if (appLanguage == "en") "Date (YYYY-MM-DD)" else "Дата (ГГГГ-ММ-ДД)") },
                        singleLine = true,
                        isError = isDateError,
                        supportingText = {
                            if (isDateError) {
                                Text(
                                    text = if (appLanguage == "en") "Format YYYY-MM-DD (not in future)" else "Формат ГГГГ-ММ-ДД (не в будущем)",
                                    color = DangerColor,
                                    fontSize = 11.sp
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = OutlineStrong,
                            errorBorderColor = DangerColor,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Weight Input Field
                    OutlinedTextField(
                        value = inputWeight,
                        onValueChange = { inputWeight = it },
                        label = { Text(if (appLanguage == "en") "Weight (kg)" else "Вес (кг)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = OutlineStrong,
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
                                        val cur = inputWeight.replace(',', '.').toDoubleOrNull() ?: currentWeight
                                        inputWeight = String.format(ruLocale, "%.1f", (cur + step).coerceIn(25.0, 300.0))
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(ruLocale, "%+.1f", step),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WildWatermelon
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    val view = androidx.compose.ui.platform.LocalView.current

                    Button(
                        onClick = {
                            if (parsedDate != null && isWeightValid && parsedWeight != null) {
                                com.example.ui.util.HapticUtil.performConfirm(view)
                                onLogWeight(parsedWeight, parsedDate.toString())
                                isLogDialogOpen = false
                            }
                        },
                        enabled = parsedDate != null && isWeightValid,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014),
                            disabledContainerColor = DarkBorder,
                            disabledContentColor = TextTertiary
                        ),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (appLanguage == "en") "Save Weight" else "Сохранить вес", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
