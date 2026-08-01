package com.fghilmany.nufitai.core.notification

import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot

/**
 * TODO(dev): wire UNUserNotificationCenter (authorization request + UNCalendarNotificationTrigger)
 * here once configured -- mirrors `AppleSignInLauncher.ios.kt`'s existing precedent of stubbing
 * genuinely-unverified native integration rather than shipping unverified cinterop. [hasPermission]
 * defaults to false (safe: the UI's inline note shows rather than assuming reminders will fire).
 */
actual class MealReminderScheduler actual constructor() {
    actual fun hasPermission(): Boolean = false
    actual fun schedule(slot: MealSlot) = Unit
    actual fun cancel(slot: MealSlot) = Unit
}
