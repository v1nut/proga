package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object InactivityReminderManager {
    const val CHANNEL_ID = "kbzhu_inactivity_reminders"
    const val NOTIFICATION_ID = 2026
    const val PREFS_NAME = "kbzhu_notification_prefs"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_INTERVAL_HOURS = "interval_hours"
    const val KEY_LAST_ACTIVITY_TIME = "last_activity_time"
    const val EXTRA_IS_TEST = "extra_is_test"

    fun onUserActivity(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis()).apply()
        scheduleReminder(context)
    }

    fun scheduleReminder(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        if (!isEnabled) {
            cancelReminder(context)
            return
        }

        val intervalHours = prefs.getInt(KEY_INTERVAL_HOURS, 4)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, InactivityReminderReceiver::class.java).apply {
            putExtra(EXTRA_IS_TEST, false)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + (intervalHours * 3600 * 1000L)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerTestNotification(context: Context) {
        val intent = Intent(context, InactivityReminderReceiver::class.java).apply {
            putExtra(EXTRA_IS_TEST, true)
        }
        context.sendBroadcast(intent)
    }

    fun cancelReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, InactivityReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
