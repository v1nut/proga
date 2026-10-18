package com.example.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FitnessGoal
import com.example.data.model.MealEntry
import com.example.data.model.Product

enum class FoodTier(
    val letter: String,
    val color: Color,
    val titleRu: String,
    val titleEn: String,
    val score: Int
) {
    S(
        letter = "S",
        color = Color(0xFFFF6B6B),
        titleRu = "S-Tier • Топ польза",
        titleEn = "S-Tier • Optimal",
        score = 5
    ),
    A(
        letter = "A",
        color = Color(0xFFFFA94D),
        titleRu = "A-Tier • Отличный выбор",
        titleEn = "A-Tier • Great choice",
        score = 4
    ),
    B(
        letter = "B",
        color = Color(0xFFFFD43B),
        titleRu = "B-Tier • Хорошо в меру",
        titleEn = "B-Tier • Good in moderation",
        score = 3
    ),
    C(
        letter = "C",
        color = Color(0xFFE9FAC8),
        titleRu = "C-Tier • Ниже среднего",
        titleEn = "C-Tier • Below average",
        score = 2
    ),
    D(
        letter = "D",
        color = Color(0xFF8CE99A),
        titleRu = "D-Tier • Читмил / Джанк",
        titleEn = "D-Tier • Cheatmeal / Junk",
        score = 1
    )
}

enum class TierMode(
    val key: String,
    val titleRu: String,
    val titleEn: String
) {
    AUTO_BY_PROFILE("auto", "Авто (по цели профиля)", "Auto (by profile goal)"),
    WEIGHT_LOSS("loss", "Похудение (Сушка)", "Weight Loss (Cutting)"),
    WEIGHT_GAIN("gain", "Набор массы", "Weight Gain (Bulking)")
}

@Composable
fun TierSquareBadge(
    tier: FoodTier,
    size: Dp = 26.dp,
    modifier: Modifier = Modifier
) {
    val fontSize = (size.value * 0.58f).sp

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(7.dp))
            .background(tier.color)
            .border(1.dp, Color(0xFF101014).copy(alpha = 0.4f), RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = tier.letter,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF101014)
        )
    }
}

object FoodTierSystem {

    private val NON_LETTER_REGEX = Regex("""[^\p{L}\d]+""")

