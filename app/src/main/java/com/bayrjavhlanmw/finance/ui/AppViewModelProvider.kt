package com.bayrjavhlanmw.finance.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bayrjavhlanmw.finance.FinanceApplication

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            val container = (this[APPLICATION_KEY] as FinanceApplication).container
            HomeViewModel(container.financeRepository, container.preferencesRepository, createSavedStateHandle())
        }
        initializer {
            val container = (this[APPLICATION_KEY] as FinanceApplication).container
            EntryViewModel(container.financeRepository, createSavedStateHandle())
        }
        initializer {
            val container = (this[APPLICATION_KEY] as FinanceApplication).container
            SettingsViewModel(container.preferencesRepository)
        }
    }
}
