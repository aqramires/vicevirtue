package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemindersUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    operator fun invoke(trackableId: Long): Flow<List<Reminder>> {
        return repository.getRemindersForTrackable(trackableId)
    }
}
