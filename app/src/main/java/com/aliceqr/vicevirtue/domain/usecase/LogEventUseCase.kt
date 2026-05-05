package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import java.util.Calendar
import javax.inject.Inject

class LogEventUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    /**
     * Logs an event for a given trackable.
     * Consolidation rule: events with the same description (including empty)
     * on the SAME CALENDAR DAY are merged — only one event is stored per
     * (trackableId, description, calendarDay) triple.
     * If a matching event already exists for today, no duplicate is inserted;
     * instead the existing event's timestamp is updated to now.
     */
    suspend operator fun invoke(
        trackableId: Long,
        description: String
    ): Result<Long> {
        val normalizedDesc = description.trim()
        val event = TrackableEvent(
            trackableId = trackableId,
            description = normalizedDesc,
            timestamp = System.currentTimeMillis()
        )
        return Result.success(repository.logEvent(event))
    }
}
