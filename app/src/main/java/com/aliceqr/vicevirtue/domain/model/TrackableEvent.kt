package com.aliceqr.vicevirtue.domain.model

data class TrackableEvent(
    val id: Long = 0,
    val trackableId: Long,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val count: Int = 1
)
