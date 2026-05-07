package com.aliceqr.vicevirtue.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.domain.usecase.GetStreakUseCase
import com.aliceqr.vicevirtue.domain.usecase.LogEventUseCase
import com.aliceqr.vicevirtue.domain.model.TrackableEvent
import com.aliceqr.vicevirtue.domain.model.TrackableType
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

import com.aliceqr.vicevirtue.data.repository.AppSettingsRepository
import com.aliceqr.vicevirtue.data.repository.ThemeMode

import androidx.compose.runtime.Immutable

@Immutable
data class DashboardUiState(
    val vices: List<TrackableWithStreak> = emptyList(),
    val virtues: List<TrackableWithStreak> = emptyList(),
    val allTrackables: List<TrackableWithStreak> = emptyList(),
    val vicesEvents: List<ConsolidatedEvent> = emptyList(),
    val virtuesEvents: List<ConsolidatedEvent> = emptyList(),
    val allRecentEvents: List<ConsolidatedEvent> = emptyList(),
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isCommentaryEnabled: Boolean = true,
    val expandedSections: Map<Int, Boolean> = emptyMap()
)

@Immutable
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
                val cal = Calendar.getInstance()
                val eventsByTrackable = events.groupBy { it.trackableId }
                val trackableMap = trackables.associateBy { it.id }

                // Consolidate all events (optimized)
                val allConsolidated = events.asSequence()
                    .groupBy { event ->
                        cal.timeInMillis = event.timestamp
                        cal.set(Calendar.HOUR_OF_DAY, 0)
                        cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0)
                        cal.set(Calendar.MILLISECOND, 0)
                        Triple(event.trackableId, event.description.trim(), cal.timeInMillis)
                    }
                    .mapNotNull { (key, occurrences) ->
                        val (trackableId, description, date) = key
                        val trackable = trackableMap[trackableId] ?: return@mapNotNull null
                        
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
                    val trackableEvents = eventsByTrackable[trackable.id] ?: emptyList()
                    TrackableWithStreak(
                        trackable = trackable,
                        streak = getStreakUseCase.invoke(trackable, trackableEvents, cal)
                    )
                }

                val vices = trackablesWithStreak.filter { it.trackable.type == TrackableType.VICE }
                val virtues = trackablesWithStreak.filter { it.trackable.type == TrackableType.VIRTUE }
                
                val vicesEvents = allConsolidated.filter { it.trackable.type == TrackableType.VICE }
                val virtuesEvents = allConsolidated.filter { it.trackable.type == TrackableType.VIRTUE }
                
                DashboardData(
                    vices = vices,
                    virtues = virtues,
                    allTrackables = trackablesWithStreak,
                    vicesEvents = vicesEvents,
                    virtuesEvents = virtuesEvents,
                    allRecentEvents = allConsolidated
                )
            }
            .flowOn(Dispatchers.Default)
            .collectLatest { data ->
                _uiState.update { it.copy(
                    vices = data.vices,
                    virtues = data.virtues,
                    allTrackables = data.allTrackables,
                    vicesEvents = data.vicesEvents,
                    virtuesEvents = data.virtuesEvents,
                    allRecentEvents = data.allRecentEvents,
                    isLoading = false
                ) }
            }
        }
    }

    data class DashboardData(
        val vices: List<TrackableWithStreak>,
        val virtues: List<TrackableWithStreak>,
        val allTrackables: List<TrackableWithStreak>,
        val vicesEvents: List<ConsolidatedEvent>,
        val virtuesEvents: List<ConsolidatedEvent>,
        val allRecentEvents: List<ConsolidatedEvent>
    )

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