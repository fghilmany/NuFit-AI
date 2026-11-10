package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MacroTarget
import com.fghilmany.nufitai.domain.mealplan.entity.MealPlan
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.repository.MealPlanRepository
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.time.Instant

/**
 * AC-5/AC-8 -- caller is responsible for the regenerate-confirmation gate (checking for existing
 * MealLog rows) BEFORE invoking this; the repository unconditionally deletes+re-inserts the
 * week's meals once called.
 */
class GenerateMealPlan(private val repository: MealPlanRepository) {
    suspend operator fun invoke(
        weekStartDate: Instant,
        dayNumber: Int,
        target: MacroTarget,
        goal: GoalCategory,
        weightKgAtGeneration: Double,
        preference: MealPreference,
    ): AppResult<MealPlan> = repository.generateRemote(
        weekStartDate = weekStartDate,
        dayNumber = dayNumber,
        target = target,
        goal = goal,
        weightKgAtGeneration = weightKgAtGeneration,
        preference = preference,
    )
}
