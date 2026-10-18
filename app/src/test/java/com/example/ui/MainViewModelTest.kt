package com.example.ui

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Product
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = MainViewModel(application)
    }

    private suspend fun waitForProduct(predicate: (Product) -> Boolean): Product? {
        repeat(20) {
            val list = viewModel.allProducts.first()
            val found = list.find(predicate)
            if (found != null) return found
            delay(100)
            shadowOf(Looper.getMainLooper()).idle()
        }
        return viewModel.allProducts.first().find(predicate)
    }

    @Test
    fun `openEditCustomProduct sets editingProduct and preserves category`() {
        val productA = Product(
            id = 10,
            name = "Куриная грудка",
            category = "Мясо и птица",
            calories = 165.0,
            protein = 31.0,
            fat = 3.6,
            carbs = 0.0,
            isCustom = true
        )

        viewModel.openEditCustomProduct(productA)

        val editing = viewModel.editingProduct.value
        assertNotNull(editing)
        assertEquals(10L, editing?.id)
        assertEquals("Куриная грудка", editing?.name)

        val prefilled = viewModel.prefilledProductData.value
        assertNotNull(prefilled)
        assertEquals("Куриная грудка", prefilled?.name)
        assertEquals("Мясо и птица", prefilled?.category)
    }

    @Test
    fun `closeCreateProduct resets editingProduct to null`() {
        val productA = Product(
            id = 11,
            name = "Сырники",
            category = "Перекусы и десерты",
            calories = 220.0,
            protein = 14.0,
            fat = 8.0,
            carbs = 22.0,
            isCustom = true
        )

        viewModel.openEditCustomProduct(productA)
        assertNotNull(viewModel.editingProduct.value)

        viewModel.closeCreateProduct()

        assertNull(viewModel.editingProduct.value)
        assertNull(viewModel.prefilledProductData.value)
        assertEquals(false, viewModel.isCreateProductOpen.value)
    }

    @Test
    fun `openCreateProduct and openCreateProductPrefilled reset editingProduct to null`() {
        val productA = Product(
            id = 12,
            name = "Омлет",
            category = "Молочные продукты",
            calories = 180.0,
            protein = 12.0,
            fat = 13.0,
            carbs = 2.0,
            isCustom = true
        )

        viewModel.openEditCustomProduct(productA)
        assertNotNull(viewModel.editingProduct.value)

        // Opening creation for a NEW product
        viewModel.openCreateProduct()
        assertNull(viewModel.editingProduct.value)

        // Re-set editing product and open creation prefilled for a NEW product
        viewModel.openEditCustomProduct(productA)
        assertNotNull(viewModel.editingProduct.value)

        viewModel.openCreateProductPrefilled(name = "Новый сок", barcode = "123456")
        assertNull(viewModel.editingProduct.value)
        assertEquals("Новый сок", viewModel.prefilledProductData.value?.name)
    }

    @Test
    fun `editing product A then creating product B results in two distinct products and preserves category`() = runBlocking {
        // Wait for repository initialization if needed
        waitForProduct { true }

        // 1. Create Product A
        viewModel.saveCustomProduct(
            name = "Индейка запеченная",
            category = "Мясо и птица",
            calories = 140.0,
            protein = 28.0,
            fat = 2.5,
            carbs = 0.0
        )

        val productA = waitForProduct { it.name == "Индейка запеченная" }
        assertNotNull("Product A should be created", productA)
        assertEquals("Мясо и птица", productA?.category)

        // 2. Edit Product A
        viewModel.openEditCustomProduct(productA!!)
        assertEquals(productA, viewModel.editingProduct.value)
        assertEquals("Мясо и птица", viewModel.prefilledProductData.value?.category)

        // Simulate MainActivity saving edited product A
        val currentEditing = viewModel.editingProduct.value
        assertNotNull(currentEditing)
        viewModel.updateCustomProduct(
            currentEditing!!.copy(
                name = "Индейка запеченная (филе)",
                calories = 145.0
            )
        )

        val updatedA = waitForProduct { it.name == "Индейка запеченная (филе)" }
        assertNotNull("Updated product A should be saved", updatedA)

        // Verify editingProduct was reset to null after update
        assertNull(viewModel.editingProduct.value)

        // 3. Create NEW Product B
        viewModel.openCreateProduct()
        assertNull("editingProduct must be null when creating product B", viewModel.editingProduct.value)

        viewModel.saveCustomProduct(
            name = "Яблоко Гренни Смит",
            category = "Фрукты и ягоды",
            calories = 52.0,
            protein = 0.3,
            fat = 0.2,
            carbs = 14.0
        )

        val productB = waitForProduct { it.name == "Яблоко Гренни Смит" }
        assertNotNull("New product B should be saved", productB)

        // 4. Verify both products exist and categories are correct
        val finalProducts = viewModel.allProducts.first()
        val foundUpdatedA = finalProducts.find { it.id == updatedA?.id || it.name == "Индейка запеченная (филе)" }
        val foundProductB = finalProducts.find { it.name == "Яблоко Гренни Смит" }

        assertNotNull("Updated product A must exist in list", foundUpdatedA)
        assertNotNull("New product B must exist in list", foundProductB)

        // Ensure category A was preserved and two separate products exist
        assertEquals("Мясо и птица", foundUpdatedA?.category)
        assertEquals("Фрукты и ягоды", foundProductB?.category)
    }

    @Test
    fun `logWeight rejects invalid and future dates`() = runBlocking {
        // Invalid date formats
        viewModel.logWeight(75.0, "2026-99-99")
        viewModel.logWeight(75.0, "вчера")
        viewModel.logWeight(75.0, "")

        // Future date
        val futureDate = java.time.LocalDate.now().plusDays(10).toString()
        viewModel.logWeight(75.0, futureDate)

        delay(200)
        shadowOf(Looper.getMainLooper()).idle()

        // Weight entries should not contain invalid or future dates
        val currentWeights = viewModel.weightEntries.value
        val futureFound = currentWeights.any { it.dateString == futureDate || it.dateString == "2026-99-99" || it.dateString == "вчера" }
        assertEquals(false, futureFound)
    }
}
