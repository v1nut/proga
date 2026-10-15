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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealType
import com.example.data.service.GeminiFoodService
import com.example.data.service.RecognizedMeal
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Mic
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun QuickAiMealBar(
    onAddMeal: (dishName: String, grams: Int, calories: Int, protein: Double, fat: Double, carbs: Double, mealType: MealType) -> Unit,
    onOpenCameraPhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var lastAddedMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val geminiService = remember { GeminiFoodService() }
    val focusManager = LocalFocusManager.current

    fun executeAddMeal(query: String) {
        if (query.isNotBlank() && !isLoading) {
            isLoading = true
            focusManager.clearFocus()
            scope.launch {
                val result = geminiService.parseQuickMealText(query)
                result.onSuccess { meal ->
                    if (meal.items.isNotEmpty()) {
                        meal.items.forEach { item ->
                            onAddMeal(
                                item.name,
                                item.grams,
                                item.calories,
                                item.protein,
                                item.fat,
                                item.carbs,
                                meal.mealType
                            )
                        }
                    } else {
                        onAddMeal(
                            meal.dishName,
                            meal.estimatedGrams,
                            meal.calories,
                            meal.protein,
                            meal.fat,
                            meal.carbs,
                            meal.mealType
                        )
                    }
                    val mealNameRu = when (meal.mealType) {
                        MealType.BREAKFAST -> "Завтрак"
                        MealType.LUNCH -> "Обед"
                        MealType.DINNER -> "Ужин"
                        MealType.SNACK -> "Перекус"
                    }
                    val summary = if (meal.items.size > 1) {
                        meal.items.joinToString(", ") { "${it.name} (${it.grams}г)" }
                    } else {
                        "${meal.dishName} (${meal.estimatedGrams}г)"
                    }
                    lastAddedMessage = "Добавлено в $mealNameRu: $summary • ${meal.calories} ккал"
                    textInput = ""
                }
                isLoading = false
            }
        }
    }

    val submitText: () -> Unit = {
        executeAddMeal(textInput.trim())
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                textInput = spokenText
                executeAddMeal(spokenText.trim())
            }
        }
    }

    val launchVoiceInput: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите, что вы съели (например: «2 яйца и кофе»)")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Используйте голосовой ввод на клавиатуре или напечатайте текст", Toast.LENGTH_LONG).show()
        }
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
                        text = "ИИ Экспресс-запись",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WildWatermelon,
                        maxLines = 1
                    )
                    Text(
                        text = "Напишите, что съели, или сделайте фото",
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
                // Voice input button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkCardElevated)
                        .clickable { launchVoiceInput() }
                        .testTag("voice_input_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Голосовой ввод",
                        tint = WildWatermelon,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Camera photo button
                Box(
                    modifier = Modifier
                        .size(36.dp)
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
                        modifier = Modifier.size(18.dp)
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
                        text = "например: 2 яйца и кофе 200мл",
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

            // Submit / Loading button
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

        // Confirmation snackbar message
        AnimatedVisibility(
            visible = lastAddedMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(WildWatermelonContainer)
                    .border(0.5.dp, WildWatermelon.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lastAddedMessage ?: "",
                    fontSize = 11.sp,
                    color = WildWatermelon,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { lastAddedMessage = null },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = WildWatermelon,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
