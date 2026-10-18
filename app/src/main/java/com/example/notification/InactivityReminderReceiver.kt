package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class InactivityReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        InactivityReminderManager.scheduleReminder(context)
    }
}
