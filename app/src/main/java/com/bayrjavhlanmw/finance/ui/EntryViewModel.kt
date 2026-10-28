package com.bayrjavhlanmw.finance.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bayrjavhlanmw.finance.data.EntryType
import com.bayrjavhlanmw.finance.data.FinanceEntry
import com.bayrjavhlanmw.finance.data.FinanceRepository
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EntryFormState(
    val type: EntryType = EntryType.EXPENSE,
    val name: String = "",
    val amount: String = "",
    val date: LocalDate = LocalDate.now(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val missing: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val error: String? = null
)

class EntryViewModel(private val repository: FinanceRepository, private val savedState: SavedStateHandle) : ViewModel() {
    val entryId: Long = savedState["id"] ?: 0L
    var state by mutableStateOf(EntryFormState(
        type = EntryType.valueOf(savedState["type"] ?: EntryType.EXPENSE.name),
        name = savedState["name"] ?: "",
        amount = savedState["amount"] ?: "",
        date = LocalDate.ofEpochDay(savedState["date"] ?: LocalDate.now().toEpochDay()),
        loading = entryId != 0L && savedState.get<Boolean>("initialized") != true
    ))
        private set

    init {
        if (state.loading) viewModelScope.launch {
            try {
                val entry = repository.observeEntry(entryId).first()
                state = if (entry == null) state.copy(loading = false, missing = true)
                else EntryFormState(entry.type, entry.name, Money.editable(entry.amountMinor), LocalDate.ofEpochDay(entry.dateEpochDay))
                persist()
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                state = state.copy(loading = false, missing = true, error = "Бүртгэл уншиж чадсангүй.")
            }
        }
    }

    fun type(value: EntryType) { state = state.copy(type = value); persist() }
    fun name(value: String) { state = state.copy(name = value.take(80), nameError = null); persist() }
    fun amount(value: String) { state = state.copy(amount = value, amountError = null); persist() }
    fun date(value: LocalDate) { state = state.copy(date = value); persist() }
    private fun persist() {
        savedState["type"] = state.type.name
        savedState["name"] = state.name
        savedState["amount"] = state.amount
        savedState["date"] = state.date.toEpochDay()
        savedState["initialized"] = true
    }

    fun save(onSaved: (Long) -> Unit) {
        if (state.saving || state.loading || state.missing) return
        val minor = Money.parseMinor(state.amount)
        val name = state.name.trim()
        state = state.copy(
            nameError = if (name.isBlank()) "Гүйлгээний нэр оруулна уу." else null,
            amountError = if (minor == null) "0-ээс их дүн оруулна уу. Бутархай нь 2 орон хүртэл байна." else null
        )
        if (minor == null || name.isBlank()) return
        val entry = FinanceEntry(entryId, state.type, name, minor, state.date.toEpochDay())
        state = state.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                val savedId = repository.save(entry)
                state = state.copy(saving = false)
                onSaved(savedId)
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                state = state.copy(saving = false, error = "Хадгалж чадсангүй. Дахин оролдоно уу.")
            }
        }
    }
}
