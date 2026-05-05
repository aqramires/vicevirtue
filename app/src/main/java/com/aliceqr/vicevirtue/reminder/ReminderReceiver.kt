package com.aliceqr.vicevirtue.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var reminderManager: ReminderManager

    @Inject
    lateinit var repository: TrackableRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        val reminderId = intent.getLongExtra("reminder_id", -1L)
        val trackableId = intent.getLongExtra("trackable_id", -1L)
        val trackableName = intent.getStringExtra("trackable_name") ?: "ViceVirtue"
        val typeName = intent.getStringExtra("trackable_type") ?: TrackableType.VICE.name
        val trackableType = try { TrackableType.valueOf(typeName) } catch (e: Exception) { TrackableType.VICE }

        if (reminderId == -1L) {
            pendingResult.finish()
            return
        }

        // Use a coroutine to check DB before showing notification
        scope.launch {
            try {
                val reminder = repository.getReminderById(reminderId)
                if (reminder != null && reminder.isEnabled) {
                    notificationHelper.showReminderNotification(trackableId, trackableName, trackableType)
                    
                    // Reschedule for tomorrow
                    reminderManager.scheduleReminder(reminder, trackableName, trackableType)
                }
            } catch (e: Exception) {
                Log.e("ReminderReceiver", "Error processing alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
