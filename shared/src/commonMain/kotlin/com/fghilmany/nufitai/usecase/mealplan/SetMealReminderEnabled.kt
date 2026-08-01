package com.fghilmany.nufitai.usecase.mealplan

import com.fghilmany.nufitai.core.notification.MealReminderScheduler
import com.fghilmany.nufitai.data.mealplan.datasource.MealReminderPreferenceLocalDataSource
import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot

/** AC-11 -- fixed default times per slot, on/off toggle only. */
class SetMealReminderEnabled(
    private val preference: MealReminderPreferenceLocalDataSource,
    private val scheduler: MealReminderScheduler,
) {
    operator fun invoke(enabled: Boolean) {
        preference.setEnabled(enabled)
        MealSlot.entries.forEach { slot -> if (enabled) scheduler.schedule(slot) else scheduler.cancel(slot) }
    }
}
