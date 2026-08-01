package com.fghilmany.nufitai.core.notification

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.fghilmany.nufitai.domain.mealplan.entity.MealSlot

/** Registered in `androidApp`'s AndroidManifest.xml. Fires the notification, then re-arms tomorrow's alarm (AlarmManager has no repeating-exact-alarm API). */
class MealReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getStringExtra(MEAL_REMINDER_EXTRA_SLOT)?.let { runCatching { MealSlot.valueOf(it) }.getOrNull() } ?: return

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            // TODO(dev): replace with a real app icon resource -- placeholder system drawable for now.
            val notification = NotificationCompat.Builder(context, MEAL_REMINDER_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("NuFit AI")
                .setContentText(slot.name)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(slot.ordinal, notification)
        }

        scheduleAlarm(context, slot)
    }
}
