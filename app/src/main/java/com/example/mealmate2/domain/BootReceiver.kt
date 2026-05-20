package com.example.mealmate2.domain

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mealmate2.data.UserPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = UserPreferences(context)
        if (prefs.weightReminderEnabled) {
            ReminderScheduler(context).scheduleWeightReminder(true, prefs.weightReminderTime)
        }
    }
}
