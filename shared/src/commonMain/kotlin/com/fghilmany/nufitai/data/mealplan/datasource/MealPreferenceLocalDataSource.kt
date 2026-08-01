package com.fghilmany.nufitai.data.mealplan.datasource

import com.fghilmany.nufitai.db.Meal_preference as MealPreferenceRow
import com.fghilmany.nufitai.db.NuFitDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SINGLETON_ID = "meal_preference"

class MealPreferenceLocalDataSource(private val database: NuFitDatabase) {

    suspend fun getMealPreference(): MealPreferenceRow? = withContext(Dispatchers.Default) {
        database.mealPreferenceQueries.getMealPreference(SINGLETON_ID).executeAsOneOrNull()
    }

    suspend fun upsertMealPreference(cuisineThemesJson: String, mood: String?, allergiesJson: String, scheduleMode: String) = withContext(Dispatchers.Default) {
        database.mealPreferenceQueries.upsertMealPreference(SINGLETON_ID, cuisineThemesJson, mood, allergiesJson, scheduleMode)
    }
}
