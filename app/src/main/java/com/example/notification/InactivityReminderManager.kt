package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object InactivityReminderManager {
    const val CHANNEL_ID = "kbzhu_inactivity_reminders"
    const val NOTIFICATION_ID = 2026
    const val PREFS_NAME = "kbzhu_notification_prefs"
    const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val KEY_INTERVAL_HOURS = "interval_hours"
    const val KEY_LAST_ACTIVITY_TIME = "last_activity_time"
    const val WORK_NAME = "kbzhu_inactivity_reminder_work"

    fun onUserActivity(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis()).apply()
        scheduleReminder(context)
    }

    fun scheduleReminder(context: Context) {
        cancelLegacyAlarmManager(context)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

        if (prefs.getLong(KEY_LAST_ACTIVITY_TIME, 0L) <= 0L) {
            prefs.edit().putLong(KEY_LAST_ACTIVITY_TIME, System.currentTimeMillis()).apply()
        }

        if (!isEnabled) {
            cancelReminder(context)
            return
        }

        try {
            val workRequest = PeriodicWorkRequestBuilder<InactivityReminderWorker>(30, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            // Catch WorkManager uninitialized in unit test environments
            e.printStackTrace()
        }
    }

    fun triggerTestNotification(context: Context): Boolean {
        val areEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        if (!areEnabled) {
            return false
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val intervalHours = prefs.getInt(KEY_INTERVAL_HOURS, 4)

        InactivityReminderWorker.showReminderNotification(context, intervalHours, isTest = true)
        return true
    }

    fun cancelReminder(context: Context) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cancelLegacyAlarmManager(context)
    }

    private fun cancelLegacyAlarmManager(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, InactivityReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
