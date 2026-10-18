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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FitnessGoal
import com.example.data.model.MealType
import com.example.data.model.Product
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
import com.example.ui.util.FoodTierSystem
import com.example.ui.util.HapticUtil
import com.example.ui.util.TierSquareBadge
import java.util.Locale
import kotlin.math.roundToInt

enum class PortionInputMode {
    GRAMS, PORTIONS;

    fun title(lang: String = "ru"): String {
        return if (lang == "en") {
            if (this == GRAMS) "Grams (g)" else "Portions / Pcs"
        } else {
            if (this == GRAMS) "Граммы (г)" else "Порции / Штуки"
        }
    }
}

data class SmartPortionInfo(
    val unitName: String,
    val gramsPerUnit: Double
)

fun detectSmartPortion(product: Product, appLanguage: String = "ru"): SmartPortionInfo {
    val name = product.name.lowercase().trim()
    val cat = product.category.lowercase().trim()

    val pcsUnit = if (appLanguage == "en") "pcs" else "шт"
    val sliceUnit = if (appLanguage == "en") "slice" else "ломтик"
    val pieceUnit = if (appLanguage == "en") "piece" else "кусок"
    val tbspUnit = if (appLanguage == "en") "tbsp" else "ст. ложка"
    val cupUnit = if (appLanguage == "en") "cup" else "стакан"
    val glassUnit = if (appLanguage == "en") "cup" else "стаканчик"
    val portionUnit = if (appLanguage == "en") "portion" else "порция"

    return when {
        name.contains("яйц") || name.contains("яичн") || name.contains("egg") ->
            SmartPortionInfo(pcsUnit, 55.0)
        name.contains("яблоко") || name.contains("банан") || name.contains("апельсин") || name.contains("груша") || name.contains("персик") || name.contains("apple") || name.contains("banana") ->
            SmartPortionInfo(pcsUnit, 120.0)
        name.contains("хлебц") ->
            SmartPortionInfo(pcsUnit, 10.0)
        name.contains("хлеб") || name.contains("тост") || name.contains("батон") || name.contains("bread") ->
            SmartPortionInfo(pieceUnit, 35.0)
        name.contains("паста") || name.contains("масло") || name.contains("oil") || name.contains("butter") ->
            SmartPortionInfo(tbspUnit, 15.0)
        (name.contains("сыр") && !name.contains("творог")) || name.contains("cheese") ->
            SmartPortionInfo(sliceUnit, 20.0)
        name.contains("котлет") || name.contains("сырник") || name.contains("patty") || name.contains("cutlet") ->
            SmartPortionInfo(pcsUnit, 80.0)
        name.contains("йогурт") || name.contains("творожок") || name.contains("yogurt") ->
            SmartPortionInfo(glassUnit, 150.0)
        name.contains("молоко") || name.contains("кефир") || name.contains("сок") || name.contains("milk") || name.contains("juice") ->
            SmartPortionInfo(cupUnit, 200.0)
        cat.contains("напитки") || cat.contains("drinks") || cat.contains("beverages") ->
            SmartPortionInfo(cupUnit, 250.0)
        else ->
            SmartPortionInfo(portionUnit, 150.0)
    }
}

