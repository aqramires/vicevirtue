package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTrackableUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    operator fun invoke(id: Long): Flow<Trackable?> {
        return repository.getAllTrackables().map { list ->
            list.find { it.id == id }
        }
    }
}
