package com.bayrjavhlanmw.finance.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context)
    fun schedule() {
        workManager.enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.REPLACE, request())
    }
    fun cancel() { workManager.cancelUniqueWork(NAME) }

    companion object {
        const val NAME = "daily_finance_reminder"
        fun request(): PeriodicWorkRequest = PeriodicWorkRequestBuilder<FinanceReminderWorker>(
            24, TimeUnit.HOURS
        ).setInitialDelay(24, TimeUnit.HOURS).addTag(NAME).build()
    }
}
