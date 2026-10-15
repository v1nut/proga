package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.MealType
import com.example.data.model.Product
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import java.util.Locale
import kotlin.math.roundToInt

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

enum class PortionInputMode(val title: String) {
    GRAMS("Граммы (г)"),
    PORTIONS("Порции / шт")
}

data class SmartPortionInfo(
    val unitName: String, // e.g. "шт", "порция", "кусок", "пачка", "стакан"
    val gramsPerUnit: Double
)

fun detectSmartPortion(product: Product): SmartPortionInfo {
    val nameLower = product.name.lowercase()
    val catLower = product.category.lowercase()

    return when {
        nameLower.contains("яйц") || nameLower.contains("яйцо") ->
            SmartPortionInfo(unitName = "шт", gramsPerUnit = 55.0)

        nameLower.contains("банан") ->
            SmartPortionInfo(unitName = "шт", gramsPerUnit = 120.0)

        nameLower.contains("яблок") ->
            SmartPortionInfo(unitName = "шт", gramsPerUnit = 150.0)

        nameLower.contains("огур") ->
            SmartPortionInfo(unitName = "шт", gramsPerUnit = 100.0)

        nameLower.contains("помидор") || nameLower.contains("томат") ->
            SmartPortionInfo(unitName = "шт", gramsPerUnit = 120.0)

        nameLower.contains("хлебцы") || nameLower.contains("хлебец") ->
            SmartPortionInfo(unitName = "шт", gramsPerUnit = 10.0)

        nameLower.contains("хлеб") || nameLower.contains("батон") ->
            SmartPortionInfo(unitName = "кусок", gramsPerUnit = 35.0)

        nameLower.contains("творог") ->
            SmartPortionInfo(unitName = "пачка", gramsPerUnit = 180.0)

        nameLower.contains("йогурт") ->
            SmartPortionInfo(unitName = "стаканчик", gramsPerUnit = 150.0)

        nameLower.contains("молоко") || nameLower.contains("кефир") || nameLower.contains("ряженка") ->
            SmartPortionInfo(unitName = "стакан", gramsPerUnit = 200.0)

        nameLower.contains("масло") ->
            SmartPortionInfo(unitName = "ст. ложка", gramsPerUnit = 15.0)

        catLower.contains("орехи") ->
            SmartPortionInfo(unitName = "горсть", gramsPerUnit = 30.0)

        catLower.contains("мясо") || catLower.contains("рыба") || nameLower.contains("филе") || nameLower.contains("стейк") ->
            SmartPortionInfo(unitName = "порция", gramsPerUnit = 180.0)

        catLower.contains("крупы") || catLower.contains("гарнир") || nameLower.contains("каша") ->
            SmartPortionInfo(unitName = "порция", gramsPerUnit = 180.0)

        else ->
            SmartPortionInfo(unitName = "порция", gramsPerUnit = 150.0)
    }
}

