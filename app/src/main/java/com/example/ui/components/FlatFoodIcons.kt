package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.MealType

private enum class FoodIconType {
    AVOCADO, APPLE, BANANA, WATERMELON, ORANGE_CITRUS, BERRIES, TOMATO,
    EGG, CHEESECAKE_SWEETS, EGGPLANT, BEETROOT, PASTA_GRAINS, BURGER_FASTFOOD,
    MEAT_POULTRY, FISH_SEAFOOD, MILK_DAIRY, CHEESE, NUTS_OILS, BREAD,
    COFFEE_TEA, WATER, DEFAULT_FOOD
}

private fun detectFoodIconType(productName: String, category: String): FoodIconType {
    val nameLower = productName.lowercase().trim()

    // 1. Evaluate Product Name FIRST (highest precision)
    when {
        nameLower.contains("авокадо") || nameLower.contains("avocado") -> return FoodIconType.AVOCADO
        nameLower.contains("арбуз") || nameLower.contains("watermelon") -> return FoodIconType.WATERMELON
        nameLower.contains("банан") || nameLower.contains("banana") -> return FoodIconType.BANANA
        nameLower.contains("апельсин") || nameLower.contains("лимон") || nameLower.contains("грейпфрут") || nameLower.contains("мандарин") || nameLower.contains("orange") || nameLower.contains("citrus") -> return FoodIconType.ORANGE_CITRUS
        nameLower.contains("яблок") || nameLower.contains("apple") -> return FoodIconType.APPLE
        nameLower.contains("вишн") || nameLower.contains("черешн") || nameLower.contains("клубник") || nameLower.contains("черник") || nameLower.contains("голубик") || nameLower.contains("малин") || nameLower.contains("ягод") || nameLower.contains("berry") || nameLower.contains("cherry") -> return FoodIconType.BERRIES
        nameLower.contains("помидор") || nameLower.contains("томат") || nameLower.contains("tomato") -> return FoodIconType.TOMATO
        nameLower.contains("яйц") || nameLower.contains("омлет") || nameLower.contains("белок") || nameLower.contains("egg") -> return FoodIconType.EGG
        nameLower.contains("молок") || nameLower.contains("творог") || nameLower.contains("кефир") || nameLower.contains("йогурт") || nameLower.contains("сметан") || nameLower.contains("ряженк") || nameLower.contains("milk") || nameLower.contains("yogurt") -> return FoodIconType.MILK_DAIRY
        Regex("""\bсыр\b|\bсыра\b|\bсыром\b|\bсыры\b|\bcheese\b""", RegexOption.IGNORE_CASE).containsMatchIn(nameLower) || nameLower.contains("моцарелл") || nameLower.contains("ламбер") -> return FoodIconType.CHEESE
        nameLower.contains("орех") || nameLower.contains("миндал") || nameLower.contains("арахис") || nameLower.contains("масл") || nameLower.contains("nut") || nameLower.contains("oil") -> return FoodIconType.NUTS_OILS
        nameLower.contains("макарон") || nameLower.contains("паст") || nameLower.contains("спагетти") || nameLower.contains("гречк") || nameLower.contains("гречн") || nameLower.contains("рис") || nameLower.contains("овсян") || nameLower.contains("геркулес") || nameLower.contains("круп") || nameLower.contains("pasta") -> return FoodIconType.PASTA_GRAINS
        nameLower.contains("кури") || nameLower.contains("индейк") || nameLower.contains("говяд") || nameLower.contains("мяс") || nameLower.contains("котлет") || nameLower.contains("филе") || nameLower.contains("цыплен") || nameLower.contains("meat") -> return FoodIconType.MEAT_POULTRY
        nameLower.contains("рыб") || nameLower.contains("лосос") || nameLower.contains("сёмг") || nameLower.contains("тунец") || nameLower.contains("минтай") || nameLower.contains("креветк") || nameLower.contains("fish") -> return FoodIconType.FISH_SEAFOOD
        nameLower.contains("бургер") || nameLower.contains("шаурм") || nameLower.contains("шаверм") || nameLower.contains("пицц") || nameLower.contains("burger") || nameLower.contains("pizza") -> return FoodIconType.BURGER_FASTFOOD
        nameLower.contains("хлеб") || nameLower.contains("лаваш") || nameLower.contains("батон") || nameLower.contains("хлебц") || nameLower.contains("bread") -> return FoodIconType.BREAD
        nameLower.contains("чизкейк") || nameLower.contains("торт") || nameLower.contains("пирог") || nameLower.contains("сырник") || nameLower.contains("конфет") || nameLower.contains("шоколад") || nameLower.contains("cake") || nameLower.contains("десерт") -> return FoodIconType.CHEESECAKE_SWEETS
        nameLower.contains("баклажан") || nameLower.contains("eggplant") -> return FoodIconType.EGGPLANT
        nameLower.contains("свекл") || nameLower.contains("свёкл") || nameLower.contains("beet") -> return FoodIconType.BEETROOT
        nameLower.contains("кофе") || nameLower.contains("чай") || nameLower.contains("coffee") || nameLower.contains("tea") -> return FoodIconType.COFFEE_TEA
        nameLower.contains("вод") || nameLower.contains("water") -> return FoodIconType.WATER
    }

    // 2. Fallback to Category if Name did not match
    val catLower = category.lowercase().trim()
    return when {
        catLower.contains("молочн") || catLower.contains("dairy") -> FoodIconType.MILK_DAIRY
        catLower.contains("мясо") || catLower.contains("meat") -> FoodIconType.MEAT_POULTRY
        catLower.contains("рыба") || catLower.contains("fish") -> FoodIconType.FISH_SEAFOOD
        catLower.contains("круп") || catLower.contains("grains") -> FoodIconType.PASTA_GRAINS
        catLower.contains("овощ") || catLower.contains("veg") -> FoodIconType.TOMATO
        catLower.contains("фрукт") || catLower.contains("fruit") -> FoodIconType.APPLE
        catLower.contains("ягод") || catLower.contains("berry") -> FoodIconType.BERRIES
        catLower.contains("орех") || catLower.contains("nut") || catLower.contains("oil") -> FoodIconType.NUTS_OILS
        catLower.contains("хлеб") || catLower.contains("bread") -> FoodIconType.BREAD
        catLower.contains("перекус") || catLower.contains("десерт") || catLower.contains("snack") -> FoodIconType.CHEESECAKE_SWEETS
        else -> FoodIconType.DEFAULT_FOOD
    }
}

