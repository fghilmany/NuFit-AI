package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository

class GetPlannedMealsForPlan(private val repository: MealPlanRepository) {
    suspend operator fun invoke(mealPlanId: String): AppResult<List<PlannedMeal>> = repository.getPlannedMeals(mealPlanId)
}
