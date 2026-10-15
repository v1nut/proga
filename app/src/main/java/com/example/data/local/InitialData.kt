package com.example.data.local

import com.example.data.model.Product

object InitialData {
    val predefinedProducts = listOf(
        // Мясо и птица
        Product(name = "Куриное филе грудки (варёное)", category = "Мясо и птица", calories = 165.0, protein = 31.0, fat = 3.6, carbs = 0.0, barcode = "4601112223334"),
        Product(name = "Куриное филе (сырое)", category = "Мясо и птица", calories = 113.0, protein = 23.6, fat = 1.9, carbs = 0.4, barcode = "4601112223341"),
        Product(name = "Индейка филе грудки", category = "Мясо и птица", calories = 120.0, protein = 25.0, fat = 2.0, carbs = 0.0, barcode = "4601112223358"),
        Product(name = "Говядина постная (тушёная)", category = "Мясо и птица", calories = 180.0, protein = 26.0, fat = 8.5, carbs = 0.0, barcode = "4601112223365"),
        Product(name = "Фарш из грудки индейки", category = "Мясо и птица", calories = 135.0, protein = 21.0, fat = 5.5, carbs = 0.5, barcode = "4601112223372"),

        // Рыба и морепродукты
        Product(name = "Лосось / Сёмга на пару", category = "Рыба и морепродукты", calories = 197.0, protein = 20.0, fat = 13.0, carbs = 0.0, barcode = "4607778889990"),
        Product(name = "Тунец в собственном соку", category = "Рыба и морепродукты", calories = 101.0, protein = 23.5, fat = 0.8, carbs = 0.0, barcode = "4608889990001"),
        Product(name = "Минтай отварной", category = "Рыба и морепродукты", calories = 72.0, protein = 16.0, fat = 0.9, carbs = 0.0, barcode = "4608889990018"),
        Product(name = "Креветки варёные", category = "Рыба и морепродукты", calories = 95.0, protein = 21.0, fat = 1.0, carbs = 1.0, barcode = "4608889990025"),
        Product(name = "Горбуша запечённая", category = "Рыба и морепродукты", calories = 142.0, protein = 21.0, fat = 6.5, carbs = 0.0, barcode = "4608889990032"),

        // Крупы и гарниры
        Product(name = "Гречневая каша (отварная)", category = "Крупы и гарниры", calories = 110.0, protein = 4.2, fat = 1.3, carbs = 21.3, barcode = "4607001234567"),
        Product(name = "Гречневая крупа (сухая)", category = "Крупы и гарниры", calories = 330.0, protein = 12.6, fat = 3.3, carbs = 62.1, barcode = "4607001234574"),
        Product(name = "Рис белый отварной", category = "Крупы и гарниры", calories = 130.0, protein = 2.7, fat = 0.3, carbs = 28.2, barcode = "4601234000035"),
        Product(name = "Рис бурый отварной", category = "Крупы и гарниры", calories = 111.0, protein = 2.6, fat = 0.9, carbs = 23.0, barcode = "4601234000042"),
        Product(name = "Овсяные хлопья (Геркулес, сухие)", category = "Крупы и гарниры", calories = 352.0, protein = 12.3, fat = 6.2, carbs = 61.8, barcode = "4601234567890"),
        Product(name = "Макароны тв. сортов (отварные)", category = "Крупы и гарниры", calories = 158.0, protein = 5.8, fat = 0.9, carbs = 30.9, barcode = "4601234000011"),
        Product(name = "Картофель отварной без масла", category = "Крупы и гарниры", calories = 82.0, protein = 2.0, fat = 0.4, carbs = 16.7, barcode = "4601234000059"),
        Product(name = "Киноа отварная", category = "Крупы и гарниры", calories = 120.0, protein = 4.4, fat = 1.9, carbs = 21.3, barcode = "4601234000066"),

        // Молочные продукты и яйца
        Product(name = "Яйцо куриное (варёное / 100 г)", category = "Молочные продукты", calories = 155.0, protein = 13.0, fat = 11.0, carbs = 1.1, barcode = "4602223334445"),
        Product(name = "Белок яичный", category = "Молочные продукты", calories = 52.0, protein = 11.1, fat = 0.2, carbs = 0.7, barcode = "4602223334452"),
        Product(name = "Творог 5%", category = "Молочные продукты", calories = 121.0, protein = 17.2, fat = 5.0, carbs = 1.8, barcode = "4605432109876"),
        Product(name = "Творог 0.2% (обезжиренный)", category = "Молочные продукты", calories = 71.0, protein = 16.5, fat = 0.2, carbs = 1.3, barcode = "4605432109883"),
        Product(name = "Греческий йогурт 2%", category = "Молочные продукты", calories = 67.0, protein = 9.0, fat = 2.0, carbs = 3.5, barcode = "4601234000028"),
        Product(name = "Молоко 2.5%", category = "Молочные продукты", calories = 52.0, protein = 2.8, fat = 2.5, carbs = 4.7, barcode = "4609876543210"),
        Product(name = "Кефир 1%", category = "Молочные продукты", calories = 40.0, protein = 3.0, fat = 1.0, carbs = 4.0, barcode = "4609876543227"),
        Product(name = "Сыр Российский 45%", category = "Молочные продукты", calories = 363.0, protein = 24.1, fat = 29.5, carbs = 0.3, barcode = "4609990001112"),
        Product(name = "Сыр Моцарелла", category = "Молочные продукты", calories = 280.0, protein = 22.0, fat = 21.0, carbs = 2.2, barcode = "4609990001129"),

        // Овощи и зелень
        Product(name = "Авокадо", category = "Овощи и зелень", calories = 160.0, protein = 2.0, fat = 14.7, carbs = 8.5, barcode = "4606667778889"),
        Product(name = "Помидоры свежие", category = "Овощи и зелень", calories = 18.0, protein = 0.9, fat = 0.2, carbs = 3.9, barcode = "4606667778896"),
        Product(name = "Огурцы свежие", category = "Овощи и зелень", calories = 15.0, protein = 0.8, fat = 0.1, carbs = 3.0, barcode = "4606667778902"),
        Product(name = "Брокколи на пару", category = "Овощи и зелень", calories = 35.0, protein = 2.8, fat = 0.4, carbs = 7.0, barcode = "4606667778919"),
        Product(name = "Перец болгарский сладкий", category = "Овощи и зелень", calories = 27.0, protein = 1.3, fat = 0.1, carbs = 5.3, barcode = "4606667778926"),
        Product(name = "Шпинат свежий", category = "Овощи и зелень", calories = 23.0, protein = 2.9, fat = 0.4, carbs = 3.6, barcode = "4606667778933"),

        // Фрукты и ягоды
        Product(name = "Банан свежий", category = "Фрукты и ягоды", calories = 89.0, protein = 1.1, fat = 0.3, carbs = 22.8, barcode = "4604445556667"),
        Product(name = "Яблоко свежее с кожурой", category = "Фрукты и ягоды", calories = 52.0, protein = 0.3, fat = 0.2, carbs = 13.8, barcode = "4605556667778"),
        Product(name = "Апельсин", category = "Фрукты и ягоды", calories = 43.0, protein = 0.9, fat = 0.2, carbs = 8.2, barcode = "4605556667785"),
        Product(name = "Клубника свежая", category = "Фрукты и ягоды", calories = 33.0, protein = 0.7, fat = 0.3, carbs = 7.7, barcode = "4605556667792"),
        Product(name = "Черника / Голубика", category = "Фрукты и ягоды", calories = 57.0, protein = 0.7, fat = 0.3, carbs = 14.5, barcode = "4605556667808"),

        // Орехи и масла
        Product(name = "Масло оливковое Extra Virgin", category = "Орехи и масла", calories = 884.0, protein = 0.0, fat = 100.0, carbs = 0.0, barcode = "4603332221110"),
        Product(name = "Масло сливочное 82.5%", category = "Орехи и масла", calories = 748.0, protein = 0.6, fat = 82.5, carbs = 0.8, barcode = "4603332221127"),
        Product(name = "Миндаль сырой", category = "Орехи и масла", calories = 579.0, protein = 21.2, fat = 49.9, carbs = 21.6, barcode = "4603332221134"),
        Product(name = "Грецкий орех", category = "Орехи и масла", calories = 654.0, protein = 15.2, fat = 65.2, carbs = 13.7, barcode = "4603332221141"),
        Product(name = "Арахисовая паста без сахара", category = "Орехи и масла", calories = 588.0, protein = 25.0, fat = 50.0, carbs = 20.0, barcode = "4603332221158"),

        // Хлеб и выпечка
        Product(name = "Хлеб цельнозерновой", category = "Хлеб и выпечка", calories = 247.0, protein = 13.0, fat = 3.4, carbs = 41.3, barcode = "4603334445556"),
        Product(name = "Лаваш тонкий армянский", category = "Хлеб и выпечка", calories = 236.0, protein = 7.9, fat = 1.0, carbs = 47.6, barcode = "4603334445563"),
        Product(name = "Хлебцы ржаные хрустящие", category = "Хлеб и выпечка", calories = 310.0, protein = 11.0, fat = 2.5, carbs = 58.0, barcode = "4603334445570")
    )
}