@Composable
fun FlatFoodIcon(
    productName: String,
    category: String = "",
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val iconType = detectFoodIconType(productName, category)

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (iconType) {
            FoodIconType.AVOCADO -> drawAvocado(w, h)
            FoodIconType.APPLE -> drawApple(w, h)
            FoodIconType.BANANA -> drawBanana(w, h)
            FoodIconType.WATERMELON -> drawWatermelon(w, h)
            FoodIconType.ORANGE_CITRUS -> drawOrangeCitrus(w, h)
            FoodIconType.BERRIES -> drawCherries(w, h)
            FoodIconType.TOMATO -> drawTomato(w, h)
            FoodIconType.EGG -> drawEggs(w, h)
            FoodIconType.CHEESECAKE_SWEETS -> drawCheesecake(w, h)
            FoodIconType.EGGPLANT -> drawEggplant(w, h)
            FoodIconType.BEETROOT -> drawBeetroot(w, h)
            FoodIconType.PASTA_GRAINS -> drawPasta(w, h)
            FoodIconType.BURGER_FASTFOOD -> drawBurger(w, h)
            FoodIconType.MEAT_POULTRY -> drawMeat(w, h)
            FoodIconType.FISH_SEAFOOD -> drawFish(w, h)
            FoodIconType.MILK_DAIRY -> drawMilk(w, h)
            FoodIconType.CHEESE -> drawCheese(w, h)
            FoodIconType.NUTS_OILS -> drawNutsOil(w, h)
            FoodIconType.BREAD -> drawBread(w, h)
            FoodIconType.COFFEE_TEA -> drawCoffeeTea(w, h)
            FoodIconType.WATER -> drawWater(w, h)
            FoodIconType.DEFAULT_FOOD -> drawDefaultFood(w, h)
        }
    }
}

