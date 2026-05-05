package com.aliceqr.vicevirtue.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.model.TrackableType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleReminder(reminder: Reminder, trackableName: String, trackableType: TrackableType) {
        if (!reminder.isEnabled) {
            cancelReminder(reminder)
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.aliceqr.vicevirtue.REMINDER_ACTION_${reminder.id}"
            putExtra("reminder_id", reminder.id)
            putExtra("trackable_id", reminder.trackableId)
            putExtra("trackable_name", trackableName)
            putExtra("trackable_type", trackableType.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If time is in the past, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            val alarmClockInfo = AlarmManager.AlarmClockInfo(
                calendar.timeInMillis,
                pendingIntent
            )
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            
            Log.d("ReminderManager", "Scheduled alarm clock for reminder ${reminder.id} at ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("ReminderManager", "SecurityException scheduling alarm clock: ${e.message}")
            // Fallback to non-exact
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.aliceqr.vicevirtue.REMINDER_ACTION_${reminder.id}"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
