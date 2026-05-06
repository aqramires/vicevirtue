package com.aliceqr.vicevirtue.ui.screens.addtrackable

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.model.Reminder
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.usecase.*
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTrackableUiState(
    val trackableId: Long = -1L,
    val name: String = "",
    val type: TrackableType = TrackableType.VICE,
    val reminders: List<Reminder> = emptyList(),
    val targetStreak: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddTrackableViewModel @Inject constructor(
    private val addTrackableUseCase: AddTrackableUseCase,
    private val updateTrackableUseCase: UpdateTrackableUseCase,
    private val getTrackableUseCase: GetTrackableUseCase,
    private val getRemindersUseCase: GetRemindersUseCase,
    private val saveReminderUseCase: SaveReminderUseCase,
    private val deleteReminderUseCase: DeleteReminderUseCase,
    private val repository: TrackableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialType: String? = savedStateHandle["type"]
    private val trackableId: Long = savedStateHandle.get<Long>("trackableId") ?: -1L

    private val _uiState = MutableStateFlow(AddTrackableUiState())
    private val _localReminders = MutableStateFlow<List<Reminder>>(emptyList())
    
    val uiState: StateFlow<AddTrackableUiState> = combine(
        _uiState,
        _localReminders
    ) { state, local ->
        state.copy(reminders = local)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = AddTrackableUiState()
    )

    init {
        _uiState.update { it.copy(trackableId = trackableId) }
        if (trackableId != -1L) {
            loadExistingTrackable(trackableId)
        } else {
            initialType?.let { typeStr ->
                try {
                    val type = TrackableType.valueOf(typeStr)
                    _uiState.update { it.copy(type = type) }
                } catch (e: Exception) {
                    // Ignore invalid type
                }
            }
        }
    }

    private fun loadExistingTrackable(id: Long) {
        viewModelScope.launch {
            val trackable = getTrackableUseCase(id).first()
            trackable?.let { t ->
                _uiState.update { it.copy(
                    name = t.name, 
                    type = t.type,
                    targetStreak = t.targetStreak?.toString() ?: ""
                ) }
            }
            
            // Load initial reminders into the local list
            val reminders = repository.getRemindersForTrackable(id).first()
            _localReminders.value = reminders
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onTargetStreakChange(newTarget: String) {
        // Only allow numbers
        if (newTarget.all { it.isDigit() }) {
            _uiState.update { it.copy(targetStreak = newTarget) }
        }
    }

    fun onTypeChange(newType: TrackableType) {
        _uiState.update { it.copy(type = newType) }
    }

    fun saveTrackable() {
        val name = _uiState.value.name.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(error = "error_name_empty") }
            return
        }

        val target = _uiState.value.targetStreak.toIntOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            // Check for duplicate names
            val existingTrackables = repository.getAllTrackables().first()
            val isDuplicate = existingTrackables.any { 
                it.name.equals(name, ignoreCase = true) && it.id != trackableId 
            }
            
            if (isDuplicate) {
                _uiState.update { 
                    it.copy(isSaving = false, error = "error_name_exists") 
                }
                return@launch
            }

            val result = if (trackableId != -1L) {
                updateTrackableUseCase(Trackable(
                    id = trackableId, 
                    name = name, 
                    type = _uiState.value.type,
                    targetStreak = target
                ))
            } else {
                addTrackableUseCase(name = name, type = _uiState.value.type, targetStreak = target)
            }

            if (result.isSuccess) {
                val finalTrackableId = if (trackableId == -1L) result.getOrNull() as Long else trackableId
                
                // Handle reminders batch
                val currentDbReminders = if (trackableId != -1L) repository.getRemindersForTrackable(trackableId).first() else emptyList()
                val localReminders = _localReminders.value

                // 1. Delete reminders that are in DB but not in local list
                currentDbReminders.forEach { dbReminder ->
                    if (localReminders.none { it.id == dbReminder.id }) {
                        deleteReminderUseCase(dbReminder)
                    }
                }

                // 2. Save/Update reminders in local list
                localReminders.forEach { localReminder ->
                    saveReminderUseCase(
                        localReminder.copy(trackableId = finalTrackableId),
                        name,
                        _uiState.value.type
                    )
                }

                _uiState.update { it.copy(isSaved = true, isSaving = false) }
            } else {
                _uiState.update { it.copy(isSaving = false, error = "failed_to_save") }
            }
        }
    }

    fun addReminder(hour: Int, minute: Int) {
        val tempReminder = Reminder(
            id = -System.nanoTime(),
            trackableId = trackableId,
            hour = hour,
            minute = minute
        )
        _localReminders.update { it + tempReminder }
    }

    fun toggleReminder(reminder: Reminder) {
        val updatedReminder = reminder.copy(isEnabled = !reminder.isEnabled)
        _localReminders.update { list ->
            list.map { if (it.id == reminder.id) updatedReminder else it }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        _localReminders.update { list ->
            list.filter { it.id != reminder.id }
        }
    }
}
