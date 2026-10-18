package com.example.ui.screens

import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FitnessGoal
import com.example.data.model.MealType
import com.example.data.model.Product
import com.example.data.model.SavedMeal
import com.example.ui.components.FlatFoodIcon
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WildWatermelon
import com.example.ui.theme.WildWatermelonContainer
import com.example.ui.util.FoodTier
import com.example.ui.util.TierSquareBadge
import kotlin.math.roundToInt

private fun formatProductCount(count: Int, appLanguage: String = "ru"): String {
    if (appLanguage == "en") {
        return "Found: $count ${if (count == 1) "item" else "items"}"
    }
    val rem10 = count % 10
    val rem100 = count % 100
    val word = when {
        rem100 in 11..19 -> "продуктов"
        rem10 == 1 -> "продукт"
        rem10 in 2..4 -> "продукта"
        else -> "продуктов"
    }
    return "Найдено: $count $word"
}

@Composable
fun CatalogScreen(
    products: List<Product>,
    onlineProducts: List<Product> = emptyList(),
    savedMeals: List<SavedMeal> = emptyList(),
    isOnlineSearching: Boolean = false,
    searchQuery: String,
    selectedCategory: String,
    selectedTierFilter: FoodTier? = null,
    effectiveTierGoal: FitnessGoal = FitnessGoal.MAINTENANCE,
    targetMealType: MealType,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSelectTierFilter: (FoodTier?) -> Unit = {},
    onSelectTargetMeal: (MealType) -> Unit = {},
    onToggleFavorite: (Product) -> Unit = {},
    onAddSavedMealClick: (SavedMeal) -> Unit = {},
    onDeleteSavedMealClick: (SavedMeal) -> Unit = {},
    onProductClick: (Product) -> Unit,
    onOpenProductInfo: (Product) -> Unit = {},
    onEditCustomProduct: (Product) -> Unit = {},
    onCreateProductWithQuery: (String) -> Unit = {},
    onOpenAiRecognition: () -> Unit = {},
    onCreateProductClick: () -> Unit,
    onOpenScanner: () -> Unit,
    onDeleteProduct: (Product) -> Unit,
    appLanguage: String = "ru",
    modifier: Modifier = Modifier
) {
    var isMealMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var productToDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    val productToDelete = remember(productToDeleteId, products) {
        if (productToDeleteId != null) products.find { it.id == productToDeleteId } else null
    }

    val categories = if (appLanguage == "en") {
        listOf(
            "All",
            "Favorites",
            "Recent",
            "Scanned",
            "My Foods",
            "Snacks & Desserts",
            "Meat & Poultry",
            "Fish & Seafood",
            "Grains & Sides",
            "Dairy & Eggs",
            "Vegetables & Greens",
            "Fruits & Berries",
            "Nuts & Oils",
            "Bread & Bakery"
        )
    } else {
        listOf(
            "Все",
            "Избранное",
            "Недавние",
            "Сканированное",
            "Мои продукты",
            "Перекусы и десерты",
            "Мясо и птица",
            "Рыба и морепродукты",
            "Крупы и гарниры",
            "Молочные продукты",
            "Овощи и зелень",
            "Фрукты и ягоды",
            "Орехи и масла",
            "Хлеб и выпечка"
        )
    }

    val showSavedMealsSection = (selectedCategory.equals("Все", ignoreCase = true) ||
            selectedCategory.equals("All", ignoreCase = true) ||
            selectedCategory.equals("Мои продукты", ignoreCase = true) ||
            selectedCategory.equals("Свои продукты", ignoreCase = true) ||
            selectedCategory.equals("My Foods", ignoreCase = true)) && savedMeals.isNotEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateProductClick,
                containerColor = WildWatermelon,
                contentColor = Color(0xFF101014),
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("catalog_screen")
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (appLanguage == "en") "Food Catalog" else "Каталог продуктов",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Meal Target Selector Dropdown
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WildWatermelonContainer)
                            .border(1.dp, WildWatermelon.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { isMealMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (appLanguage == "en") "To ${targetMealType.titleEn.lowercase()} ▾" else "В ${targetMealType.titleRu.lowercase()} ▾",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = WildWatermelon
                        )
                    }

                    DropdownMenu(
                        expanded = isMealMenuExpanded,
                        onDismissRequest = { isMealMenuExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        MealType.entries.forEach { meal ->
                            DropdownMenuItem(
                                text = { Text(meal.title(appLanguage), color = TextPrimary, fontSize = 13.sp) },
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

            // Search Input Row with Camera Scanner Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(if (appLanguage == "en") "Search food..." else "Поиск продукта...", color = TextTertiary, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск", tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Очистить", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WildWatermelon,
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
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(WildWatermelonContainer)
                        .border(1.dp, WildWatermelon.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onOpenScanner() }
                        .testTag("catalog_open_scanner_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Сканировать штрихкод",
                        tint = WildWatermelon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Categories Scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory.equals(category, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) WildWatermelon else DarkCard)
                            .border(1.dp, if (isSelected) WildWatermelon else DarkBorder, RoundedCornerShape(12.dp))
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

            Spacer(modifier = Modifier.height(10.dp))

            // Tier Filter Buttons Row (S, A, B, C, D)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAllSelected = selectedTierFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAllSelected) WildWatermelon else DarkCard)
                            .border(1.dp, if (isAllSelected) WildWatermelon else DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectTierFilter(null) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (appLanguage == "en") "All Tiers" else "Все тиры",
                            fontSize = 11.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAllSelected) Color(0xFF101014) else TextSecondary
                        )
                    }
                }

                items(FoodTier.entries) { tier ->
                    val isSelected = selectedTierFilter == tier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) tier.color else DarkCard)
                            .border(1.dp, if (isSelected) tier.color else DarkBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectTierFilter(if (isSelected) null else tier) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${tier.letter}-Tier",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color(0xFF101014) else tier.color
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Add Horizontal Strip
            if (products.isNotEmpty()) {
                val quickProducts = remember(products) { products.take(10) }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickProducts, key = { "quick_${it.id}" }) { prod ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { onProductClick(prod) }
                                .width(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DarkCard)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                FlatFoodIcon(productName = prod.name, category = prod.category, size = 36.dp)

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(WildWatermelon),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Добавить",
                                        tint = Color(0xFF101014),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = prod.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Products Count Pluralization
            Text(
                text = formatProductCount(products.size, appLanguage),
                fontSize = 12.sp,
                color = TextTertiary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Empty State Handling
            if (products.isEmpty() && onlineProducts.isEmpty() && !isOnlineSearching && !showSavedMealsSection) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = if (appLanguage == "en") "No food found" else "Продукты не найдены",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                if (appLanguage == "en") "No results for \"$searchQuery\"" else "По запросу «$searchQuery» ничего не найдено"
                            } else {
                                if (appLanguage == "en") "No items in this category yet" else "В этой категории пока нет продуктов"
                            },
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (searchQuery.isNotBlank()) {
                            Button(
                                onClick = { onCreateProductWithQuery(searchQuery.trim()) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WildWatermelon,
                                    contentColor = Color(0xFF101014)
                                ),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (appLanguage == "en") "Create \"${searchQuery.trim()}\"" else "Создать «${searchQuery.trim()}»", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onOpenAiRecognition,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = WildWatermelon),
                                border = androidx.compose.foundation.BorderStroke(1.dp, WildWatermelon.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (appLanguage == "en") "AI Recognition" else "Распознать через ИИ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 110.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Saved Recipes Section
                    if (showSavedMealsSection) {
                        item {
                            Text(
                                text = if (appLanguage == "en") "My Saved Recipes (${savedMeals.size}):" else "Мои сохранённые блюда (рецепты) (${savedMeals.size}):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(savedMeals, key = { "saved_${it.id}" }) { savedMeal ->
                            SavedMealCard(
                                savedMeal = savedMeal,
                                targetMealType = targetMealType,
                                onAddClick = { onAddSavedMealClick(savedMeal) },
                                onDeleteClick = { onDeleteSavedMealClick(savedMeal) },
                                appLanguage = appLanguage
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (appLanguage == "en") "Catalog Products:" else "Продукты каталога:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    // Local Products List
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            effectiveTierGoal = effectiveTierGoal,
                            onClick = { onProductClick(product) },
                            onInfoClick = { onOpenProductInfo(product) },
                            onToggleFavorite = { onToggleFavorite(product) },
                            onEdit = { onEditCustomProduct(product) },
                            onDelete = { productToDeleteId = product.id },
                            appLanguage = appLanguage
                        )
                    }

                    // Online Open Food Facts Results
                    if (isOnlineSearching) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = WildWatermelon, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (appLanguage == "en") "Searching Open Food Facts..." else "Поиск в базе Open Food Facts...", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    } else if (onlineProducts.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (appLanguage == "en") "From Open Food Facts (${onlineProducts.size}):" else "Из Open Food Facts (${onlineProducts.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = WildWatermelon
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(onlineProducts) { onlineProd ->
                            ProductCard(
                                product = onlineProd,
                                effectiveTierGoal = effectiveTierGoal,
                                onClick = { onProductClick(onlineProd) },
                                onInfoClick = { onOpenProductInfo(onlineProd) },
                                onToggleFavorite = { onToggleFavorite(onlineProd) },
                                onDelete = {},
                                appLanguage = appLanguage
                            )
                        }
                    }
                }
            }
        }
    }

    productToDelete?.let { prod ->
        AlertDialog(
            onDismissRequest = { productToDeleteId = null },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp),
            title = { Text(if (appLanguage == "en") "Delete Product" else "Удаление продукта", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary) },
            text = {
                Text(
                    text = if (appLanguage == "en") "Delete \"${prod.name}\" from catalog?" else "Удалить продукт «${prod.name}» из каталога?",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(prod)
                        productToDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (appLanguage == "en") "Delete" else "Удалить", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDeleteId = null }) {
                    Text(if (appLanguage == "en") "Cancel" else "Отмена", color = TextTertiary)
                }
            }
        )
    }
}

