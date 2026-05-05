package com.aliceqr.vicevirtue.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: TrackableRepository

    @Inject
    lateinit var reminderManager: ReminderManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling active reminders")
            scope.launch {
                val enabledReminders = repository.getAllEnabledReminders()
                enabledReminders.forEach { reminder ->
                    val trackable = repository.getTrackableById(reminder.trackableId)
                    if (trackable != null) {
                        reminderManager.scheduleReminder(reminder, trackable.name, trackable.type)
                    }
                }
            }
        }
    }
}
