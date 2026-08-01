package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.repository.MealPreferenceRepository

/** null = never saved -- caller applies its own defaults (empty themes, AI_RANDOM). */
class GetMealPreference(private val repository: MealPreferenceRepository) {
    suspend operator fun invoke(): AppResult<MealPreference?> = repository.getMealPreference()
}