@Composable
fun FlatMealTypeIcon(
    mealType: MealType,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (mealType) {
            MealType.BREAKFAST -> drawBreakfastMeal(w, h)
            MealType.LUNCH -> drawLunchMeal(w, h)
            MealType.DINNER -> drawDinnerMeal(w, h)
            MealType.SNACK -> drawSnackMeal(w, h)
        }
    }
}

// ==========================================
// FOOD DRAWING SCOPES
// ==========================================

private fun DrawScope.drawAvocado(w: Float, h: Float) {
    // Outer Dark Skin
    drawOval(
        color = Color(0xFF2D6A4F),
        topLeft = Offset(w * 0.20f, h * 0.15f),
        size = Size(w * 0.60f, h * 0.75f)
    )
    // Inner Light Pulp
    drawOval(
        color = Color(0xFFB7E4C7),
        topLeft = Offset(w * 0.26f, h * 0.22f),
        size = Size(w * 0.48f, h * 0.62f)
    )
    // Seed
    drawCircle(
        color = Color(0xFF8D5B4C),
        radius = w * 0.14f,
        center = Offset(w * 0.50f, h * 0.62f)
    )
}

private fun DrawScope.drawApple(w: Float, h: Float) {
    // Red Apple Body
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.36f, center = Offset(w * 0.50f, h * 0.56f))

    // Green Leaf
    val leafPath = Path().apply {
        moveTo(w * 0.50f, h * 0.22f)
        quadraticTo(w * 0.68f, h * 0.12f, w * 0.72f, h * 0.28f)
        quadraticTo(w * 0.54f, h * 0.32f, w * 0.50f, h * 0.22f)
        close()
    }
    drawPath(leafPath, color = Color(0xFF34C759))

    // Stem
    drawLine(
        color = Color(0xFF8D5B4C),
        start = Offset(w * 0.50f, h * 0.22f),
        end = Offset(w * 0.50f, h * 0.14f),
        strokeWidth = w * 0.05f
    )
}

private fun DrawScope.drawBanana(w: Float, h: Float) {
    // Curved Yellow Banana
    val bananaPath = Path().apply {
        moveTo(w * 0.20f, h * 0.30f)
        quadraticTo(w * 0.50f, h * 0.88f, w * 0.82f, h * 0.60f)
        quadraticTo(w * 0.50f, h * 0.70f, w * 0.25f, h * 0.26f)
        close()
    }
    drawPath(bananaPath, color = Color(0xFFFFCC00))

    // Stem tip
    drawCircle(color = Color(0xFF8D5B4C), radius = w * 0.04f, center = Offset(w * 0.22f, h * 0.28f))
}

private fun DrawScope.drawWatermelon(w: Float, h: Float) {
    // Rind
    drawCircle(color = Color(0xFF34C759), radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.52f))
    // Flesh
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.32f, center = Offset(w * 0.50f, h * 0.52f))

    // Seeds
    drawCircle(color = Color(0xFF101014), radius = w * 0.03f, center = Offset(w * 0.42f, h * 0.45f))
    drawCircle(color = Color(0xFF101014), radius = w * 0.03f, center = Offset(w * 0.58f, h * 0.45f))
    drawCircle(color = Color(0xFF101014), radius = w * 0.03f, center = Offset(w * 0.50f, h * 0.60f))
}

private fun DrawScope.drawOrangeCitrus(w: Float, h: Float) {
    // Orange Circle
    drawCircle(color = Color(0xFFFF9500), radius = w * 0.38f, center = Offset(w * 0.50f, h * 0.52f))
    drawCircle(color = Color(0xFFFFCC00), radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.52f))

    // Segment Radial Lines
    for (angle in listOf(0f, 60f, 120f)) {
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = Offset(w * 0.50f - w * 0.25f * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(), h * 0.52f - w * 0.25f * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()),
            end = Offset(w * 0.50f + w * 0.25f * kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat(), h * 0.52f + w * 0.25f * kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat()),
            strokeWidth = w * 0.03f
        )
    }
}

