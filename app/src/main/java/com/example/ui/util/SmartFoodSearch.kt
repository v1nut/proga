package com.example.ui.util

import com.example.data.model.Product

object SmartFoodSearch {

    private val aliasDictionary: Map<String, List<String>> = mapOf(
        "яйца" to listOf("яйцо", "яичн"),
        "яйцо" to listOf("яйца", "яичн"),
        "яичница" to listOf("яйцо", "белок"),
        "курица" to listOf("куриное", "филе", "грудка"),
        "курицы" to listOf("куриное", "филе"),
        "куриное" to listOf("курица", "филе"),
        "цыпленок" to listOf("куриное"),
        "яблоки" to listOf("яблоко"),
        "яблоко" to listOf("яблоки"),
        "бананы" to listOf("банан"),
        "банан" to listOf("бананы"),
        "огурцы" to listOf("огурец"),
        "огурец" to listOf("огурцы"),
        "помидоры" to listOf("помидор", "томат"),
        "помидор" to listOf("помидоры", "томат"),
        "томаты" to listOf("помидоры", "томат"),
        "томат" to listOf("помидор", "помидоры"),
        "гречка" to listOf("гречневая", "гречневая каша", "гречневая крупа"),
        "греча" to listOf("гречневая", "гречка"),
        "гречневая" to listOf("гречка"),
        "овсянка" to listOf("овсяные", "геркулес"),
        "овсяные" to listOf("овсянка", "геркулес"),
        "геркулес" to listOf("овсянка", "овсяные"),
        "макароны" to listOf("паста", "спагетти"),
        "паста" to listOf("макароны"),
        "рыба" to listOf("лосось", "семга", "тунец", "минтай", "горбуша"),
        "семга" to listOf("лосось"),
        "лосось" to listOf("семга"),
        "творожок" to listOf("творог"),
        "творог" to listOf("творожок"),
        "молоко" to listOf("молочные"),
        "креветка" to listOf("креветки"),
        "креветки" to listOf("креветка"),
        "хлеб" to listOf("хлебцы", "цельнозерновой"),
        "индейка" to listOf("индейк"),
        "говядина" to listOf("говяж")
    )

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^a-zа-я0-9\\s]"), " ")
            .trim()
    }

    /**
     * Extracts common Russian stem prefix (simple stemming without inflections)
     */
    private fun getStem(word: String): String {
        val norm = normalize(word)
        if (norm.length <= 3) return norm

        // Strip common Russian noun and adjective endings
        val endings = listOf("ное", "ная", "ный", "ных", "ные", "ого", "ему", "ыми", "ями", "ей", "ов", "ев", "ам", "ям", "ах", "ях", "ка", "ки", "ку", "ке", "ко", "цы", "ца", "це", "ое", "ые", "ая", "яя", "ий", "ый", "ой", "а", "я", "о", "е", "ы", "и", "у", "ю")
        for (ending in endings) {
            if (norm.endsWith(ending) && (norm.length - ending.length) >= 3) {
                return norm.substring(0, norm.length - ending.length)
            }
        }
        return norm
    }

    /**
     * Checks if product matches user search query taking into account
     * synonyms, plurals/singulars, and root forms (e.g. "яйца" -> "яйцо куриное").
     */
    fun matches(product: Product, query: String): Boolean {
        val cleanQuery = normalize(query)
        if (cleanQuery.isBlank()) return true

        val productNameNorm = normalize(product.name)
        val productCategoryNorm = normalize(product.category)

        // 1. Direct contains check
        if (productNameNorm.contains(cleanQuery) || productCategoryNorm.contains(cleanQuery)) {
            return true
        }

        val queryTokens = cleanQuery.split(Regex("\\s+")).filter { it.isNotBlank() }
        val nameTokens = productNameNorm.split(Regex("\\s+")).filter { it.isNotBlank() }

        // 2. Check alias dictionary
        for (qToken in queryTokens) {
            val aliases = aliasDictionary[qToken] ?: emptyList()
            for (alias in aliases) {
                if (productNameNorm.contains(alias) || productCategoryNorm.contains(alias)) {
                    return true
                }
            }
        }

        // 3. Stemming match: if query stem is prefix of any name token or vice versa
        val queryStems = queryTokens.map { getStem(it) }
        val nameStems = nameTokens.map { getStem(it) }

        return queryStems.all { qStem ->
            nameStems.any { nStem ->
                qStem.startsWith(nStem) || nStem.startsWith(qStem) ||
                        (qStem.length >= 4 && nStem.contains(qStem)) ||
                        (nStem.length >= 4 && qStem.contains(nStem))
            }
        }
    }
}
