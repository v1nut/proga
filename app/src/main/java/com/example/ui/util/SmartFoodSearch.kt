package com.example.ui.util

import com.example.data.model.Product

object SmartFoodSearch {

    private val unitTokens = setOf("г", "гр", "мл", "кг", "шт", "л", "пачка", "кусок", "стакан")

    private val aliasDictionary: Map<String, List<String>> = mapOf(
        "яйца" to listOf("яйц", "яичн"),
        "яйцо" to listOf("яйц", "яичн"),
        "яичница" to listOf("яйц"),
        "курица" to listOf("курин", "филе", "грудк"),
        "куриное" to listOf("курин", "куриц"),
        "цыпленок" to listOf("курин"),
        "яблоки" to listOf("яблок"),
        "яблоко" to listOf("яблок"),
        "бананы" to listOf("банан"),
        "банан" to listOf("банан"),
        "огурцы" to listOf("огур"),
        "огурец" to listOf("огур"),
        "помидоры" to listOf("помидор", "томат"),
        "помидор" to listOf("помидор", "томат"),
        "томаты" to listOf("томат", "помидор"),
        "гречка" to listOf("гречн", "гречк"),
        "греча" to listOf("гречн", "гречк"),
        "гречневая" to listOf("гречк", "гречн"),
        "овсянка" to listOf("овсян", "геркулес"),
        "овсяные" to listOf("овсян", "геркулес"),
        "геркулес" to listOf("овсян", "геркулес"),
        "макароны" to listOf("паст", "спагетти", "макарон"),
        "паста" to listOf("макарон", "паст"),
        "рыба" to listOf("лосось", "семга", "тунец", "минтай", "горбуш"),
        "семга" to listOf("лосось", "семг"),
        "лосось" to listOf("семг", "лосос"),
        "творожок" to listOf("творог"),
        "творог" to listOf("творожок", "творог"),
        "креветка" to listOf("креветк"),
        "креветки" to listOf("креветк"),
        "хлеб" to listOf("хлеб", "хлебц"),
        "индейка" to listOf("индейк"),
        "говядина" to listOf("говяд")
    )

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace('ё', 'е')
            .replace(Regex("[^a-zа-я0-9\\s]"), " ")
            .trim()
    }

    private fun getTokens(text: String): List<String> {
        val rawTokens = normalize(text).split(Regex("\\s+")).filter { it.isNotBlank() }
        return rawTokens.filter { token ->
            val clean = token.filter { it.isLetter() }
            clean.length >= 3 && clean !in unitTokens
        }
    }

    fun getStem(word: String): String {
        val norm = normalize(word).filter { it.isLetter() }
        if (norm.length <= 3) return norm

        val endings = listOf("ное", "ная", "ный", "ных", "ные", "ого", "ему", "ыми", "ями", "ей", "ов", "ев", "ам", "ям", "ах", "ях", "ка", "ки", "ку", "ке", "ко", "цы", "ца", "це", "ое", "ые", "ая", "яя", "ий", "ый", "ой", "а", "я", "о", "е", "ы", "и", "у", "ю")
        for (ending in endings) {
            if (norm.endsWith(ending) && (norm.length - ending.length) >= 3) {
                return norm.substring(0, norm.length - ending.length)
            }
        }
        return norm
    }

    fun matchesToken(product: Product, qToken: String): Boolean {
        val qStem = getStem(qToken)
        val aliases = aliasDictionary[qToken] ?: emptyList()
        val nameTokens = getTokens(product.name)

        return nameTokens.any { nToken ->
            val nStem = getStem(nToken)

            nToken.startsWith(qToken) ||
            nToken.startsWith(qStem) ||
            qToken.startsWith(nStem) ||
            nStem.startsWith(qStem) ||
            qStem.startsWith(nStem) ||
            aliases.any { alias -> nToken.startsWith(alias) || nStem.startsWith(alias) }
        }
    }

    fun matches(product: Product, query: String): Boolean {
        val cleanQuery = normalize(query)
        if (cleanQuery.isBlank()) return true

        val queryTokens = getTokens(query)
        if (queryTokens.isEmpty()) return true

        return queryTokens.all { qToken -> matchesToken(product, qToken) }
    }

    fun filterAndSort(products: List<Product>, query: String): List<Product> {
        val cleanQuery = normalize(query)
        if (cleanQuery.isBlank()) return products

        val queryTokens = getTokens(query)
        if (queryTokens.isEmpty()) return products

        val allMatches = products.filter { p ->
            queryTokens.all { qToken -> matchesToken(p, qToken) }
        }

        val sortedAllMatches = sortProducts(allMatches, cleanQuery, queryTokens)

        if (sortedAllMatches.size >= 3) {
            return sortedAllMatches
        }

        val allMatchIds = sortedAllMatches.map { it.id }.toSet()
        val partialMatches = products.filter { p ->
            p.id !in allMatchIds && queryTokens.any { qToken -> matchesToken(p, qToken) }
        }

        val sortedPartialMatches = sortProducts(partialMatches, cleanQuery, queryTokens)

        return sortedAllMatches + sortedPartialMatches
    }

    private fun sortProducts(productsList: List<Product>, cleanQuery: String, queryTokens: List<String>): List<Product> {
        return productsList.sortedWith { p1, p2 ->
            val p1NameNorm = normalize(p1.name)
            val p2NameNorm = normalize(p2.name)

            val p1StartsWith = p1NameNorm.startsWith(cleanQuery)
            val p2StartsWith = p2NameNorm.startsWith(cleanQuery)

            if (p1StartsWith && !p2StartsWith) return@sortedWith -1
            if (!p1StartsWith && p2StartsWith) return@sortedWith 1

            val p1Tokens = getTokens(p1.name)
            val p2Tokens = getTokens(p2.name)

            val p1ExactWord = queryTokens.any { qt -> p1Tokens.contains(qt) }
            val p2ExactWord = queryTokens.any { qt -> p2Tokens.contains(qt) }

            if (p1ExactWord && !p2ExactWord) return@sortedWith -1
            if (!p1ExactWord && p2ExactWord) return@sortedWith 1

            p1.name.compareTo(p2.name, ignoreCase = true)
        }
    }
}
