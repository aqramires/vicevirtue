package com.aliceqr.vicevirtue.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackables")
data class TrackableEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,           // "VICE" or "VIRTUE"
    val createdAt: Long = System.currentTimeMillis(),
    val targetStreak: Int? = null
)
