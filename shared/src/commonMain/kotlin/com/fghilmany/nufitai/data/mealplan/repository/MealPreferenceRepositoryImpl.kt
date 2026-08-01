package com.fghilmany.nufitai.data.mealplan.repository

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.core.error.runCatchingDatabase
import com.fghilmany.nufitai.data.mealplan.datasource.MealPreferenceLocalDataSource
import com.fghilmany.nufitai.domain.mealplan.entity.MealMood
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.entity.MealScheduleMode
import com.fghilmany.nufitai.domain.mealplan.repository.MealPreferenceRepository
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MealPreferenceRepositoryImpl(
    private val local: MealPreferenceLocalDataSource,
) : MealPreferenceRepository {
    private val json = Json

    override suspend fun getMealPreference(): AppResult<MealPreference?> = runCatchingDatabase {
        val row = local.getMealPreference() ?: return@runCatchingDatabase null
        MealPreference(
            cuisineThemes = json.decodeFromString(row.cuisine_themes),
            mood = row.mood?.let { MealMood.valueOf(it) },
            allergies = json.decodeFromString(row.allergies),
            scheduleMode = MealScheduleMode.valueOf(row.schedule_mode),
        )
    }

    override suspend fun saveMealPreference(preference: MealPreference): AppResult<Unit> = runCatchingDatabase {
        local.upsertMealPreference(
            cuisineThemesJson = json.encodeToString(preference.cuisineThemes),
            mood = preference.mood?.name,
            allergiesJson = json.encodeToString(preference.allergies),
            scheduleMode = preference.scheduleMode.name,
        )
    }
}
