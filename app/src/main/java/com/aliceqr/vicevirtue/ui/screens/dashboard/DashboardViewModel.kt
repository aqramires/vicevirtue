package com.aliceqr.vicevirtue.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.domain.usecase.GetStreakUseCase
import com.aliceqr.vicevirtue.domain.usecase.LogEventUseCase
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.ui.screens.history.ConsolidatedEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val trackables: List<TrackableWithStreak> = emptyList(),
    val allRecentEvents: List<ConsolidatedEvent> = emptyList(),
    val isLoading: Boolean = true,
    val showVices: Boolean = true,
    val showVirtues: Boolean = true
)

data class TrackableWithStreak(
    val trackable: Trackable,
    val streak: Int,
    val recentEvents: List<ConsolidatedEvent> = emptyList()
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TrackableRepository,
    private val getStreakUseCase: GetStreakUseCase,
    private val logEventUseCase: LogEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTrackables()
    }

    private fun loadTrackables() {
        viewModelScope.launch {
            val trackablesFlow = repository.getAllTrackables()
            val eventsFlow = repository.getAllEvents()

            combine(trackablesFlow, eventsFlow) { trackables, events ->
                // Consolidate all events for the general history section
                val recentConsolidated = events.asSequence()
                    .groupBy { event ->
                        val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        // Group by trackable, description and date
                        Triple(event.trackableId, event.description.trim(), cal.timeInMillis)
                    }
                    .mapNotNull { (triple, occurrences) ->
                        val trackable = trackables.find { it.id == triple.first }
                        if (trackable != null) {
                            ConsolidatedEvent(
                                trackable = trackable,
                                description = triple.second,
                                date = triple.third,
                                occurrences = occurrences.sortedByDescending { it.timestamp }
                            )
                        } else null
                    }
                    .sortedByDescending { it.occurrences.first().timestamp }
                    .take(10) // Show last 10 consolidated events on dashboard
                    .toList()

                val trackablesWithStreak = trackables.map { trackable ->
                    TrackableWithStreak(
                        trackable = trackable,
                        streak = getStreakUseCase(trackable)
                    )
                }
                
                DashboardUiState(
                    trackables = trackablesWithStreak,
                    allRecentEvents = recentConsolidated,
                    isLoading = false,
                    showVices = _uiState.value.showVices,
                    showVirtues = _uiState.value.showVirtues
                )
            }.collect { newState ->
                _uiState.update { it.copy(
                    trackables = newState.trackables,
                    allRecentEvents = newState.allRecentEvents,
                    isLoading = false
                ) }
            }
        }
    }

    fun toggleViceFilter() {
        _uiState.update { it.copy(showVices = !it.showVices) }
    }

    fun toggleVirtueFilter() {
        _uiState.update { it.copy(showVirtues = !it.showVirtues) }
    }

    fun logEvent(trackable: Trackable) {
        viewModelScope.launch {
            logEventUseCase(
                trackableId = trackable.id,
                description = "" // Quick log from dashboard uses empty description
            )
        }
    }

    fun deleteEvent(event: TrackableEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    fun deleteEvents(events: List<TrackableEvent>) {
        viewModelScope.launch {
            repository.deleteEvents(events)
        }
    }

    fun updateEvent(event: TrackableEvent, newDescription: String) {
        viewModelScope.launch {
            repository.updateEvent(event.copy(description = newDescription))
        }
    }
}
