package com.example.data.calculator

import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class KbzhuCalculatorTest {

    @Test
    fun testStandardMaleBmrAndTdee() {
        val result = KbzhuCalculator.calculate(
            gender = Gender.MALE,
            age = 26,
            heightCm = 178.0,
            weightKg = 75.0,
            activityLevel = ActivityLevel.MODERATE,
            goal = FitnessGoal.MAINTENANCE
        )

        assertEquals(1738, result.bmr)
        assertEquals(2694, result.tdee)
        assertEquals(2694, result.targetCalories)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun testEnergyAlignmentAcrossProfiles() {
        val testProfiles = listOf(
            ProfileSpec(Gender.MALE, 26, 178.0, 75.0, ActivityLevel.MODERATE, FitnessGoal.MAINTENANCE),
            ProfileSpec(Gender.FEMALE, 30, 165.0, 60.0, ActivityLevel.LIGHT, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.MALE, 40, 185.0, 95.0, ActivityLevel.HIGH, FitnessGoal.WEIGHT_GAIN),
            ProfileSpec(Gender.FEMALE, 110, 165.0, 110.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.FEMALE, 80, 150.0, 40.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.MALE, 16, 170.0, 55.0, ActivityLevel.MODERATE, FitnessGoal.MAINTENANCE),
            ProfileSpec(Gender.FEMALE, 22, 170.0, 48.0, ActivityLevel.LIGHT, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.MALE, 50, 175.0, 80.0, ActivityLevel.VERY_HIGH, FitnessGoal.WEIGHT_GAIN),
            ProfileSpec(Gender.FEMALE, 35, 160.0, 70.0, ActivityLevel.MODERATE, FitnessGoal.MAINTENANCE),
            ProfileSpec(Gender.MALE, 28, 180.0, 120.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS)
        )

        for (spec in testProfiles) {
            val res = KbzhuCalculator.calculate(
                gender = spec.gender,
                age = spec.age,
                heightCm = spec.heightCm,
                weightKg = spec.weightKg,
                activityLevel = spec.activityLevel,
                goal = spec.goal
            )

            val pfcKcal = (res.targetProtein * 4) + (res.targetFat * 9) + (res.targetCarbs * 4)
            val diff = abs(pfcKcal - res.targetCalories)

            assertTrue(
                "Profile $spec PFC calories ($pfcKcal) differs from target (${res.targetCalories}) by $diff",
                diff <= 10
            )
        }
    }

    @Test
    fun testProteinCappedAt35PercentCalories() {
        val testProfiles = listOf(
            ProfileSpec(Gender.FEMALE, 30, 165.0, 110.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.MALE, 28, 180.0, 130.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.FEMALE, 25, 160.0, 95.0, ActivityLevel.LIGHT, FitnessGoal.WEIGHT_LOSS)
        )

        for (spec in testProfiles) {
            val res = KbzhuCalculator.calculate(
                gender = spec.gender,
                age = spec.age,
                heightCm = spec.heightCm,
                weightKg = spec.weightKg,
                activityLevel = spec.activityLevel,
                goal = spec.goal
            )

            val proteinCal = res.targetProtein * 4.0
            val maxAllowedCal = res.targetCalories * 0.35 + 1.0 // 35% with tolerance

            assertTrue(
                "Protein calories $proteinCal exceeds 35% of total ${res.targetCalories} ($maxAllowedCal)",
                proteinCal <= maxAllowedCal
            )
        }
    }

    @Test
    fun testWeightLossNeverExceedsTdee() {
        val testProfiles = listOf(
            ProfileSpec(Gender.FEMALE, 80, 150.0, 40.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.FEMALE, 30, 165.0, 60.0, ActivityLevel.LIGHT, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.MALE, 25, 175.0, 70.0, ActivityLevel.MODERATE, FitnessGoal.WEIGHT_LOSS)
        )

        for (spec in testProfiles) {
            val res = KbzhuCalculator.calculate(
                gender = spec.gender,
                age = spec.age,
                heightCm = spec.heightCm,
                weightKg = spec.weightKg,
                activityLevel = spec.activityLevel,
                goal = spec.goal
            )

            assertTrue(
                "Target calories ${res.targetCalories} exceeds TDEE ${res.tdee} under WEIGHT_LOSS",
                res.targetCalories <= maxOf(1000, res.tdee)
            )
        }
    }

    @Test
    fun testExtremeProfileWarnings() {
        // 1. Under 18 warning
        val childRes = KbzhuCalculator.calculate(
            gender = Gender.MALE,
            age = 15,
            heightCm = 170.0,
            weightKg = 55.0,
            activityLevel = ActivityLevel.MODERATE,
            goal = FitnessGoal.MAINTENANCE
        )
        assertTrue(childRes.warnings.any { it.contains("Для детей и подростков") })

        // 2. Underweight + WEIGHT_LOSS
        val underweightRes = KbzhuCalculator.calculate(
            gender = Gender.FEMALE,
            age = 22,
            heightCm = 170.0,
            weightKg = 48.0, // BMI = 16.6
            activityLevel = ActivityLevel.LIGHT,
            goal = FitnessGoal.WEIGHT_LOSS
        )
        assertTrue(underweightRes.warnings.any { it.contains("При недостаточном весе") })

        // 3. Deficit impossible floor >= TDEE
        val elderlyRes = KbzhuCalculator.calculate(
            gender = Gender.FEMALE,
            age = 80,
            heightCm = 150.0,
            weightKg = 40.0,
            activityLevel = ActivityLevel.SEDENTARY,
            goal = FitnessGoal.WEIGHT_LOSS
        )
        assertTrue(elderlyRes.warnings.any { it.contains("Дефицит невозможен") })
    }

    @Test
    fun testExtremeIntermediateValuesNoCrash() {
        val extremeValues = listOf(
            ProfileSpec(Gender.MALE, 1, 1.0, 1.0, ActivityLevel.SEDENTARY, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.FEMALE, 0, 0.0, 0.0, ActivityLevel.LIGHT, FitnessGoal.WEIGHT_LOSS),
            ProfileSpec(Gender.MALE, 5, 16.0, 5.0, ActivityLevel.MODERATE, FitnessGoal.MAINTENANCE),
            ProfileSpec(Gender.FEMALE, 200, 500.0, 1000.0, ActivityLevel.VERY_HIGH, FitnessGoal.WEIGHT_GAIN),
            ProfileSpec(Gender.MALE, -10, -50.0, -10.0, ActivityLevel.HIGH, FitnessGoal.WEIGHT_LOSS)
        )

        for (spec in extremeValues) {
            val res = KbzhuCalculator.calculate(
                gender = spec.gender,
                age = spec.age,
                heightCm = spec.heightCm,
                weightKg = spec.weightKg,
                activityLevel = spec.activityLevel,
                goal = spec.goal
            )

            assertTrue(res.targetCalories >= 1000)
            assertTrue(res.targetProtein >= 40)
            assertTrue(res.targetFat >= 30)
            assertTrue(res.targetCarbs >= 30)
        }
    }

    private data class ProfileSpec(
        val gender: Gender,
        val age: Int,
        val heightCm: Double,
        val weightKg: Double,
        val activityLevel: ActivityLevel,
        val goal: FitnessGoal
    )
}
