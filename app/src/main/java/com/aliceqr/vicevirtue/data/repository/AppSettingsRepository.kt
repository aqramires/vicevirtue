package com.aliceqr.vicevirtue.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val COMMENTARY_ENABLED_KEY = booleanPreferencesKey("is_commentary_enabled")

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val modeName = preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name
            ThemeMode.valueOf(modeName)
        }

    val isCommentaryEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[COMMENTARY_ENABLED_KEY] ?: true }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setCommentaryEnabled(enabled: Boolean) {
        context.dataStore.edit { it[COMMENTARY_ENABLED_KEY] = enabled }
    }
}
