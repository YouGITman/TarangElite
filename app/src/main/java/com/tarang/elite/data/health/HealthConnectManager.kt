package com.tarang.elite.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.tarang.elite.data.db.DailyHealthEntity
import com.tarang.elite.data.db.SyncedActivityEntity
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

enum class HcStatus { AVAILABLE, NEEDS_UPDATE, UNAVAILABLE }

class HealthConnectManager(private val context: Context) {

    /** Background reads are optional; everything else is required. */
    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
    )

    val permissionsToRequest: Set<String> =
        requiredPermissions + "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"

    fun status(): HcStatus = when (HealthConnectClient.getSdkStatus(context)) {
        HealthConnectClient.SDK_AVAILABLE -> HcStatus.AVAILABLE
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> HcStatus.NEEDS_UPDATE
        else -> HcStatus.UNAVAILABLE
    }

    private val client: HealthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun hasRequiredPermissions(): Boolean = try {
        client.permissionController.getGrantedPermissions().containsAll(requiredPermissions)
    } catch (e: Exception) {
        false
    }

    private val zone: ZoneId get() = ZoneId.systemDefault()
    private fun LocalDate.startInstant(): Instant = atStartOfDay(zone).toInstant()

    /** Builds one summary row per day for the last [days] days (oldest first). */
    suspend fun readDailySummaries(days: Int): List<DailyHealthEntity> {
        val today = LocalDate.now()
        val out = mutableListOf<DailyHealthEntity>()
        for (offset in (days - 1) downTo 0) {
            val date = today.minusDays(offset.toLong())
            out.add(readDay(date, isToday = date == today))
        }
        return out
    }

    private suspend fun readDay(date: LocalDate, isToday: Boolean): DailyHealthEntity {
        val dayStart = date.startInstant()
        val dayEnd = if (isToday) Instant.now() else date.plusDays(1).startInstant()

        // Steps and active calories across the calendar day
        var steps: Long? = null
        var kcal: Double? = null
        try {
            val agg = client.aggregate(
                AggregateRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL, TotalCaloriesBurnedRecord.ENERGY_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(dayStart, dayEnd),
                )
            )
            steps = agg[StepsRecord.COUNT_TOTAL]
            kcal = agg[TotalCaloriesBurnedRecord.ENERGY_TOTAL]?.inKilocalories
        } catch (_: Exception) {
        }

        // "Last night" window: previous evening 18:00 -> today 14:00
        val sleepStart = date.minusDays(1).atTime(LocalTime.of(18, 0)).atZone(zone).toInstant()
        val sleepEnd = date.atTime(LocalTime.of(14, 0)).atZone(zone).toInstant()

        var sleepMinutes: Int? = null
        var deepMinutes: Int? = null
        var remMinutes: Int? = null
        try {
            val sessions = client.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(sleepStart, sleepEnd),
                )
            ).records
            val main = sessions.maxByOrNull { Duration.between(it.startTime, it.endTime) }
            if (main != null) {
                sleepMinutes = Duration.between(main.startTime, main.endTime).toMinutes().toInt()
                var deep = 0L
                var rem = 0L
                main.stages.forEach { stage ->
                    val mins = Duration.between(stage.startTime, stage.endTime).toMinutes()
                    when (stage.stage) {
                        SleepSessionRecord.STAGE_TYPE_DEEP -> deep += mins
                        SleepSessionRecord.STAGE_TYPE_REM -> rem += mins
                    }
                }
                if (deep > 0) deepMinutes = deep.toInt()
                if (rem > 0) remMinutes = rem.toInt()
            }
        } catch (_: Exception) {
        }

        var restingHr: Long? = null
        try {
            restingHr = client.readRecords(
                ReadRecordsRequest(
                    recordType = RestingHeartRateRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(dayStart, dayEnd),
                )
            ).records.maxByOrNull { it.time }?.beatsPerMinute
        } catch (_: Exception) {
        }

        var hrv: Double? = null
        try {
            val hrvRecords = client.readRecords(
                ReadRecordsRequest(
                    recordType = HeartRateVariabilityRmssdRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(sleepStart, sleepEnd),
                )
            ).records
            if (hrvRecords.isNotEmpty()) {
                hrv = hrvRecords.map { it.heartRateVariabilityMillis }.average()
            }
        } catch (_: Exception) {
        }

        return DailyHealthEntity(
            date = date.toString(),
            steps = steps,
            activeKcal = kcal,
            sleepMinutes = sleepMinutes,
            deepMinutes = deepMinutes,
            remMinutes = remMinutes,
            restingHr = restingHr,
            hrvMs = hrv,
            syncedAt = System.currentTimeMillis(),
        )
    }

    suspend fun readActivities(days: Int): List<SyncedActivityEntity> {
        val end = Instant.now()
        val start = LocalDate.now().minusDays(days.toLong()).startInstant()
        return try {
            client.readRecords(
                ReadRecordsRequest(
                    recordType = ExerciseSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(start, end),
                )
            ).records.map { record ->
                SyncedActivityEntity(
                    uid = record.metadata.id,
                    title = record.title ?: exerciseTypeName(record.exerciseType),
                    type = exerciseTypeName(record.exerciseType),
                    startMillis = record.startTime.toEpochMilli(),
                    durationMinutes = Duration.between(record.startTime, record.endTime).toMinutes().toInt(),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun exerciseTypeName(type: Int): String = when (type) {
        ExerciseSessionRecord.EXERCISE_TYPE_BOXING -> "Boxing"
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Run"
        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING_TREADMILL -> "Treadmill Run"
        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walk"
        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Ride"
        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength"
        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Swim"
        ExerciseSessionRecord.EXERCISE_TYPE_HIKING -> "Hike"
        ExerciseSessionRecord.EXERCISE_TYPE_ELLIPTICAL -> "Elliptical"
        ExerciseSessionRecord.EXERCISE_TYPE_ROWING_MACHINE -> "Rowing"
        ExerciseSessionRecord.EXERCISE_TYPE_PILATES -> "Pilates"
        else -> "Workout"
    }
}
