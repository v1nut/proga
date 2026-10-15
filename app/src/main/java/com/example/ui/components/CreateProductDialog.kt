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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CarbsColor
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.FatColor
import com.example.ui.theme.ProteinColor
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CreateProductDialog(
    initialBarcode: String? = null,
    onDismiss: () -> Unit,
    onSaveProduct: (name: String, category: String, calories: Double, protein: Double, fat: Double, carbs: Double, barcode: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var barcodeText by remember { mutableStateOf(initialBarcode ?: "") }
    var category by remember { mutableStateOf("Мои продукты") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf(
        "Мои продукты",
        "Мясо и птица",
        "Рыба и морепродукты",
        "Крупы и гарниры",
        "Молочные продукты",
        "Овощи и зелень",
        "Фрукты и ягоды",
        "Орехи и масла",
        "Хлеб и выпечка",
        "Перекусы и десерты"
    )

    // Calculate approximate kcal from macros
    val pVal = proteinText.toDoubleOrNull() ?: 0.0
    val fVal = fatText.toDoubleOrNull() ?: 0.0
    val cVal = carbsText.toDoubleOrNull() ?: 0.0
    val estimatedKcal = (pVal * 4.0) + (fVal * 9.0) + (cVal * 4.0)

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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Новый продукт",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_create_product")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Укажите пищевую ценность на 100 г продукта",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Product Name Field
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Название продукта *") },
                    placeholder = { Text("Например: Домашние сырники") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_name_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WildWatermelon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedLabelColor = WildWatermelon,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Barcode Field (Optional)
                OutlinedTextField(
                    value = barcodeText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) barcodeText = input
                    },
                    label = { Text("Штрих-код (EAN / UPC)") },
                    placeholder = { Text("Например: 4601234567890") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_product_barcode_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WildWatermelon,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard,
                        focusedLabelColor = WildWatermelon,
                        unfocusedLabelColor = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Категория:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) WildWatermelon else DarkCard)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF101014) else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Macro Inputs: Protein, Fat, Carbs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NutrientInputField(
                        label = "Белки",
                        value = proteinText,
                        onValueChange = { proteinText = it },
                        accentColor = ProteinColor,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInputField(
                        label = "Жиры",
                        value = fatText,
                        onValueChange = { fatText = it },
                        accentColor = FatColor,
                        modifier = Modifier.weight(1f)
                    )
                    NutrientInputField(
                        label = "Углеводы",
                        value = carbsText,
                        onValueChange = { carbsText = it },
                        accentColor = CarbsColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calories Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = caloriesText,
                        onValueChange = { caloriesText = it },
                        label = { Text("Калории (ккал) *") },
                        placeholder = { Text("0") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("new_product_calories_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WildWatermelon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard,
                            focusedLabelColor = WildWatermelon,
                            unfocusedLabelColor = TextSecondary
                        )
                    )

                    if (estimatedKcal > 0) {
                        OutlinedButton(
                            onClick = { caloriesText = estimatedKcal.roundToInt().toString() },
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, WildWatermelon),
                            modifier = Modifier.height(54.dp)
                        ) {
                            Text(
                                text = "Авто: ${estimatedKcal.roundToInt()}",
                                fontSize = 12.sp,
                                color = WildWatermelon,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFF87171),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        if (name.isBlank()) {
                            errorMessage = "Пожалуйста, введите название продукта"
                            return@Button
                        }
                        val cal = caloriesText.toDoubleOrNull() ?: estimatedKcal
                        if (cal <= 0 && estimatedKcal <= 0) {
                            errorMessage = "Укажите калорийность или БЖУ продукта"
                            return@Button
                        }
                        onSaveProduct(
                            name.trim(),
                            category,
                            cal,
                            pVal,
                            fVal,
                            cVal,
                            barcodeText.trim().ifEmpty { null }
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WildWatermelon,
                        contentColor = Color(0xFF101014)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_custom_product_button")
                ) {
                    Text(
                        text = "Сохранить продукт",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun NutrientInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                onValueChange(input)
            }
        },
        label = {
            Text(
                text = label,
                color = accentColor,
                fontSize = 12.sp
            )
        },
        placeholder = { Text("0") },
        modifier = modifier,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = DarkCard,
            unfocusedContainerColor = DarkCard
        )
    )
}
