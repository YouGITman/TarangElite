package com.tarang.elite.di

import android.content.Context
import com.tarang.elite.data.db.AppDatabase
import com.tarang.elite.data.health.HealthConnectManager
import com.tarang.elite.data.repo.HealthRepository
import com.tarang.elite.data.repo.TrackingRepository

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.build(context)
    val healthConnect: HealthConnectManager = HealthConnectManager(context)
    val tracking: TrackingRepository = TrackingRepository(database)
    val healthRepo: HealthRepository = HealthRepository(healthConnect, database)
}
