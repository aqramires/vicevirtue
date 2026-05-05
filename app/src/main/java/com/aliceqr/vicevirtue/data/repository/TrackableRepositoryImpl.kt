package com.aliceqr.vicevirtue.data.repository

import com.aliceqr.vicevirtue.data.db.dao.EventDao
import com.aliceqr.vicevirtue.data.db.dao.ReminderDao
import com.aliceqr.vicevirtue.data.db.dao.TrackableDao
import com.aliceqr.vicevirtue.data.toDomain
import com.aliceqr.vicevirtue.data.toEntity
import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackableRepositoryImpl @Inject constructor(
    private val trackableDao: TrackableDao,
    private val eventDao: EventDao,
    private val reminderDao: ReminderDao
) : TrackableRepository {

    override fun getAllTrackables(): Flow<List<Trackable>> =
        trackableDao.getAllTrackables().map { list ->
            list.map { it.toDomain() }
        }

    override fun getTrackablesByType(type: TrackableType): Flow<List<Trackable>> =
        trackableDao.getByType(type.name).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getTrackableById(id: Long): Trackable? =
        trackableDao.getById(id)?.toDomain()

    override suspend fun addTrackable(trackable: Trackable): Long =
        trackableDao.insert(trackable.toEntity())

    override suspend fun deleteTrackable(trackable: Trackable) =
        trackableDao.delete(trackable.toEntity())

    override fun getAllEvents(): Flow<List<TrackableEvent>> =
        eventDao.getAllEvents().map { it.map { e -> e.toDomain() } }

    override fun getEventsForTrackable(trackableId: Long): Flow<List<TrackableEvent>> =
        eventDao.getEventsForTrackable(trackableId).map { it.map { e -> e.toDomain() } }

    override fun getEventsBetween(from: Long, to: Long): Flow<List<TrackableEvent>> =
        eventDao.getEventsBetween(from, to).map { it.map { e -> e.toDomain() } }

    override fun getEventsForTrackableBetween(
        trackableId: Long, from: Long, to: Long
    ): Flow<List<TrackableEvent>> =
        eventDao.getEventsForTrackableBetween(trackableId, from, to)
            .map { it.map { e -> e.toDomain() } }

    override suspend fun getEventsForTrackableBetweenOnce(
        trackableId: Long, from: Long, to: Long
    ): List<TrackableEvent> =
        eventDao.getEventsForTrackableBetween(trackableId, from, to).first().map { it.toDomain() }

    override suspend fun updateEvent(event: TrackableEvent): Long =
        eventDao.insert(event.toEntity()) // Room's OnConflictStrategy.REPLACE handles updates

    override suspend fun logEvent(event: TrackableEvent): Long =
        eventDao.insert(event.toEntity())

    override suspend fun deleteEvent(event: TrackableEvent) =
        eventDao.delete(event.toEntity())

    override suspend fun deleteEvents(events: List<TrackableEvent>) =
        eventDao.deleteEvents(events.map { it.toEntity() })

    override suspend fun getLatestEventForTrackable(trackableId: Long): TrackableEvent? =
        eventDao.getLatestEventForTrackable(trackableId)?.toDomain()

    override suspend fun getAllEventsForTrackableAsc(trackableId: Long): List<TrackableEvent> =
        eventDao.getAllEventsForTrackableAsc(trackableId).map { it.toDomain() }

    override fun getRemindersForTrackable(trackableId: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersForTrackable(trackableId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getAllEnabledReminders(): List<Reminder> =
        reminderDao.getAllEnabledReminders().map { it.toDomain() }

    override suspend fun saveReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder.toEntity())

    override suspend fun deleteReminder(reminder: Reminder) =
        reminderDao.deleteReminder(reminder.toEntity())

    override suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder.toEntity())

    override suspend fun getReminderById(id: Long): Reminder? =
        reminderDao.getReminderById(id)?.toDomain()
}
