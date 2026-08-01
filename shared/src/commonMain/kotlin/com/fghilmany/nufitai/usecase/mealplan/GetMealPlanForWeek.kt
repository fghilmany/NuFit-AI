package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealPlan
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository
import kotlin.time.Instant

/** AC-6 -- null means that week was never generated; week navigation must never fabricate one. */
class GetMealPlanForWeek(private val repository: MealPlanRepository) {
    suspend operator fun invoke(weekStartDate: Instant): AppResult<MealPlan?> = repository.getMealPlanForWeek(weekStartDate)
}
