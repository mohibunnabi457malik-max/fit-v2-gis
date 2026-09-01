package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.FitDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WorkoutAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("reminder_id", 1L).toInt()
        
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showNotification(
            context = context,
            reminderId = reminderId,
            title = "Time to Crush Your Workout! 💪",
            message = "Your scheduled workout is ready. Let's stay consistent today!"
        )

        // Reschedule for next week
        CoroutineScope(Dispatchers.IO).launch {
            val db = FitDatabase.getDatabase(context)
            val reminders = db.fitDao().getAllReminders()
            val reminder = reminders.find { it.id.toInt() == reminderId }
            if (reminder != null && reminder.isEnabled) {
                NotificationHelper.scheduleWorkoutReminder(context, reminder)
            }
        }
    }
}
