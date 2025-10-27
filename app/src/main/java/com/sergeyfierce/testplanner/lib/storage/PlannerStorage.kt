package com.sergeyfierce.testplanner.lib.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sergeyfierce.testplanner.lib.types.Settings
import com.sergeyfierce.testplanner.lib.types.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

private const val DATA_STORE_NAME = "planner_store"

private val Context.dataStore by preferencesDataStore(name = DATA_STORE_NAME)

/**
 * Thin wrapper around [DataStore] providing persistence for tasks and settings.
 */
class PlannerStorage(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val tasksKey = stringPreferencesKey("planner_tasks")
    private val settingsKey = stringPreferencesKey("planner_settings")
    private val lastSyncKey = stringPreferencesKey("planner_last_sync")

    val tasks: Flow<List<Task>> = context.dataStore.data
        .handleErrors()
        .map { preferences ->
            preferences[tasksKey]?.let { stored ->
                runCatching { json.decodeFromString<List<Task>>(stored) }
                    .getOrDefault(emptyList())
            } ?: emptyList()
        }

    val settings: Flow<Settings> = context.dataStore.data
        .handleErrors()
        .map { preferences ->
            preferences[settingsKey]?.let { stored ->
                runCatching { json.decodeFromString<Settings>(stored) }
                    .getOrDefault(Settings())
            } ?: Settings()
        }

    val lastSync: Flow<String?> = context.dataStore.data
        .handleErrors()
        .map { preferences -> preferences[lastSyncKey] }

    suspend fun persistTasks(tasks: List<Task>) {
        context.dataStore.edit { prefs ->
            prefs[tasksKey] = json.encodeToString(tasks)
        }
    }

    suspend fun persistSettings(settings: Settings) {
        context.dataStore.edit { prefs ->
            prefs[settingsKey] = json.encodeToString(settings)
        }
    }

    suspend fun updateLastSync(timestamp: String) {
        context.dataStore.edit { prefs ->
            prefs[lastSyncKey] = timestamp
        }
    }

    private fun Flow<Preferences>.handleErrors(): Flow<Preferences> = catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
}

