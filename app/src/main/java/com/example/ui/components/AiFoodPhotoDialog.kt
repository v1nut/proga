package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.model.MealEntry
import com.example.data.model.MealType
import com.example.ui.AiPhotoItemUi
import com.example.ui.AiPhotoState
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import com.example.ui.util.HapticUtil
import kotlin.math.roundToInt

@Composable
fun AiFoodPhotoDialog(
    aiPhotoState: AiPhotoState,
    onRequestCameraTempUri: (Context) -> Uri,
    onProcessPhotoUri: (Context, Uri) -> Unit,
    onUpdateDishName: (String) -> Unit,
    onUpdateItemName: (id: String, name: String) -> Unit = { _, _ -> },
    onUpdateItemGrams: (id: String, text: String) -> Unit = { _, _ -> },
    onUpdateItemCalories: (id: String, text: String) -> Unit = { _, _ -> },
    onUpdateItemProtein: (id: String, text: String) -> Unit = { _, _ -> },
    onUpdateItemFat: (id: String, text: String) -> Unit = { _, _ -> },
    onUpdateItemCarbs: (id: String, text: String) -> Unit = { _, _ -> },
    onRemoveItem: (id: String) -> Unit = {},
    onAddManualItem: () -> Unit = {},
    onUpdateMealType: (MealType) -> Unit,
    onOpenCatalogWithSearch: (String) -> Unit = {},
    currentDateString: String = java.time.LocalDate.now().toString(),
    effectiveTierGoal: com.example.data.model.FitnessGoal = com.example.data.model.FitnessGoal.MAINTENANCE,
    onDismiss: () -> Unit,
    onSaveMealEntries: (List<MealEntry>) -> Unit,
    appLanguage: String = "ru"
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showValidationErrors by remember { mutableStateOf(false) }

    // Camera launcher using High-Res TakePicture contract
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && aiPhotoState.photoUri != null) {
            onProcessPhotoUri(context, aiPhotoState.photoUri)
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = onRequestCameraTempUri(context)
            cameraLauncher.launch(uri)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            onProcessPhotoUri(context, uri)
        }
    }

    val totalGrams = aiPhotoState.items.sumOf { it.gramsText.toDoubleOrNull() ?: 0.0 }
    val totalCalories = aiPhotoState.items.sumOf { it.caloriesText.toDoubleOrNull() ?: 0.0 }
    val totalProtein = aiPhotoState.items.sumOf { it.proteinText.replace(',', '.').toDoubleOrNull() ?: 0.0 }
    val totalFat = aiPhotoState.items.sumOf { it.fatText.replace(',', '.').toDoubleOrNull() ?: 0.0 }
    val totalCarbs = aiPhotoState.items.sumOf { it.carbsText.replace(',', '.').toDoubleOrNull() ?: 0.0 }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp)
                    .testTag("ai_food_photo_dialog")
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
                                .size(36.dp)
                                .clip(CircleShape)
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
                                text = if (appLanguage == "en") "AI Photo Recognition" else "ИИ Распознавание блюда",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (appLanguage == "en") "Take a photo or upload from gallery" else "Сделайте фото или загрузите из галереи",
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

                // Photo Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkCard)
                        .border(1.dp, WildWatermelon.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (aiPhotoState.selectedBitmap != null) {
                        Image(
                            bitmap = aiPhotoState.selectedBitmap.asImageBitmap(),
                            contentDescription = "Фото еды",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = WildWatermelon,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (appLanguage == "en") "Tap buttons below to take or upload a photo" else "Нажмите кнопки ниже, чтобы сделать фото",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Analyzing overlay
                    if (aiPhotoState.isAnalyzing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = WildWatermelon, strokeWidth = 3.dp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (appLanguage == "en") "AI is analyzing dish..." else "ИИ анализирует блюдо...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Camera & Gallery Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                val uri = onRequestCameraTempUri(context)
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (appLanguage == "en") "Take Photo" else "Снять фото", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkCardElevated,
                            contentColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (appLanguage == "en") "Gallery Upload" else "Из галереи", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Error Card
                AnimatedVisibility(
                    visible = aiPhotoState.errorMessage != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A1517))
                            .border(1.dp, Color(0xFFF87171), RoundedCornerShape(12.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = aiPhotoState.errorMessage ?: "Ошибка", fontSize = 12.sp, color = Color(0xFFF87171))
                        }
                    }
                }

                if (aiPhotoState.items.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Section Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (appLanguage == "en") "Recognized Items (${aiPhotoState.items.size}):" else "Распознанные блюда (${aiPhotoState.items.size}):",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        TextButton(
                            onClick = {
                                onDismiss()
                                onOpenCatalogWithSearch(aiPhotoState.dishName)
                            },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (appLanguage == "en") "Search Catalog" else "Найти в каталоге", fontSize = 11.sp, color = WildWatermelon)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of Item Cards
                    aiPhotoState.items.forEach { item ->
                        AiPhotoItemCard(
                            item = item,
                            effectiveTierGoal = effectiveTierGoal,
                            showValidationErrors = showValidationErrors,
                            onUpdateName = { name -> onUpdateItemName(item.id, name) },
                            onUpdateGrams = { grams -> onUpdateItemGrams(item.id, grams) },
                            onUpdateCalories = { cal -> onUpdateItemCalories(item.id, cal) },
                            onUpdateProtein = { p -> onUpdateItemProtein(item.id, p) },
                            onUpdateFat = { f -> onUpdateItemFat(item.id, f) },
                            onUpdateCarbs = { c -> onUpdateItemCarbs(item.id, c) },
                            onRemove = { onRemoveItem(item.id) },
                            appLanguage = appLanguage
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Add Manual Item Button
                    OutlinedButton(
                        onClick = onAddManualItem,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WildWatermelon.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WildWatermelon),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (appLanguage == "en") "+ Add another item" else "+ Добавить ещё блюдо", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Total Summary Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(DarkCard)
                            .border(1.dp, WildWatermelon.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (appLanguage == "en") "Total Meal (${totalGrams.roundToInt()} g):" else "Итого обед (${totalGrams.roundToInt()} г):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                            Text(
                                text = "${totalCalories.roundToInt()} ккал",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = if (appLanguage == "en") "Protein: ${String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", totalProtein)}g" else "Б: ${String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", totalProtein)}г", fontSize = 11.sp, color = ProteinColor, fontWeight = FontWeight.Bold)
                            Text(text = if (appLanguage == "en") "Fat: ${String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", totalFat)}g" else "Ж: ${String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", totalFat)}г", fontSize = 11.sp, color = FatColor, fontWeight = FontWeight.Bold)
                            Text(text = if (appLanguage == "en") "Carbs: ${String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", totalCarbs)}g" else "У: ${String.format(java.util.Locale.forLanguageTag("ru"), "%.1f", totalCarbs)}г", fontSize = 11.sp, color = CarbsColor, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Meal Type Selector
                    Text(if (appLanguage == "en") "Meal Section:" else "Приём пищи:", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MealType.entries.forEach { mType ->
                            val isSelected = aiPhotoState.selectedMealType == mType
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) WildWatermelon else DarkCardElevated)
                                    .clickable { onUpdateMealType(mType) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mType.title(appLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(0xFF101014) else TextPrimary
                                )
                            }
                        }
                    }

                    if (aiPhotoState.analysisNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 ${aiPhotoState.analysisNotes}",
                            fontSize = 11.sp,
                            color = TextSecondary,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showValidationErrors = true
                            val allItemsValid = aiPhotoState.items.isNotEmpty() && aiPhotoState.items.all { item ->
                                val g = item.gramsText.toIntOrNull() ?: 0
                                item.name.isNotBlank() && g in 1..3000
                            }

                            if (allItemsValid) {
                                val entries = aiPhotoState.items.map { item ->
                                    val g = item.gramsText.toDoubleOrNull() ?: 100.0
                                    val cal = item.caloriesText.toDoubleOrNull() ?: (item.baseCalPer100g * g / 100.0)
                                    val p = item.proteinText.replace(',', '.').toDoubleOrNull() ?: (item.basePPer100g * g / 100.0)
                                    val f = item.fatText.replace(',', '.').toDoubleOrNull() ?: (item.baseFPer100g * g / 100.0)
                                    val c = item.carbsText.replace(',', '.').toDoubleOrNull() ?: (item.baseCPer100g * g / 100.0)

                                    MealEntry(
                                        dateString = currentDateString,
                                        mealType = aiPhotoState.selectedMealType.name,
                                        productName = item.name.trim().ifBlank { "Блюдо" },
                                        weightGrams = g,
                                        calories = cal,
                                        protein = p,
                                        fat = f,
                                        carbs = c
                                    )
                                }
                                if (entries.isNotEmpty()) {
                                    HapticUtil.performConfirm(view)
                                    onSaveMealEntries(entries)
                                    onDismiss()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WildWatermelon,
                            contentColor = Color(0xFF101014)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (appLanguage == "en") "Save to Diary" else "Добавить в дневник", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    val allItemsValid = aiPhotoState.items.isNotEmpty() && aiPhotoState.items.all { item ->
                        val g = item.gramsText.toIntOrNull() ?: 0
                        item.name.isNotBlank() && g in 1..3000
                    }

                    if (showValidationErrors && !allItemsValid) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (appLanguage == "en")
                                "Fill in the name and weight from 1 to 3000 g for each item"
                            else
                                "Укажите название и массу от 1 до 3000 г для каждой позиции",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiPhotoItemCard(
    item: AiPhotoItemUi,
    effectiveTierGoal: com.example.data.model.FitnessGoal = com.example.data.model.FitnessGoal.MAINTENANCE,
    showValidationErrors: Boolean,
    onUpdateName: (String) -> Unit,
    onUpdateGrams: (String) -> Unit,
    onUpdateCalories: (String) -> Unit,
    onUpdateProtein: (String) -> Unit,
    onUpdateFat: (String) -> Unit,
    onUpdateCarbs: (String) -> Unit,
    onRemove: () -> Unit,
    appLanguage: String = "ru"
) {
    val gramsVal = item.gramsText.toIntOrNull() ?: 0
    val isGramsValid = gramsVal in 1..3000

    val (tier, _) = remember(item, effectiveTierGoal, appLanguage) {
        com.example.ui.util.FoodTierSystem.evaluateProductTier(
            name = item.name,
            category = "",
            calories = item.baseCalPer100g,
            protein = item.basePPer100g,
            fat = item.baseFPer100g,
            carbs = item.baseCPer100g,
            goal = effectiveTierGoal,
            lang = appLanguage
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            com.example.ui.util.TierSquareBadge(tier = tier, size = 26.dp)
            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = item.name,
                onValueChange = onUpdateName,
                label = { Text(if (appLanguage == "en") "Food Name" else "Название блюда", fontSize = 10.sp) },
                singleLine = true,
                isError = showValidationErrors && item.name.isBlank(),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WildWatermelon,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(36.dp).padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить",
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = item.gramsText,
                onValueChange = onUpdateGrams,
                label = { Text(if (appLanguage == "en") "Weight (g)" else "Масса (г)", fontSize = 10.sp) },
                singleLine = true,
                isError = showValidationErrors && !isGramsValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WildWatermelon,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            OutlinedTextField(
                value = item.caloriesText,
                onValueChange = onUpdateCalories,
                label = { Text(if (appLanguage == "en") "Kcal" else "Калории", fontSize = 10.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WildWatermelon,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = item.proteinText,
                onValueChange = onUpdateProtein,
                label = { Text(if (appLanguage == "en") "Prot" else "Белки", color = ProteinColor, fontSize = 9.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ProteinColor,
                    unfocusedBorderColor = DarkBorder
                )
            )

            OutlinedTextField(
                value = item.fatText,
                onValueChange = onUpdateFat,
                label = { Text(if (appLanguage == "en") "Fat" else "Жиры", color = FatColor, fontSize = 9.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FatColor,
                    unfocusedBorderColor = DarkBorder
                )
            )

            OutlinedTextField(
                value = item.carbsText,
                onValueChange = onUpdateCarbs,
                label = { Text(if (appLanguage == "en") "Carb" else "Углеводы", color = CarbsColor, fontSize = 9.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CarbsColor,
                    unfocusedBorderColor = DarkBorder
                )
            )
        }
    }
}
