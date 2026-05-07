package com.aliceqr.vicevirtue.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.usecase.DeleteTrackableUseCase
import com.aliceqr.vicevirtue.domain.usecase.GetEventsUseCase
import com.aliceqr.vicevirtue.domain.usecase.GetStreakUseCase
import com.aliceqr.vicevirtue.domain.usecase.GetTrackableUseCase
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.ui.screens.history.ConsolidatedEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getTrackableUseCase: GetTrackableUseCase,
    private val getEventsUseCase: GetEventsUseCase,
    private val getStreakUseCase: GetStreakUseCase,
    private val deleteTrackableUseCase: DeleteTrackableUseCase,
    private val repository: com.aliceqr.vicevirtue.domain.repository.TrackableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trackableId: Long = checkNotNull(savedStateHandle["trackableId"])

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    private fun loadDetail() {
        viewModelScope.launch {
            combine(
                getTrackableUseCase(trackableId),
                getEventsUseCase.eventsForTrackable(trackableId)
            ) { trackable, events ->
                if (trackable == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Trackable not found") }
                    emptyList<ConsolidatedEvent>()
                } else {
                    val streak = getStreakUseCase(trackable)
                    
                    val consolidated = events.asSequence()
                        .groupBy { event ->
                            val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
                            cal.set(Calendar.HOUR_OF_DAY, 0)
                            cal.set(Calendar.MINUTE, 0)
                            cal.set(Calendar.SECOND, 0)
                            cal.set(Calendar.MILLISECOND, 0)
                            Pair(event.description.trim(), cal.timeInMillis)
                        }
                        .map { (pair, occurrences) ->
                            ConsolidatedEvent(
                                trackable = trackable,
                                description = pair.first,
                                date = pair.second,
                                occurrences = occurrences.sortedByDescending { it.timestamp }
                            )
                        }
                        .sortedByDescending { it.occurrences.first().timestamp }
                        .take(10)
                        .toList()

                    _uiState.update { 
                        it.copy(
                            trackable = trackable,
                            streak = streak,
                            recentEvents = consolidated,
                            isLoading = false
                        )
                    }
                    consolidated
                }
            }
            .flowOn(Dispatchers.Default)
            .collect { consolidated ->
                _uiState.update { 
                    it.copy(
                        trackable = consolidated.firstOrNull()?.trackable, // This is just for safety, trackable is already in scope
                        recentEvents = consolidated,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun deleteTrackable() {
        viewModelScope.launch {
            uiState.value.trackable?.let {
                deleteTrackableUseCase(it)
            }
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
