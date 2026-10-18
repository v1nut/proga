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
    val targetCarbs: Int,
    val warnings: List<String> = emptyList()
)

object KbzhuCalculator {

    fun calculateTargetWaterMl(weightKg: Double, isManual: Boolean, customTargetMl: Int): Int {
        if (isManual) {
            return customTargetMl.coerceIn(500, 6000)
        }
        return maxOf(1500, (weightKg * 30.0).roundToInt())
    }

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
     * Computes full daily target metrics (Calories, Protein, Fat, Carbs) and safety warnings.
     */
    fun calculate(
        gender: Gender,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal
    ): CalculationResult {
        val safeAge = age.coerceIn(10, 120)
        val safeHeightCm = heightCm.coerceIn(100.0, 250.0)
        val safeWeightKg = weightKg.coerceIn(25.0, 300.0)

        val bmrDouble = calculateBmr(gender, safeAge, safeHeightCm, safeWeightKg)
        val bmr = bmrDouble.roundToInt()
        val tdeeDouble = bmr * activityLevel.multiplier
        val tdee = tdeeDouble.roundToInt()

        val heightM = safeHeightCm / 100.0
        val bmi = if (heightM > 0) safeWeightKg / (heightM * heightM) else 22.0

        val warnings = mutableListOf<String>()

        // 1. Calorie Target Calculation and Deficit Floor/Ceiling
        var targetCal = tdeeDouble * goal.calorieMultiplier

        if (goal == FitnessGoal.WEIGHT_LOSS) {
            val minFloor = if (gender == Gender.FEMALE) 1200.0 else 1500.0
            if (targetCal < minFloor) {
                targetCal = minFloor
            }
            if (targetCal >= tdeeDouble) {
                targetCal = tdeeDouble
                warnings.add("Дефицит невозможен при текущих параметрах, посоветуйтесь с врачом")
            }
        }

        val targetCalories = if (goal == FitnessGoal.WEIGHT_LOSS) {
            targetCal.roundToInt().coerceAtMost(maxOf(1000, tdee)).coerceAtLeast(1000)
        } else {
            targetCal.roundToInt().coerceAtLeast(1000)
        }

        // 2. Safety Warnings for Age & BMI
        if (age < 18) {
            warnings.add("Формула рассчитана на взрослых. Для детей и подростков нормы определяет врач")
        }

        if (bmi < 18.5 && goal == FitnessGoal.WEIGHT_LOSS) {
            warnings.add("При недостаточном весе похудение не рекомендуется, обратитесь к врачу")
        }

        // 3. Protein Calculation (Adjusted for Obesity BMI >= 30 and Capped at 35% Calories)
        val proteinPerKg = when (goal) {
            FitnessGoal.WEIGHT_LOSS -> 2.0
            FitnessGoal.MAINTENANCE -> 1.8
            FitnessGoal.WEIGHT_GAIN -> 2.0
        }

        val proteinGramsRaw = if (bmi >= 30.0) {
            val referenceWeightForBmi25 = 25.0 * (heightM * heightM)
            val effectiveWeight = minOf(referenceWeightForBmi25, safeWeightKg)
            effectiveWeight * proteinPerKg
        } else {
            safeWeightKg * proteinPerKg
        }

        val maxProteinCalories = targetCalories * 0.35
        val maxProteinGrams = maxOf(40, (maxProteinCalories / 4.0).toInt())
        val targetProteinGrams = proteinGramsRaw.roundToInt().coerceIn(40, maxProteinGrams)
        val proteinCalories = targetProteinGrams * 4.0

        // 4. Fat Calculation (0.9 - 1.0 g per kg, bounded between 20% and 35% calories)
        val minFat = ((targetCalories * 0.20) / 9.0).roundToInt()
        val maxFat = maxOf(minFat, ((targetCalories * 0.35) / 9.0).roundToInt())
        val targetFatGrams = (safeWeightKg * 1.0).roundToInt().coerceIn(minFat, maxFat).coerceAtLeast(30)
        val fatCalories = targetFatGrams * 9.0

        // 5. Carbohydrates Calculation (Remaining Calories)
        val remainingCalories = (targetCalories - proteinCalories - fatCalories).coerceAtLeast(0.0)
        val targetCarbsGrams = (remainingCalories / 4.0).roundToInt().coerceAtLeast(30)

        return CalculationResult(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            targetProtein = targetProteinGrams,
            targetFat = targetFatGrams,
            targetCarbs = targetCarbsGrams,
            warnings = warnings
        )
    }
}