private fun DrawScope.drawTomato(w: Float, h: Float) {
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.55f))
    drawCircle(color = Color.White.copy(alpha = 0.35f), radius = w * 0.08f, center = Offset(w * 0.36f, h * 0.42f))

    val leafPath = Path().apply {
        moveTo(w * 0.5f, h * 0.18f)
        lineTo(w * 0.56f, h * 0.28f)
        lineTo(w * 0.68f, h * 0.22f)
        lineTo(w * 0.58f, h * 0.32f)
        lineTo(w * 0.65f, h * 0.42f)
        lineTo(w * 0.5f, h * 0.34f)
        lineTo(w * 0.35f, h * 0.42f)
        lineTo(w * 0.42f, h * 0.32f)
        lineTo(w * 0.32f, h * 0.22f)
        lineTo(w * 0.44f, h * 0.28f)
        close()
    }
    drawPath(leafPath, color = Color(0xFF34C759))
}

private fun DrawScope.drawEggs(w: Float, h: Float) {
    drawOval(color = Color(0xFFD4AF83), topLeft = Offset(w * 0.18f, h * 0.25f), size = Size(w * 0.48f, h * 0.60f))
    drawOval(color = Color(0xFFE8C59B), topLeft = Offset(w * 0.38f, h * 0.30f), size = Size(w * 0.48f, h * 0.60f))
    drawCircle(color = Color.White.copy(alpha = 0.4f), radius = w * 0.06f, center = Offset(w * 0.52f, h * 0.45f))
}

private fun DrawScope.drawCheesecake(w: Float, h: Float) {
    val crustPath = Path().apply {
        moveTo(w * 0.12f, h * 0.72f)
        lineTo(w * 0.88f, h * 0.72f)
        lineTo(w * 0.88f, h * 0.82f)
        lineTo(w * 0.12f, h * 0.82f)
        close()
    }
    drawPath(crustPath, color = Color(0xFFE09F3E))

    val bodyPath = Path().apply {
        moveTo(w * 0.15f, h * 0.72f)
        lineTo(w * 0.85f, h * 0.72f)
        lineTo(w * 0.50f, h * 0.25f)
        close()
    }
    drawPath(bodyPath, color = Color(0xFFFFF3B0))
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.12f, center = Offset(w * 0.50f, h * 0.25f))
}

private fun DrawScope.drawEggplant(w: Float, h: Float) {
    val bodyPath = Path().apply {
        moveTo(w * 0.30f, h * 0.35f)
        cubicTo(w * 0.10f, h * 0.65f, w * 0.40f, h * 0.90f, w * 0.70f, h * 0.75f)
        cubicTo(w * 0.90f, h * 0.60f, w * 0.75f, h * 0.35f, w * 0.45f, h * 0.30f)
        close()
    }
    drawPath(bodyPath, color = Color(0xFF7B2CBF))

    val capPath = Path().apply {
        moveTo(w * 0.25f, h * 0.20f)
        lineTo(w * 0.40f, h * 0.35f)
        lineTo(w * 0.20f, h * 0.35f)
        close()
    }
    drawPath(capPath, color = Color(0xFF52B788))
}

private fun DrawScope.drawBeetroot(w: Float, h: Float) {
    drawCircle(color = Color(0xFFD90429), radius = w * 0.32f, center = Offset(w * 0.5f, h * 0.58f))
    val tailPath = Path().apply {
        moveTo(w * 0.46f, h * 0.85f)
        lineTo(w * 0.50f, h * 0.96f)
        lineTo(w * 0.54f, h * 0.85f)
        close()
    }
    drawPath(tailPath, color = Color(0xFFD90429))

    val leafPath = Path().apply {
        moveTo(w * 0.5f, h * 0.32f)
        lineTo(w * 0.35f, h * 0.12f)
        lineTo(w * 0.50f, h * 0.20f)
        lineTo(w * 0.65f, h * 0.12f)
        close()
    }
    drawPath(leafPath, color = Color(0xFF80B918))
}