    fun evaluateProductTier(
        name: String,
        category: String,
        calories: Double,
        protein: Double,
        fat: Double,
        carbs: Double,
        goal: FitnessGoal,
        lang: String = "ru"
    ): Pair<FoodTier, String> {
        val normName = name.lowercase().replace('ё', 'е').trim()
        val normCat = category.lowercase().replace('ё', 'е').trim()
        val tokens = normName.split(NON_LETTER_REGEX).filter { it.isNotBlank() }

        // Helper word matchers (matching word starts/tokens)
        fun hasWordStartingWith(prefix: String): Boolean = tokens.any { it.startsWith(prefix) }
        fun hasExactWord(word: String): Boolean = tokens.any { it == word }

        // 1. Junk / Sugar / Soda / Processed Snacks / Instant Noodles
        val isPureSugarOrSweet = hasWordStartingWith("сахар") || hasWordStartingWith("мед") ||
                hasWordStartingWith("джем") || hasWordStartingWith("варенье") || hasWordStartingWith("сироп") ||
                hasWordStartingWith("sugar") || hasWordStartingWith("honey") || hasWordStartingWith("jam") ||
                hasWordStartingWith("syrup")

        if (isPureSugarOrSweet) {
            val tier = if (goal == FitnessGoal.WEIGHT_LOSS) FoodTier.D else FoodTier.C
            val reason = if (lang == "en")
                "Concentrated refined sugar, low nutrient density"
            else
                "Концентрированный сахар, минимальная питательная ценность"
            return Pair(tier, reason)
        }

        val isInstantNoodles = normName.contains("доширак") || normName.contains("роллтон") ||
                (normName.contains("лапша") && normName.contains("быстр")) || normName.contains("ramen") ||
                normName.contains("instant noodle")

        val isCookieOrPastry = (hasWordStartingWith("печен") && !hasWordStartingWith("запечен") && !hasExactWord("печень")) ||
                hasWordStartingWith("cookie") || hasWordStartingWith("biscuit") ||
                (hasExactWord("торт") || hasExactWord("торты") || hasExactWord("торта") || hasExactWord("пирожное") || hasWordStartingWith("чизкейк") || hasWordStartingWith("cheesecake") || hasExactWord("cake") || hasExactWord("pastry")) ||
                hasWordStartingWith("конфет") || hasWordStartingWith("шоколад") || hasWordStartingWith("сникерс") ||
                hasWordStartingWith("чипс") || hasWordStartingWith("газировк") || hasWordStartingWith("сосиск") ||
                hasWordStartingWith("колбас") || hasWordStartingWith("candy") || hasWordStartingWith("chocolate") ||
                hasWordStartingWith("chip") || hasWordStartingWith("soda") || hasWordStartingWith("sausage") ||
                isInstantNoodles || normCat.contains("сладост") || normCat.contains("snack")

        if (isCookieOrPastry) {
            val reason = if (lang == "en")
                "High in refined sugars & fats, low nutrient value"
            else
                "Избыток быстрых сахаров/жиров, низкая нутриентная польза"
            return Pair(FoodTier.D, reason)
        }

        // 2. Fast Food (Shawarma, Burger, Pizza)
        val isFastFood = hasWordStartingWith("шаурм") || hasWordStartingWith("шаверм") ||
                hasWordStartingWith("бургер") || hasWordStartingWith("пицц") || hasWordStartingWith("донер") ||
                hasWordStartingWith("burger") || hasWordStartingWith("pizza") || hasWordStartingWith("shawarma")

        if (isFastFood) {
            return if (goal == FitnessGoal.WEIGHT_GAIN) {
                val reason = if (lang == "en")
                    "High calorie & protein density, acceptable for bulking"
                else
                    "Высокая калорийность и белок, допустимо на массе"
                Pair(FoodTier.B, reason)
            } else {
                val reason = if (lang == "en")
                    "High calorie density and sauce fats"
                else
                    "Высокая плотность калорий и жиров из соусов"
                Pair(FoodTier.C, reason)
            }
        }

        // 3. Liver (Печень) - Healthy Organ Meat
        if (hasExactWord("печень") || hasExactWord("liver")) {
            val reason = if (lang == "en")
                "Rich in iron, vitamin A and essential micronutrients"
            else
                "Богата железом, витамином A и микронутриентами"
            return Pair(FoodTier.A, reason)
        }

        // 4. Lean Protein Keyword Check
        val hasProteinKeyword = hasWordStartingWith("кури") || hasWordStartingWith("индейк") ||
                hasWordStartingWith("говяд") || hasWordStartingWith("лосось") || hasWordStartingWith("семга") ||
                hasWordStartingWith("тунец") || hasWordStartingWith("минтай") || hasWordStartingWith("креветк") ||
                hasWordStartingWith("горбуш") || hasWordStartingWith("треск") || hasWordStartingWith("судак") ||
                hasWordStartingWith("хек") || hasWordStartingWith("кальмар") || hasWordStartingWith("морепродукт") ||
                hasWordStartingWith("яйц") || hasWordStartingWith("белок") ||
                (hasWordStartingWith("творог") && fat <= 5.5 && carbs <= 5.0) ||
                hasWordStartingWith("филе") ||
                hasWordStartingWith("chicken") || hasWordStartingWith("turkey") || hasWordStartingWith("beef") ||
                hasWordStartingWith("salmon") || hasWordStartingWith("tuna") || hasWordStartingWith("shrimp") ||
                hasWordStartingWith("egg") || hasWordStartingWith("fillet") ||
                (hasWordStartingWith("cottage") && fat <= 5.5)

        val isNotFriedOrCutlet = !hasWordStartingWith("жарен") && !hasWordStartingWith("колбас") && !hasWordStartingWith("котлет")

        // Reinforce Lean Protein with Strict Macro Validation per 100g for S-Tier!
        if (hasProteinKeyword && isNotFriedOrCutlet) {
            val isUltraLeanS = protein >= 18.0 && fat <= 4.2 && carbs <= 3.0 && calories <= 170.0
            if (isUltraLeanS) {
                val reason = if (lang == "en")
                    "Optimal bioavailable protein & amino acid profile"
                else
                    "Эталонный биодоступный белок и аминокислотный профиль"
                return Pair(FoodTier.S, reason)
            } else {
                val reason = if (lang == "en")
                    "Great source of bioavailable protein"
                else
                    "Отличный источник легкоусвояемого белка"
                return Pair(FoodTier.A, reason)
            }
        }

        // 5. Healthy Fats & Nuts (Nuts, Seeds, Avocado, Olive Oil, Salmon)
        val isHealthyFats = hasWordStartingWith("орех") || hasWordStartingWith("миндал") ||
                hasWordStartingWith("арахис") || hasWordStartingWith("авокадо") || hasWordStartingWith("оливков") ||
                hasWordStartingWith("семеч") || hasWordStartingWith("семена") ||
                hasWordStartingWith("nut") || hasWordStartingWith("almond") || hasWordStartingWith("peanut") ||
                hasWordStartingWith("avocado") || hasWordStartingWith("seed")

        if (isHealthyFats) {
            return when (goal) {
                FitnessGoal.WEIGHT_GAIN -> {
                    val reason = if (lang == "en")
                        "Healthy omega fats and clean calorie density for gain"
                    else
                        "Полезные омега-жиры и плотность чистых калорий на наборе"
                    Pair(FoodTier.A, reason)
                }
                FitnessGoal.MAINTENANCE -> {
                    val reason = if (lang == "en")
                        "Valuable unsaturated fats and micronutrients"
                    else
                        "Ценные ненасыщенные жиры и микронутриенты"
                    Pair(FoodTier.A, reason)
                }
                FitnessGoal.WEIGHT_LOSS -> {
                    val reason = if (lang == "en")
                        "Nutritious but calorie-dense — portion control essential"
                    else
                        "Очень полезно, но высокая калорийность — важен контроль порции"
                    Pair(FoodTier.B, reason)
                }
            }
        }

        // 6. Complex Carbs & Whole Grains
        val isTopGrain = hasWordStartingWith("гречк") || hasWordStartingWith("гречн") ||
                hasWordStartingWith("овсян") || hasWordStartingWith("геркулес") || hasWordStartingWith("киноа") ||
                hasWordStartingWith("buckwheat") || hasWordStartingWith("oat") || hasWordStartingWith("quinoa")
        val isOtherGrain = hasWordStartingWith("рис") || hasWordStartingWith("макарон") ||
                hasWordStartingWith("спагетти") || hasWordStartingWith("картоф") || hasWordStartingWith("хлебц") ||
                hasWordStartingWith("цельнозерн") || hasWordStartingWith("rice") || hasWordStartingWith("pasta") ||
                hasWordStartingWith("spaghetti") || hasWordStartingWith("potato")

        if (isTopGrain || isOtherGrain) {
            val reason = if (lang == "en")
                "Clean complex carbs & fiber for energy"
            else
                "Сложные углеводы и клетчатка для стабильной энергии"
            return Pair(FoodTier.A, reason)
        }

        // 7. Vegetables, Greens, Berries, Watermelon
        val isVeggieOrBerries = hasWordStartingWith("помидор") || hasWordStartingWith("огур") ||
                hasWordStartingWith("броккол") || hasWordStartingWith("шпинат") || hasWordStartingWith("перец") ||
                hasWordStartingWith("кабачок") || hasWordStartingWith("морков") || hasWordStartingWith("арбуз") ||
                hasWordStartingWith("клубник") || hasWordStartingWith("черник") || hasWordStartingWith("голубик") ||
                hasWordStartingWith("tomato") || hasWordStartingWith("cucumber") || hasWordStartingWith("broccoli") ||
                hasWordStartingWith("spinach") || hasWordStartingWith("pepper") || hasWordStartingWith("berry") ||
                hasWordStartingWith("watermelon") || normCat.contains("овощ") || normCat.contains("veg")

        if (isVeggieOrBerries) {
            val reason = if (lang == "en")
                "Vitamins, fiber and low-calorie density"
            else
                "Витамины, клетчатка и низкая калорийность"
            return Pair(FoodTier.A, reason)
        }

        // 8. Fruits & Fermented Dairy
        val isGreekYogurt = (hasWordStartingWith("греческ") || hasWordStartingWith("greek")) &&
                (hasWordStartingWith("йогурт") || hasWordStartingWith("yogurt"))
        if (isGreekYogurt) {
            val reason = if (lang == "en")
                "High protein density & healthy probiotics"
            else
                "Высокая концентрация белка и пробиотики"
            return Pair(FoodTier.S, reason)
        }

        val isFruitOrDairy = hasWordStartingWith("банан") || hasWordStartingWith("яблок") ||
                hasWordStartingWith("апельсин") || hasWordStartingWith("дыня") || hasWordStartingWith("молок") ||
                hasWordStartingWith("кефир") || hasWordStartingWith("йогурт") || hasWordStartingWith("banana") ||
                hasWordStartingWith("apple") || hasWordStartingWith("orange") || hasWordStartingWith("milk") ||
                hasWordStartingWith("yogurt")

        if (isFruitOrDairy) {
            val reason = if (lang == "en")
                "Nutritious natural food with vitamins & minerals"
            else
                "Полезный натуральный продукт с витаминами"
            return Pair(FoodTier.A, reason)
        }

        // 9. Cheeses, Butter, Sour Cream, Cutlets
        val isDairyFatsOrCutlet = hasWordStartingWith("сыр") || hasWordStartingWith("масло") ||
                hasWordStartingWith("сметан") || hasWordStartingWith("котлет") || hasWordStartingWith("cheese") ||
                hasWordStartingWith("butter") || hasWordStartingWith("cream")

        if (isDairyFatsOrCutlet) {
            return if (goal == FitnessGoal.WEIGHT_GAIN) {
                val reason = if (lang == "en")
                    "Good calorie & fat source for bulking"
                else
                    "Хороший источник калорий и жиров на наборе"
                Pair(FoodTier.A, reason)
            } else {
                val reason = if (lang == "en")
                    "Nutritious, but watch fat & portion intake"
                else
                    "Питательно, но следите за долей жиров"
                Pair(FoodTier.B, reason)
            }
        }

        // 10. General Macro Fallback Formula (Strict Calibrated Thresholds)
        val proteinCalShare = (protein * 4.0) / calories.coerceAtLeast(1.0)

        return when (goal) {
            FitnessGoal.WEIGHT_LOSS -> {
                when {
                    (proteinCalShare >= 0.45 && calories <= 160.0) ->
                        Pair(FoodTier.S, if (lang == "en") "High protein density & low calories" else "Высокая концентрация белка при низкой калорийности")
                    (proteinCalShare >= 0.25 && calories <= 220.0) || (calories <= 60.0 && protein >= 0.5) ->
                        Pair(FoodTier.A, if (lang == "en") "Good protein to calorie ratio" else "Хорошее соотношение белка и калорий")
                    calories <= 320.0 ->
                        Pair(FoodTier.B, if (lang == "en") "Moderate calorie intake" else "Умеренная калорийность")
                    calories <= 480.0 ->
                        Pair(FoodTier.C, if (lang == "en") "High calorie density" else "Высокая плотность калорий")
                    else ->
                        Pair(FoodTier.D, if (lang == "en") "Excess calories for cutting" else "Избыточная калорийность на сушке")
                }
            }
            FitnessGoal.WEIGHT_GAIN -> {
                when {
                    protein >= 20.0 && fat <= 8.0 && calories in 110.0..280.0 ->
                        Pair(FoodTier.S, if (lang == "en") "Nutrient profile optimal for growth" else "Питательный профиль для роста и восстановления")
                    (protein >= 10.0 && calories in 100.0..400.0) || (carbs >= 20.0 && protein >= 3.0 && calories <= 350.0) ->
                        Pair(FoodTier.A, if (lang == "en") "Good energy & macro balance" else "Хороший запас энергии и белка")
                    calories <= 500.0 ->
                        Pair(FoodTier.B, if (lang == "en") "Standard energy profile" else "Обычный энергетический профиль")
                    calories <= 650.0 ->
                        Pair(FoodTier.C, if (lang == "en") "Calorie-dense profile" else "Высокая калорийность")
                    else ->
                        Pair(FoodTier.D, if (lang == "en") "Empty calories without protein" else "Калорийность без белковой ценности")
                }
            }
            FitnessGoal.MAINTENANCE -> {
                when {
                    (proteinCalShare >= 0.40 && calories <= 180.0) ->
                        Pair(FoodTier.S, if (lang == "en") "Optimal protein ratio for maintenance" else "Оптимальный баланс белка для поддержания")
                    (proteinCalShare >= 0.20 && calories <= 280.0) || calories <= 80.0 ->
                        Pair(FoodTier.A, if (lang == "en") "Balanced macro profile for maintenance" else "Баланс нутриентов для поддержки формы")
                    calories <= 380.0 ->
                        Pair(FoodTier.B, if (lang == "en") "Suitable for everyday diet" else "Подходит в повседневный рацион")
                    calories <= 550.0 ->
                        Pair(FoodTier.C, if (lang == "en") "Watch your portion size" else "Следите за порцией")
                    else ->
                        Pair(FoodTier.D, if (lang == "en") "Excessive calories" else "Избыточная калорийность")
                }
            }
        }
    }

