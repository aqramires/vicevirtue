package com.aliceqr.vicevirtue.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.aliceqr.vicevirtue.data.widget.removeWidgetBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViceVirtueWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ViceVirtueWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            val manager = GlanceAppWidgetManager(context)
            appWidgetIds.forEach { id ->
                val glanceId = manager.getGlanceIdBy(id)
                (glanceAppWidget as ViceVirtueWidget).refreshWidgetState(context, glanceId, id)
                glanceAppWidget.update(context, glanceId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        CoroutineScope(Dispatchers.IO).launch {
            appWidgetIds.forEach { id ->
                context.removeWidgetBinding(id)
            }
        }
    }
}
