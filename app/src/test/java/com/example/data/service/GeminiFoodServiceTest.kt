package com.example.data.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GeminiFoodServiceTest {

    private val service = GeminiFoodService()

    @Test
    fun testValidJsonParsing() {
        val json = """
            {
                "dishName": "Овсяная каша с яблоком",
                "estimatedGrams": 250,
                "calories": 220,
                "protein": 6.5,
                "fat": 3.2,
                "carbs": 42.0,
                "mealType": "BREAKFAST",
                "confidence": "Высокая",
                "items": [
                    {
                        "name": "Овсянка",
                        "grams": 200,
                        "calories": 170,
                        "protein": 5.5,
                        "fat": 3.0,
                        "carbs": 31.0
                    },
                    {
                        "name": "Яблоко",
                        "grams": 50,
                        "calories": 50,
                        "protein": 1.0,
                        "fat": 0.2,
                        "carbs": 11.0
                    }
                ]
            }
        """.trimIndent()

        val method = GeminiFoodService::class.java.getDeclaredMethod("parseRecognizedMealJson", String::class.java)
        method.isAccessible = true
        val result = method.invoke(service, json) as RecognizedMeal

        assertNotNull(result)
        assertEquals("Овсяная каша с яблоком", result.dishName)
        assertEquals(250, result.estimatedGrams)
        assertEquals(220, result.calories)
        assertEquals(2, result.items.size)
    }

    @Test
    fun testMissingMandatoryFieldsThrowsAiException() {
        val invalidJson = """
            {
                "dishName": "Неизвестное блюдо"
            }
        """.trimIndent()

        val method = GeminiFoodService::class.java.getDeclaredMethod("parseRecognizedMealJson", String::class.java)
        method.isAccessible = true

        try {
            method.invoke(service, invalidJson)
            fail("Expected exception for missing mandatory fields")
        } catch (e: java.lang.reflect.InvocationTargetException) {
            assertTrue("Target exception should be AiException", e.cause is AiException)
        }
    }
}
