package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.Failure
import com.fghilmany.nufitai.domain.fullassessment.entity.FullAssessmentResult
import com.fghilmany.nufitai.domain.fullassessment.entity.Gender
import com.fghilmany.nufitai.domain.mealplan.entity.MacroTarget
import com.fghilmany.nufitai.domain.onboarding.entity.BodyMeasurement
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.math.roundToInt

private const val PROTEIN_G_PER_KG = 1.8
private const val FAT_CALORIE_SHARE = 0.25
private const val KCAL_PER_G_FAT = 9
private const val KCAL_PER_G_CARB = 4
private const val FAT_LOSS_MULTIPLIER = 0.85
private const val MUSCLE_GAIN_MULTIPLIER = 1.10

/**
 * techspec meal-plan.md §5 (AC-4, BLOCKING) -- pure, on-device, no repository dependency: TDEE/macro
 * targets must never require a network call. Mifflin-St Jeor BMR + activity multiplier + goal
 * calorie adjustment. Coefficients are proposed defaults (techspec §5), NOT yet dev-confirmed.
 */
class CalculateTdeeAndMacros {
    operator fun invoke(bodyMeasurement: BodyMeasurement, fullAssessmentResult: FullAssessmentResult): AppResult<MacroTarget> {
        val heightCm = bodyMeasurement.heightCm
        val weightKg = bodyMeasurement.weightKg
        val age = fullAssessmentResult.age
        val gender = fullAssessmentResult.gender
        val activityLevel = fullAssessmentResult.activityLevel

        if (heightCm == null || weightKg == null || age == null || gender == null || activityLevel == null) {
            return AppResult.Error(Failure.Validation("Data tubuh/aktivitas belum lengkap"))
        }

        val bmr = when (gender) {
            Gender.MALE -> 10 * weightKg + 6.25 * heightCm - 5 * age + 5
            Gender.FEMALE -> 10 * weightKg + 6.25 * heightCm - 5 * age - 161
        }
        val tdee = bmr * activityLevel.tdeeMultiplier

        val goalMultiplier = when (fullAssessmentResult.goal) {
            GoalCategory.FAT_LOSS -> FAT_LOSS_MULTIPLIER
            GoalCategory.MUSCLE_GAIN -> MUSCLE_GAIN_MULTIPLIER
            GoalCategory.GENERAL_HEALTH, GoalCategory.STRENGTH, GoalCategory.ENDURANCE -> 1.0
        }
        val calories = tdee * goalMultiplier

        val proteinG = PROTEIN_G_PER_KG * weightKg
        val fatG = (calories * FAT_CALORIE_SHARE) / KCAL_PER_G_FAT
        val carbG = (calories - proteinG * KCAL_PER_G_CARB - fatG * KCAL_PER_G_FAT) / KCAL_PER_G_CARB

        return AppResult.Success(
            MacroTarget(
                calories = calories.roundToInt(),
                proteinG = proteinG.roundToInt(),
                carbG = carbG.roundToInt(),
                fatG = fatG.roundToInt(),
            ),
        )
    }
}
