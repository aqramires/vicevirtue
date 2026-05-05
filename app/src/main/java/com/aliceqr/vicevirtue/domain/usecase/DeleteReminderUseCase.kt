package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.reminder.ReminderManager
import javax.inject.Inject

class DeleteReminderUseCase @Inject constructor(
    private val repository: TrackableRepository,
    private val reminderManager: ReminderManager
) {
    suspend operator fun invoke(reminder: Reminder): Result<Unit> {
        reminderManager.cancelReminder(reminder)
        repository.deleteReminder(reminder)
        return Result.success(Unit)
    }
}