private fun DrawScope.drawCherries(w: Float, h: Float) {
    val stem1 = Path().apply {
        moveTo(w * 0.32f, h * 0.60f)
        quadraticTo(w * 0.40f, h * 0.30f, w * 0.50f, h * 0.20f)
    }
    val stem2 = Path().apply {
        moveTo(w * 0.68f, h * 0.60f)
        quadraticTo(w * 0.60f, h * 0.30f, w * 0.50f, h * 0.20f)
    }
    drawPath(stem1, color = Color(0xFF34C759), style = Stroke(width = w * 0.06f))
    drawPath(stem2, color = Color(0xFF34C759), style = Stroke(width = w * 0.06f))

    drawCircle(color = Color(0xFFFF2D55), radius = w * 0.20f, center = Offset(w * 0.32f, h * 0.68f))
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.20f, center = Offset(w * 0.68f, h * 0.68f))
}

private fun DrawScope.drawPasta(w: Float, h: Float) {
    drawCircle(color = Color(0xFF2C2C2E), radius = w * 0.42f, center = Offset(w * 0.5f, h * 0.55f))
    drawCircle(color = Color(0xFF3A3A3C), radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.55f))
    drawCircle(color = Color(0xFFFFCC00), radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.55f))
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.14f, center = Offset(w * 0.5f, h * 0.55f))
}

private fun DrawScope.drawBurger(w: Float, h: Float) {
    drawRoundRect(color = Color(0xFFC78B38), topLeft = Offset(w * 0.18f, h * 0.72f), size = Size(w * 0.64f, h * 0.12f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFF8B5A2B), topLeft = Offset(w * 0.15f, h * 0.58f), size = Size(w * 0.70f, h * 0.12f), cornerRadius = CornerRadius(w * 0.04f))
    drawRoundRect(color = Color(0xFFFFCC00), topLeft = Offset(w * 0.18f, h * 0.48f), size = Size(w * 0.64f, h * 0.08f), cornerRadius = CornerRadius(w * 0.02f))
    drawRoundRect(color = Color(0xFF34C759), topLeft = Offset(w * 0.15f, h * 0.42f), size = Size(w * 0.70f, h * 0.06f), cornerRadius = CornerRadius(w * 0.02f))
    val topBunPath = Path().apply {
        moveTo(w * 0.18f, h * 0.40f)
        lineTo(w * 0.82f, h * 0.40f)
        cubicTo(w * 0.82f, h * 0.15f, w * 0.18f, h * 0.15f, w * 0.18f, h * 0.40f)
        close()
    }
    drawPath(topBunPath, color = Color(0xFFE09F3E))
}

private fun DrawScope.drawMeat(w: Float, h: Float) {
    drawRoundRect(color = Color(0xFFF5EEF8), topLeft = Offset(w * 0.18f, h * 0.65f), size = Size(w * 0.35f, h * 0.12f), cornerRadius = CornerRadius(w * 0.06f))
    drawCircle(color = Color(0xFFF5EEF8), radius = w * 0.08f, center = Offset(w * 0.20f, h * 0.71f))
    drawCircle(color = Color(0xFFD9534F), radius = w * 0.28f, center = Offset(w * 0.62f, h * 0.48f))
    drawCircle(color = Color(0xFFB03A2E), radius = w * 0.22f, center = Offset(w * 0.62f, h * 0.48f))
}

private fun DrawScope.drawFish(w: Float, h: Float) {
    val bodyPath = Path().apply {
        moveTo(w * 0.20f, h * 0.50f)
        quadraticTo(w * 0.50f, h * 0.18f, w * 0.80f, h * 0.50f)
        quadraticTo(w * 0.50f, h * 0.82f, w * 0.20f, h * 0.50f)
        close()
    }
    drawPath(bodyPath, color = Color(0xFFFF7A00))
    drawLine(color = Color(0xFFFFF0E5), start = Offset(w * 0.35f, h * 0.35f), end = Offset(w * 0.45f, h * 0.65f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFFFFF0E5), start = Offset(w * 0.55f, h * 0.32f), end = Offset(w * 0.65f, h * 0.68f), strokeWidth = w * 0.04f)
}

