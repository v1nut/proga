package com.example.ui.util

object AppStrings {
    fun dashboard(lang: String): String = if (lang == "en") "Main" else "Главная"
    fun diary(lang: String): String = if (lang == "en") "Diary" else "Дневник"
    fun catalog(lang: String): String = if (lang == "en") "Catalog" else "Продукты"
    fun profile(lang: String): String = if (lang == "en") "Profile" else "Профиль"
    fun settings(lang: String): String = if (lang == "en") "Settings" else "Настройки"

    fun breakfast(lang: String): String = if (lang == "en") "Breakfast" else "Завтрак"
    fun lunch(lang: String): String = if (lang == "en") "Lunch" else "Обед"
    fun dinner(lang: String): String = if (lang == "en") "Dinner" else "Ужин"
    fun snack(lang: String): String = if (lang == "en") "Snack" else "Перекус"

    fun waterBalance(lang: String): String = if (lang == "en") "Water Balance" else "Водный баланс"
    fun waterRemaining(lang: String, amount: Int): String = if (lang == "en") "$amount ml left" else "Осталось: $amount мл"
    fun waterGoalMet(lang: String): String = if (lang == "en") "Goal reached! 🎉" else "Норма выполнена! 🎉"

    fun add(lang: String): String = if (lang == "en") "Add" else "Добавить"
    fun cancel(lang: String): String = if (lang == "en") "Cancel" else "Отмена"
    fun save(lang: String): String = if (lang == "en") "Save" else "Сохранить"
    fun delete(lang: String): String = if (lang == "en") "Delete" else "Удалить"
    fun undo(lang: String): String = if (lang == "en") "Undo" else "Отменить"
    fun calories(lang: String): String = if (lang == "en") "kcal" else "ккал"
    fun protein(lang: String): String = if (lang == "en") "Protein" else "Белки"
    fun fat(lang: String): String = if (lang == "en") "Fat" else "Жиры"
    fun carbs(lang: String): String = if (lang == "en") "Carbs" else "Углеводы"
}
