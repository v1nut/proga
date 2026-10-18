package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.data.service.GeminiFoodService
import com.example.data.service.RecognizedMeal
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private class EditableItemState(
    initialName: String,
    initialGrams: Double,
    initialCalories: Double,
    initialProtein: Double,
    initialFat: Double,
    initialCarbs: Double
) {
    val id: String = java.util.UUID.randomUUID().toString()
    var name by mutableStateOf(initialName)

    var baseCaloriesPer100g by mutableDoubleStateOf(
        if (initialGrams > 0) (initialCalories / initialGrams) * 100.0 else initialCalories
    )
    val baseProteinPer100g: Double = if (initialGrams > 0) (initialProtein / initialGrams) * 100.0 else initialProtein
    val baseFatPer100g: Double = if (initialGrams > 0) (initialFat / initialGrams) * 100.0 else initialFat
    val baseCarbsPer100g: Double = if (initialGrams > 0) (initialCarbs / initialGrams) * 100.0 else initialCarbs

    var gramsInputText by mutableStateOf(if (initialGrams > 0) initialGrams.roundToInt().toString() else "")
    var caloriesInputText by mutableStateOf(if (initialCalories > 0) initialCalories.roundToInt().toString() else "")

    val grams: Double
        get() = gramsInputText.toDoubleOrNull() ?: 0.0

    val calories: Double
        get() = caloriesInputText.toDoubleOrNull() ?: (baseCaloriesPer100g * grams / 100.0)

    val protein: Double
        get() = baseProteinPer100g * grams / 100.0

    val fat: Double
        get() = baseFatPer100g * grams / 100.0

    val carbs: Double
        get() = baseCarbsPer100g * grams / 100.0

    fun updateGramsText(text: String) {
        val clean = text.filter { it.isDigit() }
        gramsInputText = clean
        val g = clean.toDoubleOrNull()
        if (g != null && g > 0) {
            caloriesInputText = (baseCaloriesPer100g * g / 100.0).roundToInt().toString()
        } else {
            caloriesInputText = ""
        }
    }

    fun updateCaloriesText(text: String) {
        val clean = text.filter { it.isDigit() }
        caloriesInputText = clean
        val newCal = clean.toDoubleOrNull()
        if (newCal != null && grams > 0) {
            baseCaloriesPer100g = (newCal / grams) * 100.0
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAiMealBar(
    onSaveMealEntries: (entries: List<MealEntry>) -> Unit,
    onOpenCatalog: () -> Unit = {},
    onOpenCameraPhoto: () -> Unit,
    currentDateString: String = java.time.LocalDate.now().toString(),
    effectiveTierGoal: com.example.data.model.FitnessGoal = com.example.data.model.FitnessGoal.MAINTENANCE,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedQuery by remember { mutableStateOf("") }
    var pendingMeal by remember { mutableStateOf<RecognizedMeal?>(null) }
    val scope = rememberCoroutineScope()
    val geminiService = remember(context) { GeminiFoodService(context) }
    val focusManager = LocalFocusManager.current

    fun executeAddMeal(query: String) {
        if (query.isNotBlank() && !isLoading) {
            isLoading = true
            errorMessage = null
            failedQuery = query
            focusManager.clearFocus()
            scope.launch {
                val result = geminiService.parseQuickMealText(query)
                result.onSuccess { meal ->
                    pendingMeal = meal
                }.onFailure { exception ->
                    errorMessage = exception.message ?: "Не удалось распознать блюдо"
                }
                isLoading = false
            }
        }
    }

    val submitText: () -> Unit = {
        executeAddMeal(textInput.trim())
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, WildWatermelon.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(12.dp)
            .testTag("quick_ai_meal_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(WildWatermelonContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WildWatermelon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (appLanguage == "en") "AI Express Input" else "ИИ Экспресс-запись",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon,
                        maxLines = 1
                    )
                    Text(
                        text = if (appLanguage == "en") "Type what you ate or take a photo" else "Напишите, что съели, или сделайте фото",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                        .clickable { onOpenCameraPhoto() }
                        .testTag("quick_bar_camera_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Сфотографировать еду",
                        tint = WildWatermelon,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input text field with Send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        text = if (appLanguage == "en") "e.g. 2 eggs & coffee 200ml" else "например: 2 яйца и кофе 200мл",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                },
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    color = TextPrimary,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)
                ),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 52.dp)
                    .testTag("quick_meal_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { submitText() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WildWatermelon,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = WildWatermelon
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (textInput.isNotBlank()) WildWatermelon else DarkCardElevated)
                    .clickable(enabled = textInput.isNotBlank() && !isLoading) { submitText() }
                    .testTag("quick_meal_send_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = if (textInput.isNotBlank()) Color(0xFF101014) else WildWatermelon,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = if (textInput.isNotBlank()) Color(0xFF101014) else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Error Card with Retry button
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A1517))
                    .border(1.dp, Color(0xFFF87171), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage ?: "Ошибка ИИ",
                            fontSize = 12.sp,
                            color = Color(0xFFF87171),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(
                        onClick = { errorMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val q = failedQuery
                        errorMessage = null
                        executeAddMeal(q)
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelon,
                        contentColor = Color(0xFF101014)
                    ),
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Повторить", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // ModalBottomSheet: "Проверьте перед записью"
    if (pendingMeal != null) {
        val meal = pendingMeal!!
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val itemsList = remember(meal) {
            val initial = if (meal.items.isNotEmpty()) {
                meal.items.map { item ->
                    EditableItemState(
                        initialName = item.name,
                        initialGrams = item.grams.toDouble(),
                        initialCalories = item.calories.toDouble(),
                        initialProtein = item.protein,
                        initialFat = item.fat,
                        initialCarbs = item.carbs
                    )
                }
            } else {
                listOf(
                    EditableItemState(
                        initialName = meal.dishName.ifBlank { "Блюдо" },
                        initialGrams = meal.estimatedGrams.toDouble(),
                        initialCalories = meal.calories.toDouble(),
                        initialProtein = meal.protein,
                        initialFat = meal.fat,
                        initialCarbs = meal.carbs
                    )
                )
            }
            mutableStateListOf<EditableItemState>().apply { addAll(initial) }
        }

        var selectedMealType by remember(meal) { mutableStateOf(meal.mealType) }

        ModalBottomSheet(
            onDismissRequest = { pendingMeal = null },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (appLanguage == "en") "Review before logging" else "Проверьте перед записью",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = { pendingMeal = null }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Закрыть", tint = TextSecondary)
                    }
                }

                // Offline estimate banner
                if (meal.isEstimate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF332615))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (appLanguage == "en") "Offline estimate, check values" else "Офлайн-оценка, проверьте значения",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meal Type choice buttons
                Text(if (appLanguage == "en") "Meal Section:" else "Приём пищи:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { type ->
                        val isSelected = selectedMealType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) WildWatermelon else DarkCardElevated)
                                .clickable { selectedMealType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = type.title(appLanguage),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF101014) else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of items
                Text(if (appLanguage == "en") "Items (${itemsList.size}):" else "Позиции (${itemsList.size}):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                itemsList.forEachIndexed { index, item ->
                    val (itemTier, _) = remember(item.name, item.baseCaloriesPer100g, effectiveTierGoal, appLanguage) {
                        com.example.ui.util.FoodTierSystem.evaluateProductTier(
                            name = item.name,
                            category = "",
                            calories = item.baseCaloriesPer100g,
                            protein = item.baseProteinPer100g,
                            fat = item.baseFatPer100g,
                            carbs = item.baseCarbsPer100g,
                            goal = effectiveTierGoal,
                            lang = appLanguage
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            com.example.ui.util.TierSquareBadge(tier = itemTier, size = 24.dp)
                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = item.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { itemsList.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Удалить позицию",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Grams & Calories Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (appLanguage == "en") "Weight (g):" else "Масса (г):", fontSize = 10.sp, color = TextSecondary)
                                OutlinedTextField(
                                    value = item.gramsInputText,
                                    onValueChange = { item.updateGramsText(it) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WildWatermelon,
                                        unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(if (appLanguage == "en") "Calories (kcal):" else "Калории (ккал):", fontSize = 10.sp, color = TextSecondary)
                                OutlinedTextField(
                                    value = item.caloriesInputText,
                                    onValueChange = { item.updateCaloriesText(it) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = WildWatermelon,
                                        unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = TextPrimary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Macros row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (appLanguage == "en") "P: ${(item.protein * 10).roundToInt() / 10.0}g" else "Б: ${(item.protein * 10).roundToInt() / 10.0}г", fontSize = 11.sp, color = ProteinColor)
                            Text(if (appLanguage == "en") "F: ${(item.fat * 10).roundToInt() / 10.0}g" else "Ж: ${(item.fat * 10).roundToInt() / 10.0}г", fontSize = 11.sp, color = FatColor)
                            Text(if (appLanguage == "en") "C: ${(item.carbs * 10).roundToInt() / 10.0}g" else "У: ${(item.carbs * 10).roundToInt() / 10.0}г", fontSize = 11.sp, color = CarbsColor)
                        }
                    }
                }

                // Unrecognized items
                if (meal.unrecognizedItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    meal.unrecognizedItems.forEach { unparsed ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkCardElevated)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (appLanguage == "en") "Unrecognized: \"$unparsed\"" else "Не распознано: «$unparsed»",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(
                                onClick = {
                                    pendingMeal = null
                                    onOpenCatalog()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (appLanguage == "en") "Search Catalog" else "Найти в каталоге", fontSize = 11.sp, color = WildWatermelon)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Total Summary
                val totalGrams = itemsList.sumOf { it.grams }.roundToInt()
                val totalCalories = itemsList.sumOf { it.calories }.roundToInt()
                val totalP = (itemsList.sumOf { it.protein } * 10).roundToInt() / 10.0
                val totalF = (itemsList.sumOf { it.fat } * 10).roundToInt() / 10.0
                val totalC = (itemsList.sumOf { it.carbs } * 10).roundToInt() / 10.0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(WildWatermelonContainer)
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (appLanguage == "en") "Total to log:" else "Итого к зачислению:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
                            Text(if (appLanguage == "en") "$totalGrams g • $totalCalories kcal" else "$totalGrams г • $totalCalories ккал", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (appLanguage == "en") "Protein: $totalP g" else "Белки: $totalP г", fontSize = 11.sp, color = TextPrimary)
                            Text(if (appLanguage == "en") "Fat: $totalF g" else "Жиры: $totalF г", fontSize = 11.sp, color = TextPrimary)
                            Text(if (appLanguage == "en") "Carbs: $totalC g" else "Углеводы: $totalC г", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { pendingMeal = null },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardElevated,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (appLanguage == "en") "Cancel" else "Отмена", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            val allValid = itemsList.isNotEmpty() && itemsList.all { it.grams > 0.0 && it.calories >= 0.0 }
                            if (allValid) {
                                val entries = itemsList.map { item ->
                                    MealEntry(
                                        dateString = currentDateString,
                                        mealType = selectedMealType.name,
                                        productName = item.name.ifBlank { "Продукт" },
                                        weightGrams = item.grams,
                                        calories = item.calories,
                                        protein = item.protein,
                                        fat = item.fat,
                                        carbs = item.carbs
                                    )
                                }
                                onSaveMealEntries(entries)
                                textInput = ""
                                pendingMeal = null
                            }
                        },
                        enabled = itemsList.isNotEmpty() && itemsList.all { it.grams > 0.0 },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014)
                        ),
                        modifier = Modifier.weight(1.3f).height(48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(if (appLanguage == "en") "Add to Diary" else "Добавить в дневник", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
}
