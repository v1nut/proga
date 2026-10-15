package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.MealType
import com.example.data.service.GeminiFoodService
import com.example.data.service.RecognizedMeal
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AiFoodPhotoDialog(
    initialMealType: MealType = MealType.LUNCH,
    onDismiss: () -> Unit,
    onAddMeal: (dishName: String, grams: Int, calories: Int, protein: Double, fat: Double, carbs: Double, mealType: MealType) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val geminiService = remember { GeminiFoodService() }

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisDone by remember { mutableStateOf(false) }

    var editableDishName by remember { mutableStateOf("") }
    var editableGrams by remember { mutableIntStateOf(200) }
    var baseCaloriesPer100g by remember { mutableDoubleStateOf(150.0) }
    var baseProteinPer100g by remember { mutableDoubleStateOf(10.0) }
    var baseFatPer100g by remember { mutableDoubleStateOf(5.0) }
    var baseCarbsPer100g by remember { mutableDoubleStateOf(16.0) }
    var selectedMealType by remember { mutableStateOf(initialMealType) }
    var analysisNotes by remember { mutableStateOf("") }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            isAnalyzing = true
            scope.launch {
                val result = geminiService.analyzeMealPhoto(bitmap)
                result.onSuccess { meal ->
                    editableDishName = meal.dishName
                    editableGrams = meal.estimatedGrams
                    val factor = if (meal.estimatedGrams > 0) 100.0 / meal.estimatedGrams else 1.0
                    baseCaloriesPer100g = meal.calories * factor
                    baseProteinPer100g = meal.protein * factor
                    baseFatPer100g = meal.fat * factor
                    baseCarbsPer100g = meal.carbs * factor
                    selectedMealType = meal.mealType
                    analysisNotes = meal.description
                    analysisDone = true
                }.onFailure { error ->
                    Toast.makeText(context, error.message ?: "Лимит ИИ запросов. Введите параметры вручную.", Toast.LENGTH_LONG).show()
                    editableDishName = "Блюдо по фото"
                    editableGrams = 200
                    baseCaloriesPer100g = 150.0
                    baseProteinPer100g = 12.0
                    baseFatPer100g = 5.0
                    baseCarbsPer100g = 15.0
                    analysisNotes = error.message ?: "Превышен лимит запросов к ИИ. Укажите название и макросы вручную."
                    analysisDone = true
                }
                isAnalyzing = false
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Не удалось открыть камеру", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Необходимо разрешение на камеру", Toast.LENGTH_SHORT).show()
        }
    }

    val launchCameraSafely = {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraLauncher.launch(null)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Не удалось открыть камеру", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                selectedBitmap = bitmap
                isAnalyzing = true
                scope.launch {
                    val result = geminiService.analyzeMealPhoto(bitmap)
                    result.onSuccess { meal ->
                        editableDishName = meal.dishName
                        editableGrams = meal.estimatedGrams
                        val factor = if (meal.estimatedGrams > 0) 100.0 / meal.estimatedGrams else 1.0
                        baseCaloriesPer100g = meal.calories * factor
                        baseProteinPer100g = meal.protein * factor
                        baseFatPer100g = meal.fat * factor
                        baseCarbsPer100g = meal.carbs * factor
                        selectedMealType = meal.mealType
                        analysisNotes = meal.description
                        analysisDone = true
                    }.onFailure { error ->
                        Toast.makeText(context, error.message ?: "Лимит ИИ запросов. Введите параметры вручную.", Toast.LENGTH_LONG).show()
                        editableDishName = "Блюдо по фото"
                        editableGrams = 200
                        baseCaloriesPer100g = 150.0
                        baseProteinPer100g = 12.0
                        baseFatPer100g = 5.0
                        baseCarbsPer100g = 15.0
                        analysisNotes = error.message ?: "Превышен лимит запросов к ИИ. Укажите название и макросы вручную."
                        analysisDone = true
                    }
                    isAnalyzing = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Current recalculated values
    val currentCalories = (baseCaloriesPer100g * editableGrams / 100.0).roundToInt()
    val currentProtein = ((baseProteinPer100g * editableGrams / 100.0) * 10).roundToInt() / 10.0
    val currentFat = ((baseFatPer100g * editableGrams / 100.0) * 10).roundToInt() / 10.0
    val currentCarbs = ((baseCarbsPer100g * editableGrams / 100.0) * 10).roundToInt() / 10.0

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(26.dp))
                .border(1.dp, DarkBorder, RoundedCornerShape(26.dp))
                .testTag("ai_food_photo_dialog"),
            color = DarkSurface
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(WildWatermelonContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = WildWatermelon,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ИИ-анализ блюда",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Gemini 3.5 Flash",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Photo preview or Capture Buttons
                if (selectedBitmap == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(DarkCard)
                            .border(1.dp, DarkBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Сфотографируйте еду или выберите фото",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 14.dp)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { launchCameraSafely() },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = WildWatermelon,
                                        contentColor = Color(0xFF101014)
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Камера", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DarkCardElevated,
                                        contentColor = TextPrimary
                                    )
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Галерея")
                                }
                            }
                        }
                    }
                } else {
                    // Display captured image with change button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Фотография блюда",
                            modifier = Modifier.fillMaxWidth().height(190.dp),
                            contentScale = ContentScale.Crop
                        )

                        // Retake button overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkBackground.copy(alpha = 0.8f))
                                .clickable { selectedBitmap = null; analysisDone = false }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = WildWatermelon,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Переснять", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Analyzing indicator
                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCard)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = WildWatermelon,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "ИИ распознаёт блюдо и КБЖУ...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Оценка массы и питательной ценности",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Editable result card
                AnimatedVisibility(
                    visible = analysisDone && !isAnalyzing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(DarkCard)
                            .border(1.dp, WildWatermelon.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Результат распознавания (можно отредактировать):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WildWatermelon
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dish Name input
                        OutlinedTextField(
                            value = editableDishName,
                            onValueChange = { editableDishName = it },
                            label = { Text("Название блюда") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WildWatermelon,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick override chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Омлет с сыром" to Triple(200, 280, Triple(18.0, 20.0, 3.0)),
                                "Салат Цезарь" to Triple(220, 240, Triple(14.0, 16.0, 10.0)),
                                "Паста Карбонара" to Triple(280, 450, Triple(16.0, 18.0, 52.0)),
                                "Пицца Маргарита" to Triple(250, 380, Triple(14.0, 12.0, 50.0)),
                                "Кофе Капучино" to Triple(200, 110, Triple(3.5, 4.0, 12.0)),
                                "Стейк из лосося" to Triple(220, 360, Triple(30.0, 22.0, 1.0))
                            ).forEach { (name, info) ->
                                val grams = info.first
                                val cal = info.second
                                val p = info.third.first
                                val f = info.third.second
                                val c = info.third.third
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkCardElevated)
                                        .border(0.5.dp, WildWatermelon.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            editableDishName = name
                                            editableGrams = grams
                                            baseCaloriesPer100g = (cal.toDouble() / grams) * 100
                                            baseProteinPer100g = (p / grams) * 100
                                            baseFatPer100g = (f / grams) * 100
                                            baseCarbsPer100g = (c / grams) * 100
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(name, fontSize = 11.sp, color = TextPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Portion Grams input
                        OutlinedTextField(
                            value = editableGrams.toString(),
                            onValueChange = { str ->
                                val parsed = str.filter { it.isDigit() }.toIntOrNull() ?: 0
                                editableGrams = parsed.coerceIn(1, 2000)
                            },
                            label = { Text("Масса порции (г)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WildWatermelon,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Meal Type Selector
                        Text(
                            text = "Приём пищи:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                MealType.BREAKFAST to "Завтрак",
                                MealType.LUNCH to "Обед",
                                MealType.DINNER to "Ужин",
                                MealType.SNACK to "Перекус"
                            ).forEach { (mType, label) ->
                                val isSelected = selectedMealType == mType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) WildWatermelon else DarkCardElevated)
                                        .clickable { selectedMealType = mType }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF101014) else TextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Calculated Macros Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MacroChip(title = "Ккал", value = currentCalories.toString(), color = WildWatermelon, modifier = Modifier.weight(1f))
                            MacroChip(title = "Белки", value = "${currentProtein}г", color = ProteinColor, modifier = Modifier.weight(1f))
                            MacroChip(title = "Жиры", value = "${currentFat}г", color = FatColor, modifier = Modifier.weight(1f))
                            MacroChip(title = "Углеводы", value = "${currentCarbs}г", color = CarbsColor, modifier = Modifier.weight(1f))
                        }

                        if (analysisNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 $analysisNotes",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Add to diary button
                        Button(
                            onClick = {
                                if (editableDishName.isNotBlank() && editableGrams > 0) {
                                    onAddMeal(
                                        editableDishName,
                                        editableGrams,
                                        currentCalories,
                                        currentProtein,
                                        currentFat,
                                        currentCarbs,
                                        selectedMealType
                                    )
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WildWatermelon,
                                contentColor = Color(0xFF101014)
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить в дневник", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroChip(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 10.sp, color = TextSecondary)
        }
    }
}
