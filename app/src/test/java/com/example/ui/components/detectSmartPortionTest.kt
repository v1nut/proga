package com.example.ui.components

import com.example.data.local.InitialData
import com.example.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class detectSmartPortionTest {

    @Test
    fun testGreekYogurtPortion() {
        val yogurt = Product(name = "Греческий йогурт 2%", category = "Молочные продукты", calories = 67.0, protein = 9.0, fat = 2.0, carbs = 3.5)
        val portion = detectSmartPortion(yogurt)
        assertEquals("стаканчик", portion.unitName)
        assertEquals(150.0, portion.gramsPerUnit, 0.01)
    }

    @Test
    fun testCheesePortion() {
        val cheese = Product(name = "Сыр Российский 45%", category = "Молочные продукты", calories = 363.0, protein = 24.1, fat = 29.5, carbs = 0.3)
        val portion = detectSmartPortion(cheese)
        assertEquals("ломтик", portion.unitName)
        assertEquals(20.0, portion.gramsPerUnit, 0.01)
    }

    @Test
    fun testPeanutButterPortion() {
        val pb = Product(name = "Арахисовая паста без сахара", category = "Орехи и масла", calories = 588.0, protein = 25.0, fat = 50.0, carbs = 20.0)
        val portion = detectSmartPortion(pb)
        assertEquals("ст. ложка", portion.unitName)
        assertEquals(15.0, portion.gramsPerUnit, 0.01)
    }

    @Test
    fun testAllInitialDataProducts() {
        for (prod in InitialData.predefinedProducts) {
            val portion = detectSmartPortion(prod)
            assertNotNull(portion.unitName)
            assert(portion.gramsPerUnit > 0.0)
        }
    }
}
