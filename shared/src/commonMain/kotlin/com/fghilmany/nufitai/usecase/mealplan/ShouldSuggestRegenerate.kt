package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealPlan
import com.fghilmany.nufitai.usecase.onboarding.GetLatestBodyMeasurement
import kotlin.math.abs

/** techspec meal-plan.md §5 -- threshold proposed by dev recommendation, not design-confirmed. */
private const val WEIGHT_CHANGE_THRESHOLD_KG = 3.0

/** AC-9 -- suggestion only, never triggers generation itself. */
class ShouldSuggestRegenerate(private val getLatestBodyMeasurement: GetLatestBodyMeasurement) {
    suspend operator fun invoke(mealPlan: MealPlan): AppResult<Boolean> =
        when (val result = getLatestBodyMeasurement()) {
            is AppResult.Error -> result
            is AppResult.Success -> {
                val latestWeightKg = result.data?.weightKg
                val suggest = latestWeightKg != null && abs(latestWeightKg - mealPlan.weightKgAtGeneration) >= WEIGHT_CHANGE_THRESHOLD_KG
                AppResult.Success(suggest)
            }
        }
}
