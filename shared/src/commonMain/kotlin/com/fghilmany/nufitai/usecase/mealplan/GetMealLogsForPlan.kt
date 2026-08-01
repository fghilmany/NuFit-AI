package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealLog
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository

/** AC-8 -- used to check whether the viewed week has any non-PENDING log before allowing regenerate. */
class GetMealLogsForPlan(private val repository: MealPlanRepository) {
    suspend operator fun invoke(mealPlanId: String): AppResult<List<MealLog>> = repository.getMealLogs(mealPlanId)
}
