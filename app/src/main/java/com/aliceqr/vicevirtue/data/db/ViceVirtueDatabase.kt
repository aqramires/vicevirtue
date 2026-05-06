package com.aliceqr.vicevirtue.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aliceqr.vicevirtue.data.db.dao.EventDao
import com.aliceqr.vicevirtue.data.db.dao.ReminderDao
import com.aliceqr.vicevirtue.data.db.dao.TrackableDao
import com.aliceqr.vicevirtue.data.db.entity.EventEntity
import com.aliceqr.vicevirtue.data.db.entity.ReminderEntity
import com.aliceqr.vicevirtue.data.db.entity.TrackableEntity
@Database(
    entities = [TrackableEntity::class, EventEntity::class, ReminderEntity::class],
    version = 6,
    exportSchema = false
)
abstract class ViceVirtueDatabase : RoomDatabase() {
    abstract fun trackableDao(): TrackableDao
    abstract fun eventDao(): EventDao
    abstract fun reminderDao(): ReminderDao
}