private fun DrawScope.drawMilk(w: Float, h: Float) {
    drawRoundRect(color = Color(0xFF5AC8FA), topLeft = Offset(w * 0.28f, h * 0.35f), size = Size(w * 0.44f, h * 0.52f), cornerRadius = CornerRadius(w * 0.04f))
    val roofPath = Path().apply {
        moveTo(w * 0.28f, h * 0.35f)
        lineTo(w * 0.50f, h * 0.18f)
        lineTo(w * 0.72f, h * 0.35f)
        close()
    }
    drawPath(roofPath, color = Color(0xFF34AADC))
    drawCircle(color = Color.White, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.60f))
}

private fun DrawScope.drawCheese(w: Float, h: Float) {
    val wedgePath = Path().apply {
        moveTo(w * 0.15f, h * 0.75f)
        lineTo(w * 0.85f, h * 0.75f)
        lineTo(w * 0.50f, h * 0.25f)
        close()
    }
    drawPath(wedgePath, color = Color(0xFFFFCA3A))
    drawCircle(color = Color(0xFFE0A900), radius = w * 0.06f, center = Offset(w * 0.40f, h * 0.60f))
    drawCircle(color = Color(0xFFE0A900), radius = w * 0.05f, center = Offset(w * 0.60f, h * 0.52f))
}

private fun DrawScope.drawNutsOil(w: Float, h: Float) {
    val nutPath = Path().apply {
        moveTo(w * 0.20f, h * 0.60f)
        quadraticTo(w * 0.40f, h * 0.20f, w * 0.60f, h * 0.60f)
        quadraticTo(w * 0.40f, h * 0.85f, w * 0.20f, h * 0.60f)
        close()
    }
    drawPath(nutPath, color = Color(0xFF8D5B4C))

    val dropPath = Path().apply {
        moveTo(w * 0.70f, h * 0.30f)
        quadraticTo(w * 0.85f, h * 0.60f, w * 0.70f, h * 0.75f)
        quadraticTo(w * 0.55f, h * 0.60f, w * 0.70f, h * 0.30f)
        close()
    }
    drawPath(dropPath, color = Color(0xFFFFCC00))
}

private fun DrawScope.drawBread(w: Float, h: Float) {
    val breadPath = Path().apply {
        moveTo(w * 0.20f, h * 0.35f)
        cubicTo(w * 0.20f, h * 0.15f, w * 0.80f, h * 0.15f, w * 0.80f, h * 0.35f)
        lineTo(w * 0.80f, h * 0.80f)
        lineTo(w * 0.20f, h * 0.80f)
        close()
    }
    drawPath(breadPath, color = Color(0xFFA04000))

    val innerPath = Path().apply {
        moveTo(w * 0.26f, h * 0.37f)
        cubicTo(w * 0.26f, h * 0.22f, w * 0.74f, h * 0.22f, w * 0.74f, h * 0.37f)
        lineTo(w * 0.74f, h * 0.74f)
        lineTo(w * 0.26f, h * 0.74f)
        close()
    }
    drawPath(innerPath, color = Color(0xFFF7DC6F))
}

private fun DrawScope.drawCoffeeTea(w: Float, h: Float) {
    drawCircle(color = Color(0xFF8B5A2B), radius = w * 0.35f, center = Offset(w * 0.50f, h * 0.55f))
    drawCircle(color = Color(0xFF5D3A1A), radius = w * 0.28f, center = Offset(w * 0.50f, h * 0.55f))
}

