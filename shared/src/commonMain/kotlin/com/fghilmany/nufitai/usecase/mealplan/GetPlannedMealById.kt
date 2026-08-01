package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository

/** P-17 meal detail screen. */
class GetPlannedMealById(private val repository: MealPlanRepository) {
    suspend operator fun invoke(id: String): AppResult<PlannedMeal?> = repository.getPlannedMealById(id)
}
