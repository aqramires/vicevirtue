package com.aliceqr.vicevirtue.ui.screens.addtrackable

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.usecase.AddTrackableUseCase
import com.aliceqr.vicevirtue.domain.usecase.GetTrackableUseCase
import com.aliceqr.vicevirtue.domain.usecase.UpdateTrackableUseCase
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTrackableUiState(
    val name: String = "",
    val type: TrackableType = TrackableType.VICE,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false
)

@HiltViewModel
class AddTrackableViewModel @Inject constructor(
    private val addTrackableUseCase: AddTrackableUseCase,
    private val updateTrackableUseCase: UpdateTrackableUseCase,
    private val getTrackableUseCase: GetTrackableUseCase,
    private val repository: TrackableRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialType: String? = savedStateHandle["type"]
    private val trackableId: Long = savedStateHandle.get<Long>("trackableId") ?: -1L

    private val _uiState = MutableStateFlow(AddTrackableUiState())
    val uiState: StateFlow<AddTrackableUiState> = _uiState.asStateFlow()

    init {
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
            getTrackableUseCase(id).collect { trackable ->
                trackable?.let { t ->
                    _uiState.update { it.copy(name = t.name, type = t.type) }
                }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
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

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            // Check for duplicate names
            val existingTrackables = repository.getAllTrackables().first()
            val isDuplicate = existingTrackables.any { 
                it.name.equals(name, ignoreCase = true) && it.id != trackableId 
            }
            
            if (isDuplicate) {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        error = "error_name_exists"
                    ) 
                }
                return@launch
            }

            val result = if (trackableId != -1L) {
                updateTrackableUseCase(
                    Trackable(id = trackableId, name = name, type = _uiState.value.type)
                )
            } else {
                addTrackableUseCase(
                    name = name,
                    type = _uiState.value.type
                ).map { Unit }
            }

            if (result.isSuccess) {
                _uiState.update { it.copy(isSaved = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        error = "failed_to_save"
                    ) 
                }
            }
        }
    }
}
