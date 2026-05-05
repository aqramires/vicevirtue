package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import javax.inject.Inject

class DeleteTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    suspend operator fun invoke(trackable: Trackable) =
        repository.deleteTrackable(trackable)
}
