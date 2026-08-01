package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealLog
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository

class GetMealLogForPlannedMeal(private val repository: MealPlanRepository) {
    suspend operator fun invoke(plannedMealId: String): AppResult<MealLog?> = repository.getMealLogForPlannedMeal(plannedMealId)
}
