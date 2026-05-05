package com.aliceqr.vicevirtue.data.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_preferences"
)

fun widgetTrackableKey(appWidgetId: Int): Preferences.Key<Long> =
    longPreferencesKey("widget_trackable_$appWidgetId")

suspend fun Context.getTrackableIdForWidget(appWidgetId: Int): Long? {
    return widgetDataStore.data.first()[widgetTrackableKey(appWidgetId)]
}

suspend fun Context.removeWidgetBinding(appWidgetId: Int) {
    widgetDataStore.edit { prefs ->
        prefs.remove(widgetTrackableKey(appWidgetId))
    }
}
