package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealLogStatus
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository

/** AC-7, BLOCKING -- local-first commit, called from the meal detail screen. */
class LogMealStatus(private val repository: MealPlanRepository) {
    suspend operator fun invoke(plannedMealId: String, status: MealLogStatus): AppResult<Unit> =
        repository.logMealStatus(plannedMealId, status)
}
