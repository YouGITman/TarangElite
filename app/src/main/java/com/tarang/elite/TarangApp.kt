package com.tarang.elite

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.tarang.elite.data.health.HealthSyncWorker
import com.tarang.elite.di.AppContainer
import java.util.concurrent.TimeUnit

class TarangApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        val request = PeriodicWorkRequestBuilder<HealthSyncWorker>(6, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "health_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
