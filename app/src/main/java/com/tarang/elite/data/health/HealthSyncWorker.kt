package com.tarang.elite.data.health

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tarang.elite.TarangApp

class HealthSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as? TarangApp ?: return Result.success()
        return try {
            app.container.healthRepo.safeSync()
            Result.success()
        } catch (e: SecurityException) {
            // Background read permission not granted — foreground sync on app open covers it.
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
