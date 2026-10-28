package com.bayrjavhlanmw.finance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayrjavhlanmw.finance.data.EntryFilter
import com.bayrjavhlanmw.finance.data.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferences: UserPreferencesRepository) : ViewModel() {
    val filter = preferences.filter.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryFilter.ALL)
    fun select(filter: EntryFilter) { viewModelScope.launch { preferences.saveFilter(filter) } }
}
