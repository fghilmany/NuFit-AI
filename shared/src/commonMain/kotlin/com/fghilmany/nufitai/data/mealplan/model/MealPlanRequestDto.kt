package com.fghilmany.nufitai.data.mealplan.model

import com.fghilmany.nufitai.domain.mealplan.entity.MacroTarget
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.onboarding.entity.GoalCategory
import kotlinx.serialization.Serializable

@Serializable
data class MealPlanRequestDto(
    val target: MacroTargetDto,
    val goal: String,
    val preference: MealPreferenceDto,
    // Client orchestrates 7 sequential per-day calls instead of one 7-day request (2026-11-10) --
    // each invocation needs far less of Supabase Edge Functions' own wall-clock cap. See
    // MealPlanRepositoryImpl.generateRemote.
    val dayNumber: Int,
) {
    companion object {
        fun from(target: MacroTarget, goal: GoalCategory, preference: MealPreference, dayNumber: Int): MealPlanRequestDto = MealPlanRequestDto(
            target = MacroTargetDto.from(target),
            goal = goal.name,
            preference = MealPreferenceDto.from(preference),
            dayNumber = dayNumber,
        )
    }
}

@Serializable
data class MacroTargetDto(val calories: Int, val proteinG: Int, val carbG: Int, val fatG: Int) {
    fun toEntity(): MacroTarget = MacroTarget(calories = calories, proteinG = proteinG, carbG = carbG, fatG = fatG)

    companion object {
        fun from(target: MacroTarget): MacroTargetDto = MacroTargetDto(
            calories = target.calories,
            proteinG = target.proteinG,
            carbG = target.carbG,
            fatG = target.fatG,
        )
    }
}

@Serializable
data class MealPreferenceDto(
    val cuisineThemes: Set<String>,
    val mood: String?,
    val allergies: List<String>,
    val scheduleMode: String,
) {
    companion object {
        fun from(preference: MealPreference): MealPreferenceDto = MealPreferenceDto(
            cuisineThemes = preference.cuisineThemes,
            mood = preference.mood?.name,
            allergies = preference.allergies,
            scheduleMode = preference.scheduleMode.name,
        )
    }
}
