package com.aliceqr.vicevirtue.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.domain.usecase.GetEventsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getEventsUseCase: GetEventsUseCase,
    private val repository: TrackableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trackableId: Long? = savedStateHandle.get<Long>("trackableId")?.takeIf { it != -1L }

    private val _uiState = MutableStateFlow(HistoryUiState(filterTrackableId = trackableId))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                repository.getAllEvents(),
                repository.getAllTrackables(),
                _uiState
            ) { events, trackables, state ->
                val trackableMap = trackables.associateBy { it.id }
                
                events.asSequence()
                    .filter { event ->
                        val trackable = trackableMap[event.trackableId] ?: return@filter false
                        
                        val matchesTrackable = state.filterTrackableId == null || event.trackableId == state.filterTrackableId
                        val matchesType = state.filterType == null || trackable.type == state.filterType
                        val matchesDate = (state.startDate == null || event.timestamp >= state.startDate) &&
                                         (state.endDate == null || event.timestamp <= state.endDate)
                        
                        matchesTrackable && matchesType && matchesDate
                    }
                    .groupBy { event ->
                        val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        val startOfDay = cal.timeInMillis
                        Triple(event.trackableId, event.description.trim(), startOfDay)
                    }
                    .mapNotNull { (triple, occurrences) ->
                        val trackable = trackableMap[triple.first] ?: return@mapNotNull null
                        ConsolidatedEvent(
                            trackable = trackable,
                            description = triple.second,
                            date = triple.third,
                            occurrences = occurrences.sortedByDescending { it.timestamp }
                        )
                    }
                    .sortedByDescending { it.occurrences.first().timestamp }
                    .toList()
            }.collect { combinedList ->
                _uiState.update { it.copy(consolidatedEvents = combinedList, isLoading = false) }
            }
        }
    }

    fun onFilterTypeChange(type: TrackableType?) {
        _uiState.update { it.copy(filterType = type) }
    }

    fun onDateRangeChange(start: Long?, end: Long?) {
        val adjustedEnd = end?.let {
            Calendar.getInstance().apply {
                timeInMillis = it
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        }
        _uiState.update { it.copy(startDate = start, endDate = adjustedEnd) }
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
