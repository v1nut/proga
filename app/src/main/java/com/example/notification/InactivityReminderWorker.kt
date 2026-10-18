package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import java.time.LocalTime

class InactivityReminderWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = appContext.getSharedPreferences(InactivityReminderManager.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(InactivityReminderManager.KEY_NOTIFICATIONS_ENABLED, true)
        if (!isEnabled) return Result.success()

        val areNotificationsEnabled = androidx.core.app.NotificationManagerCompat.from(appContext).areNotificationsEnabled()
        if (!areNotificationsEnabled) return Result.success()

        // Quiet Hours check (22:00 – 09:00): Do NOT notify during quiet hours!
        val currentHour = LocalTime.now().hour
        if (currentHour >= 22 || currentHour < 9) {
            return Result.success()
        }

        val lastActivityTime = prefs.getLong(InactivityReminderManager.KEY_LAST_ACTIVITY_TIME, 0L)
        val intervalHours = prefs.getInt(InactivityReminderManager.KEY_INTERVAL_HOURS, 4)
        val currentTime = System.currentTimeMillis()

        if (lastActivityTime <= 0L) {
            prefs.edit().putLong(InactivityReminderManager.KEY_LAST_ACTIVITY_TIME, currentTime).apply()
            return Result.success()
        }

        val elapsedMillis = currentTime - lastActivityTime
        val requiredMillis = intervalHours * 3600 * 1000L
        if (elapsedMillis < requiredMillis) {
            return Result.success()
        }

        showReminderNotification(appContext, intervalHours, isTest = false)
        prefs.edit().putLong(InactivityReminderManager.KEY_LAST_ACTIVITY_TIME, currentTime).apply()
        return Result.success()
    }

    companion object {
        fun formatHoursWord(hours: Int): String {
            val rem10 = hours % 10
            val rem100 = hours % 100
            return when {
                hours == 1 || (rem10 == 1 && rem100 != 11) -> "час"
                hours in 2..4 || (rem10 in 2..4 && rem100 !in 12..14) -> "часа"
                else -> "часов"
            }
        }

        fun showReminderNotification(context: Context, intervalHours: Int, isTest: Boolean) {
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

            val hoursWord = formatHoursWord(intervalHours)

            val title = if (isTest) {
                "Тестовое напоминание «КБЖУ Рацион» 🥗"
            } else {
                "Время зафиксировать приём пищи или воду! 💧🥗"
            }

            val message = if (isTest) {
                "Система напоминаний работает отлично! Интервал проверки: $intervalHours $hoursWord."
            } else {
                "Вы не вносили записи более $intervalHours $hoursWord. Зафиксируйте приём пищи или стакан воды для поддержания нормы!"
            }

            val notification = NotificationCompat.Builder(context, InactivityReminderManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(0xFFFC6C85.toInt())
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
}
