package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MealType
import com.example.data.model.Product
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
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CatalogScreen(
    products: List<Product>,
    searchQuery: String,
    selectedCategory: String,
    targetMealType: MealType,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSelectTargetMeal: (MealType) -> Unit = {},
    onProductClick: (Product) -> Unit,
    onCreateProductClick: () -> Unit,
    onOpenScanner: () -> Unit,
    onDeleteProduct: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMealMenuExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        "Все",
        "Мои продукты",
        "Мясо и птица",
        "Рыба и морепродукты",
        "Крупы и гарниры",
        "Молочные продукты",
        "Овощи и зелень",
        "Фрукты и ягоды",
        "Орехи и масла",
        "Хлеб и выпечка"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateProductClick,
                containerColor = WildWatermelon,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(52.dp)
                    .testTag("fab_create_product")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Свой продукт",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            // Screen Header & Target Meal Indicator Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Каталог продуктов",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkCard)
                            .border(1.dp, WildWatermelon.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { isMealMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "в ${targetMealType.titleRu.lowercase()} ▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WildWatermelon,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    DropdownMenu(
                        expanded = isMealMenuExpanded,
                        onDismissRequest = { isMealMenuExpanded = false },
                        modifier = Modifier.background(DarkCardElevated)
                    ) {
                        MealType.entries.forEach { meal ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = meal.titleRu,
                                        color = if (meal == targetMealType) WildWatermelon else TextPrimary,
                                        fontWeight = if (meal == targetMealType) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    onSelectTargetMeal(meal)
                                    isMealMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar & Scanner Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Поиск продукта...", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ElectricLime
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Очистить",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricLime,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("catalog_search_input")
                )

                // Barcode Scanner Button
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCard)
                        .border(1.dp, ElectricLime.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onOpenScanner() }
                        .testTag("open_barcode_scanner_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Сканировать штрих-код",
                        tint = ElectricLime,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricLime else DarkCard)
                            .clickable { onCategoryChange(category) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF101014) else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Products Count
            Text(
                text = "Найдено: ${products.size} продуктов",
                fontSize = 12.sp,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Product List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Продукты не найдены",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Создайте свой продукт кнопкой внизу справа",
                            fontSize = 13.sp,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            onDelete = { onDeleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (product.isCustom) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(WildWatermelonContainer)
                                .border(0.5.dp, WildWatermelon.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Свой",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = "${product.category} • на 100 г"
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${product.calories.roundToInt()} ккал",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricLime
                )
                if (product.isCustom) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Удалить свой продукт",
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Macros Breakdown Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroMiniChip(label = "Б", grams = product.protein, color = ProteinColor)
            MacroMiniChip(label = "Ж", grams = product.fat, color = FatColor)
            MacroMiniChip(label = "У", grams = product.carbs, color = CarbsColor)
        }
    }
}

@Composable
private fun MacroMiniChip(
    label: String,
    grams: Double,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCardElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = String.format(Locale.US, "%s: %.1fг", label, grams),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}
