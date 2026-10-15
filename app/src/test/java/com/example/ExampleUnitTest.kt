package com.example

import com.example.data.calculator.KbzhuCalculator
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.MealType
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBmrCalculationForMale() {
        // Mifflin-St Jeor for 26yo Male, 75kg, 178cm
        // 10*75 + 6.25*178 - 5*26 + 5 = 750 + 1112.5 - 130 + 5 = 1737.5
        val bmr = KbzhuCalculator.calculateBmr(
            gender = Gender.MALE,
            age = 26,
            heightCm = 178.0,
            weightKg = 75.0
        )
        assertEquals(1737.5, bmr, 0.5)
    }

    @Test
    fun testBmrCalculationForFemale() {
        // Mifflin-St Jeor for 26yo Female, 60kg, 165cm
        // 10*60 + 6.25*165 - 5*26 - 161 = 600 + 1031.25 - 130 - 161 = 1340.25
        val bmr = KbzhuCalculator.calculateBmr(
            gender = Gender.FEMALE,
            age = 26,
            heightCm = 165.0,
            weightKg = 60.0
        )
        assertEquals(1340.25, bmr, 0.5)
    }

    @Test
    fun testFullKbzhuCalculation() {
        val result = KbzhuCalculator.calculate(
            gender = Gender.MALE,
            age = 26,
            heightCm = 178.0,
            weightKg = 75.0,
            activityLevel = ActivityLevel.MODERATE,
            goal = FitnessGoal.WEIGHT_LOSS
        )

        // Moderate activity = 1.55, Weight loss = 0.82
        assertTrue("TDEE should be higher than BMR", result.tdee > result.bmr)
        assertTrue("Target calories should be positive and reasonable", result.targetCalories in 1200..3500)
        assertTrue("Protein should be calculated appropriately", result.targetProtein >= 120)
        assertTrue("Fat should be in healthy range", result.targetFat >= 45)
        assertTrue("Carbs should be allocated", result.targetCarbs >= 50)
    }

    @Test
    fun testProductMacroPortionCalculation() {
        val chickenBreast = Product(
            name = "Куриное филе",
            category = "Мясо и птица",
            calories = 165.0,
            protein = 31.0,
            fat = 3.6,
            carbs = 0.0
        )

        val portion = chickenBreast.calculateForWeight(200.0)
        assertEquals(330.0, portion.calories, 0.01)
        assertEquals(62.0, portion.protein, 0.01)
        assertEquals(7.2, portion.fat, 0.01)
        assertEquals(0.0, portion.carbs, 0.01)
    }

    @Test
    fun testMealTypeFromString() {
        assertEquals(MealType.BREAKFAST, MealType.fromString("BREAKFAST"))
        assertEquals(MealType.LUNCH, MealType.fromString("lunch"))
        assertEquals(MealType.DINNER, MealType.fromString("DINNER"))
        assertEquals(MealType.SNACK, MealType.fromString("SNACK"))
        assertEquals(MealType.BREAKFAST, MealType.fromString("UNKNOWN"))
    }

    @Test
    fun testSmartFoodSearchMorphology() {
        val eggProduct = Product(
            name = "Яйцо куриное (варёное)",
            category = "Молочные продукты",
            calories = 155.0,
            protein = 13.0,
            fat = 11.0,
            carbs = 1.1
        )
        // User searches for plural "яйца" -> should match "Яйцо куриное"
        assertTrue("Query 'яйца' must match 'Яйцо куриное'", com.example.ui.util.SmartFoodSearch.matches(eggProduct, "яйца"))
        assertTrue("Query 'яйцо' must match 'Яйцо куриное'", com.example.ui.util.SmartFoodSearch.matches(eggProduct, "яйцо"))

        val chickenProduct = Product(
            name = "Куриное филе грудки",
            category = "Мясо и птица",
            calories = 110.0,
            protein = 23.0,
            fat = 1.2,
            carbs = 0.0
        )
        // User searches for "курица" -> should match "Куриное филе"
        assertTrue("Query 'курица' must match 'Куриное филе'", com.example.ui.util.SmartFoodSearch.matches(chickenProduct, "курица"))

        val bananaProduct = Product(
            name = "Банан свежий",
            category = "Фрукты и ягоды",
            calories = 89.0,
            protein = 1.1,
            fat = 0.3,
            carbs = 23.0
        )
        assertTrue("Query 'бананы' must match 'Банан'", com.example.ui.util.SmartFoodSearch.matches(bananaProduct, "бананы"))
    }

    @Test
    fun testSmartPortionDetection() {
        val egg = Product(name = "Яйцо куриное С1", category = "Молочные продукты", calories = 155.0, protein = 13.0, fat = 11.0, carbs = 1.1)
        val eggPortion = com.example.ui.components.detectSmartPortion(egg)
        assertEquals("шт", eggPortion.unitName)
        assertEquals(55.0, eggPortion.gramsPerUnit, 0.1)

        val banana = Product(name = "Банан", category = "Фрукты и ягоды", calories = 89.0, protein = 1.1, fat = 0.3, carbs = 23.0)
        val bananaPortion = com.example.ui.components.detectSmartPortion(banana)
        assertEquals("шт", bananaPortion.unitName)
        assertEquals(120.0, bananaPortion.gramsPerUnit, 0.1)

        val bread = Product(name = "Хлеб Бородинский", category = "Хлеб и выпечка", calories = 208.0, protein = 6.8, fat = 1.3, carbs = 40.7)
        val breadPortion = com.example.ui.components.detectSmartPortion(bread)
        assertEquals("кусок", breadPortion.unitName)
        assertEquals(35.0, breadPortion.gramsPerUnit, 0.1)
    }

    @Test
    fun testWaterTargetCalculation() {
        val weight = 70.0
        val targetWater = (weight * 30.0).toInt().coerceAtLeast(1500)
        assertEquals(2100, targetWater)
    }
}
