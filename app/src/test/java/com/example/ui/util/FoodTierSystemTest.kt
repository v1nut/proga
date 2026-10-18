package com.example.ui.util

import com.example.data.local.InitialData
import com.example.data.model.FitnessGoal
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodTierSystemTest {

    @Test
    fun testBakedFishAndLiverAreNotD() {
        val bakedFish = Product(
            name = "Горбуша запечённая",
            category = "Рыба и морепродукты",
            calories = 142.0,
            protein = 21.0,
            fat = 6.5,
            carbs = 0.0
        )
        val (fishTier, _) = FoodTierSystem.evaluateProduct(bakedFish, FitnessGoal.WEIGHT_LOSS)
        assertNotEquals(FoodTier.D, fishTier)
        assertEquals(FoodTier.A, fishTier)

        val liver = Product(
            name = "Печень говяжья тушёная",
            category = "Мясо и птица",
            calories = 130.0,
            protein = 20.0,
            fat = 4.0,
            carbs = 3.0
        )
        val (liverTier, _) = FoodTierSystem.evaluateProduct(liver, FitnessGoal.WEIGHT_LOSS)
        assertNotEquals(FoodTier.D, liverTier)
        assertEquals(FoodTier.A, liverTier)
    }

    @Test
    fun testSugarAndHoneyAreNotSInAnyMode() {
        val sugar = Product(name = "Сахар белый", category = "Сладости", calories = 398.0, protein = 0.0, fat = 0.0, carbs = 99.8)
        val honey = Product(name = "Мёд натуральный", category = "Сладости", calories = 328.0, protein = 0.8, fat = 0.0, carbs = 80.3)

        for (goal in FitnessGoal.entries) {
            val (sTier, _) = FoodTierSystem.evaluateProduct(sugar, goal)
            val (hTier, _) = FoodTierSystem.evaluateProduct(honey, goal)

            assertNotEquals("Sugar should not be S-Tier in $goal", FoodTier.S, sTier)
            assertNotEquals("Honey should not be S-Tier in $goal", FoodTier.S, hTier)
            assertNotEquals("Sugar should not be A-Tier in $goal", FoodTier.A, sTier)
            assertNotEquals("Honey should not be A-Tier in $goal", FoodTier.A, hTier)
        }
    }

    @Test
    fun testInstantNoodlesIsNotS() {
        val noodles = Product(
            name = "Лапша быстрого приготовления Роллтон",
            category = "Бакалея",
            calories = 448.0,
            protein = 8.8,
            fat = 20.7,
            carbs = 56.7
        )
        for (goal in FitnessGoal.entries) {
            val (nTier, _) = FoodTierSystem.evaluateProduct(noodles, goal)
            assertNotEquals("Instant noodles should not be S in $goal", FoodTier.S, nTier)
            assertEquals("Instant noodles should be D-Tier", FoodTier.D, nTier)
        }
    }

    @Test
    fun testPredefinedProductsSTierPercentageIsLessThan15Percent() {
        val totalCount = InitialData.predefinedProducts.size
        val maxAllowedSTier = (totalCount * 0.15).toInt() // 9

        for (goal in FitnessGoal.entries) {
            val sTierCount = InitialData.predefinedProducts.count { prod ->
                val (tier, _) = FoodTierSystem.evaluateProduct(prod, goal)
                tier == FoodTier.S
            }
            assertTrue(
                "S-Tier count ($sTierCount) in $goal should be <= 15% ($maxAllowedSTier) of total $totalCount products",
                sTierCount <= maxAllowedSTier
            )
        }
    }

    @Test
    fun testEquivalentRussianAndEnglishFoodNamesClassifyIdentically() {
        val ruChicken = Product(name = "Куриная грудка отварная", category = "Мясо и птица", calories = 165.0, protein = 31.0, fat = 3.6, carbs = 0.0)
        val enChicken = Product(name = "Boiled chicken breast", category = "Meat & Poultry", calories = 165.0, protein = 31.0, fat = 3.6, carbs = 0.0)

        for (goal in FitnessGoal.entries) {
            val (ruTier, _) = FoodTierSystem.evaluateProduct(ruChicken, goal, "ru")
            val (enTier, _) = FoodTierSystem.evaluateProduct(enChicken, goal, "en")
            assertEquals("Chicken breast tier should match in $goal", ruTier, enTier)
        }

        val ruApple = Product(name = "Яблоко свежее", category = "Фрукты и ягоды", calories = 52.0, protein = 0.3, fat = 0.2, carbs = 13.8)
        val enApple = Product(name = "Fresh apple", category = "Fruits & Berries", calories = 52.0, protein = 0.3, fat = 0.2, carbs = 13.8)

        for (goal in FitnessGoal.entries) {
            val (ruTier, _) = FoodTierSystem.evaluateProduct(ruApple, goal, "ru")
            val (enTier, _) = FoodTierSystem.evaluateProduct(enApple, goal, "en")
            assertEquals("Apple tier should match in $goal", ruTier, enTier)
        }
    }
}