@Composable
private fun SavedMealCard(
    savedMeal: SavedMeal,
    targetMealType: MealType,
    onAddClick: () -> Unit,
    onDeleteClick: () -> Unit,
    appLanguage: String = "ru"
) {
    val items = remember(savedMeal.itemsJson) {
        try {
            val jsonArray = org.json.JSONArray(savedMeal.itemsJson)
            val list = mutableListOf<com.example.data.model.SavedMealItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    com.example.data.model.SavedMealItem(
                        productName = obj.optString("productName", "Ингредиент"),
                        weightGrams = obj.optDouble("weightGrams", 100.0),
                        calories = obj.optDouble("calories", 0.0),
                        protein = obj.optDouble("protein", 0.0),
                        fat = obj.optDouble("fat", 0.0),
                        carbs = obj.optDouble("carbs", 0.0)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    val totalCalories = remember(items) { items.sumOf { it.calories } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(1.dp, WildWatermelon.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = savedMeal.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (appLanguage == "en")
                    "${totalCalories.roundToInt()} kcal • ${items.size} items"
                else
                    "${totalCalories.roundToInt()} ккал • ${items.size} ингр.",
                fontSize = 11.sp,
                color = TextTertiary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WildWatermelonContainer,
                    contentColor = WildWatermelon
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (appLanguage == "en") "Add to ${targetMealType.titleEn}" else "Добавить в ${targetMealType.titleRu.lowercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp).padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Удалить рецепт",
                    tint = Color(0xFFF87171),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    effectiveTierGoal: FitnessGoal = FitnessGoal.MAINTENANCE,
    onClick: () -> Unit,
    onInfoClick: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    appLanguage: String = "ru"
) {
    var isMenuOpen by remember { mutableStateOf(false) }
    val isScanned = product.category.equals("Сканированное", ignoreCase = true) || product.category.equals("Импорт (Open Food Facts)", ignoreCase = true) || (!product.barcode.isNullOrBlank() && !product.isCustom)
    val isCustomProduct = product.isCustom && !isScanned

    val (tier, reason) = remember(product, effectiveTierGoal, appLanguage) {
        com.example.ui.util.FoodTierSystem.evaluateProductTier(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            FlatFoodIcon(
                productName = product.name,
                category = product.category,
                size = 42.dp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TierSquareBadge(tier = tier, size = 22.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isCustomProduct || isScanned) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(WildWatermelonContainer)
                            .border(0.5.dp, WildWatermelon.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (appLanguage == "en") (if (isScanned) "Scanned" else "Custom") else (if (isScanned) "Сканирован" else "Свой"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = WildWatermelon
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = reason,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = tier.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val ruLocale = java.util.Locale.forLanguageTag("ru")
            val pStr = String.format(ruLocale, "%.1f", product.protein)
            val fStr = String.format(ruLocale, "%.1f", product.fat)
            val cStr = String.format(ruLocale, "%.1f", product.carbs)
            val macroLine = if (appLanguage == "en")
                "🔥 ${product.calories.roundToInt()} kcal • P: ${pStr}g • F: ${fStr}g • C: ${cStr}g"
            else
                "🔥 ${product.calories.roundToInt()} ккал • Б: ${pStr}г • Ж: ${fStr}г • У: ${cStr}г"

            Text(
                text = macroLine,
                fontSize = 10.5.sp,
                color = TextTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (product.id > 0L) onToggleFavorite() },
                enabled = product.id > 0L,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (product.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Избранное",
                    tint = if (product.id <= 0L) TextSecondary.copy(alpha = 0.3f) else if (product.isFavorite) WildWatermelon else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Справка о продукте",
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WildWatermelonContainer)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Добавить",
                    tint = WildWatermelon,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (isCustomProduct) {
                Box {
                    IconButton(
                        onClick = { isMenuOpen = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Меню",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isMenuOpen,
                        onDismissRequest = { isMenuOpen = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (appLanguage == "en") "Edit" else "Изменить", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = WildWatermelon, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                isMenuOpen = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (appLanguage == "en") "Delete" else "Удалить", color = Color(0xFFF87171), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(16.dp)) },
                            onClick = {
                                isMenuOpen = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
