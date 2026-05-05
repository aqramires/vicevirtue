package com.aliceqr.vicevirtue.domain.repository

import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
import kotlinx.coroutines.flow.Flow

interface TrackableRepository {
    fun getAllTrackables(): Flow<List<Trackable>>
    fun getTrackablesByType(type: TrackableType): Flow<List<Trackable>>
    suspend fun getTrackableById(id: Long): Trackable?
    suspend fun addTrackable(trackable: Trackable): Long
    suspend fun deleteTrackable(trackable: Trackable)

    fun getAllEvents(): Flow<List<TrackableEvent>>
    fun getEventsForTrackable(trackableId: Long): Flow<List<TrackableEvent>>
    fun getEventsBetween(from: Long, to: Long): Flow<List<TrackableEvent>>
    fun getEventsForTrackableBetween(trackableId: Long, from: Long, to: Long): Flow<List<TrackableEvent>>
    
    // Added for LogEventUseCase consolidation logic
    suspend fun getEventsForTrackableBetweenOnce(trackableId: Long, from: Long, to: Long): List<TrackableEvent>
    suspend fun updateEvent(event: TrackableEvent): Long

    suspend fun logEvent(event: TrackableEvent): Long
    suspend fun deleteEvent(event: TrackableEvent)
    suspend fun deleteEvents(events: List<TrackableEvent>)
    suspend fun getLatestEventForTrackable(trackableId: Long): TrackableEvent?
    suspend fun getAllEventsForTrackableAsc(trackableId: Long): List<TrackableEvent>

    // Reminders
    fun getRemindersForTrackable(trackableId: Long): Flow<List<Reminder>>
    suspend fun getAllEnabledReminders(): List<Reminder>
    suspend fun saveReminder(reminder: Reminder): Long
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun updateReminder(reminder: Reminder)
    suspend fun getReminderById(id: Long): Reminder?
}
