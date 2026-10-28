package com.bayrjavhlanmw.finance

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.bayrjavhlanmw.finance.data.AppContainer
import com.bayrjavhlanmw.finance.data.DefaultAppContainer

class FinanceApplication : Application(), DefaultLifecycleObserver {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super<Application>.onCreate()
        container = DefaultAppContainer(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        container.reminderScheduler.cancel()
    }

    override fun onStop(owner: LifecycleOwner) {
        container.reminderScheduler.schedule()
    }
}
