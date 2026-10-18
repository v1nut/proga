package com.example.data.service

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.MealType
import com.example.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.roundToInt

sealed class AiError(val userMessage: String) {
    object NoNetwork : AiError("Нет интернета. Проверьте подключение к сети.")
    object Timeout : AiError("ИИ не ответил вовремя, попробуйте ещё раз.")
    object NotConfigured : AiError("Укажите API-ключ Gemini в Настройках приложения.")
    object QuotaExceeded : AiError("Превышен лимит запросов ИИ, попробуйте позже.")
    object BadResponse : AiError("Некорректный ответ от ИИ.")
    data class Unknown(val detailMessage: String = "Неизвестная ошибка ИИ.") : AiError(detailMessage)
}

class AiException(val error: AiError) : Exception(error.userMessage)

data class RecognizedMealItem(
    val name: String,
    val grams: Int,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

data class RecognizedMeal(
    val dishName: String,
    val estimatedGrams: Int,
    val calories: Int,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val mealType: MealType = MealType.LUNCH,
    val confidence: String = "Высокая",
    val description: String = "",
    val items: List<RecognizedMealItem> = emptyList(),
    val isEstimate: Boolean = false,
    val unrecognizedItems: List<String> = emptyList()
)

data class ProductScienceInfo(
    val productName: String,
    val summary: String,
    val healthBenefits: List<String>,
    val generalInfo: String,
    val recommendedIntake: String,
    val glycemicImpact: String,
    val cautions: String
)

class GeminiFoodService(private val context: Context? = null) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val photoClient = client.newBuilder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(35, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getApiKey(): String {
        val customKey = if (context != null) {
            com.example.data.security.EncryptedPrefs.getGeminiApiKey(context)
        } else ""

        if (customKey.isNotBlank()) {
            return customKey
        }

        val buildKey = try { BuildConfig.GEMINI_API_KEY.trim() } catch (e: Exception) { "" }
        if (buildKey.isNotBlank() && buildKey != "dummy_key" && buildKey != "YOUR_API_KEY" && buildKey != "null") {
            return buildKey
        }
        return ""
    }

    private fun executeGeminiRequest(
        apiKey: String,
        promptJson: JSONObject,
        isPhoto: Boolean = false
    ): Pair<Int, String> {
        val candidateModels = listOf("gemini-3.5-flash-lite", "gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.0-flash")
        val activeClient = if (isPhoto) photoClient else client
        val body = promptJson.toString().toRequestBody(jsonMediaType)

        var lastCode = 500
        var lastBody = ""

        for (model in candidateModels) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-goog-api-key", apiKey)
                .post(body)
                .build()

            try {
                val response = activeClient.newCall(request).execute()
                lastCode = response.code
                lastBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    return Pair(lastCode, lastBody)
                }

                if (lastCode == 401 || lastCode == 403 || lastCode == 400) {
                    return Pair(lastCode, lastBody)
                }

                if (lastCode == 404 || lastCode == 429 || lastCode == 503) {
                    continue
                }

                return Pair(lastCode, lastBody)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("GEMINI_FALLBACK", "Model $model network exception", e)
                }
            }
        }
        return Pair(lastCode, lastBody)
    }

    suspend fun analyzeMealPhoto(bitmap: Bitmap): Result<RecognizedMeal> = withContext(Dispatchers.IO) {
        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(AiException(AiError.NotConfigured))
            }

            val base64Image = bitmapToBase64(bitmap)
            val prompt = """
                Ты эксперт-диетолог и нутрициолог.
                Внимательно проанализируй это фото блюда или комплексного приёма пищи.
                СТРОГОЕ ПРАВИЛО: Если на фото расположено несколько отдельных продуктов или блюд (например: суп, котлета, гарнир, салат, хлеб, напиток), ОДНОЗНАЧНО РАЗДЕЛИ их на ОТДЕЛЬНЫЕ независимые элементы в массиве "items" с индивидуальным названием на русском языке, весом в граммах, калориями и БЖУ для каждого продукта.
                Верни ответ ИСКЛЮЧИТЕЛЬНО в формате чистого JSON:
                {
                    "dishName": "Общее название приёма пищи",
                    "estimatedGrams": 450,
                    "calories": 650,
                    "protein": 35.0,
                    "fat": 20.0,
                    "carbs": 75.0,
                    "mealType": "LUNCH",
                    "confidence": "Высокая",
                    "description": "Описание комплексного приёма пищи",
                    "items": [
                        {
                            "name": "Блюдо/Продукт 1",
                            "grams": 200,
                            "calories": 300,
                            "protein": 20.0,
                            "fat": 10.0,
                            "carbs": 30.0
                        },
                        {
                            "name": "Блюдо/Продукт 2",
                            "grams": 150,
                            "calories": 200,
                            "protein": 10.0,
                            "fat": 8.0,
                            "carbs": 25.0
                        }
                    ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                val inlineData = JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                }
                                put("inlineData", inlineData)
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val (responseCode, responseString) = try {
                executeGeminiRequest(apiKey, requestJson, isPhoto = true)
            } catch (e: Exception) {
                return@withContext Result.failure(mapExceptionToAiException(e))
            }

            if (responseCode != 200) {
                val err = when (responseCode) {
                    429 -> AiError.QuotaExceeded
                    401, 403 -> AiError.NotConfigured
                    else -> AiError.BadResponse
                }
                return@withContext Result.failure(AiException(err))
            }

            val rawText = extractTextFromGeminiResponse(responseString)
            val cleanJson = cleanJsonString(rawText)
            val parsed = parseRecognizedMealJson(cleanJson)
            Result.success(parsed)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            Result.failure(mapExceptionToAiException(e))
        }
    }

    suspend fun parseQuickMealText(userInput: String): Result<RecognizedMeal> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) {
            val fallback = fallbackQuickTextAnalysis(userInput)
            if (fallback.items.isNotEmpty()) {
                return@withContext Result.success(fallback)
            }
            return@withContext Result.failure(AiException(AiError.NotConfigured))
        }

        try {
            val prompt = """
                Ты эксперт-диетолог и умный двуязычный (RU + EN) парсер приёмов пищи для фитнес-трекера.
                Пользователь ввёл фразу: "$userInput"
                Внимательно определи ВСЕ продукты и блюда на русском или английском языке.
                Верни ответ ИСКЛЮЧИТЕЛЬНО в формате чистого JSON:
                {
                    "mealType": "BREAKFAST",
                    "dishName": "Название блюда",
                    "estimatedGrams": 200,
                    "calories": 250,
                    "protein": 15.0,
                    "fat": 8.0,
                    "carbs": 30.0,
                    "confidence": "Высокая",
                    "description": "Краткое описание",
                    "items": [
                        {
                            "name": "Название продукта",
                            "grams": 200,
                            "calories": 250,
                            "protein": 15.0,
                            "fat": 8.0,
                            "carbs": 30.0
                        }
                    ]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val (responseCode, responseString) = try {
                executeGeminiRequest(apiKey, requestJson, isPhoto = false)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("GEMINI_ERROR", "Network failure, trying fallback", e)
                }
                val fallback = fallbackQuickTextAnalysis(userInput)
                if (fallback.items.isNotEmpty()) {
                    return@withContext Result.success(fallback)
                }
                return@withContext Result.failure(mapExceptionToAiException(e))
            }

            if (responseCode != 200) {
                val fallback = fallbackQuickTextAnalysis(userInput)
                if (fallback.items.isNotEmpty()) {
                    return@withContext Result.success(fallback)
                }
                val err = when (responseCode) {
                    429 -> AiError.QuotaExceeded
                    401, 403 -> AiError.NotConfigured
                    else -> AiError.BadResponse
                }
                return@withContext Result.failure(AiException(err))
            }

            val rawText = extractTextFromGeminiResponse(responseString)
            val cleanJson = cleanJsonString(rawText)
            val parsed = try {
                parseRecognizedMealJson(cleanJson)
            } catch (e: Exception) {
                val fallback = fallbackQuickTextAnalysis(userInput)
                if (fallback.items.isNotEmpty()) fallback else throw e
            }
            Result.success(parsed)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace()
            }
            val fallback = fallbackQuickTextAnalysis(userInput)
            if (fallback.items.isNotEmpty()) {
                return@withContext Result.success(fallback)
            }
            Result.failure(mapExceptionToAiException(e))
        }
    }

    fun getCuratedLocalScienceInfo(product: Product): ProductScienceInfo? {
        val lower = product.name.lowercase().trim()

        return when {
            // 1. Chicken / Курица / Филе
            lower.contains("куриц") || lower.contains("курин") || lower.contains("цыплен") || lower.contains("chicken") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Куриное филе — ценный диетический источник белка с высоким содержанием аминокислот и минимальным количеством жиров.",
                    healthBenefits = listOf(
                        "Полноценный легкоусвояемый белок для роста и восстановления мышц",
                        "Богат витаминами группы B (B3, B6, B12) и фосфором"
                    ),
                    generalInfo = "Содержит около ${product.protein}г белка и ${product.calories.roundToInt()} ккал на 100 г.",
                    recommendedIntake = "150-200 г в один приём пищи",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Без выраженных противопоказаний"
                )

            // 2. Turkey / Индейка
            lower.contains("индейк") || lower.contains("turkey") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Филе индейки — нежирное гипоаллергенное мясо с богатым аминокислотным профилем и высоким содержанием триптофана.",
                    healthBenefits = listOf(
                        "Низкое содержание насыщенных жиров и холестерина",
                        "Содержит селен и триптофан, улучшающие качество сна и настроение"
                    ),
                    generalInfo = "Содержит порядка ${product.protein}г белка на 100 г с мизерным содержанием жира.",
                    recommendedIntake = "150-200 г в порции",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Без выраженных противопоказаний"
                )

            // 3. Beef / Говядина
            lower.contains("говяд") || lower.contains("beef") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Постная говядина — ключевой источник гемового железа, цинка и креатина для кроветворения и силовой выносливости.",
                    healthBenefits = listOf(
                        "Высокая биодоступность гемового железа для профилактики анемии",
                        "Содержит цинк, B12 и природный креатин"
                    ),
                    generalInfo = "Обеспечивает ${product.protein}г качественного белка на 100 г.",
                    recommendedIntake = "120-180 г 2-3 раза в неделю",
                    glycemicImpact = "Низкий (0)",
                    cautions = "При заболеваниях почек стоит контролировать порцию"
                )

            // 4. Salmon / Лосось / Сёмга / Горбуша / Рыба
            lower.contains("лосось") || lower.contains("семга") || lower.contains("горбуш") || lower.contains("salmon") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Красная рыба — богатейший источник омега-3 полиненасыщенных жирных кислот и витамина D для здоровья сердца и сосудов.",
                    healthBenefits = listOf(
                        "Омега-3 Кислоты (EPA/DHA) снижают уровень системного воспаления",
                        "Высокое содержание легкоусвояемого белка и витамина D"
                    ),
                    generalInfo = "Содержит ${product.protein}г белка и ${product.fat}г полезных жиров на 100 г.",
                    recommendedIntake = "130-180 г 2 раза в неделю",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Без выраженных противопоказаний"
                )

            // 5. Tuna / Тунец
            lower.contains("тунец") || lower.contains("tuna") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Тунец — высокобелковая морская рыба с практически нулевым содержанием жира.",
                    healthBenefits = listOf(
                        "Максимальная концентрация белка на калорию",
                        "Источник селена и витаминов группы B"
                    ),
                    generalInfo = "Содержит до ${product.protein}г белка на 100 г порции.",
                    recommendedIntake = "100-150 г",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Умеренное употребление консервов"
                )

            // 6. Pollack / Минтай
            lower.contains("минта") || lower.contains("pollock") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Минтай — нежирная белая рыба с мягким диетическим профилем и высоким содержанием йода.",
                    healthBenefits = listOf(
                        "Легко усваивается, идеально подходит для диетического питания",
                        "Богат йодом, фосфором и кобальтом"
                    ),
                    generalInfo = "Содержит ${product.protein}г белка и меньше 1г жира на 100г.",
                    recommendedIntake = "150-200 г",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Без выраженных противопоказаний"
                )

            // 7. Buckwheat / Гречка
            lower.contains("гречк") || lower.contains("гречн") || lower.contains("buckwheat") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Гречневая крупа — уникальный ценный злак с низким гликемическим индексом, богатый растительным белком и рутином.",
                    healthBenefits = listOf(
                        "Не содержит глютен, содержит медленные углеводы для долгой сытости",
                        "Богата магнием, железом и флавоноидами"
                    ),
                    generalInfo = "Обеспечивает ${product.carbs}г сложных углеводов и ${product.protein}г белка на 100г.",
                    recommendedIntake = "150-200 г в готовом виде",
                    glycemicImpact = "Низкий (40-50)",
                    cautions = "Без выраженных противопоказаний"
                )

            // 8. Rice / Рис
            lower.contains("рис") || lower.contains("rice") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Отварной рис — классический гипоаллергенный источник углеводов для восполнения гликогена.",
                    healthBenefits = listOf(
                        "Легко усваивается и не раздражает слизистую ЖКТ",
                        "Сложные углеводы обеспечивают равномерный приток энергии"
                    ),
                    generalInfo = "Содержит ${product.carbs}г углеводов на 100 г готового блюда.",
                    recommendedIntake = "150-200 г в готовом виде",
                    glycemicImpact = "Средний",
                    cautions = "Без выраженных противопоказаний"
                )

            // 9. Oats / Овсянка / Геркулес
            lower.contains("овсян") || lower.contains("геркулес") || lower.contains("oat") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Овсяные хлопья — идеальный вариант для завтрака, богатый растворимой клетчаткой (бета-глюканом).",
                    healthBenefits = listOf(
                        "Бета-глюкан снижает уровень холестерина и стабилизирует сахар в крови",
                        "Улучшает моторику кишечника и даёт длительное насыщение"
                    ),
                    generalInfo = "Содержит ${product.carbs}г углеводов и ${product.protein}г белка на 100 г.",
                    recommendedIntake = "40-60 г сухих хлопьев или 200г каши",
                    glycemicImpact = "Низкий",
                    cautions = "При целиакии выбирать марки без глютена"
                )

            // 10. Pasta / Macaroni / Макароны / Спагетти
            lower.contains("макарон") || lower.contains("спагетти") || lower.contains("паст") || lower.contains("pasta") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Макаронные изделия из твёрдых сортов пшеницы — качественный источник сложных углеводов с высоким содержанием растительного белка.",
                    healthBenefits = listOf(
                        "Твёрдые сорта пшеницы имеют плотную структуру и не вызывают резких скачков сахара",
                        "Обеспечивают стабильное поступление энергии для физических нагрузок"
                    ),
                    generalInfo = "Содержит порядка ${product.carbs}г сложных углеводов на 100г.",
                    recommendedIntake = "150-200 г в готовом виде",
                    glycemicImpact = "Низкий / Средний",
                    cautions = "Содержит глютен"
                )

            // 11. Potato / Картофель
            lower.contains("картоф") || lower.contains("картош") || lower.contains("potato") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Отварной картофель — источник калия, крахмала и витамина C с высокой степенью насыщения.",
                    healthBenefits = listOf(
                        "Богат калием, регулирующим водно-солевой баланс и давление",
                        "Обогащён устойчивым крахмалом (особенно в охлаждённом виде)"
                    ),
                    generalInfo = "Содержит ${product.calories.roundToInt()} ккал и ${product.carbs}г углеводов на 100 г.",
                    recommendedIntake = "150-200 г",
                    glycemicImpact = "Средний / Высокий",
                    cautions = "Предпочтителен отварной или запечённый вид без жирных соусов"
                )

            // 12. Egg / Яйцо / Белок
            lower.contains("яйц") || lower.contains("омлет") || lower.contains("egg") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Куриное яйцо — эталонный продукт по аминокислотному составу и усвояемости белка.",
                    healthBenefits = listOf(
                        "Идеальный аминокислотный профиль для мышечного синтеза",
                        "Желток содержит холин для мозга, лецитин и лютеин"
                    ),
                    generalInfo = "Одно среднее яйцо даёт 6-7г идеального белка и полезные жиры.",
                    recommendedIntake = "2-3 шт в день",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Индивидуальная непереносимость"
                )

            // 13. Cottage cheese / Творог
            lower.contains("творог") || lower.contains("cottage") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Творог — традиционный кисломолочный продукт с высоким содержанием казеинового белка длительного усвоения.",
                    healthBenefits = listOf(
                        "Казеин медленно расщепляется, обеспечивая мышцы аминокислотами на протяжении 6-8 часов",
                        "Богат кальцием и фосфором для укрепления костной ткани"
                    ),
                    generalInfo = "Содержит от ${product.protein}г белка на 100 г порции.",
                    recommendedIntake = "150-200 г",
                    glycemicImpact = "Низкий",
                    cautions = "Без выраженных противопоказаний"
                )

            // 14. Milk / Kefir / Yogurt / Молоко / Кефир / Йогурт / Ряженка / Сметана
            lower.contains("молок") || lower.contains("кефир") || lower.contains("йогурт") || lower.contains("ряженк") || lower.contains("сметан") || lower.contains("milk") || lower.contains("yogurt") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Молочные и ферментированные продукты — важный источник пробиотиков, кальция и сывороточного белка.",
                    healthBenefits = listOf(
                        "Пробиотики в кефире и йогурте поддерживают здоровый микробиом кишечника",
                        "Кальций и витамин B2 укрепляют опорно-двигательный аппарат"
                    ),
                    generalInfo = "Содержит ${product.protein}г белка и ${product.calories.roundToInt()} ккал на 100 г.",
                    recommendedIntake = "200-250 мл в день",
                    glycemicImpact = "Низкий",
                    cautions = "При лактазной недостаточности выбирать безлактозные версии"
                )

            // 15. Cheese / Сыр
            Regex("""\bсыр\b|\bсыра\b|\bсыром\b|\bсыры\b|\bcheese\b""", RegexOption.IGNORE_CASE).containsMatchIn(lower) ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Твёрдые и полутвёрдые сыры — концентрированный источник молочного белка и кальция.",
                    healthBenefits = listOf(
                        "Высокая плотность белка и фосфора на минимальный объем",
                        "Практически полностью отсутствует лактоза"
                    ),
                    generalInfo = "Содержит около ${product.protein}г белка и ${product.fat}г жиров на 100 г.",
                    recommendedIntake = "30-50 г в день",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Калорийный продукт, контролировать жиры"
                )

            // 16. Avocado / Авокадо
            lower.contains("авокадо") || lower.contains("avocado") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Авокадо — фрукт с высокой концентрацией мононенасыщенных жиров (олеиновой кислоты) и клетчатки.",
                    healthBenefits = listOf(
                        "Снижает 'плохой' холестерин и укрепляет сосудистую стенку",
                        "Содержит калий, витамин E и фагоцитарные антиоксиданты"
                    ),
                    generalInfo = "Содержит ${product.fat}г полезных жиров и ${product.calories.roundToInt()} ккал на 100 г.",
                    recommendedIntake = "1/2 шт (70-100 г) в день",
                    glycemicImpact = "Низкий",
                    cautions = "Высокая калорийность"
                )

            // 17. Tomato / Cucumber / Broccoli / Помидоры / Огурцы / Брокколи / Овощи
            lower.contains("помидор") || lower.contains("томат") || lower.contains("огур") || lower.contains("броккол") || lower.contains("шпинат") || lower.contains("перец") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Свежие овощи и зелень — основа рациона с минимальной калорийностью и максимумом клетчатки и ликопина.",
                    healthBenefits = listOf(
                        "Низкая энергетическая плотность с объёмным чувством насыщения",
                        "Богаты витамином C, ликопином и пищевыми волокнами"
                    ),
                    generalInfo = "Всего ${product.calories.roundToInt()} ккал на 100 г с обилием минералов.",
                    recommendedIntake = "200-300 г в день",
                    glycemicImpact = "Очень низкий",
                    cautions = "Без выраженных противопоказаний"
                )

            // 18. Watermelon / Melon / Арбуз / Дыня
            lower.contains("арбуз") || lower.contains("дыня") || lower.contains("watermelon") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Арбуз — освежающая бахчевая культура, содержащая аминокислоту цитруллин и антиоксидант ликопин.",
                    healthBenefits = listOf(
                        "Цитруллин улучшает кровоток и расширяет сосуды",
                        "Отлично гидратирует организм за счет высокого содержания влаги"
                    ),
                    generalInfo = "Всего ${product.calories.roundToInt()} ккал на 100 г при отменном вкусе.",
                    recommendedIntake = "200-300 г в порции",
                    glycemicImpact = "Низкая гликемическая нагрузка при нормальной порции",
                    cautions = "Учитывать объём сахаров при диабете"
                )

            // 19. Banana / Apple / Orange / Berries / Банан / Яблоко / Апельсин / Клубника / Ягоды
            lower.contains("банан") || lower.contains("яблок") || lower.contains("апельсин") || lower.contains("клубник") || lower.contains("черник") || lower.contains("голубик") || lower.contains("ягод") || lower.contains("banana") || lower.contains("apple") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Свежие фрукты и ягоды — естественный источник фруктозы, пектина и природных антиоксидантов.",
                    healthBenefits = listOf(
                        "Пектин улучшает выведение токсинов и работу кишечника",
                        "Защищают клетки от окислительного стресса"
                    ),
                    generalInfo = "Содержит ${product.carbs}г природных сахаров на 100 г.",
                    recommendedIntake = "100-200 г в день",
                    glycemicImpact = "Низкий / Средний",
                    cautions = "Употреблять в умеренном количестве"
                )

            // 20. Oil / Butter / Масло
            lower.contains("масло") || lower.contains("oil") || lower.contains("butter") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Пищевые растительные и сливочные масла — концентрированный источник жирных кислот и жирорастворимых витаминов A, E, D.",
                    healthBenefits = listOf(
                        "Способствуют усвоению жирорастворимых витаминов из овощей",
                        "Обеспечивают синтез гормонов и мембран клеток"
                    ),
                    generalInfo = "Калорийный продукт: около ${product.calories.roundToInt()} ккал на 100 г.",
                    recommendedIntake = "10-20 г в день",
                    glycemicImpact = "Низкий (0)",
                    cautions = "Строго дозировать порции"
                )

            // 21. Nuts / Almonds / Peanuts / Орехи / Миндаль / Арахис
            lower.contains("орех") || lower.contains("миндаль") || lower.contains("арахис") || lower.contains("nut") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Орехи и пасты из них — питательный продукт с высоким содержанием растительного белка, полезных жиров и магния.",
                    healthBenefits = listOf(
                        "Магний и ненасыщенные жирные кислоты укрепляют ЦНС и сердце",
                        "Быстро утоляют голод даже небольшой порцией"
                    ),
                    generalInfo = "Питательный продукт: ${product.protein}г белка и ${product.fat}г жиров на 100г.",
                    recommendedIntake = "20-30 г в день",
                    glycemicImpact = "Низкий",
                    cautions = "Сильный аллерген"
                )

            // 22. Bread / Lavash / Crispbread / Хлеб / Лаваш / Хлебцы
            lower.contains("хлеб") || lower.contains("лаваш") || lower.contains("хлебц") || lower.contains("bread") ->
                ProductScienceInfo(
                    productName = product.name,
                    summary = "Хлебобулочные изделия — традиционный источник сложного крахмала и пищевых волокон для ежедневного рациона.",
                    healthBenefits = listOf(
                        "Пищевые волокна цельного зерна стимулируют нормальное пищеварение",
                        "Быстро восстанавливают физическую энергию"
                    ),
                    generalInfo = "Содержит около ${product.carbs}г углеводов на 100г.",
                    recommendedIntake = "50-100 г в день",
                    glycemicImpact = "Средний",
                    cautions = "Содержит глютен"
                )

            else -> null
        }
    }

    suspend fun getNutritionalScienceAdvice(product: Product): Result<ProductScienceInfo> = withContext(Dispatchers.IO) {
        val curatedLocalInfo = getCuratedLocalScienceInfo(product)
        if (curatedLocalInfo != null) {
            return@withContext Result.success(curatedLocalInfo)
        }

        try {
            val apiKey = getApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(AiException(AiError.NotConfigured))
            }

            val prompt = """
                Дай объективное справочное описание продукта "${product.name}" (категория: ${product.category}, калорийность: ${product.calories} ккал/100г, БЖУ: ${product.protein}/${product.fat}/${product.carbs}).
                ВАЖНОЕ ПРАВИЛО: НЕ выдумывай названия исследований, журналы или медицинские организации (ВОЗ, PubMed, EFSA). Давай только фактическую информацию о питательной ценности и свойствах продукта.
                Верни ответ ИСКЛЮЧИТЕЛЬНО в формате чистого JSON:
                {
                    "productName": "${product.name}",
                    "summary": "Краткая справочная информация о продукте (2-3 предложения)",
                    "healthBenefits": [
                        "Питательное свойство 1",
                        "Питательное свойство 2"
                    ],
                    "generalInfo": "Общие сведения о пищевой ценности",
                    "recommendedIntake": "Ориентировочная порция в рационе",
                    "glycemicImpact": "Низкий/Средний/Высокий",
                    "cautions": "Особенности употребления или 'Без выраженных противопоказаний'"
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val (responseCode, responseString) = try {
                executeGeminiRequest(apiKey, requestJson, isPhoto = false)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("GEMINI_ERROR", "Product info network failure", e)
                }
                return@withContext Result.failure(mapExceptionToAiException(e))
            }

            if (responseCode != 200) {
                val err = when (responseCode) {
                    429 -> AiError.QuotaExceeded
                    401, 403 -> AiError.NotConfigured
                    else -> AiError.BadResponse
                }
                return@withContext Result.failure(AiException(err))
            }

            val rawText = extractTextFromGeminiResponse(responseString)
            val cleanJson = cleanJsonString(rawText)
            val json = JSONObject(cleanJson)

            val benefitsList = mutableListOf<String>()
            val benefitsJson = json.optJSONArray("healthBenefits")
            if (benefitsJson != null) {
                for (i in 0 until benefitsJson.length()) {
                    benefitsList.add(benefitsJson.getString(i))
                }
            }

            val info = ProductScienceInfo(
                productName = json.optString("productName", product.name),
                summary = json.optString("summary", "").ifBlank { "Пищевой продукт с содержанием ${product.calories.roundToInt()} ккал/100г." },
                healthBenefits = if (benefitsList.isNotEmpty()) benefitsList else listOf("Источник питательных веществ"),
                generalInfo = json.optString("generalInfo", "Содержит белки, жиры и углеводы в составе порции."),
                recommendedIntake = json.optString("recommendedIntake", "100-200 г в день."),
                glycemicImpact = json.optString("glycemicImpact", "Умеренный"),
                cautions = json.optString("cautions", "Без выраженных противопоказаний")
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(mapExceptionToAiException(e))
        }
    }

    fun fallbackQuickTextAnalysis(input: String): RecognizedMeal {
        val lower = input.lowercase().trim()

        val mealType = when {
            lower.contains("завтрак") || lower.contains("breakfast") -> MealType.BREAKFAST
            lower.contains("обед") || lower.contains("lunch") -> MealType.LUNCH
            lower.contains("ужин") || lower.contains("dinner") -> MealType.DINNER
            lower.contains("перекус") || lower.contains("полдник") || lower.contains("snack") -> MealType.SNACK
            else -> MealType.LUNCH
        }

        val clauseRegex = Regex("""\s+(?:и|and|with)\s+|\s*[,+;]\s*""", RegexOption.IGNORE_CASE)
        val clauses = lower.split(clauseRegex)
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "завтрак" && it != "обед" && it != "ужин" && it != "перекус" }

        val items = mutableListOf<RecognizedMealItem>()
        val unrecognized = mutableListOf<String>()

        for (clause in clauses) {
            val gramsMatch = Regex("""(\d+)\s*(?:г|гр|мл|g|gr|gram|grams|ml)(?=\s+|$|[^a-zа-я0-9])""", RegexOption.IGNORE_CASE).find(clause)
            val explicitGrams = gramsMatch?.groupValues?.get(1)?.toIntOrNull()

            var matched = false

            // 1. Tea without sugar / Чай без сахара
            if (clause.contains("чай")) {
                matched = true
                val g = explicitGrams ?: 200
                val isSugarFree = clause.contains("без сахара") || clause.contains("без сах") || clause.contains("без сахар")
                if (isSugarFree) {
                    items.add(RecognizedMealItem("Чай без сахара", g, 2, 0.0, 0.0, 0.2))
                } else {
                    items.add(RecognizedMealItem("Чай с сахаром", g, 40, 0.0, 0.0, 10.0))
                }
            }
            // 2. Cutlet / Котлета
            else if (clause.contains("котлет") || clause.contains("cutlet")) {
                matched = true
                val g = explicitGrams ?: 120
                val factor = g / 100.0
                val isChicken = clause.contains("курин") || clause.contains("куриц") || clause.contains("chicken")
                val title = if (isChicken) "Куриная котлета" else "Котлета мясная"
                val cal = if (isChicken) 162.0 * factor else 240.0 * factor
                items.add(RecognizedMealItem(title, g, cal.roundToInt(), if (isChicken) 18.0 * factor else 15.0 * factor, if (isChicken) 8.5 * factor else 16.0 * factor, 3.0 * factor))
            }
            // 3. Shawarma / Burger / Pizza / Fastfood
            else if (clause.contains("шаурм") || clause.contains("шаверм") || clause.contains("бургер") || clause.contains("пицц") || clause.contains("shawarma") || clause.contains("burger") || clause.contains("pizza")) {
                matched = true
                val g = explicitGrams ?: 250
                val factor = g / 100.0
                val title = if (clause.contains("бургер")) "Бургер" else if (clause.contains("пицц")) "Пицца" else "Шаурма с курицей"
                items.add(RecognizedMealItem(title, g, (232.0 * factor).roundToInt(), 11.0 * factor, 10.5 * factor, 23.5 * factor))
            }
            // 4. Turkey breast / Грудка индейки / Филе индейки
            else if (clause.contains("индейк")) {
                matched = true
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(RecognizedMealItem("Филе индейки", g, (120.0 * factor).roundToInt(), 25.0 * factor, 2.0 * factor, 0.0))
            }
            // 5. Pollack fillet / Филе минтая
            else if (clause.contains("минта")) {
                matched = true
                val g = explicitGrams ?: 150
                val factor = g / 100.0
                items.add(RecognizedMealItem("Филе минтая", g, (72.0 * factor).roundToInt(), 16.0 * factor, 0.8 * factor, 0.0))
            }
            // 6. Raw egg / Сырое яйцо vs Egg / Яйцо
            else if (clause.contains("яйц") || clause.contains("омлет") || clause.contains("egg")) {
                matched = true
                val countMatch = Regex("""(\d+)\s*(?:шт|штуки|штук|pc|pcs)?\s*(?:яй|egg)""", RegexOption.IGNORE_CASE).find(clause)
                val count = countMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
                val g = explicitGrams ?: (count * 55)
                val isRaw = clause.contains("сыро") || clause.contains("сырых")
                val title = if (isRaw) "Яйцо сырое ($count шт)" else "Яйцо куриное ($count шт)"
                items.add(RecognizedMealItem(title, g, count * 78, count * 6.5, count * 5.5, count * 0.4))
            }
            // 7. Cheese / Сыр (word boundary so it doesn't match сырое)
            else if (Regex("""\bсыр\b|\bсыра\b|\bсыром\b|\bсыры\b|\bcheese\b""", RegexOption.IGNORE_CASE).containsMatchIn(clause)) {
                matched = true
                val g = explicitGrams ?: 40
                val factor = g / 100.0
                items.add(RecognizedMealItem("Сыр твёрдый", g, (350.0 * factor).roundToInt(), 24.0 * factor, 28.0 * factor, 0.5 * factor))
            }
            // 8. Cottage cheese / Творог
            else if (clause.contains("творог") || clause.contains("cottage")) {
                matched = true
                val g = explicitGrams ?: 150
                val factor = g / 100.0
                items.add(RecognizedMealItem("Творог 5%", g, (121.0 * factor).roundToInt(), 17.2 * factor, 5.0 * factor, 1.8 * factor))
            }
            // 9. Banana / Банан
            else if (clause.contains("банан") || clause.contains("banana")) {
                matched = true
                val g = explicitGrams ?: 120
                val factor = g / 100.0
                items.add(RecognizedMealItem("Банан свежий", g, (89.0 * factor).roundToInt(), 1.1 * factor, 0.3 * factor, 22.8 * factor))
            }
            // 10. Watermelon / Арбуз
            else if (clause.contains("арбуз") || clause.contains("watermelon")) {
                matched = true
                val g = explicitGrams ?: 300
                val factor = g / 100.0
                items.add(RecognizedMealItem("Арбуз свежий", g, (30.0 * factor).roundToInt(), 0.6 * factor, 0.2 * factor, 7.6 * factor))
            }
            // 11. Melon / Дыня
            else if (clause.contains("дыня") || clause.contains("дын") || clause.contains("melon")) {
                matched = true
                val g = explicitGrams ?: 250
                val factor = g / 100.0
                items.add(RecognizedMealItem("Дыня свежая", g, (34.0 * factor).roundToInt(), 0.8 * factor, 0.2 * factor, 8.2 * factor))
            }
            // 12. Oats / Овсянка
            else if (clause.contains("овсянк") || clause.contains("геркулес") || clause.contains("oat")) {
                matched = true
                val g = explicitGrams ?: 200
                val factor = g / 100.0
                items.add(RecognizedMealItem("Овсяная каша", g, (102.0 * factor).roundToInt(), 3.5 * factor, 2.0 * factor, 17.5 * factor))
            }
            // 13. Apple / Яблоко
            else if (clause.contains("яблок") || clause.contains("apple")) {
                matched = true
                val g = explicitGrams ?: 150
                val factor = g / 100.0
                items.add(RecognizedMealItem("Яблоко свежее", g, (52.0 * factor).roundToInt(), 0.3 * factor, 0.2 * factor, 13.8 * factor))
            }
            // 14. Chicken / Курица (if not cutlet)
            else if (clause.contains("куриц") || clause.contains("курин") || clause.contains("chicken")) {
                matched = true
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(RecognizedMealItem("Куриное филе отварное", g, (165.0 * factor).roundToInt(), 31.0 * factor, 3.6 * factor, 0.0))
            }
            // 15. Potato / Картофель (NOT just "пюре")
            else if (clause.contains("картоф") || clause.contains("картош") || clause.contains("картофельное пюре")) {
                matched = true
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(RecognizedMealItem("Картофель отварной", g, (86.0 * factor).roundToInt(), 2.0 * factor, 0.4 * factor, 18.0 * factor))
            }
            // 16. Buckwheat / Гречка
            else if (clause.contains("гречк") || clause.contains("buckwheat")) {
                matched = true
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(RecognizedMealItem("Гречневая каша", g, (110.0 * factor).roundToInt(), 4.2 * factor, 1.3 * factor, 21.3 * factor))
            }
            // 17. Rice / Рис
            else if (clause.contains("рис") || clause.contains("rice")) {
                matched = true
                val g = explicitGrams ?: 180
                val factor = g / 100.0
                items.add(RecognizedMealItem("Рис отварной", g, (130.0 * factor).roundToInt(), 2.7 * factor, 0.3 * factor, 28.2 * factor))
            }
            // 18. Coffee / Кофе
            else if (clause.contains("кофе") || clause.contains("coffee")) {
                matched = true
                val g = explicitGrams ?: 200
                items.add(RecognizedMealItem("Кофе с молоком", g, 35, 1.5, 1.2, 4.0))
            }

            if (!matched) {
                unrecognized.add(clause)
            }
        }

        val totalGrams = items.sumOf { it.grams }
        val totalCal = items.sumOf { it.calories }
        val totalP = items.sumOf { it.protein }
        val totalF = items.sumOf { it.fat }
        val totalC = items.sumOf { it.carbs }

        val summary = if (items.isNotEmpty()) {
            items.joinToString(" и ") { it.name }
        } else if (unrecognized.isNotEmpty()) {
            "Не распознано: ${unrecognized.joinToString(", ")}"
        } else {
            input
        }

        return RecognizedMeal(
            dishName = summary,
            estimatedGrams = totalGrams,
            calories = totalCal,
            protein = totalP,
            fat = totalF,
            carbs = totalC,
            mealType = mealType,
            confidence = "Офлайн-оценка",
            description = if (unrecognized.isNotEmpty()) "Не распознано: ${unrecognized.joinToString(", ")}" else "Офлайн-анализ",
            items = items,
            isEstimate = true,
            unrecognizedItems = unrecognized
        )
    }

    private fun mapExceptionToAiException(e: Exception): AiException {
        if (e is AiException) return e
        return when (e) {
            is SocketTimeoutException, is InterruptedIOException, is TimeoutException -> {
                AiException(AiError.Unknown("Сервер Gemini не ответил вовремя. Если вы в РФ, включите VPN на устройстве."))
            }
            is UnknownHostException, is ConnectException, is NoRouteToHostException -> {
                AiException(AiError.Unknown("Не удалось подключиться к Gemini. Проверьте сеть или включите VPN."))
            }
            else -> {
                val msg = e.message ?: ""
                if (msg.contains("timeout", ignoreCase = true)) {
                    AiException(AiError.Unknown("Сервер Gemini не ответил вовремя. Если вы в РФ, включите VPN."))
                } else if (msg.contains("host", ignoreCase = true) || msg.contains("network", ignoreCase = true) || msg.contains("connect", ignoreCase = true)) {
                    AiException(AiError.Unknown("Сбой сети. Проверьте интернет или включите VPN."))
                } else {
                    AiException(AiError.Unknown(msg.ifBlank { "Ошибка соединения с ИИ." }))
                }
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap, maxDimension: Int = 800): String {
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractTextFromGeminiResponse(jsonString: String): String {
        return try {
            val json = JSONObject(jsonString)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            parts.getJSONObject(0).optString("text", "")
        } catch (e: Exception) {
            ""
        }
    }

    private fun cleanJsonString(raw: String): String {
        var str = raw.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }

    private fun parseRecognizedMealJson(jsonStr: String): RecognizedMeal {
        val json = try {
            JSONObject(jsonStr)
        } catch (e: Exception) {
            throw AiException(AiError.BadResponse)
        }

        fun optDoubleSafe(obj: JSONObject, vararg keys: String): Double? {
            for (k in keys) {
                if (obj.has(k) && !obj.isNull(k)) {
                    val d = obj.optDouble(k, Double.NaN)
                    if (d.isFinite() && d >= 0.0) return d
                }
            }
            return null
        }

        fun optStringSafe(obj: JSONObject, vararg keys: String): String? {
            for (k in keys) {
                if (obj.has(k) && !obj.isNull(k)) {
                    val s = obj.optString(k, "").trim()
                    if (s.isNotBlank()) return s
                }
            }
            return null
        }

        val itemsList = mutableListOf<RecognizedMealItem>()
        val itemsJson = json.optJSONArray("items") ?: json.optJSONArray("products")

        if (itemsJson != null && itemsJson.length() > 0) {
            for (i in 0 until itemsJson.length()) {
                val itemObj = itemsJson.optJSONObject(i) ?: continue
                val name = optStringSafe(itemObj, "name", "productName", "product_name", "dishName")
                    ?: throw AiException(AiError.BadResponse)

                val gramsDouble = optDoubleSafe(itemObj, "grams", "weight", "estimatedGrams", "weightGrams")
                    ?: throw AiException(AiError.BadResponse)
                val calDouble = optDoubleSafe(itemObj, "calories", "kcal", "energy")
                    ?: throw AiException(AiError.BadResponse)
                val p = optDoubleSafe(itemObj, "protein", "proteins") ?: 0.0
                val f = optDoubleSafe(itemObj, "fat", "fats") ?: 0.0
                val c = optDoubleSafe(itemObj, "carbs", "carbohydrates") ?: 0.0

                val grams = gramsDouble.roundToInt()
                val cal = calDouble.roundToInt()

                if (grams !in 1..5000 || cal !in 0..5000 || p !in 0.0..500.0 || f !in 0.0..500.0 || c !in 0.0..1000.0) {
                    throw AiException(AiError.BadResponse)
                }

                itemsList.add(
                    RecognizedMealItem(
                        name = name,
                        grams = grams,
                        calories = cal,
                        protein = p,
                        fat = f,
                        carbs = c
                    )
                )
            }
        }

        val dishName = optStringSafe(json, "dishName", "dish_name", "title", "name")
            ?: if (itemsList.isNotEmpty()) itemsList.joinToString(", ") { it.name } else throw AiException(AiError.BadResponse)

        val mealTypeStr = (optStringSafe(json, "mealType", "meal_type") ?: "").uppercase()
        val mealType = when {
            mealTypeStr.contains("BREAKFAST") || mealTypeStr.contains("ЗАВТРАК") -> MealType.BREAKFAST
            mealTypeStr.contains("LUNCH") || mealTypeStr.contains("ОБЕД") -> MealType.LUNCH
            mealTypeStr.contains("DINNER") || mealTypeStr.contains("УЖИН") -> MealType.DINNER
            else -> MealType.SNACK
        }

        val grams = if (itemsList.isNotEmpty()) {
            itemsList.sumOf { it.grams }
        } else {
            (optDoubleSafe(json, "estimatedGrams", "grams", "weight") ?: throw AiException(AiError.BadResponse)).roundToInt()
        }

        val calories = if (itemsList.isNotEmpty()) {
            itemsList.sumOf { it.calories }
        } else {
            (optDoubleSafe(json, "calories", "kcal") ?: throw AiException(AiError.BadResponse)).roundToInt()
        }

        val protein = if (itemsList.isNotEmpty()) {
            itemsList.sumOf { it.protein }
        } else {
            optDoubleSafe(json, "protein", "proteins") ?: 0.0
        }

        val fat = if (itemsList.isNotEmpty()) {
            itemsList.sumOf { it.fat }
        } else {
            optDoubleSafe(json, "fat", "fats") ?: 0.0
        }

        val carbs = if (itemsList.isNotEmpty()) {
            itemsList.sumOf { it.carbs }
        } else {
            optDoubleSafe(json, "carbs", "carbohydrates") ?: 0.0
        }

        if (grams !in 1..5000 || calories !in 0..5000 || protein !in 0.0..500.0 || fat !in 0.0..500.0 || carbs !in 0.0..1000.0) {
            throw AiException(AiError.BadResponse)
        }

        return RecognizedMeal(
            dishName = dishName,
            estimatedGrams = grams,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            mealType = mealType,
            confidence = optStringSafe(json, "confidence") ?: "Высокая",
            description = optStringSafe(json, "description") ?: "",
            items = itemsList,
            isEstimate = false
        )
    }

    suspend fun lookupProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        val cleanCode = barcode.trim()
        if (cleanCode.isBlank()) return@withContext null

        try {
            val offUrl = "https://world.openfoodfacts.org/api/v2/product/$cleanCode.json"
            val request = Request.Builder().url(offUrl).get().build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val respStr = response.body?.string() ?: ""
                val jsonObj = JSONObject(respStr)
                if (jsonObj.optInt("status") == 1) {
                    val prodObj = jsonObj.getJSONObject("product")
                    val rawName = prodObj.optString("product_name_ru").ifBlank {
                        prodObj.optString("product_name").ifBlank {
                            prodObj.optString("brands", "Продукт") + " " + prodObj.optString("generic_name", "")
                        }
                    }.trim()

                    val nutriments = prodObj.optJSONObject("nutriments")

                    fun parseDoubleSafe(obj: JSONObject?, key: String): Double? {
                        if (obj == null || !obj.has(key)) return null
                        val v = obj.optDouble(key, Double.NaN)
                        return if (v.isNaN() || v < 0) null else v
                    }

                    val kcal = parseDoubleSafe(nutriments, "energy-kcal_100g")
                        ?: parseDoubleSafe(nutriments, "energy-kcal_value")
                        ?: parseDoubleSafe(nutriments, "energy-kcal")

                    val protein = parseDoubleSafe(nutriments, "proteins_100g")
                    val fat = parseDoubleSafe(nutriments, "fat_100g")
                    val carbs = parseDoubleSafe(nutriments, "carbohydrates_100g")

                    val finalName = if (rawName.isNotBlank() && rawName != "null") rawName else "Продукт ($cleanCode)"

                    if (kcal == null || protein == null || fat == null || carbs == null) {
                        return@withContext Product(
                            name = finalName,
                            category = "Сканированное",
                            calories = 0.0,
                            protein = 0.0,
                            fat = 0.0,
                            carbs = 0.0,
                            barcode = cleanCode,
                            isCustom = true
                        )
                    }

                    return@withContext Product(
                        name = finalName,
                        category = "Сканированное",
                        calories = kcal,
                        protein = protein,
                        fat = fat,
                        carbs = carbs,
                        barcode = cleanCode,
                        isCustom = true
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        null
    }
}
