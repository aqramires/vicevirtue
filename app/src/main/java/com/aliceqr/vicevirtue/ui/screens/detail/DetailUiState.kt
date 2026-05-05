package com.aliceqr.vicevirtue.ui.screens.detail

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent

import com.aliceqr.vicevirtue.ui.screens.history.ConsolidatedEvent

data class DetailUiState(
    val trackable: Trackable? = null,
    val streak: Int = 0,
    val recentEvents: List<ConsolidatedEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
