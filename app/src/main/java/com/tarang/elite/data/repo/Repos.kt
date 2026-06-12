package com.tarang.elite.data.repo

import com.tarang.elite.data.db.AppDatabase
import com.tarang.elite.data.db.BadFoodEntity
import com.tarang.elite.data.db.BodyMetricEntity
import com.tarang.elite.data.db.CheckInEntity
import com.tarang.elite.data.db.DailyHealthEntity
import com.tarang.elite.data.db.ExerciseTickEntity
import com.tarang.elite.data.db.MeditationDayEntity
import com.tarang.elite.data.db.SyncedActivityEntity
import com.tarang.elite.data.db.WaterDayEntity
import com.tarang.elite.data.db.WorkoutLogEntity
import com.tarang.elite.data.health.HcStatus
import com.tarang.elite.data.health.HealthConnectManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TrackingRepository(private val db: AppDatabase) {

    // ---- Check-ins ----
    fun checkIn(date: String): Flow<CheckInEntity?> = db.checkInDao().byDate(date)
    fun recentCheckIns(limit: Int = 30): Flow<List<CheckInEntity>> = db.checkInDao().recent(limit)
    fun checkInDates(): Flow<List<String>> = db.checkInDao().allDates()
    suspend fun saveCheckIn(date: String, mood: Int, energy: Int, stress: Int, note: String) {
        db.checkInDao().upsert(
            CheckInEntity(date, mood, energy, stress, note, System.currentTimeMillis())
        )
    }

    // ---- Water ----
    fun water(date: String): Flow<WaterDayEntity?> = db.waterDao().byDate(date)
    suspend fun addWater(date: String, deltaMl: Int) {
        val current = db.waterDao().byDate(date).first()?.ml ?: 0
        val next = (current + deltaMl).coerceAtLeast(0)
        db.waterDao().upsert(WaterDayEntity(date, next))
    }

    // ---- Meditation ----
    fun meditation(date: String): Flow<MeditationDayEntity?> = db.meditationDao().byDate(date)
    suspend fun setMeditation(date: String, morning: Boolean? = null, evening: Boolean? = null) {
        val current = db.meditationDao().byDate(date).first()
            ?: MeditationDayEntity(date, morning = false, evening = false, breathCycles = 0)
        db.meditationDao().upsert(
            current.copy(
                morning = morning ?: current.morning,
                evening = evening ?: current.evening,
            )
        )
    }

    suspend fun addBreathCycles(date: String, cycles: Int) {
        val current = db.meditationDao().byDate(date).first()
            ?: MeditationDayEntity(date, morning = false, evening = false, breathCycles = 0)
        db.meditationDao().upsert(current.copy(breathCycles = current.breathCycles + cycles))
    }

    // ---- Exercise ticks ----
    fun ticks(date: String): Flow<List<ExerciseTickEntity>> = db.tickDao().forDate(date)
    suspend fun setTick(date: String, exerciseId: String, done: Boolean) {
        db.tickDao().upsert(ExerciseTickEntity(date, exerciseId, done))
    }

    // ---- Workout logs ----
    fun workoutLogs(): Flow<List<WorkoutLogEntity>> = db.workoutLogDao().all()
    fun workoutDates(): Flow<List<String>> = db.workoutLogDao().allDates()
    suspend fun logWorkout(date: String, dayLabel: String, name: String, completed: Int, total: Int) {
        db.workoutLogDao().insert(
            WorkoutLogEntity(
                date = date,
                dayLabel = dayLabel,
                workoutName = name,
                completed = completed,
                total = total,
                timestamp = System.currentTimeMillis(),
            )
        )
    }

    // ---- Bad food ----
    fun badFoods(): Flow<List<BadFoodEntity>> = db.badFoodDao().recent()
    suspend fun logBadFood(food: String) {
        val now = LocalDate.now().toString()
        val time = java.time.LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        db.badFoodDao().insert(
            BadFoodEntity(food = food, date = now, time = time, timestamp = System.currentTimeMillis())
        )
    }

    // ---- Body metrics ----
    fun bodyMetrics(): Flow<List<BodyMetricEntity>> = db.bodyMetricDao().all()
    suspend fun addBodyMetric(weightKg: Double, bodyFatPct: Double, muscleKg: Double) {
        db.bodyMetricDao().insert(
            BodyMetricEntity(
                weightKg = weightKg,
                bodyFatPct = bodyFatPct,
                muscleKg = muscleKg,
                timestamp = System.currentTimeMillis(),
            )
        )
    }
}

class HealthRepository(
    private val hc: HealthConnectManager,
    private val db: AppDatabase,
) {
    fun day(date: String): Flow<DailyHealthEntity?> = db.healthDao().byDate(date)
    fun lastDays(n: Int): Flow<List<DailyHealthEntity>> = db.healthDao().recent(n)
    fun recentActivities(): Flow<List<SyncedActivityEntity>> = db.healthDao().recentActivities()

    fun status(): HcStatus = hc.status()
    suspend fun hasPermissions(): Boolean = hc.hasRequiredPermissions()
    fun permissionsToRequest(): Set<String> = hc.permissionsToRequest

    suspend fun syncNow(days: Int = 10) {
        if (hc.status() != HcStatus.AVAILABLE) return
        if (!hc.hasRequiredPermissions()) return
        val daily = hc.readDailySummaries(days)
        if (daily.isNotEmpty()) db.healthDao().upsertDays(daily)
        val activities = hc.readActivities(days)
        if (activities.isNotEmpty()) db.healthDao().upsertActivities(activities)
    }

    suspend fun safeSync() {
        try {
            syncNow()
        } catch (_: Exception) {
        }
    }
}
