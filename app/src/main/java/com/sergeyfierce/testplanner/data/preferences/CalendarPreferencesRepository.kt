package com.sergeyfierce.testplanner.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.sergeyfierce.testplanner.domain.model.CalendarMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CalendarPreferencesRepository(context: Context) {

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile(DATASTORE_FILE) }
    )

    val mode: Flow<CalendarMode> = dataStore.data.map { preferences ->
        preferences[MODE_KEY]?.let { stored ->
            runCatching { CalendarMode.valueOf(stored) }.getOrNull()
        } ?: CalendarMode.DAY
    }

    suspend fun setMode(mode: CalendarMode) {
        dataStore.edit { preferences ->
            preferences[MODE_KEY] = mode.name
        }
    }

    companion object {
        private const val DATASTORE_FILE = "calendar_preferences"
        private val MODE_KEY = stringPreferencesKey("mode")
    }
}
