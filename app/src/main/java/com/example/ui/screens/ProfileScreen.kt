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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.WildWatermelon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.calculator.KbzhuCalculator
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.UserProfile
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale

private fun formatMetric(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.forLanguageTag("ru"), "%.1f", value)

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onSaveProfile: (
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        isManual: Boolean,
        customCal: Int,
        customP: Int,
        customF: Int,
        customC: Int
    ) -> Unit,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    var gender by remember(userProfile) {
        mutableStateOf(Gender.entries.find { it.name == userProfile.gender } ?: Gender.MALE)
    }
    var ageText by rememberSaveable(userProfile) { mutableStateOf(userProfile.age.toString()) }
    var heightText by rememberSaveable(userProfile) { mutableStateOf(formatMetric(userProfile.heightCm)) }
    var weightText by rememberSaveable(userProfile) { mutableStateOf(formatMetric(userProfile.weightKg)) }
    var targetWeightText by rememberSaveable(userProfile) { mutableStateOf(formatMetric(userProfile.targetWeightKg)) }

    var activityLevel by remember(userProfile) {
        mutableStateOf(ActivityLevel.entries.find { it.name == userProfile.activityLevel } ?: ActivityLevel.MODERATE)
    }
    var goal by remember(userProfile) {
        mutableStateOf(FitnessGoal.entries.find { it.name == userProfile.goal } ?: FitnessGoal.MAINTENANCE)
    }

    var isManual by rememberSaveable(userProfile) { mutableStateOf(userProfile.isManualTarget) }
    var customCalText by rememberSaveable(userProfile) { mutableStateOf(userProfile.targetCalories.toString()) }
    var customPText by rememberSaveable(userProfile) { mutableStateOf(userProfile.targetProtein.toString()) }
    var customFText by rememberSaveable(userProfile) { mutableStateOf(userProfile.targetFat.toString()) }
    var customCText by rememberSaveable(userProfile) { mutableStateOf(userProfile.targetCarbs.toString()) }

    var showSaveBanner by rememberSaveable { mutableStateOf(false) }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }
    var showUnderweightConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val age = ageText.toIntOrNull() ?: 26
    val height = heightText.toDoubleOrNull() ?: 175.0
    val weight = weightText.toDoubleOrNull() ?: 72.0

    // Compute live Mifflin-St Jeor results
    val calcResult = remember(gender, age, height, weight, activityLevel, goal) {
        KbzhuCalculator.calculate(
            gender = gender,
            age = age,
            heightCm = height,
            weightKg = weight,
            activityLevel = activityLevel,
            goal = goal
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (appLanguage == "en") "Profile & Calorie Goals" else "Профиль и расчёт КБЖУ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = if (appLanguage == "en") "Scientific calorie & macro calculation using Mifflin-St Jeor equation" else "Научный расчёт нормы калорий и макронутриентов по формуле Миффлина-Сан Жеора",
            fontSize = 12.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save confirmation banner
        if (showSaveBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x264ADE80))
                    .border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (appLanguage == "en") "Profile and calorie targets saved!" else "Параметры и нормы КБЖУ сохранены!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4ADE80)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Section 1: Anthropometrics
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (appLanguage == "en") "Personal Data" else "Личные данные",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Gender Switcher
            Text(text = if (appLanguage == "en") "Gender:" else "Пол:", fontSize = 13.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Gender.entries.forEach { g ->
                    val isSelected = gender == g
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WildWatermelon else DarkCardElevated)
                            .clickable {
                                gender = g
                                showSaveBanner = false
                                validationError = null
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g.title(appLanguage),
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Age, Height, Weight inputs
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricInput(
                        label = if (appLanguage == "en") "Age" else "Возраст",
                        unit = if (appLanguage == "en") "yrs" else "лет",
                        value = ageText,
                        onValueChange = {
                            ageText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricInput(
                        label = if (appLanguage == "en") "Height" else "Рост",
                        unit = if (appLanguage == "en") "cm" else "см",
                        value = heightText,
                        allowDecimal = true,
                        onValueChange = {
                            heightText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricInput(
                        label = if (appLanguage == "en") "Current Weight" else "Текущий вес",
                        unit = if (appLanguage == "en") "kg" else "кг",
                        value = weightText,
                        allowDecimal = true,
                        onValueChange = {
                            weightText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricInput(
                        label = if (appLanguage == "en") "Target Weight" else "Целевой вес",
                        unit = if (appLanguage == "en") "kg" else "кг",
                        value = targetWeightText,
                        allowDecimal = true,
                        onValueChange = {
                            targetWeightText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 2: Physical Activity
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (appLanguage == "en") "Physical Activity Level" else "Уровень физической активности",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.entries.forEach { level ->
                    val isSelected = activityLevel == level
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DarkCardElevated else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) WildWatermelon else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                activityLevel = level
                                showSaveBanner = false
                                validationError = null
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) WildWatermelon else DarkBorder)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = level.title(appLanguage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) WildWatermelon else TextPrimary
                            )
                            Text(
                                text = level.description(appLanguage),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 3: Fitness Goal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Text(
                text = if (appLanguage == "en") "Fitness Goal" else "Основная цель",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FitnessGoal.entries.forEach { g ->
                    val isSelected = goal == g
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DarkCardElevated else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) WildWatermelon else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                val bmiVal = if (height > 0) weight / ((height / 100.0) * (height / 100.0)) else 22.0
                                if (g == FitnessGoal.WEIGHT_LOSS && age < 18) {
                                    validationError = if (appLanguage == "en") "Weight loss is not allowed for minors under 18" else "Для детей и подростков до 18 лет похудение недоступно"
                                    goal = FitnessGoal.MAINTENANCE
                                } else if (g == FitnessGoal.WEIGHT_LOSS && bmiVal < 18.5) {
                                    showUnderweightConfirmDialog = true
                                } else {
                                    goal = g
                                    showSaveBanner = false
                                    validationError = null
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) WildWatermelon else DarkBorder)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = g.title(appLanguage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) WildWatermelon else TextPrimary
                            )
                            Text(
                                text = g.description(appLanguage),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section 4: Calculation Results Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(DarkCard)
                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage == "en") "Calculated Targets" else "Расчётная норма КБЖУ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (appLanguage == "en") "${calcResult.targetCalories} kcal/day" else "${calcResult.targetCalories} ккал/день",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = WildWatermelon,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkCardElevated)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (appLanguage == "en") "Base BMR" else "Базовый BMR", fontSize = 11.sp, color = TextTertiary)
                    Text(if (appLanguage == "en") "${calcResult.bmr} kcal" else "${calcResult.bmr} ккал", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (appLanguage == "en") "TDEE Expense" else "Расход TDEE", fontSize = 11.sp, color = TextTertiary)
                    Text(if (appLanguage == "en") "${calcResult.tdee} kcal" else "${calcResult.tdee} ккал", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated macro targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatedMacroCard(
                    title = if (appLanguage == "en") "Protein" else "Белки",
                    grams = calcResult.targetProtein,
                    color = ProteinColor,
                    modifier = Modifier.weight(1f)
                )
                CalculatedMacroCard(
                    title = if (appLanguage == "en") "Fat" else "Жиры",
                    grams = calcResult.targetFat,
                    color = FatColor,
                    modifier = Modifier.weight(1f)
                )
                CalculatedMacroCard(
                    title = if (appLanguage == "en") "Carbs" else "Углеводы",
                    grams = calcResult.targetCarbs,
                    color = CarbsColor,
                    modifier = Modifier.weight(1f)
                )
            }

            val targetWeightVal = targetWeightText.toDoubleOrNull() ?: 70.0
            val goalConsistencyWarning = when {
                goal == FitnessGoal.WEIGHT_LOSS && targetWeightVal > weight ->
                    if (appLanguage == "en")
                        "Target weight is higher than current ($targetWeightVal > $weight kg). For weight loss, target weight should be lower."
                    else
                        "Желаемый вес выше текущего ($targetWeightVal > $weight кг). Для похудения целевой вес должен быть меньше текущего."
                goal == FitnessGoal.WEIGHT_GAIN && targetWeightVal < weight ->
                    if (appLanguage == "en")
                        "Target weight is lower than current ($targetWeightVal < $weight kg). For weight gain, target weight should be higher."
                    else
                        "Желаемый вес ниже текущего ($targetWeightVal < $weight кг). Для набора массы целевой вес должен быть больше текущего."
                else -> null
            }

            fun translateWarning(warn: String): String {
                if (appLanguage != "en") return warn
                return when {
                    warn.contains("Дефицит невозможен") || warn.contains("BMR") ->
                        "Safe calorie deficit below BMR is limited to protect metabolism. Minimum target set to BMR."
                    warn.contains("Минимальный калораж") ->
                        "Minimum calorie target is capped at BMR level."
                    else -> warn
                }
            }

            val allWarnings = remember(calcResult.warnings, goalConsistencyWarning, appLanguage) {
                val list = mutableListOf<String>()
                list.addAll(calcResult.warnings.map { translateWarning(it) })
                if (goalConsistencyWarning != null) list.add(goalConsistencyWarning)
                list
            }

            if (allWarnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                allWarnings.forEach { warn ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF332615))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warn,
                                fontSize = 12.sp,
                                color = Color(0xFFF59E0B),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Toggle manual mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (appLanguage == "en") "Manual Target Customization" else "Ручная корректировка норм",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = if (appLanguage == "en") "Specify custom calorie and macro targets" else "Указать собственные значения калорий и БЖУ",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isManual,
                    onCheckedChange = {
                        isManual = it
                        showSaveBanner = false
                        validationError = null
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = WildWatermelon,
                        checkedTrackColor = DarkCardElevated
                    )
                )
            }

            if (isManual) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customCalText,
                    onValueChange = { input ->
                        if (input.length <= 5 && input.all { ch -> ch.isDigit() }) {
                            customCalText = input
                            showSaveBanner = false
                            validationError = null
                        }
                    },
                    label = { Text(if (appLanguage == "en") "Target Calories (kcal)" else "Целевые калории (ккал)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WildWatermelon,
                        unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkCardElevated,
                        unfocusedContainerColor = DarkCardElevated
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricInput(
                        label = if (appLanguage == "en") "P (g)" else "Б (г)",
                        unit = if (appLanguage == "en") "g" else "г",
                        value = customPText,
                        onValueChange = {
                            customPText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricInput(
                        label = if (appLanguage == "en") "F (g)" else "Ж (г)",
                        unit = if (appLanguage == "en") "g" else "г",
                        value = customFText,
                        onValueChange = {
                            customFText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MetricInput(
                        label = if (appLanguage == "en") "C (g)" else "У (г)",
                        unit = if (appLanguage == "en") "g" else "г",
                        value = customCText,
                        onValueChange = {
                            customCText = it
                            showSaveBanner = false
                            validationError = null
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                val pManual = customPText.toIntOrNull() ?: 0
                val fManual = customFText.toIntOrNull() ?: 0
                val cManual = customCText.toIntOrNull() ?: 0
                val macroCalories = (pManual * 4) + (fManual * 9) + (cManual * 4)
                val manualCal = customCalText.toIntOrNull() ?: 0

                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardElevated)
                        .padding(10.dp)
                ) {
                    Text(
                        text = if (appLanguage == "en") "Calories from P/F/C formula (4/9/4): $macroCalories kcal" else "Калорийность по формуле БЖУ (4/9/4): $macroCalories ккал",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (macroCalories > 0 && kotlin.math.abs(manualCal - macroCalories) > 50) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (appLanguage == "en") "Warning: target calories ($manualCal kcal) differ from sum of macros ($macroCalories kcal)" else "Внимание: калорийность ($manualCal ккал) значительно отличается от суммы БЖУ ($macroCalories ккал)",
                            fontSize = 11.sp,
                            color = Color(0xFFF87171)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(WildWatermelon)
                                .clickable {
                                    customCalText = macroCalories.toString()
                                    validationError = null
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (appLanguage == "en") "Align Calories ($macroCalories kcal)" else "Выровнять калории ($macroCalories ккал)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF101014)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (validationError != null) {
            Text(
                text = validationError ?: "",
                color = Color(0xFFF87171),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Save Button
        Button(
            onClick = {
                val ageVal = ageText.toIntOrNull()
                if (ageVal == null || ageVal < 10 || ageVal > 120) {
                    validationError = if (appLanguage == "en") "Age must be between 10 and 120" else "Возраст должен быть от 10 до 120 лет"
                    return@Button
                }

                val heightVal = heightText.toDoubleOrNull()
                if (heightVal == null || heightVal < 100.0 || heightVal > 250.0) {
                    validationError = if (appLanguage == "en") "Height must be between 100 and 250 cm" else "Рост должен быть от 100 до 250 см"
                    return@Button
                }

                val weightVal = weightText.toDoubleOrNull()
                if (weightVal == null || weightVal < 25.0 || weightVal > 300.0) {
                    validationError = if (appLanguage == "en") "Weight must be between 25 and 300 kg" else "Текущий вес должен быть от 25 до 300 кг"
                    return@Button
                }

                val targetWeightVal = targetWeightText.toDoubleOrNull()
                if (targetWeightVal == null || targetWeightVal < 25.0 || targetWeightVal > 300.0) {
                    validationError = if (appLanguage == "en") "Target weight must be between 25 and 300 kg" else "Целевой вес должен быть от 25 до 300 кг"
                    return@Button
                }

                if (isManual) {
                    val calVal = customCalText.toIntOrNull()
                    if (calVal == null || calVal < 800 || calVal > 10000) {
                        validationError = "Калории должны быть от 800 до 10000 ккал"
                        return@Button
                    }
                }

                validationError = null

                val cal = if (isManual) customCalText.toIntOrNull() ?: calcResult.targetCalories else calcResult.targetCalories
                val p = if (isManual) customPText.toIntOrNull() ?: calcResult.targetProtein else calcResult.targetProtein
                val f = if (isManual) customFText.toIntOrNull() ?: calcResult.targetFat else calcResult.targetFat
                val c = if (isManual) customCText.toIntOrNull() ?: calcResult.targetCarbs else calcResult.targetCarbs

                onSaveProfile(
                    ageVal,
                    gender,
                    heightVal,
                    weightVal,
                    targetWeightVal,
                    activityLevel,
                    goal,
                    isManual,
                    cal,
                    p,
                    f,
                    c
                )
                showSaveBanner = true
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WildWatermelon,
                contentColor = Color(0xFF101014)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_profile_button")
        ) {
            Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (appLanguage == "en") "Save Profile & Calorie Goals" else "Сохранить профиль и норму КБЖУ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showUnderweightConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUnderweightConfirmDialog = false },
            title = {
                Text("Предупреждение врача", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            },
            text = {
                Text(
                    "При недостаточном весе (ИМТ < 18.5) похудение не рекомендуется, обратитесь к врачу. Вы уверены, что хотите выбрать цель «Похудение»?",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        goal = FitnessGoal.WEIGHT_LOSS
                        showUnderweightConfirmDialog = false
                        showSaveBanner = false
                        validationError = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WildWatermelon)
                ) {
                    Text("Продолжить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnderweightConfirmDialog = false }) {
                    Text("Отмена", color = TextTertiary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun MetricInput(
    label: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
    allowDecimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = { input ->
                if (allowDecimal) {
                    val normalized = input.replace(',', '.')
                    if (normalized.isEmpty() || normalized.matches(Regex("^\\d{0,3}(\\.\\d{0,1})?$"))) {
                        onValueChange(normalized)
                    }
                } else {
                    if (input.length <= 4 && input.all { it.isDigit() }) {
                        onValueChange(input)
                    }
                }
            },
            trailingIcon = { Text(unit, fontSize = 11.sp, color = TextTertiary, modifier = Modifier.padding(end = 6.dp)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WildWatermelon,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkCardElevated,
                unfocusedContainerColor = DarkCardElevated
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CalculatedMacroCard(
    title: String,
    grams: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardElevated)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$grams г",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
