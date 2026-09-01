package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.database.FitDatabase
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = FitDatabase.getDatabase(context)
                val reminders = db.fitDao().getAllReminders()
                for (reminder in reminders) {
                    if (reminder.isEnabled) {
                        NotificationHelper.scheduleWorkoutReminder(context, reminder)
                    }
                }
            }
        }
    }
}
