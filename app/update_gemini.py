import re

with open('app/src/main/java/com/example/data/service/GeminiFoodService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

start_marker = '    private fun fallbackQuickTextAnalysis(input: String): RecognizedMeal {'
end_marker = '    private fun getCuratedLocalScienceAdvice(product: Product): ScienceNutritionAdvice {'

new_method = """    private fun fallbackQuickTextAnalysis(input: String): RecognizedMeal {
        val lower = input.lowercase()

        val mealType = when {
            lower.contains("завтрак") -> MealType.BREAKFAST
            lower.contains("обед") -> MealType.LUNCH
            lower.contains("ужин") -> MealType.DINNER
            lower.contains("перекус") || lower.contains("полдник") -> MealType.SNACK
            lower.contains("яйц") || lower.contains("овсянк") || lower.contains("каша") || lower.contains("творог") || lower.contains("сырник") -> MealType.BREAKFAST
            lower.contains("суп") || lower.contains("борщ") || lower.contains("котлет") || lower.contains("гречк") || lower.contains("рис") || lower.contains("макарон") -> MealType.LUNCH
            else -> MealType.LUNCH
        }

        val items = mutableListOf<RecognizedMealItem>()
        val recognizedFoods = mutableSetOf<String>()

        fun extractFood(text: String) {
            val gramsMatch = Regex(\"\"\"(\\d+)\\s*(?:г|гр|g|gram)\"\"\").find(text)
            val explicitGrams = gramsMatch?.groupValues?.get(1)?.toIntOrNull()

            if (text.contains("яйц") && !recognizedFoods.contains("egg")) {
                recognizedFoods.add("egg")
                val countMatch = Regex(\"\"\"(\\d+)\\s*(?:шт|штуки|штук)?\\s*яй\"\"\").find(text)
                    ?: Regex(\"\"\"яй[а-я]*\\s*(\\d+)\"\"\").find(text)
                val count = countMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val g = count * 55
                items.add(
                    RecognizedMealItem(
                        name = if (count == 1) "Яйцо куриное (1 шт)" else "Яйца куриные ($count шт)",
                        grams = g,
                        calories = count * 78,
                        protein = count * 6.5,
                        fat = count * 5.5,
                        carbs = count * 0.4
                    )
                )
            }

            if (text.contains("творог") && !recognizedFoods.contains("cottage")) {
                recognizedFoods.add("cottage")
                val g = explicitGrams ?: 150
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Творог 5%",
                        grams = g,
                        calories = (121 * factor).toInt(),
                        protein = 16.0 * factor,
                        fat = 5.0 * factor,
                        carbs = 3.0 * factor
                    )
                )
            }

            if ((text.contains("овсянк") || text.contains("геркулес")) && !recognizedFoods.contains("oats")) {
                recognizedFoods.add("oats")
                val g = explicitGrams ?: 200
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Овсяная каша",
                        grams = g,
                        calories = (102 * factor).toInt(),
                        protein = 3.5 * factor,
                        fat = 2.0 * factor,
                        carbs = 17.5 * factor
                    )
                )
            }

            if (text.contains("банан") && !recognizedFoods.contains("banana")) {
                recognizedFoods.add("banana")
                val countMatch = Regex(\"\"\"(\\d+)\\s*(?:шт)?\\s*банан\"\"\").find(text)
                val count = countMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val g = explicitGrams ?: (count * 120)
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = if (count > 1) "Банан ($count шт)" else "Банан свежий",
                        grams = g,
                        calories = (89 * factor).toInt(),
                        protein = 1.1 * factor,
                        fat = 0.3 * factor,
                        carbs = 21.0 * factor
                    )
                )
            }

            if (text.contains("гречк") && !recognizedFoods.contains("buckwheat")) {
                recognizedFoods.add("buckwheat")
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Гречневая каша",
                        grams = g,
                        calories = (110 * factor).toInt(),
                        protein = 4.2 * factor,
                        fat = 1.1 * factor,
                        carbs = 21.0 * factor
                    )
                )
            }

            if ((text.contains("кур") || text.contains("филе") || text.contains("грудк")) && !recognizedFoods.contains("chicken")) {
                recognizedFoods.add("chicken")
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Куриное филе отварное",
                        grams = g,
                        calories = (165 * factor).toInt(),
                        protein = 31.0 * factor,
                        fat = 3.6 * factor,
                        carbs = 0.0
                    )
                )
            }

            if (text.contains("рис") && !recognizedFoods.contains("rice")) {
                recognizedFoods.add("rice")
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Рис отварной",
                        grams = g,
                        calories = (130 * factor).toInt(),
                        protein = 2.7 * factor,
                        fat = 0.3 * factor,
                        carbs = 28.0 * factor
                    )
                )
            }

            if (text.contains("кофе") && !recognizedFoods.contains("coffee")) {
                recognizedFoods.add("coffee")
                val hasMilk = text.contains("молок") || text.contains("сливк")
                items.add(
                    RecognizedMealItem(
                        name = if (hasMilk) "Кофе с молоком" else "Черный кофе",
                        grams = 200,
                        calories = if (hasMilk) 55 else 4,
                        protein = if (hasMilk) 2.5 else 0.2,
                        fat = if (hasMilk) 2.0 else 0.1,
                        carbs = if (hasMilk) 6.0 else 0.5
                    )
                )
            }

            if ((text.contains("сырник") || text.contains("сырники")) && !recognizedFoods.contains("syrniki")) {
                recognizedFoods.add("syrniki")
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Сырники творожные",
                        grams = g,
                        calories = (210 * factor).toInt(),
                        protein = 16.0 * factor,
                        fat = 8.0 * factor,
                        carbs = 18.0 * factor
                    )
                )
            }

            if (text.contains("яблок") && !recognizedFoods.contains("apple")) {
                recognizedFoods.add("apple")
                val count = 1
                val g = explicitGrams ?: (count * 150)
                val factor = g / 100.0
                items.add(
                    RecognizedMealItem(
                        name = "Яблоко свежее",
                        grams = g,
                        calories = (52 * factor).toInt(),
                        protein = 0.3 * factor,
                        fat = 0.2 * factor,
                        carbs = 14.0 * factor
                    )
                )
            }
        }

        val clauses = lower.split(Regex(\"\"\"\\b(?:и|\\+|\\,|\\;)\\b|\\band\\b\"\"\"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (clause in clauses) {
            extractFood(clause)
        }

        if (items.size < 2) {
            extractFood(lower)
        }

        if (items.isEmpty()) {
            val grams = Regex(\"\"\"(\\d+)\\s*(?:г|гр|g|gram)\"\"\").find(lower)?.groupValues?.get(1)?.toIntOrNull() ?: 200
            val factor = grams / 100.0
            val cleanName = input.replace(Regex(\"\"\"(?i)\\b(?:съел|съела|съели|на\\s+завтрак|на\\s+обед|на\\s+ужин|на\\s+перекус)\\b\"\"\"), "").trim()
                .replaceFirstChar { it.uppercase() }
                .ifBlank { "Приём пищи" }
            return RecognizedMeal(
                dishName = cleanName,
                estimatedGrams = grams,
                calories = (160 * factor).toInt(),
                protein = 7.0 * factor,
                fat = 5.0 * factor,
                carbs = 20.0 * factor,
                mealType = mealType,
                confidence = "Оценка",
                description = "Порция $grams г",
                items = listOf(RecognizedMealItem(cleanName, grams, (160 * factor).toInt(), 7.0 * factor, 5.0 * factor, 20.0 * factor))
            )
        }

        val totalGrams = items.sumOf { it.grams }
        val totalCalories = items.sumOf { it.calories }
        val totalProtein = items.sumOf { it.protein }
        val totalFat = items.sumOf { it.fat }
        val totalCarbs = items.sumOf { it.carbs }
        val summaryTitle = items.joinToString(", ") { "${it.name} (${it.grams}г)" }

        return RecognizedMeal(
            dishName = items.joinToString(" и ") { it.name },
            estimatedGrams = totalGrams,
            calories = totalCalories,
            protein = totalProtein,
            fat = totalFat,
            carbs = totalCarbs,
            mealType = mealType,
            confidence = "Высокая",
            description = summaryTitle,
            items = items
        )
    }

"""

idx_start = content.find(start_marker)
idx_end = content.find(end_marker)

if idx_start != -1 and idx_end != -1:
    new_content = content[:idx_start] + new_method + content[idx_end:]
    with open('app/src/main/java/com/example/data/service/GeminiFoodService.kt', 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("SUCCESS")
else:
    print(f"FAILED: start={idx_start}, end={idx_end}")