private fun DrawScope.drawWater(w: Float, h: Float) {
    val dropPath = Path().apply {
        moveTo(w * 0.50f, h * 0.18f)
        quadraticTo(w * 0.80f, h * 0.58f, w * 0.50f, h * 0.85f)
        quadraticTo(w * 0.20f, h * 0.58f, w * 0.50f, h * 0.18f)
        close()
    }
    drawPath(dropPath, color = Color(0xFF30B0C7))
}

private fun DrawScope.drawDefaultFood(w: Float, h: Float) {
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.38f, center = Offset(w * 0.5f, h * 0.5f))
    drawCircle(color = Color(0xFFFFD60A), radius = w * 0.26f, center = Offset(w * 0.5f, h * 0.5f))
}

// ==========================================
// MEAL TYPE DRAWING SCOPES
// ==========================================

private fun DrawScope.drawBreakfastMeal(w: Float, h: Float) {
    drawCircle(color = Color.White, radius = w * 0.38f, center = Offset(w * 0.42f, h * 0.52f))
    drawCircle(color = Color(0xFFFFCC00), radius = w * 0.18f, center = Offset(w * 0.42f, h * 0.52f))
    drawCircle(color = Color(0xFF8B5A2B), radius = w * 0.18f, center = Offset(w * 0.78f, h * 0.35f))
    drawCircle(color = Color(0xFF5D3A1A), radius = w * 0.14f, center = Offset(w * 0.78f, h * 0.35f))
}

private fun DrawScope.drawLunchMeal(w: Float, h: Float) {
    val bowlPath = Path().apply {
        moveTo(w * 0.18f, h * 0.45f)
        lineTo(w * 0.82f, h * 0.45f)
        cubicTo(w * 0.82f, h * 0.85f, w * 0.18f, h * 0.85f, w * 0.18f, h * 0.45f)
        close()
    }
    drawPath(bowlPath, color = Color(0xFFFF9500))

    drawOval(color = Color(0xFFFF3B30), topLeft = Offset(w * 0.18f, h * 0.38f), size = Size(w * 0.64f, h * 0.14f))

    drawLine(color = Color(0xFF52B788), start = Offset(w * 0.35f, h * 0.32f), end = Offset(w * 0.35f, h * 0.18f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF52B788), start = Offset(w * 0.50f, h * 0.28f), end = Offset(w * 0.50f, h * 0.14f), strokeWidth = w * 0.04f)
    drawLine(color = Color(0xFF52B788), start = Offset(w * 0.65f, h * 0.32f), end = Offset(w * 0.65f, h * 0.18f), strokeWidth = w * 0.04f)
}

private fun DrawScope.drawDinnerMeal(w: Float, h: Float) {
    val moonPath = Path().apply {
        moveTo(w * 0.75f, h * 0.15f)
        cubicTo(w * 0.50f, h * 0.25f, w * 0.50f, h * 0.65f, w * 0.75f, h * 0.75f)
        cubicTo(w * 0.60f, h * 0.60f, w * 0.60f, h * 0.30f, w * 0.75f, h * 0.15f)
        close()
    }
    drawPath(moonPath, color = Color(0xFFFFD60A))

    drawCircle(color = Color(0xFFD9534F), radius = w * 0.26f, center = Offset(w * 0.42f, h * 0.60f))
    drawCircle(color = Color(0xFFB03A2E), radius = w * 0.18f, center = Offset(w * 0.42f, h * 0.60f))
}

private fun DrawScope.drawSnackMeal(w: Float, h: Float) {
    drawCircle(color = Color(0xFFFF3B30), radius = w * 0.32f, center = Offset(w * 0.48f, h * 0.55f))

    val leafPath = Path().apply {
        moveTo(w * 0.48f, h * 0.25f)
        quadraticTo(w * 0.65f, h * 0.15f, w * 0.68f, h * 0.30f)
        quadraticTo(w * 0.52f, h * 0.32f, w * 0.48f, h * 0.25f)
        close()
    }
    drawPath(leafPath, color = Color(0xFF34C759))

    drawCircle(color = Color(0xFFFFCC00), radius = w * 0.12f, center = Offset(w * 0.80f, h * 0.72f))
}
