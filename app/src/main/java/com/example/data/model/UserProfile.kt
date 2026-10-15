package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender(val titleRu: String) {
    MALE("Мужской"),
    FEMALE("Женский")
}

enum class ActivityLevel(val titleRu: String, val descriptionRu: String, val multiplier: Double) {
    SEDENTARY("Сидячий", "Мало или нет физ. активности", 1.2),
    LIGHT("Легкая активность", "Тренировки 1-3 раза в неделю", 1.375),
    MODERATE("Умеренная активность", "Тренировки 3-5 раз в неделю", 1.55),
    HIGH("Высокая активность", "Интенсивный спорт 6-7 дней", 1.725),
    VERY_HIGH("Экстремальная", "Ежедневные тяжелые нагрузки", 1.9)
}

enum class FitnessGoal(val titleRu: String, val descriptionRu: String, val calorieMultiplier: Double) {
    WEIGHT_LOSS("Похудение", "Дефицит калорий (-18%) для сжигания жира", 0.82),
    MAINTENANCE("Поддержание", "Баланс калорий для сохранения текущего веса", 1.0),
    WEIGHT_GAIN("Набор массы", "Профицит калорий (+15%) для роста мышц", 1.15)
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val age: Int = 26,
    val gender: String = Gender.MALE.name,
    val heightCm: Double = 175.0,
    val weightKg: Double = 72.0,
    val activityLevel: String = ActivityLevel.MODERATE.name,
    val goal: String = FitnessGoal.MAINTENANCE.name,
    val targetCalories: Int = 2200,
    val targetProtein: Int = 140,
    val targetFat: Int = 70,
    val targetCarbs: Int = 250,
    val isManualTarget: Boolean = false
)
