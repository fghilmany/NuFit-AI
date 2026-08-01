package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.error.AppResult
import com.fghilmany.nufitai.domain.mealplan.entity.MealPreference
import com.fghilmany.nufitai.domain.mealplan.repository.MealPreferenceRepository

/** AC-5 -- "Simpan Preferensi" CTA. */
class SaveMealPreference(private val repository: MealPreferenceRepository) {
    suspend operator fun invoke(preference: MealPreference): AppResult<Unit> = repository.saveMealPreference(preference)
}
