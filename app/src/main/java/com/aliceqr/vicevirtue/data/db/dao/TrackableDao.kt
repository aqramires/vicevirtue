package com.aliceqr.vicevirtue.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aliceqr.vicevirtue.data.db.entity.TrackableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackableDao {
    @Query("SELECT * FROM trackables ORDER BY name ASC")
    fun getAllTrackables(): Flow<List<TrackableEntity>>

    @Query("SELECT * FROM trackables WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): Flow<List<TrackableEntity>>

    @Query("SELECT * FROM trackables WHERE id = :id")
    suspend fun getById(id: Long): TrackableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trackable: TrackableEntity): Long

    @Delete
    suspend fun delete(trackable: TrackableEntity)
}
