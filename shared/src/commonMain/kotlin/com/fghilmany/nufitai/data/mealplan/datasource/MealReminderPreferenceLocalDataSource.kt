package com.fghilmany.nufitai.data.mealplan.datasource

import com.russhwolf.settings.Settings

private const val KEY_ENABLED = "meal_reminders_enabled"

/** techspec meal-plan.md §3 -- on/off toggle only, mirrors `LanguageLocalDataSource`'s Settings pattern. */
class MealReminderPreferenceLocalDataSource(private val settings: Settings) {
    fun isEnabled(): Boolean = settings.getBoolean(KEY_ENABLED, false)
    fun setEnabled(enabled: Boolean) = settings.putBoolean(KEY_ENABLED, enabled)
}
