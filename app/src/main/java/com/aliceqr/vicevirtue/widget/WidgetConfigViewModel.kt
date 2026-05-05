package com.aliceqr.vicevirtue.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliceqr.vicevirtue.data.widget.widgetDataStore
import com.aliceqr.vicevirtue.data.widget.widgetTrackableKey
import com.aliceqr.vicevirtue.domain.model.Trackable
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val repository: TrackableRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    data class ConfigUiState(
        val vices: List<Trackable> = emptyList(),
        val virtues: List<Trackable> = emptyList(),
        val isLoading: Boolean = true
    )

    val uiState: StateFlow<ConfigUiState> = repository.getAllTrackables()
        .map { list ->
            ConfigUiState(
                vices = list.filter { it.type == TrackableType.VICE },
                virtues = list.filter { it.type == TrackableType.VIRTUE },
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConfigUiState())

    suspend fun saveWidgetBinding(appWidgetId: Int, trackableId: Long) {
        context.widgetDataStore.edit { prefs ->
            prefs[widgetTrackableKey(appWidgetId)] = trackableId
        }
    }
}
