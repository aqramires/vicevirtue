package com.aliceqr.vicevirtue.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliceqr.vicevirtue.data.db.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    // All events, newest first
    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<EventEntity>>

    // Events for a specific trackable
    @Query("SELECT * FROM events WHERE trackableId = :id ORDER BY timestamp DESC")
    fun getEventsForTrackable(id: Long): Flow<List<EventEntity>>

    // Events filtered by date range
    @Query("""
        SELECT * FROM events
        WHERE timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    fun getEventsBetween(from: Long, to: Long): Flow<List<EventEntity>>

    // Events for a specific trackable within date range
    @Query("""
        SELECT * FROM events
        WHERE trackableId = :id AND timestamp BETWEEN :from AND :to
        ORDER BY timestamp DESC
    """)
    fun getEventsForTrackableBetween(id: Long, from: Long, to: Long): Flow<List<EventEntity>>

    // Latest event for a trackable (for streak calculation)
    @Query("SELECT * FROM events WHERE trackableId = :id ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEventForTrackable(id: Long): EventEntity?

    // All events for a trackable ordered ASC (for streak chain)
    @Query("SELECT * FROM events WHERE trackableId = :id ORDER BY timestamp ASC")
    suspend fun getAllEventsForTrackableAsc(id: Long): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity): Long

    @Delete
    suspend fun delete(event: EventEntity)

    @Delete
    suspend fun deleteEvents(events: List<EventEntity>)
}
