package com.tarang.elite.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Daily morning check-in: the mental-health pulse of the app. */
@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey val date: String,      // ISO yyyy-MM-dd
    val mood: Int,                     // 1..5
    val energy: Int,                   // 1..10
    val stress: Int,                   // 1..10
    val note: String,
    val timestamp: Long,
)

@Entity(tableName = "water_days")
data class WaterDayEntity(
    @PrimaryKey val date: String,
    val ml: Int,
)

@Entity(tableName = "meditation_days")
data class MeditationDayEntity(
    @PrimaryKey val date: String,
    val morning: Boolean,
    val evening: Boolean,
    val breathCycles: Int,
)

@Entity(tableName = "exercise_ticks", primaryKeys = ["date", "exerciseId"])
data class ExerciseTickEntity(
    val date: String,
    val exerciseId: String,
    val done: Boolean,
)

@Entity(tableName = "workout_logs")
data class WorkoutLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val dayLabel: String,
    val workoutName: String,
    val completed: Int,
    val total: Int,
    val timestamp: Long,
)

@Entity(tableName = "bad_foods")
data class BadFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val food: String,
    val date: String,
    val time: String,
    val timestamp: Long,
)

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weightKg: Double,
    val bodyFatPct: Double,
    val muscleKg: Double,
    val timestamp: Long,
)

/** One row per day of Garmin data pulled from Health Connect. */
@Entity(tableName = "daily_health")
data class DailyHealthEntity(
    @PrimaryKey val date: String,
    val steps: Long?,
    val activeKcal: Double?,
    val sleepMinutes: Int?,
    val deepMinutes: Int?,
    val remMinutes: Int?,
    val restingHr: Long?,
    val hrvMs: Double?,
    val syncedAt: Long,
)

/** Garmin workouts (exercise sessions) synced from Health Connect. */
@Entity(tableName = "synced_activities")
data class SyncedActivityEntity(
    @PrimaryKey val uid: String,
    val title: String,
    val type: String,
    val startMillis: Long,
    val durationMinutes: Int,
)
