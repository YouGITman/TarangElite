package com.tarang.elite.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {
    @Upsert
    suspend fun upsert(entity: CheckInEntity)

    @Query("SELECT * FROM check_ins WHERE date = :date")
    fun byDate(date: String): Flow<CheckInEntity?>

    @Query("SELECT * FROM check_ins ORDER BY date DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<CheckInEntity>>

    @Query("SELECT date FROM check_ins")
    fun allDates(): Flow<List<String>>
}

@Dao
interface WaterDao {
    @Upsert
    suspend fun upsert(entity: WaterDayEntity)

    @Query("SELECT * FROM water_days WHERE date = :date")
    fun byDate(date: String): Flow<WaterDayEntity?>
}

@Dao
interface MeditationDao {
    @Upsert
    suspend fun upsert(entity: MeditationDayEntity)

    @Query("SELECT * FROM meditation_days WHERE date = :date")
    fun byDate(date: String): Flow<MeditationDayEntity?>
}

@Dao
interface TickDao {
    @Upsert
    suspend fun upsert(entity: ExerciseTickEntity)

    @Query("SELECT * FROM exercise_ticks WHERE date = :date")
    fun forDate(date: String): Flow<List<ExerciseTickEntity>>
}

@Dao
interface WorkoutLogDao {
    @Insert
    suspend fun insert(entity: WorkoutLogEntity)

    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun all(): Flow<List<WorkoutLogEntity>>

    @Query("SELECT date FROM workout_logs")
    fun allDates(): Flow<List<String>>
}

@Dao
interface BadFoodDao {
    @Insert
    suspend fun insert(entity: BadFoodEntity)

    @Query("SELECT * FROM bad_foods ORDER BY timestamp DESC LIMIT 30")
    fun recent(): Flow<List<BadFoodEntity>>
}

@Dao
interface BodyMetricDao {
    @Insert
    suspend fun insert(entity: BodyMetricEntity)

    @Query("SELECT * FROM body_metrics ORDER BY timestamp ASC")
    fun all(): Flow<List<BodyMetricEntity>>
}

@Dao
interface HealthDao {
    @Upsert
    suspend fun upsertDays(entities: List<DailyHealthEntity>)

    @Upsert
    suspend fun upsertActivities(entities: List<SyncedActivityEntity>)

    @Query("SELECT * FROM daily_health WHERE date = :date")
    fun byDate(date: String): Flow<DailyHealthEntity?>

    @Query("SELECT * FROM daily_health ORDER BY date DESC LIMIT :limit")
    fun recent(limit: Int): Flow<List<DailyHealthEntity>>

    @Query("SELECT * FROM synced_activities ORDER BY startMillis DESC LIMIT 20")
    fun recentActivities(): Flow<List<SyncedActivityEntity>>
}
