package com.aliceqr.vicevirtue.data.db.dao

import androidx.room.*
import com.aliceqr.vicevirtue.data.db.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE trackableId = :trackableId")
    fun getRemindersForTrackable(trackableId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1")
    suspend fun getAllEnabledReminders(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE trackableId = :trackableId")
    suspend fun deleteRemindersForTrackable(trackableId: Long)
}
