package com.bayrjavhlanmw.finance.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.bayrjavhlanmw.finance.worker.ReminderScheduler

private val Context.dataStore by preferencesDataStore(name = "finance_preferences")

interface AppContainer {
    val financeRepository: FinanceRepository
    val preferencesRepository: UserPreferencesRepository
    val reminderScheduler: ReminderScheduler
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val appContext = context.applicationContext
    override val financeRepository: FinanceRepository by lazy {
        OfflineFinanceRepository(FinanceDatabase.getDatabase(appContext).financeDao())
    }
    override val preferencesRepository by lazy { UserPreferencesRepository(appContext.dataStore) }
    override val reminderScheduler by lazy { ReminderScheduler(appContext) }
}
