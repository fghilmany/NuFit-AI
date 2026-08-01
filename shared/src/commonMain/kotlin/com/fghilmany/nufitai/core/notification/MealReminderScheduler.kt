package com.fghilmany.nufitai.core.notification

import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot

/** techspec meal-plan.md §3 -- fixed default per slot, no custom time picker this pass. */
fun MealSlot.defaultReminderHour(): Int = when (this) {
    MealSlot.BREAKFAST -> 7
    MealSlot.LUNCH -> 12
    MealSlot.DINNER -> 19
}

/**
 * techspec meal-plan.md §6 -- first local-notification feature in this codebase (tech-stack.md's
 * already-locked AlarmManager/UNUserNotificationCenter choice, unused until now). Minimal expect
 * surface per architecture.md §6: schedule/cancel + a permission check only. Requesting the
 * permission itself (Android 13+ POST_NOTIFICATIONS prompt / iOS's authorization prompt) happens
 * as a side effect of [schedule] on both platforms -- there's no separate "request" entry point,
 * matching a lazy-request-on-opt-in UX. [hasPermission] reports current/last-known status only,
 * so the UI can show an inline note (§8) rather than crash or silently no-op.
 */
expect class MealReminderScheduler() {
    fun hasPermission(): Boolean
    fun schedule(slot: MealSlot)
    fun cancel(slot: MealSlot)
}
