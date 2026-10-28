package com.bayrjavhlanmw.finance.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayrjavhlanmw.finance.data.EntryFilter
import com.bayrjavhlanmw.finance.data.FinanceEntry
import com.bayrjavhlanmw.finance.data.FinanceRepository
import com.bayrjavhlanmw.finance.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val entries: List<FinanceEntry> = emptyList(),
    val filter: EntryFilter = EntryFilter.ALL,
    val index: Int = 0,
    val loading: Boolean = true,
    val error: String? = null
) {
    val current: FinanceEntry? get() = entries.getOrNull(index)
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: FinanceRepository,
    preferences: UserPreferencesRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val selectedId = savedState.getStateFlow("selectedId", 0L)
    private val error = MutableStateFlow<String?>(null)
    private val filteredEntries = preferences.filter.flatMapLatest { filter ->
        repository.observeEntries(filter.type).map { filter to it }
    }.catch { error.value = "Бүртгэл уншихад алдаа гарлаа."; emit(EntryFilter.ALL to emptyList()) }

    val uiState = combine(filteredEntries, selectedId, error) { (filter, entries), id, message ->
        HomeUiState(entries, filter, entries.indexOfFirst { it.id == id }.coerceAtLeast(0), false, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previous() = select(uiState.value.index - 1)
    fun next() = select(uiState.value.index + 1)
    fun selectId(id: Long) { savedState["selectedId"] = id }
    private fun select(index: Int) {
        uiState.value.entries.getOrNull(index)?.let { savedState["selectedId"] = it.id }
    }
    fun delete(entry: FinanceEntry) {
        viewModelScope.launch {
            runCatching {
                repository.delete(entry.id)
            }.onFailure { error.value = "Устгаж чадсангүй. Дахин оролдоно уу." }
        }
    }
    fun dismissError() { error.value = null }
}
