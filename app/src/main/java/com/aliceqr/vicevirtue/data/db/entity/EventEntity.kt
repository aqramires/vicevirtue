package com.aliceqr.vicevirtue.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "events",
    foreignKeys = [ForeignKey(
        entity = TrackableEntity::class,
        parentColumns = ["id"],
        childColumns = ["trackableId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("trackableId")]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackableId: Long,
    val description: String,    // Empty string if no reason given
    val timestamp: Long = System.currentTimeMillis(),
    val count: Int = 1
)
