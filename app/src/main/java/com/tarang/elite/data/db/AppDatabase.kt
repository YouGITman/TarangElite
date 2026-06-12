package com.tarang.elite.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CheckInEntity::class,
        WaterDayEntity::class,
        MeditationDayEntity::class,
        ExerciseTickEntity::class,
        WorkoutLogEntity::class,
        BadFoodEntity::class,
        BodyMetricEntity::class,
        DailyHealthEntity::class,
        SyncedActivityEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checkInDao(): CheckInDao
    abstract fun waterDao(): WaterDao
    abstract fun meditationDao(): MeditationDao
    abstract fun tickDao(): TickDao
    abstract fun workoutLogDao(): WorkoutLogDao
    abstract fun badFoodDao(): BadFoodDao
    abstract fun bodyMetricDao(): BodyMetricDao
    abstract fun healthDao(): HealthDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "tarang_elite.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
