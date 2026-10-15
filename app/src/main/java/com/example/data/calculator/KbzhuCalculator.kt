package com.example.data.calculator

import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import kotlin.math.roundToInt

data class CalculationResult(
    val bmr: Int,
    val tdee: Int,
    val targetCalories: Int,
    val targetProtein: Int,
    val targetFat: Int,
    val targetCarbs: Int
)

object KbzhuCalculator {

    /**
     * Calculates Basal Metabolic Rate (BMR) using Mifflin-St Jeor equation.
     */
    fun calculateBmr(
        gender: Gender,
        age: Int,
        heightCm: Double,
        weightKg: Double
    ): Double {
        val base = (10.0 * weightKg) + (6.25 * heightCm) - (5.0 * age)
        return if (gender == Gender.MALE) {
            base + 5.0
        } else {
            base - 161.0
        }
    }

    /**
     * Computes full daily target metrics (Calories, Protein, Fat, Carbs) based on user metrics and goals.
     */
    fun calculate(
        gender: Gender,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal
    ): CalculationResult {
        val bmr = calculateBmr(gender, age, heightCm, weightKg)
        val tdee = bmr * activityLevel.multiplier

        // Apply calorie adjustment based on fitness goal
        val targetCalories = (tdee * goal.calorieMultiplier).roundToInt().coerceAtLeast(1000)

        // Protein calculation:
        // Weight loss: ~2.0 g/kg to protect muscle mass during calorie restriction
        // Maintenance: ~1.8 g/kg
        // Weight gain: ~2.0 g/kg for muscle hypertrophy
        val proteinPerKg = when (goal) {
            FitnessGoal.WEIGHT_LOSS -> 2.0
            FitnessGoal.MAINTENANCE -> 1.8
            FitnessGoal.WEIGHT_GAIN -> 2.0
        }
        val proteinGrams = (weightKg * proteinPerKg).roundToInt().coerceIn(40, 300)
        val proteinCalories = proteinGrams * 4

        // Fat calculation: ~0.9 - 1.0 g per kg of body weight for healthy endocrine function
        // capped between 20% and 35% of total calories
        val targetFatGrams = (weightKg * 1.0).roundToInt().coerceIn(
            ((targetCalories * 0.20) / 9.0).roundToInt(),
            ((targetCalories * 0.35) / 9.0).roundToInt()
        ).coerceAtLeast(35)
        val fatCalories = targetFatGrams * 9

        // Carbohydrates: Remaining calories allocated to carbohydrates (4 kcal/g)
        val remainingCalories = (targetCalories - proteinCalories - fatCalories).coerceAtLeast(0)
        val targetCarbsGrams = (remainingCalories / 4.0).roundToInt().coerceAtLeast(50)

        return CalculationResult(
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            targetCalories = targetCalories,
            targetProtein = proteinGrams,
            targetFat = targetFatGrams,
            targetCarbs = targetCarbsGrams
        )
    }
}
