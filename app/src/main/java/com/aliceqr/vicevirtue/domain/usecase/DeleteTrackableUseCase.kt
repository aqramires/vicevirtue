package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.reminder.ReminderManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository,
    private val reminderManager: ReminderManager
) {
    suspend operator fun invoke(trackable: Trackable) {
        // Cancel all alarms before deleting
        val reminders = repository.getRemindersForTrackable(trackable.id).first()
        reminders.forEach { reminder ->
            reminderManager.cancelReminder(reminder)
        }
        repository.deleteTrackable(trackable)
    }
}
