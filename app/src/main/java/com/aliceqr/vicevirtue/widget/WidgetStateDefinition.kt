package com.aliceqr.vicevirtue.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.state.GlanceStateDefinition
import java.io.File

data class WidgetState(
    val trackableId: Long = -1L,
    val trackableName: String = "",
    val trackableType: String = "",
    val streak: Int = 0,
    val isLoading: Boolean = true,
    val isError: Boolean = false
)

object WidgetStateDefinition : GlanceStateDefinition<Preferences> {
    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<Preferences> =
        context.widgetStateDataStore

    override fun getLocation(context: Context, fileKey: String): File =
        File(context.filesDir, "datastore/glance_widget_state.preferences_pb")

    private val Context.widgetStateDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "glance_widget_state")

    val KEY_ID = longPreferencesKey("trackableId")
    val KEY_NAME = stringPreferencesKey("name")
    val KEY_TYPE = stringPreferencesKey("type")
    val KEY_STREAK = intPreferencesKey("streak")
    val KEY_LOADING = booleanPreferencesKey("loading")
    val KEY_ERROR = booleanPreferencesKey("error")

    fun toWidgetState(prefs: Preferences): WidgetState {
        return WidgetState(
            trackableId = prefs[KEY_ID] ?: -1L,
            trackableName = prefs[KEY_NAME] ?: "",
            trackableType = prefs[KEY_TYPE] ?: "",
            streak = prefs[KEY_STREAK] ?: 0,
            isLoading = prefs[KEY_LOADING] ?: true,
            isError = prefs[KEY_ERROR] ?: false
        )
    }
}

private fun longPreferencesKey(name: String) = androidx.datastore.preferences.core.longPreferencesKey(name)
