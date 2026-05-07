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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aliceqr.vicevirtue.data.repository.AppSettingsRepository
import com.aliceqr.vicevirtue.data.repository.ThemeMode

data class DashboardUiState(
    val trackables: List<TrackableWithStreak> = emptyList(),
    val allRecentEvents: List<ConsolidatedEvent> = emptyList(),
    val isLoading: Boolean = true,
    val showVices: Boolean = true,
    val showVirtues: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isCommentaryEnabled: Boolean = true,
    val expandedSections: Map<Int, Boolean> = emptyMap()
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
    private val logEventUseCase: LogEventUseCase,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTrackables()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            appSettingsRepository.themeMode.collectLatest { mode ->
                _uiState.update { it.copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            appSettingsRepository.isCommentaryEnabled.collectLatest { enabled ->
                _uiState.update { it.copy(isCommentaryEnabled = enabled) }
            }
        }
    }

    private fun loadTrackables() {
        viewModelScope.launch {
            val trackablesFlow = repository.getAllTrackables()
            val eventsFlow = repository.getAllEvents()

            combine(trackablesFlow, eventsFlow) { trackables, events ->
                // Consolidate all events
                val allConsolidated = events.asSequence()
                    .groupBy { event ->
                        val cal = Calendar.getInstance().apply { timeInMillis = event.timestamp }
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        listOf(event.trackableId, event.description.trim(), cal.timeInMillis)
                    }
                    .mapNotNull { (key, occurrences) ->
                        val trackableId = key[0] as Long
                        val description = key[1] as String
                        val date = key[2] as Long
                        val trackable = trackables.find { it.id == trackableId } ?: return@mapNotNull null
                        
                        ConsolidatedEvent(
                            trackable = trackable,
                            description = description,
                            date = date,
                            occurrences = occurrences.sortedByDescending { it.timestamp }
                        )
                    }
                    .sortedByDescending { it.occurrences.first().timestamp }
                    .take(10)
                    .toList()

                val trackablesWithStreak = trackables.map { trackable ->
                    TrackableWithStreak(
                        trackable = trackable,
                        streak = getStreakUseCase(trackable)
                    )
                }
                
                _uiState.update { it.copy(
                    trackables = trackablesWithStreak,
                    allRecentEvents = allConsolidated,
                    isLoading = false
                ) }
            }.collectLatest { }
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
                description = "" 
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

    fun toggleSection(pageIndex: Int) {
        val current = _uiState.value.expandedSections[pageIndex] ?: false
        _uiState.update { 
            it.copy(expandedSections = it.expandedSections + (pageIndex to !current))
        }
    }
}