@Composable
fun PortionDialog(
    product: Product,
    initialMealType: MealType = MealType.BREAKFAST,
    initialGrams: Double = 100.0,
    isEditMode: Boolean = false,
    effectiveTierGoal: FitnessGoal = FitnessGoal.MAINTENANCE,
    onOpenProductInfo: (Product) -> Unit = {},
    onDismiss: () -> Unit,
    onConfirm: (mealType: MealType, grams: Double) -> Unit,
    appLanguage: String = "ru"
) {
    val view = LocalView.current
    var selectedMeal by rememberSaveable(product.id, initialGrams) { mutableStateOf(initialMealType) }
    var inputMode by rememberSaveable(product.id, initialGrams) { mutableStateOf(PortionInputMode.GRAMS) }

    var gramsText by rememberSaveable(product.id, initialGrams) {
        mutableStateOf(
            if (initialGrams % 1.0 == 0.0) initialGrams.toInt().toString() else String.format(Locale.forLanguageTag("ru"), "%.1f", initialGrams)
        )
    }

    val smartPortion = remember(product, appLanguage) { detectSmartPortion(product, appLanguage) }
    var portionCount by rememberSaveable(product.id, initialGrams) { mutableDoubleStateOf(1.0) }

    val effectiveGrams = if (inputMode == PortionInputMode.GRAMS) {
        gramsText.replace(',', '.').toDoubleOrNull() ?: 0.0
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

    val (tier, reason) = remember(product, effectiveTierGoal, appLanguage) {
        FoodTierSystem.evaluateProductTier(
            name = product.name,
            category = product.category,
            calories = product.calories,
            protein = product.protein,
            fat = product.fat,
            carbs = product.carbs,
            goal = effectiveTierGoal,
            lang = appLanguage
        )
    }

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
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TierSquareBadge(tier = tier, size = 22.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = reason,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tier.color,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { onOpenProductInfo(product) }
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appLanguage == "en") "Info" else "Справка", fontSize = 12.sp, color = WildWatermelon, fontWeight = FontWeight.Bold)
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
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Meal Selector
                Text(
                    text = if (appLanguage == "en") "Meal Section:" else "Приём пищи:",
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
                                .clickable {
                                    HapticUtil.performClick(view)
                                    selectedMeal = meal
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = meal.title(appLanguage),
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
                                    HapticUtil.performClick(view)
                                    inputMode = mode
                                    if (mode == PortionInputMode.PORTIONS) {
                                        val g = gramsText.replace(',', '.').toDoubleOrNull() ?: 100.0
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
                                text = mode.title(appLanguage),
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
                        text = if (appLanguage == "en") "Portion Weight (grams):" else "Масса порции (в граммах):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = gramsText,
                        onValueChange = { input ->
                            val norm = input.replace(',', '.')
                            if (norm.isEmpty() || norm.matches(Regex("^\\d{0,4}(\\.\\d{0,1})?$"))) {
                                gramsText = norm
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("portion_grams_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(14.dp),
                        trailingIcon = {
                            Text(
                                text = if (appLanguage == "en") "g" else "г",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
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
                            val label = if (appLanguage == "en") "${amt.toInt()} g" else "${amt.toInt()} г"
                            val isCurrent = (gramsText.replace(',', '.').toDoubleOrNull() ?: 0.0) == amt
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) DarkCardElevated else DarkCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) WildWatermelon else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        HapticUtil.performClick(view)
                                        gramsText = amt.toInt().toString()
                                    }
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
                            text = if (appLanguage == "en") "Amount of ${smartPortion.unitName}:" else "Количество ${smartPortion.unitName}:",
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
                                text = if (appLanguage == "en")
                                    "1 ${smartPortion.unitName} ≈ ${smartPortion.gramsPerUnit.roundToInt()} g"
                                else
                                    "1 ${smartPortion.unitName} ≈ ${smartPortion.gramsPerUnit.roundToInt()} г",
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
                                    HapticUtil.performClick(view)
                                    if (portionCount > 0.5) portionCount -= 0.5
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Меньше", tint = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val countStr = if (portionCount % 1.0 == 0.0) portionCount.toInt().toString() else String.format(Locale.forLanguageTag("ru"), "%.1f", portionCount)
                            val portionLabel = if (smartPortion.unitName != "порция") smartPortion.unitName else if (portionCount % 1.0 == 0.0 && portionCount.toInt() == 1) "порция" else if (portionCount % 1.0 == 0.0 && portionCount.toInt() in 2..4) "порции" else "порц."
                            Text(
                                text = "$countStr $portionLabel",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (appLanguage == "en") "= ${effectiveGrams.roundToInt()} grams" else "= ${effectiveGrams.roundToInt()} грамм",
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
                                .clickable {
                                    HapticUtil.performClick(view)
                                    portionCount += 0.5
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Больше", tint = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Portion Presets
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(portionPresets) { preset ->
                            val isCurrent = portionCount == preset
                            val presetLabel = if (preset % 1.0 == 0.0) "${preset.toInt()} ${smartPortion.unitName}" else "$preset ${smartPortion.unitName}"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) DarkCardElevated else DarkCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isCurrent) WildWatermelon else DarkBorder,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        HapticUtil.performClick(view)
                                        portionCount = preset
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = presetLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isCurrent) WildWatermelon else TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macros Calculation Summary
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCard)
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == "en") "Portion Total (${effectiveGrams.roundToInt()} g):" else "Итого в порции (${effectiveGrams.roundToInt()} г):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "${calcCalories.roundToInt()} ${if (appLanguage == "en") "kcal" else "ккал"}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = WildWatermelon
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        NutrientCard(label = if (appLanguage == "en") "Protein" else "Белки", value = calcProtein, color = ProteinColor, appLanguage = appLanguage, modifier = Modifier.weight(1f))
                        NutrientCard(label = if (appLanguage == "en") "Fat" else "Жиры", value = calcFat, color = FatColor, appLanguage = appLanguage, modifier = Modifier.weight(1f))
                        NutrientCard(label = if (appLanguage == "en") "Carbs" else "Углеводы", value = calcCarbs, color = CarbsColor, appLanguage = appLanguage, modifier = Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Confirm button
                Button(
                    onClick = {
                        if (effectiveGrams > 0) {
                            HapticUtil.performConfirm(view)
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
                        text = if (isEditMode) {
                            if (appLanguage == "en") "Save Changes (${effectiveGrams.roundToInt()} g)" else "Сохранить изменения (${effectiveGrams.roundToInt()} г)"
                        } else {
                            if (appLanguage == "en") "Add to ${selectedMeal.titleEn.lowercase()} (${effectiveGrams.roundToInt()} g)" else "Добавить в ${selectedMeal.titleRu.lowercase()} (${effectiveGrams.roundToInt()} г)"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NutrientCard(
    label: String,
    value: Double,
    color: Color,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkCardElevated)
            .padding(vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = String.format(Locale.forLanguageTag("ru"), "%.1f %s", value, if (appLanguage == "en") "g" else "г"),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
