package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender(val titleRu: String, val titleEn: String) {
    MALE("Мужской", "Male"),
    FEMALE("Женский", "Female");

    fun title(lang: String): String = if (lang == "en") titleEn else titleRu
}

enum class ActivityLevel(
    val titleRu: String,
    val titleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val multiplier: Double
) {
    SEDENTARY("Сидячий", "Sedentary", "Мало или нет физ. активности", "Little to no exercise", 1.2),
    LIGHT("Легкая активность", "Light Activity", "Тренировки 1-3 раза в неделю", "1-3 workouts / week", 1.375),
    MODERATE("Умеренная активность", "Moderate Activity", "Тренировки 3-5 раз в неделю", "3-5 workouts / week", 1.55),
    HIGH("Высокая активность", "High Activity", "Интенсивный спорт 6-7 дней", "Intense workout 6-7 days", 1.725),
    VERY_HIGH("Экстремальная", "Extreme Activity", "Ежедневные тяжелые нагрузки", "Heavy daily labor / sports", 1.9);

    fun title(lang: String): String = if (lang == "en") titleEn else titleRu
    fun description(lang: String): String = if (lang == "en") descriptionEn else descriptionRu
}

enum class FitnessGoal(
    val titleRu: String,
    val titleEn: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val calorieMultiplier: Double
) {
    WEIGHT_LOSS("Похудение", "Weight Loss", "Дефицит калорий (-18%) для сжигания жира", "Calorie deficit (-18%) for fat loss", 0.82),
    MAINTENANCE("Поддержание", "Maintenance", "Баланс калорий для сохранения текущего веса", "Calorie balance to maintain weight", 1.0),
    WEIGHT_GAIN("Набор массы", "Weight Gain", "Профицит калорий (+15%) для роста мышц", "Calorie surplus (+15%) for muscle gain", 1.15);

    fun title(lang: String): String = if (lang == "en") titleEn else titleRu
    fun description(lang: String): String = if (lang == "en") descriptionEn else descriptionRu
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey
    val id: Int = 1,
    val age: Int = 26,
    val gender: String = Gender.MALE.name,
    val heightCm: Double = 175.0,
    val weightKg: Double = 72.0,
    val targetWeightKg: Double = 68.0,
    val activityLevel: String = ActivityLevel.MODERATE.name,
    val goal: String = FitnessGoal.MAINTENANCE.name,
    val targetCalories: Int = 2200,
    val targetProtein: Int = 140,
    val targetFat: Int = 70,
    val targetCarbs: Int = 250,
    val isManualTarget: Boolean = false
)
