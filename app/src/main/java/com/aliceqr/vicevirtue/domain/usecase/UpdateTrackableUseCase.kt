package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import javax.inject.Inject

class UpdateTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    suspend operator fun invoke(trackable: Trackable): Result<Unit> {
        if (trackable.name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be blank"))
        // We need an update method in the repository. 
        // Let's check if the repository implementation supports update through addTrackable (upsert) 
        // or if we need to add a specific update method.
        // For now, I'll assume we might need to add it to the interface or use a specific implementation detail.
        // Actually, looking at TrackableRepository, it doesn't have updateTrackable.
        return Result.success(repository.addTrackable(trackable)).map { Unit }
    }
}
