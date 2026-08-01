package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.notification.MealReminderScheduler
import com.fghilmany.nufitai.data.mealplan.datasource.MealReminderPreferenceLocalDataSource

data class MealReminderState(val enabled: Boolean, val hasPermission: Boolean)

class GetMealReminderState(
    private val preference: MealReminderPreferenceLocalDataSource,
    private val scheduler: MealReminderScheduler,
) {
    operator fun invoke(): MealReminderState = MealReminderState(enabled = preference.isEnabled(), hasPermission = scheduler.hasPermission())
}