@Composable
fun PortionDialog(
    product: Product,
    initialMealType: MealType = MealType.BREAKFAST,
    onDismiss: () -> Unit,
    onConfirm: (mealType: MealType, grams: Double) -> Unit
) {
    var selectedMeal by remember { mutableStateOf(initialMealType) }
    var inputMode by remember { mutableStateOf(PortionInputMode.GRAMS) }

    // Grams mode state
    var gramsText by remember { mutableStateOf("100") }

    // Portions mode state
    val smartPortion = remember(product) { detectSmartPortion(product) }
    var portionCount by remember { mutableDoubleStateOf(1.0) }

    // Final effective grams
    val effectiveGrams = if (inputMode == PortionInputMode.GRAMS) {
        gramsText.toDoubleOrNull() ?: 0.0
    } else {
        (portionCount * smartPortion.gramsPerUnit).coerceAtLeast(0.0)
    }

    val multiplier = effectiveGrams / 100.0
    val calcCalories = product.calories * multiplier
    val calcProtein = product.protein * multiplier
    val calcFat = product.fat * multiplier
    val calcCarbs = product.carbs * multiplier

    val quickAmounts = listOf(50.0, 100.0, 150.0, 200.0, 250.0, 300.0)
    val portionPresets = listOf(0.5, 1.0, 1.5, 2.0, 3.0, 4.0)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = product.category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_portion_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary
                        )
                    }
                }

                // Top Tab Switcher: "Выбор порции" ⇄ "🔬 Польза и наука (ИИ)"
                var dialogTab by remember { mutableStateOf(0) } // 0 = Portion, 1 = Science
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (dialogTab == 0) DarkCardElevated else Color.Transparent)
                            .clickable { dialogTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Выбор порции",
                            fontSize = 12.sp,
                            fontWeight = if (dialogTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (dialogTab == 0) WildWatermelon else TextSecondary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (dialogTab == 1) DarkCardElevated else Color.Transparent)
                            .clickable { dialogTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔬 Польза и наука",
                            fontSize = 12.sp,
                            fontWeight = if (dialogTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (dialogTab == 1) WildWatermelon else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (dialogTab == 1) {
                    NutritionScienceView(product = product)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Meal Selector
                Text(
                    text = "Приём пищи:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MealType.entries.forEach { meal ->
                        val isSelected = selectedMeal == meal
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WildWatermelon else DarkCard)
                                .clickable { selectedMeal = meal }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = meal.titleRu,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF101014) else TextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Switcher: Grams vs Portions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCard)
                        .padding(3.dp)
                ) {
                    PortionInputMode.entries.forEach { mode ->
                        val isSelected = inputMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DarkCardElevated else Color.Transparent)
                                .clickable {
                                    inputMode = mode
                                    if (mode == PortionInputMode.PORTIONS) {
                                        // Update portion count from current grams if sensible
                                        val g = gramsText.toDoubleOrNull() ?: 100.0
                                        portionCount = (g / smartPortion.gramsPerUnit * 2).roundToInt() / 2.0
                                        if (portionCount <= 0.0) portionCount = 1.0
                                    } else {
                                        gramsText = effectiveGrams.roundToInt().toString()
                                    }
                                }
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) WildWatermelon else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (inputMode == PortionInputMode.GRAMS) {
                    // Grams mode: input field + quick presets
                    Text(
                        text = "Масса порции (в граммах):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gramsText,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                gramsText = input
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("portion_grams_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            Text(
                                text = "г",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Gram Presets
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickAmounts) { amt ->
                            val label = "${amt.toInt()} г"
                            val isCurrent = (gramsText.toDoubleOrNull() ?: 0.0) == amt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) DarkCardElevated else DarkCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) WildWatermelon else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { gramsText = amt.toInt().toString() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isCurrent) WildWatermelon else TextSecondary
                                )
                            }
                        }
                    }
                } else {
                    // Portions / Pieces Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Количество ${smartPortion.unitName}:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkCardElevated)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "1 ${smartPortion.unitName} ≈ ${smartPortion.gramsPerUnit.roundToInt()} г",
                                fontSize = 11.sp,
                                color = WildWatermelon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Stepper control for portions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCardElevated)
                                .clickable {
                                    if (portionCount > 0.5) portionCount -= 0.5
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Меньше", tint = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val countStr = if (portionCount % 1.0 == 0.0) portionCount.toInt().toString() else String.format(Locale.US, "%.1f", portionCount)
                            Text(
                                text = "$countStr ${smartPortion.unitName}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "= ${effectiveGrams.roundToInt()} грамм",
                                fontSize = 12.sp,
                                color = WildWatermelon,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkCardElevated)
                                .clickable { portionCount += 0.5 },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Больше", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick portion chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(portionPresets) { preset ->
                            val isCurrent = portionCount == preset
                            val label = if (preset % 1.0 == 0.0) "${preset.toInt()} ${smartPortion.unitName}" else "$preset ${smartPortion.unitName}"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) DarkCardElevated else DarkCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) WildWatermelon else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { portionCount = preset }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isCurrent) WildWatermelon else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live KBZHU recalculation box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCard)
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Итого в порции (${effectiveGrams.roundToInt()} г):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "%.0f ккал", calcCalories),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WildWatermelon
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NutrientBadge(label = "Белки", value = calcProtein, color = ProteinColor)
                        NutrientBadge(label = "Жиры", value = calcFat, color = FatColor)
                        NutrientBadge(label = "Углеводы", value = calcCarbs, color = CarbsColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm button
                Button(
                    onClick = {
                        if (effectiveGrams > 0) {
                            onConfirm(selectedMeal, effectiveGrams)
                        }
                    },
                    enabled = effectiveGrams > 0,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelon,
                        contentColor = Color(0xFF101014),
                        disabledContainerColor = DarkBorder,
                        disabledContentColor = TextTertiary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_portion_button")
                ) {
                    Text(
                        text = "Добавить в ${selectedMeal.titleRu.lowercase()} (${effectiveGrams.roundToInt()} г)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NutrientBadge(
    label: String,
    value: Double,
    color: Color
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
            text = String.format(Locale.US, "%s: %.1fг", label, value),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}
