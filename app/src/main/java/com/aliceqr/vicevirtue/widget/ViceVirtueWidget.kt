package com.aliceqr.vicevirtue.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.GlanceStateDefinition
import com.aliceqr.vicevirtue.data.widget.getTrackableIdForWidget
import com.aliceqr.vicevirtue.domain.model.TrackableType
import com.aliceqr.vicevirtue.domain.repository.TrackableRepository
import com.aliceqr.vicevirtue.domain.usecase.GetStreakUseCase
import com.aliceqr.vicevirtue.widget.ui.ViceVirtueWidgetContent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ViceVirtueWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<Preferences> = WidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        refreshWidgetState(context, id, appWidgetId)

        provideContent {
            ViceVirtueWidgetContent()
        }
    }

    suspend fun refreshWidgetState(context: Context, glanceId: GlanceId, appWidgetId: Int) {
        val trackableId = context.getTrackableIdForWidget(appWidgetId)

        if (trackableId == null) {
            updateAppWidgetState(context, stateDefinition, glanceId) { prefs ->
                prefs.toMutablePreferences().apply {
                    set(WidgetStateDefinition.KEY_ERROR, true)
                    set(WidgetStateDefinition.KEY_LOADING, false)
                }
            }
        } else {
            val repository = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            ).repository()

            val trackable = repository.getTrackableById(trackableId)

            if (trackable == null) {
                updateAppWidgetState(context, stateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(WidgetStateDefinition.KEY_ERROR, true)
                        set(WidgetStateDefinition.KEY_LOADING, false)
                    }
                }
            } else {
                val streak = GetStreakUseCase(repository).invoke(trackable)
                updateAppWidgetState(context, stateDefinition, glanceId) { prefs ->
                    prefs.toMutablePreferences().apply {
                        set(WidgetStateDefinition.KEY_ID, trackable.id)
                        set(WidgetStateDefinition.KEY_NAME, trackable.name)
                        set(WidgetStateDefinition.KEY_TYPE, trackable.type.name)
                        set(WidgetStateDefinition.KEY_STREAK, streak)
                        set(WidgetStateDefinition.KEY_LOADING, false)
                        set(WidgetStateDefinition.KEY_ERROR, false)
                    }
                }
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun repository(): TrackableRepository
}
