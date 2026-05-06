package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import java.util.Calendar
import javax.inject.Inject

class LogEventUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
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
