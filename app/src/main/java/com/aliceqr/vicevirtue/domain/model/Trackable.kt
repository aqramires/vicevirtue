package com.aliceqr.vicevirtue.domain.model

data class Trackable(
    val id: Long = 0,
    val name: String,
    val type: TrackableType,
    val createdAt: Long = System.currentTimeMillis(),
    val streak: Int = 0         // Computed, not stored
)
