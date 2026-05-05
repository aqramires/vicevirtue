package com.aliceqr.vicevirtue.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TrackableEntity::class,
            parentColumns = ["id"],
            childColumns = ["trackableId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["trackableId"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackableId: Long,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true
)
