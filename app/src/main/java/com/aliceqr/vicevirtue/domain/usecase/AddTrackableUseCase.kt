package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import javax.inject.Inject

class AddTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    suspend operator fun invoke(name: String, type: TrackableType): Result<Long> {
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be blank"))
        val trackable = Trackable(name = name.trim(), type = type)
        return Result.success(repository.addTrackable(trackable))
    }
}
