package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.calculator.KbzhuCalculator
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBackground
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

@Composable
fun OnboardingScreen(
    onCompleteOnboarding: (
        age: Int,
        gender: Gender,
        heightCm: Double,
        weightKg: Double,
        targetWeightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        enableNotifications: Boolean
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by rememberSaveable { mutableIntStateOf(1) }

    // Step 1 State
    var gender by rememberSaveable { mutableStateOf(Gender.MALE) }
    var ageText by rememberSaveable { mutableStateOf("26") }
    var heightText by rememberSaveable { mutableStateOf("178") }
    var weightText by rememberSaveable { mutableStateOf("75") }
    var showStep1Errors by rememberSaveable { mutableStateOf(false) }

    // Step 2 State
    var activityLevel by rememberSaveable { mutableStateOf(ActivityLevel.MODERATE) }

    // Step 3 State
    var goal by rememberSaveable { mutableStateOf(FitnessGoal.MAINTENANCE) }
    var targetWeightText by rememberSaveable { mutableStateOf("70") }
    var showStep3Errors by rememberSaveable { mutableStateOf(false) }
    var enableNotifications by rememberSaveable { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        enableNotifications = isGranted
    }

    // Parsed values for calculation
    val ageVal = ageText.toIntOrNull() ?: 26
    val heightVal = heightText.toDoubleOrNull() ?: 178.0
    val weightVal = weightText.toDoubleOrNull() ?: 75.0
    val targetWeightVal = targetWeightText.toDoubleOrNull() ?: 70.0

    val isAgeValid = ageVal in 12..100
    val isHeightValid = heightVal in 100.0..250.0
    val isWeightValid = weightVal in 30.0..300.0
    val isTargetWeightValid = targetWeightVal in 30.0..300.0

    val calcResult = remember(gender, ageVal, heightVal, weightVal, activityLevel, goal) {
        KbzhuCalculator.calculate(
            gender = gender,
            age = ageVal,
            heightCm = heightVal,
            weightKg = weightVal,
            activityLevel = activityLevel,
            goal = goal
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("onboarding_screen")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WildWatermelonContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = WildWatermelon,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Персональная настройка",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Расчёт суточной нормы по формуле Миффлина-Сан Жеора",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Шаг $currentStep из 3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon
                    )
                    Text(
                        text = when (currentStep) {
                            1 -> "Физические данные"
                            2 -> "Активность"
                            else -> "Цель и норма"
                        },
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { currentStep / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = WildWatermelon,
                    trackColor = DarkCardElevated
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // STEP 1: Physical Parameters
            if (currentStep == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Укажите ваши физические параметры:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    // Gender Selector
                    Column {
                        Text("Пол:", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(Gender.MALE to "Мужской", Gender.FEMALE to "Женский").forEach { (g, label) ->
                                val isSelected = gender == g
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) WildWatermelon else DarkCardElevated)
                                        .border(
                                            1.dp,
                                            if (isSelected) WildWatermelon else DarkBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .selectable(
                                            selected = isSelected,
                                            role = Role.RadioButton,
                                            onClick = { gender = g }
                                        )
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF101014) else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Age Field with inline error
                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { ageText = it.filter { char -> char.isDigit() } },
                        label = { Text("Возраст (лет)") },
                        singleLine = true,
                        isError = showStep1Errors && !isAgeValid,
                        supportingText = {
                            if (showStep1Errors && !isAgeValid) {
                                Text("Введите возраст от 12 до 100 лет", color = Color(0xFFF87171), fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                            errorBorderColor = Color(0xFFF87171),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Height Field
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { heightText = it },
                        label = { Text("Рост (см)") },
                        singleLine = true,
                        isError = showStep1Errors && !isHeightValid,
                        supportingText = {
                            if (showStep1Errors && !isHeightValid) {
                                Text("Введите рост от 100 до 250 см", color = Color(0xFFF87171), fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                            errorBorderColor = Color(0xFFF87171),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Weight Field
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Текущий вес (кг)") },
                        singleLine = true,
                        isError = showStep1Errors && !isWeightValid,
                        supportingText = {
                            if (showStep1Errors && !isWeightValid) {
                                Text("Введите вес от 30 до 300 кг", color = Color(0xFFF87171), fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                            errorBorderColor = Color(0xFFF87171),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            showStep1Errors = true
                            if (isAgeValid && isHeightValid && isWeightValid) {
                                currentStep = 2
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Далее", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }

            // STEP 2: Activity Level
            if (currentStep == 2) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Выберите ваш уровень активности:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    ActivityLevel.entries.forEach { level ->
                        val isSelected = activityLevel == level
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) WildWatermelonContainer else DarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) WildWatermelon else DarkBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .selectable(
                                    selected = isSelected,
                                    role = Role.RadioButton,
                                    onClick = { activityLevel = level }
                                )
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = level.titleRu,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) WildWatermelon else TextPrimary
                                )
                                Text(
                                    text = "${level.multiplier}x",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WildWatermelon
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = level.descriptionRu,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 1 },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Назад", fontSize = 13.sp)
                        }

                        Button(
                            onClick = { currentStep = 3 },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WildWatermelon,
                                contentColor = Color(0xFF101014)
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                        ) {
                            Text("Далее", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // STEP 3: Fitness Goal & Calculated Norm
            if (currentStep == 3) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Ваша цель и расчет нормы:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )

                    FitnessGoal.entries.forEach { fitnessGoal ->
                        val isDisabled = ageVal < 18 && fitnessGoal == FitnessGoal.WEIGHT_LOSS
                        val isSelected = goal == fitnessGoal
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) WildWatermelonContainer else if (isDisabled) DarkCard.copy(alpha = 0.5f) else DarkCard)
                                .border(
                                    1.dp,
                                    if (isSelected) WildWatermelon else DarkBorder,
                                    RoundedCornerShape(14.dp)
                                )
                                .selectable(
                                    selected = isSelected,
                                    enabled = !isDisabled,
                                    role = Role.RadioButton,
                                    onClick = { goal = fitnessGoal }
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = fitnessGoal.titleRu + if (isDisabled) " (недоступно до 18 лет)" else "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) WildWatermelon else if (isDisabled) TextSecondary else TextPrimary
                            )
                            Text(
                                text = fitnessGoal.descriptionRu,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Target Weight Field
                    OutlinedTextField(
                        value = targetWeightText,
                        onValueChange = { targetWeightText = it },
                        label = { Text("Желаемый вес (кг)") },
                        singleLine = true,
                        isError = showStep3Errors && !isTargetWeightValid,
                        supportingText = {
                            if (showStep3Errors && !isTargetWeightValid) {
                                Text("Введите желаемый вес от 30 до 300 кг", color = Color(0xFFF87171), fontSize = 11.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = com.example.ui.theme.OutlineStrong,
                            errorBorderColor = Color(0xFFF87171),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Calculated Norm Summary Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(WildWatermelonContainer)
                            .border(1.dp, WildWatermelon.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ваша суточная норма:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
                            Text("${calcResult.targetCalories} ккал", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WildWatermelon)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Белки: ${calcResult.targetProtein}г", fontSize = 12.sp, color = TextPrimary)
                            Text("Жиры: ${calcResult.targetFat}г", fontSize = 12.sp, color = TextPrimary)
                            Text("Углеводы: ${calcResult.targetCarbs}г", fontSize = 12.sp, color = TextPrimary)
                        }
                    }

                    if (calcResult.warnings.isNotEmpty()) {
                        fun translateWarning(warn: String): String {
                            return when {
                                warn.contains("Дефицит невозможен") || warn.contains("BMR") ->
                                    "Safe calorie deficit below BMR is limited to protect metabolism. Minimum target set to BMR."
                                warn.contains("Минимальный калораж") ->
                                    "Minimum calorie target is capped at BMR level."
                                else -> warn
                            }
                        }

                        calcResult.warnings.forEach { rawWarn ->
                            val warn = translateWarning(rawWarn)
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

                    // Notification Switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkCard)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = WildWatermelon,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Напоминать о приёмах пищи",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Напоминания при неактивности более 4 часов",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        Switch(
                            checked = enableNotifications,
                            onCheckedChange = { checked ->
                                enableNotifications = checked
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF101014),
                                checkedTrackColor = WildWatermelon
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { currentStep = 2 },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Назад", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                showStep3Errors = true
                                if (isTargetWeightValid) {
                                    onCompleteOnboarding(
                                        ageVal,
                                        gender,
                                        heightVal,
                                        weightVal,
                                        targetWeightVal,
                                        activityLevel,
                                        goal,
                                        enableNotifications
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WildWatermelon,
                                contentColor = Color(0xFF101014)
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                        ) {
                            Text("Начать", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        }
                    }
                }
            }
        }
    }
}
