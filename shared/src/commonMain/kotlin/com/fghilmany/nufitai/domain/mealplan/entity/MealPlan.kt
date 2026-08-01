package com.fghilmany.nufitai.domain.mealplan.entity

import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlin.time.Instant

/**
 * One row per week -- keyed by [weekStartDate] (start-of-week, midnight, mirrors this codebase's
 * existing convention of representing dates as [Instant] rather than `kotlinx.datetime.LocalDate`,
 * which has no precedent anywhere else here). NOT soft-deleted/archived on regenerate --
 * regenerating the currently-viewed week overwrites that week's row in place (after the AC-8
 * confirmation); other weeks stay untouched and independently browsable (AC-6).
 */
data class MealPlan(
    val id: String,
    val weekStartDate: Instant,
    val target: MacroTarget,
    /** Body weight snapshot at generation time -- compared against the latest `BodyMeasurement` for the regenerate-suggestion banner (AC-9). */
    val weightKgAtGeneration: Double,
    val goal: GoalCategory,
    val createdAt: Instant,
)
