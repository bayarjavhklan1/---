package com.bayrjavhlanmw.finance.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {
    val filter: Flow<EntryFilter> = dataStore.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }.map { preferences ->
        EntryFilter.entries.firstOrNull { it.name == preferences[FILTER] } ?: EntryFilter.ALL
    }

    suspend fun saveFilter(filter: EntryFilter) {
        dataStore.edit { it[FILTER] = filter.name }
    }
    private companion object { val FILTER = stringPreferencesKey("entry_filter") }
}