    fun evaluateProduct(product: Product, goal: FitnessGoal, lang: String = "ru"): Pair<FoodTier, String> {
        return evaluateProductTier(
            name = product.name,
            category = product.category,
            calories = product.calories,
            protein = product.protein,
            fat = product.fat,
            carbs = product.carbs,
            goal = goal,
            lang = lang
        )
    }

    fun calculateAverageTier(entries: List<MealEntry>, goal: FitnessGoal): FoodTier? {
        if (entries.isEmpty()) return null
        var totalWeightedScore = 0.0
        var totalWeight = 0.0

        for (entry in entries) {
            val factor = if (entry.weightGrams > 0.0) 100.0 / entry.weightGrams else 1.0
            val (tier, _) = evaluateProductTier(
                name = entry.productName,
                category = "",
                calories = entry.calories * factor,
                protein = entry.protein * factor,
                fat = entry.fat * factor,
                carbs = entry.carbs * factor,
                goal = goal
            )
            val w = entry.calories.coerceAtLeast(10.0)
            totalWeightedScore += tier.score * w
            totalWeight += w
        }

        if (totalWeight <= 0) return null
        val avgScore = totalWeightedScore / totalWeight

        return when {
            avgScore >= 4.5 -> FoodTier.S
            avgScore >= 3.5 -> FoodTier.A
            avgScore >= 2.5 -> FoodTier.B
            avgScore >= 1.8 -> FoodTier.C
            else -> FoodTier.D
        }
    }
}
