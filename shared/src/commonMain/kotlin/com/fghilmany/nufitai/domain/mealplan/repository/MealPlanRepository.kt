package com.fghilmany.nufitai.domain.mealplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MacroTarget
import com.fghilmany.nufitai.domain.mealplan.entity.MealLog
import com.fghilmany.nufitai.domain.mealplan.entity.MealLogStatus
import com.fghilmany.nufitai.domain.mealplan.entity.MealPlan
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.entity.PlannedMeal
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.time.Instant

interface MealPlanRepository {
    /** null = that week was never generated (AC-6 -- week navigation never fabricates a week). */
    suspend fun getMealPlanForWeek(weekStartDate: Instant): AppResult<MealPlan?>
    suspend fun getPlannedMeals(mealPlanId: String): AppResult<List<PlannedMeal>>
    suspend fun getMealLogs(mealPlanId: String): AppResult<List<MealLog>>
    suspend fun getPlannedMealById(id: String): AppResult<PlannedMeal?>
    suspend fun getMealLogForPlannedMeal(plannedMealId: String): AppResult<MealLog?>

    /** AC-8: caller (`GenerateMealPlan`) is responsible for the confirmation gate before calling this -- deletes+re-inserts just [dayNumber]'s PlannedMeal/MealLog rows.
     * Per-day generate (2026-11-10): one HTTP call for [dayNumber] only, not a whole-week loop. */
    suspend fun generateRemote(
        weekStartDate: Instant,
        dayNumber: Int,
        target: MacroTarget,
        goal: GoalCategory,
        weightKgAtGeneration: Double,
        preference: MealPreference,
    ): AppResult<MealPlan>

    suspend fun logMealStatus(plannedMealId: String, status: MealLogStatus): AppResult<Unit>
}
