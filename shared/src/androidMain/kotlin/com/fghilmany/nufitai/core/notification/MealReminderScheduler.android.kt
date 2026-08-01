package com.fghilmany.nufitai.core.notification

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fghilmany.nufitai.core.auth.CurrentActivityHolder
import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot
import java.util.Calendar

internal const val MEAL_REMINDER_CHANNEL_ID = "meal_reminders"
internal const val MEAL_REMINDER_EXTRA_SLOT = "slot"
private const val REQUEST_CODE_BASE = 9100

/**
 * AlarmManager-based, one-shot alarms re-armed daily by [MealReminderReceiver] itself (there is
 * no repeating-exact-alarm API on Android). Requesting POST_NOTIFICATIONS (Android 13+) is a
 * follow-up -- [hasPermission] only reports current status, [schedule] silently no-ops without a
 * live Activity/Context rather than crashing (mirrors [CurrentActivityHolder]'s existing
 * "no active screen" handling in `GoogleSignInLauncher.android.kt`).
 */
actual class MealReminderScheduler actual constructor() {

    actual fun hasPermission(): Boolean {
        val context = CurrentActivityHolder.activity?.applicationContext ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    actual fun schedule(slot: MealSlot) {
        val context = CurrentActivityHolder.activity?.applicationContext ?: return
        scheduleAlarm(context, slot)
    }

    actual fun cancel(slot: MealSlot) {
        val context = CurrentActivityHolder.activity?.applicationContext ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(mealReminderPendingIntent(context, slot))
    }
}

/**
 * Shared by [MealReminderScheduler] and [MealReminderReceiver] (which re-arms tomorrow's alarm on
 * fire, using its own live `Context` rather than [CurrentActivityHolder] -- the app may not be
 * foregrounded when the alarm fires). Uses [AlarmManager.setAndAllowWhileIdle] (inexact, fires
 * within a small OS-determined window) rather than the exact-alarm APIs -- those additionally
 * require the "Alarms & reminders" special-access permission on API 33+, which isn't granted by
 * merely declaring `SCHEDULE_EXACT_ALARM` in the manifest (crashes with `SecurityException`
 * otherwise) and needs a dedicated settings flow to request. Meal reminders don't need
 * minute-level precision, so inexact is the right tradeoff, not a workaround.
 */
internal fun scheduleAlarm(context: Context, slot: MealSlot) {
    ensureNotificationChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerAtMillis = nextTriggerMillis(slot.defaultReminderHour())
    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, mealReminderPendingIntent(context, slot))
}

internal fun mealReminderPendingIntent(context: Context, slot: MealSlot): PendingIntent {
    val intent = Intent(context, MealReminderReceiver::class.java).apply { putExtra(MEAL_REMINDER_EXTRA_SLOT, slot.name) }
    return PendingIntent.getBroadcast(context, REQUEST_CODE_BASE + slot.ordinal, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}

private fun nextTriggerMillis(hourOfDay: Int): Long {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    if (calendar.timeInMillis <= System.currentTimeMillis()) {
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }
    return calendar.timeInMillis
}

private fun ensureNotificationChannel(context: Context) {
    val channel = NotificationChannelCompat.Builder(MEAL_REMINDER_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
        .setName("Meal Reminders")
        .build()
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}
