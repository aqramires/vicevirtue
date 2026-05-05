package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.reminder.ReminderManager
import javax.inject.Inject

class SaveReminderUseCase @Inject constructor(
    private val repository: TrackableRepository,
    private val reminderManager: ReminderManager
) {
    suspend operator fun invoke(
        reminder: Reminder, 
        trackableName: String,
        trackableType: com.aliceqr.vicevirtue.domain.model.TrackableType
    ): Result<Long> {
        // If ID is negative, it's a temporary UI ID, so we reset to 0 for DB insertion
        val reminderToSave = if (reminder.id < 0) reminder.copy(id = 0) else reminder
        val id = repository.saveReminder(reminderToSave)
        val savedReminder = reminderToSave.copy(id = if (reminderToSave.id == 0L) id else reminderToSave.id)
        
        if (savedReminder.isEnabled) {
            reminderManager.scheduleReminder(savedReminder, trackableName, trackableType)
        } else {
            reminderManager.cancelReminder(savedReminder)
        }
        
        return Result.success(id)
    }
}
