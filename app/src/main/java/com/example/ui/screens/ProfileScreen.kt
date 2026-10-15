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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.data.calculator.KbzhuCalculator
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.UserProfile
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    onSaveProfile: (
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        isManual: Boolean,
        customCal: Int,
        customP: Int,
        customF: Int,
        customC: Int
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var gender by remember(userProfile) {
        mutableStateOf(Gender.entries.find { it.name == userProfile.gender } ?: Gender.MALE)
    }
    var ageText by remember(userProfile) { mutableStateOf(userProfile.age.toString()) }
    var heightText by remember(userProfile) { mutableStateOf(userProfile.heightCm.toInt().toString()) }
    var weightText by remember(userProfile) { mutableStateOf(userProfile.weightKg.toInt().toString()) }

    var activityLevel by remember(userProfile) {
        mutableStateOf(ActivityLevel.entries.find { it.name == userProfile.activityLevel } ?: ActivityLevel.MODERATE)
    }
    var goal by remember(userProfile) {
        mutableStateOf(FitnessGoal.entries.find { it.name == userProfile.goal } ?: FitnessGoal.MAINTENANCE)
    }

    var isManual by remember(userProfile) { mutableStateOf(userProfile.isManualTarget) }
    var customCalText by remember(userProfile) { mutableStateOf(userProfile.targetCalories.toString()) }
    var customPText by remember(userProfile) { mutableStateOf(userProfile.targetProtein.toString()) }
    var customFText by remember(userProfile) { mutableStateOf(userProfile.targetFat.toString()) }
    var customCText by remember(userProfile) { mutableStateOf(userProfile.targetCarbs.toString()) }

    var showSaveBanner by remember { mutableStateOf(false) }

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
            text = "Профиль и расчёт КБЖУ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Научный расчёт нормы калорий и макронутриентов по формуле Миффлина-Сан Жеора",
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
                        text = "Параметры и нормы КБЖУ сохранены!",
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
                text = "Личные данные",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Gender Switcher
            Text(text = "Пол:", fontSize = 13.sp, color = TextSecondary)
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
                            .background(if (isSelected) ElectricLime else DarkCardElevated)
                            .clickable {
                                gender = g
                                showSaveBanner = false
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = g.titleRu,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Age, Height, Weight inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricInput(
                    label = "Возраст",
                    unit = "лет",
                    value = ageText,
                    onValueChange = {
                        ageText = it
                        showSaveBanner = false
                    },
                    modifier = Modifier.weight(1f)
                )
                MetricInput(
                    label = "Рост",
                    unit = "см",
                    value = heightText,
                    onValueChange = {
                        heightText = it
                        showSaveBanner = false
                    },
                    modifier = Modifier.weight(1f)
                )
                MetricInput(
                    label = "Вес",
                    unit = "кг",
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        showSaveBanner = false
                    },
                    modifier = Modifier.weight(1f)
                )
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
                text = "Уровень физической активности",
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
                                color = if (isSelected) ElectricLime else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                activityLevel = level
                                showSaveBanner = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricLime else DarkBorder)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = level.titleRu,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) ElectricLime else TextPrimary
                            )
                            Text(
                                text = level.descriptionRu,
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
                text = "Основная цель",
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
                                color = if (isSelected) ElectricLime else DarkBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                goal = g
                                showSaveBanner = false
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricLime else DarkBorder)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = g.titleRu,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) ElectricLime else TextPrimary
                            )
                            Text(
                                text = g.descriptionRu,
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
                    text = "Расчётная норма КБЖУ",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${calcResult.targetCalories} ккал/день",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLime,
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
                    Text("Базовый BMR", fontSize = 11.sp, color = TextTertiary)
                    Text("${calcResult.bmr} ккал", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(DarkBorder))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Расход TDEE", fontSize = 11.sp, color = TextTertiary)
                    Text("${calcResult.tdee} ккал", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated macro targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalculatedMacroCard(
                    title = "Белки",
                    grams = calcResult.targetProtein,
                    color = ProteinColor,
                    modifier = Modifier.weight(1f)
                )
                CalculatedMacroCard(
                    title = "Жиры",
                    grams = calcResult.targetFat,
                    color = FatColor,
                    modifier = Modifier.weight(1f)
                )
                CalculatedMacroCard(
                    title = "Углеводы",
                    grams = calcResult.targetCarbs,
                    color = CarbsColor,
                    modifier = Modifier.weight(1f)
                )
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
                        text = "Ручная корректировка норм",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Указать собственные значения калорий и БЖУ",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = isManual,
                    onCheckedChange = {
                        isManual = it
                        showSaveBanner = false
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElectricLime,
                        checkedTrackColor = DarkCardElevated
                    )
                )
            }

            if (isManual) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = customCalText,
                    onValueChange = { customCalText = it },
                    label = { Text("Целевые калории (ккал)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = DarkBorder,
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
                        label = "Б (г)",
                        unit = "г",
                        value = customPText,
                        onValueChange = { customPText = it },
                        modifier = Modifier.weight(1f)
                    )
                    MetricInput(
                        label = "Ж (г)",
                        unit = "г",
                        value = customFText,
                        onValueChange = { customFText = it },
                        modifier = Modifier.weight(1f)
                    )
                    MetricInput(
                        label = "У (г)",
                        unit = "г",
                        value = customCText,
                        onValueChange = { customCText = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                val cal = if (isManual) customCalText.toIntOrNull() ?: calcResult.targetCalories else calcResult.targetCalories
                val p = if (isManual) customPText.toIntOrNull() ?: calcResult.targetProtein else calcResult.targetProtein
                val f = if (isManual) customFText.toIntOrNull() ?: calcResult.targetFat else calcResult.targetFat
                val c = if (isManual) customCText.toIntOrNull() ?: calcResult.targetCarbs else calcResult.targetCarbs

                onSaveProfile(
                    age,
                    gender,
                    height,
                    weight,
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
                containerColor = ElectricLime,
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
                text = "Сохранить профиль и норму КБЖУ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricInput(
    label: String,
    unit: String,
    value: String,
    onValueChange: (String) -> Unit,
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
                if (input.isEmpty() || input.all { it.isDigit() }) {
                    onValueChange(input)
                }
            },
            trailingIcon = { Text(unit, fontSize = 11.sp, color = TextTertiary, modifier = Modifier.padding(end = 6.dp)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricLime,
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
