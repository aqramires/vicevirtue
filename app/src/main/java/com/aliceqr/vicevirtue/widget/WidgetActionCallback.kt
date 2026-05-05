package com.aliceqr.vicevirtue.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.aliceqr.vicevirtue.data.widget.getTrackableIdForWidget
import com.aliceqr.vicevirtue.domain.usecase.LogEventUseCase
import dagger.hilt.android.EntryPointAccessors

class WidgetActionCallback : ActionCallback {

    companion object {
        val KEY_WIDGET_ID = ActionParameters.Key<Int>("widget_id")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val appWidgetId = parameters[KEY_WIDGET_ID] ?: return

        val repository = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        ).repository()

        val trackableId = context.getTrackableIdForWidget(appWidgetId) ?: return
        val trackable = repository.getTrackableById(trackableId) ?: return

        LogEventUseCase(repository).invoke(
            trackableId = trackable.id,
            description = ""
        )

        ViceVirtueWidget().let { widget ->
            widget.refreshWidgetState(context, glanceId, appWidgetId)
            widget.update(context, glanceId)
        }
    }
}
