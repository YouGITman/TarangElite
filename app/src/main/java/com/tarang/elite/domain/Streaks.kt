package com.tarang.elite.domain

import java.time.LocalDate

data class StreakInfo(
    val count: Int,
    val activeToday: Boolean,
    val workoutDoneToday: Boolean,
)

object Streaks {

    /**
     * A day is "active" when there's a check-in or a logged workout.
     * Forgiveness rule: a single missed day between active days does NOT break
     * the streak (it just doesn't count). Two consecutive misses end it.
     */
    fun compute(
        checkInDates: Collection<String>,
        workoutDates: Collection<String>,
        today: LocalDate = LocalDate.now(),
    ): StreakInfo {
        val active: Set<LocalDate> = buildSet {
            checkInDates.forEach { runCatching { add(LocalDate.parse(it)) } }
            workoutDates.forEach { runCatching { add(LocalDate.parse(it)) } }
        }
        val workoutToday = workoutDates.any { it == today.toString() }
        val activeToday = today in active

        // Start counting from today if active, otherwise from yesterday (today still pending).
        var cursor = if (activeToday) today else today.minusDays(1)
        var count = 0
        var gapUsed = false

        while (true) {
            if (cursor in active) {
                count++
                gapUsed = false
                cursor = cursor.minusDays(1)
            } else {
                if (gapUsed) break
                gapUsed = true
                cursor = cursor.minusDays(1)
            }
            if (count > 3650) break // safety
        }

        return StreakInfo(count = count, activeToday = activeToday, workoutDoneToday = workoutToday)
    }
}
