package com.aliceqr.vicevirtue.ui.screens.logevent

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.usecase.GetTrackableUseCase
import com.aliceqr.vicevirtue.domain.usecase.LogEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LogEventUiState(
    val trackable: Trackable? = null,
    val description: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LogEventViewModel @Inject constructor(
    private val getTrackableUseCase: GetTrackableUseCase,
    private val logEventUseCase: LogEventUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trackableId: Long = checkNotNull(savedStateHandle["trackableId"])

    private val _uiState = MutableStateFlow(LogEventUiState())
    val uiState: StateFlow<LogEventUiState> = _uiState.asStateFlow()

    init {
        loadTrackable()
    }

    private fun loadTrackable() {
        viewModelScope.launch {
            getTrackableUseCase(trackableId).collect { trackable ->
                _uiState.update { it.copy(trackable = trackable) }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun saveEvent() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = logEventUseCase(
                trackableId = trackableId,
                description = _uiState.value.description
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(isSaved = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        error = "Failed to log event"
                    ) 
                }
            }
        }
    }
}
