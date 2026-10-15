package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class InactivityReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(InactivityReminderManager.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true)
        if (!isEnabled) return

        val lastActivityTime = prefs.getLong(InactivityReminderManager.KEY_LAST_ACTIVITY_TIME, 0L)
        val intervalHours = prefs.getInt(InactivityReminderManager.KEY_INTERVAL_HOURS, 4)
        val currentTime = System.currentTimeMillis()

        // Verify inactive interval has elapsed (at least 4 hours, or test flag)
        val isTest = intent.getBooleanExtra(InactivityReminderManager.EXTRA_IS_TEST, false)
        val elapsedHours = (currentTime - lastActivityTime) / (1000 * 60 * 60)

        if (!isTest && lastActivityTime > 0 && elapsedHours < intervalHours) {
            // Activity occurred recently, reschedule
            InactivityReminderManager.scheduleReminder(context)
            return
        }

        // Quiet hours check: don't notify between 22:30 and 08:30 unless test
        if (!isTest) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            if (hour < 8 || hour >= 22) {
                // Reschedule for next morning at 9:00 AM
                InactivityReminderManager.scheduleReminder(context)
                return
            }
        }

        showPushNotification(context, isTest)
        // Schedule next check
        InactivityReminderManager.scheduleReminder(context)
    }

    private fun showPushNotification(context: Context, isTest: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded(notificationManager)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isTest) {
            "Тестовое напоминание КБЖУ Рацион 🥗"
        } else {
            "Время подкрепиться или выпить воды! 💧🥗"
        }

        val message = if (isTest) {
            "Система Push-уведомлений работает отлично! Напоминания активны каждые 4 часа."
        } else {
            "Вы не вносили записи более 4 часов. Зафиксируйте приём пищи или стакан воды для поддержания нормы!"
        }

        val notification = NotificationCompat.Builder(context, InactivityReminderManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(0xFFFC6C85.toInt()) // WildWatermelon
            .build()

        notificationManager.notify(InactivityReminderManager.NOTIFICATION_ID, notification)
    }

    private fun createChannelIfNeeded(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                InactivityReminderManager.CHANNEL_ID,
                "Напоминания о рационе и воде",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Напоминает о фиксации приёма пищи и водного баланса при неактивности"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
