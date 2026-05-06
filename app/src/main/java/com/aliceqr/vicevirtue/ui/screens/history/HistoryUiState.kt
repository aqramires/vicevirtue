package com.aliceqr.vicevirtue.ui.screens.history

import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType

data class HistoryUiState(
    val consolidatedEvents: List<ConsolidatedEvent> = emptyList(),
    val isLoading: Boolean = true,
    val filterTrackableId: Long? = null,
    val filterTrackable: Trackable? = null,
    val filterType: TrackableType? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

data class ConsolidatedEvent(
    val trackable: Trackable,
    val description: String,
    val date: Long, // Start of day
    val occurrences: List<TrackableEvent>
)
