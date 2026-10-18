package com.example.data.service

import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class OpenFoodFactsResult {
    data class Complete(val product: Product) : OpenFoodFactsResult()
    data class Incomplete(
        val name: String,
        val barcode: String,
        val calories: Double,
        val protein: Double,
        val fat: Double,
        val carbs: Double
    ) : OpenFoodFactsResult()
    object NotFound : OpenFoodFactsResult()
}

class OpenFoodFactsService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private data class ExtractedNutriments(
        val calories: Double?,
        val protein: Double?,
        val fat: Double?,
        val carbs: Double?,
        val isValidAndComplete: Boolean
    )

    private fun parseAndValidateNutriments(nutrimentsObj: JSONObject?): ExtractedNutriments {
        if (nutrimentsObj == null) {
            return ExtractedNutriments(null, null, null, null, false)
        }

        fun parseDoubleKey(vararg keys: String): Double? {
            for (k in keys) {
                if (nutrimentsObj.has(k) && !nutrimentsObj.isNull(k)) {
                    val d = nutrimentsObj.optDouble(k, Double.NaN)
                    if (d.isFinite() && d >= 0.0) return d
                }
            }
            return null
        }

        var kcal = parseDoubleKey("energy-kcal_100g", "energy-kcal_value", "energy-kcal")
        if (kcal == null || kcal <= 0.0) {
            val kj = parseDoubleKey("energy-kj_100g", "energy_100g", "energy_value")
            if (kj != null && kj > 0.0) {
                kcal = kj / 4.184
            }
        }

        val protein = parseDoubleKey("proteins_100g", "proteins_value", "proteins")
        val fat = parseDoubleKey("fat_100g", "fat_value", "fat")
        val carbs = parseDoubleKey("carbohydrates_100g", "carbohydrates_value", "carbohydrates")

        // Validation rules per 100g
        val validKcal = kcal != null && kcal in 0.0..950.0
        val hasAnyMacro = protein != null || fat != null || carbs != null
        val canDefaultMissingMacros = validKcal && (hasAnyMacro || kcal == 0.0)

        val finalProtein = if (canDefaultMissingMacros) (protein ?: 0.0) else protein
        val finalFat = if (canDefaultMissingMacros) (fat ?: 0.0) else fat
        val finalCarbs = if (canDefaultMissingMacros) (carbs ?: 0.0) else carbs

        val validProtein = finalProtein != null && finalProtein in 0.0..100.0
        val validFat = finalFat != null && finalFat in 0.0..100.0
        val validCarbs = finalCarbs != null && finalCarbs in 0.0..100.0
        val validSum = (finalProtein ?: 0.0) + (finalFat ?: 0.0) + (finalCarbs ?: 0.0) <= 100.5

        val isComplete = validKcal && validProtein && validFat && validCarbs && validSum

        return ExtractedNutriments(
            calories = if (validKcal) kcal else null,
            protein = if (validProtein) finalProtein else null,
            fat = if (validFat) finalFat else null,
            carbs = if (validCarbs) finalCarbs else null,
            isValidAndComplete = isComplete
        )
    }

    private fun executeFetchBarcodeUrl(url: String, trimmedCode: String): OpenFoodFactsResult {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KBZHURacion/1.18.1 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return OpenFoodFactsResult.NotFound
                val bodyString = response.body?.string() ?: return OpenFoodFactsResult.NotFound
                val json = JSONObject(bodyString)
                if (json.optInt("status") != 1) return OpenFoodFactsResult.NotFound

                val productObj = json.optJSONObject("product") ?: return OpenFoodFactsResult.NotFound
                val name = productObj.optString("product_name_ru").ifBlank {
                    productObj.optString("product_name").ifBlank { "Продукт $trimmedCode" }
                }.trim()

                val nutrimentsObj = productObj.optJSONObject("nutriments")
                val extracted = parseAndValidateNutriments(nutrimentsObj)

                if (extracted.isValidAndComplete) {
                    OpenFoodFactsResult.Complete(
                        Product(
                            name = name,
                            category = "Сканированное",
                            calories = extracted.calories!!,
                            protein = extracted.protein!!,
                            fat = extracted.fat!!,
                            carbs = extracted.carbs!!,
                            barcode = trimmedCode,
                            isCustom = true
                        )
                    )
                } else {
                    OpenFoodFactsResult.Incomplete(
                        name = name,
                        barcode = trimmedCode,
                        calories = extracted.calories ?: 0.0,
                        protein = extracted.protein ?: 0.0,
                        fat = extracted.fat ?: 0.0,
                        carbs = extracted.carbs ?: 0.0
                    )
                }
            }
        } catch (e: Exception) {
            OpenFoodFactsResult.NotFound
        }
    }

    suspend fun fetchByBarcode(barcode: String): OpenFoodFactsResult = withContext(Dispatchers.IO) {
        val trimmedCode = barcode.trim()
        if (trimmedCode.isBlank()) return@withContext OpenFoodFactsResult.NotFound

        val ruResult = executeFetchBarcodeUrl("https://ru.openfoodfacts.org/api/v2/product/$trimmedCode.json", trimmedCode)
        if (ruResult !is OpenFoodFactsResult.NotFound) {
            return@withContext ruResult
        }
        executeFetchBarcodeUrl("https://world.openfoodfacts.org/api/v2/product/$trimmedCode.json", trimmedCode)
    }

    private fun executeSearchUrl(url: String): List<Product> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "KBZHURacion/1.18.1 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val bodyString = response.body?.string() ?: return emptyList()
                val json = JSONObject(bodyString)
                val productsArray = json.optJSONArray("products") ?: return emptyList()

                val resultList = mutableListOf<Product>()
                for (i in 0 until productsArray.length()) {
                    val item = productsArray.optJSONObject(i) ?: continue
                    val name = item.optString("product_name_ru").ifBlank {
                        item.optString("product_name").ifBlank { "" }
                    }.trim()
                    if (name.isBlank()) continue

                    val nutrimentsObj = item.optJSONObject("nutriments")
                    val extracted = parseAndValidateNutriments(nutrimentsObj)
                    val barcode = item.optString("code")

                    if (extracted.isValidAndComplete) {
                        resultList.add(
                            Product(
                                name = name,
                                category = "Импорт (Open Food Facts)",
                                calories = extracted.calories!!,
                                protein = extracted.protein!!,
                                fat = extracted.fat!!,
                                carbs = extracted.carbs!!,
                                barcode = barcode.ifBlank { null },
                                isCustom = true
                            )
                        )
                    }
                }
                resultList
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchProductsOnline(query: String): List<Product> = withContext(Dispatchers.IO) {
        if (query.trim().length < 3) return@withContext emptyList()
        val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
        val ruList = executeSearchUrl("https://ru.openfoodfacts.org/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=8")
        if (ruList.isNotEmpty()) {
            return@withContext ruList
        }
        executeSearchUrl("https://world.openfoodfacts.org/cgi/search.pl?search_terms=$encoded&search_simple=1&action=process&json=1&page_size=8")
    }
}
