package com.aliceqr.vicevirtue.domain.usecase

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repository: TrackableRepository
) {
    // All events, newest first
    fun allEvents(): Flow<List<TrackableEvent>> = repository.getAllEvents()

    // Events filtered by type (requires joining with trackable table)
    fun eventsByType(type: TrackableType, allTrackables: List<Trackable>): Flow<List<TrackableEvent>> {
        val ids = allTrackables.filter { it.type == type }.map { it.id }.toSet()
        return repository.getAllEvents().map { events -> events.filter { it.trackableId in ids } }
    }

    // Events for a specific trackable
    fun eventsForTrackable(trackableId: Long): Flow<List<TrackableEvent>> =
        repository.getEventsForTrackable(trackableId)

    // Events in a date range
    fun eventsInRange(from: Long, to: Long): Flow<List<TrackableEvent>> =
        repository.getEventsBetween(from, to)

    // Events for a specific trackable in a date range
    fun eventsForTrackableInRange(trackableId: Long, from: Long, to: Long): Flow<List<TrackableEvent>> =
        repository.getEventsForTrackableBetween(trackableId, from, to)
}
