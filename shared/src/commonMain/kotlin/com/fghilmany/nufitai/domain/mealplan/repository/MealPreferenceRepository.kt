package com.fghilmany.nufitai.domain.mealplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference

interface MealPreferenceRepository {
    /** null = never saved -- caller applies its own defaults (empty themes, AI_RANDOM). */
    suspend fun getMealPreference(): AppResult<MealPreference?>
    suspend fun saveMealPreference(preference: MealPreference): AppResult<Unit>
}
