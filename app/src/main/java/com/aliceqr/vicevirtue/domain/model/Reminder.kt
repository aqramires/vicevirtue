package com.aliceqr.vicevirtue.domain.model

data class Reminder(
    val id: Long = 0,
    val trackableId: Long,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true
)
