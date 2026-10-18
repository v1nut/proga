package com.example.ui.util

import com.example.data.model.Product
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartFoodSearchTest {

    private val eggProduct = Product(name = "Яйцо куриное (варёное / 100 г)", category = "Молочные продукты", calories = 155.0, protein = 13.0, fat = 11.0, carbs = 1.1)
    private val appleProduct = Product(name = "Яблоко свежее с кожурой", category = "Фрукты и ягоды", calories = 52.0, protein = 0.3, fat = 0.2, carbs = 13.8)
    private val pastaProduct = Product(name = "Макароны тв. сортов (отварные)", category = "Крупы и гарниры", calories = 158.0, protein = 5.8, fat = 0.9, carbs = 30.9)
    private val cucumberProduct = Product(name = "Огурцы свежие", category = "Овощи и зелень", calories = 15.0, protein = 0.8, fat = 0.1, carbs = 3.0)
    private val buckwheatProduct = Product(name = "Гречневая каша (отварная)", category = "Крупы и гарниры", calories = 110.0, protein = 4.2, fat = 1.3, carbs = 21.3)
    private val cheeseProduct = Product(name = "Сыр Российский 45%", category = "Молочные продукты", calories = 363.0, protein = 24.1, fat = 29.5, carbs = 0.3)
    private val cottageCheeseProduct = Product(name = "Творог 5%", category = "Молочные продукты", calories = 121.0, protein = 17.2, fat = 5.0, carbs = 1.8)
    private val yogurtProduct = Product(name = "Греческий йогурт 2%", category = "Молочные продукты", calories = 67.0, protein = 9.0, fat = 2.0, carbs = 3.5)
    private val milkProduct = Product(name = "Молоко 2.5%", category = "Молочные продукты", calories = 52.0, protein = 2.8, fat = 2.5, carbs = 4.7)
    private val bananaProduct = Product(name = "Банан свежий", category = "Фрукты и ягоды", calories = 89.0, protein = 1.1, fat = 0.3, carbs = 22.8)

    @Test
    fun testBuckwheatSearch() {
        assertTrue(SmartFoodSearch.matches(buckwheatProduct, "гречка"))
        assertFalse(SmartFoodSearch.matches(eggProduct, "гречка"))
    }

    @Test
    fun testCheeseSearch() {
        assertTrue(SmartFoodSearch.matches(cheeseProduct, "сыр"))
        assertFalse(SmartFoodSearch.matches(appleProduct, "сыр"))
    }

    @Test
    fun testCottageCheeseSearch() {
        assertTrue(SmartFoodSearch.matches(cottageCheeseProduct, "творог"))
        assertFalse(SmartFoodSearch.matches(pastaProduct, "творог"))
    }

    @Test
    fun testYogurtSearch() {
        assertTrue(SmartFoodSearch.matches(yogurtProduct, "йогурт"))
        assertFalse(SmartFoodSearch.matches(cucumberProduct, "йогурт"))
    }

    @Test
    fun testMilkSearch() {
        assertTrue(SmartFoodSearch.matches(milkProduct, "молоко"))
        assertFalse(SmartFoodSearch.matches(eggProduct, "молоко"))
        assertFalse(SmartFoodSearch.matches(cottageCheeseProduct, "молоко"))
    }

    @Test
    fun testBananaWithGramQuerySearch() {
        assertTrue(SmartFoodSearch.matches(bananaProduct, "банан 120г"))
    }
}
