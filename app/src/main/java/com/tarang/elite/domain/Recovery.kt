package com.tarang.elite.domain

import com.tarang.elite.data.db.CheckInEntity
import com.tarang.elite.data.db.DailyHealthEntity
import kotlin.math.roundToInt

data class RecoveryComponent(val name: String, val score: Int)

data class RecoveryResult(
    val score: Int,
    val label: String,
    val advice: String,
    val components: List<RecoveryComponent>,
)

object Recovery {

    /**
     * Blends last night's sleep, resting HR vs 30-day baseline, HRV vs baseline,
     * and the subjective check-in. Weights renormalise over whatever data exists,
     * so the score works on day one and gets sharper as Garmin history builds.
     */
    fun score(
        today: DailyHealthEntity?,
        history: List<DailyHealthEntity>,
        checkIn: CheckInEntity?,
    ): RecoveryResult? {
        if (checkIn == null) return null

        val components = mutableListOf<Pair<RecoveryComponent, Double>>()

        // --- Sleep (weight 0.35) ---
        val sleepMin = today?.sleepMinutes
        if (sleepMin != null && sleepMin > 0) {
            var s = (sleepMin / 480.0 * 100).coerceIn(0.0, 100.0)
            val deep = today.deepMinutes ?: 0
            val rem = today.remMinutes ?: 0
            if (sleepMin > 0 && (deep + rem) >= sleepMin * 0.25) s = (s + 6).coerceAtMost(100.0)
            components.add(RecoveryComponent("Sleep", s.roundToInt()) to 0.35)
        }

        // --- Resting HR vs baseline (weight 0.25) ---
        val rhr = today?.restingHr
        val rhrBaseline = history
            .filter { it.date != today?.date }
            .mapNotNull { it.restingHr }
            .takeIf { it.size >= 3 }
            ?.average()
        if (rhr != null) {
            val s = if (rhrBaseline != null) {
                val delta = rhr - rhrBaseline
                (100 - delta * 7).coerceIn(20.0, 100.0)
            } else 70.0
            components.add(RecoveryComponent("Resting HR", s.roundToInt()) to 0.25)
        }

        // --- HRV vs baseline (weight 0.15) ---
        val hrv = today?.hrvMs
        val hrvBaseline = history
            .filter { it.date != today?.date }
            .mapNotNull { it.hrvMs }
            .takeIf { it.size >= 3 }
            ?.average()
        if (hrv != null && hrv > 0) {
            val s = if (hrvBaseline != null && hrvBaseline > 0) {
                val ratio = hrv / hrvBaseline
                (50 + (ratio - 1) * 150).coerceIn(20.0, 100.0)
            } else 70.0
            components.add(RecoveryComponent("HRV", s.roundToInt()) to 0.15)
        }

        // --- Subjective check-in (weight 0.25) ---
        val subjective =
            ((checkIn.mood - 1) / 4.0 * 40) +
                (checkIn.energy / 10.0 * 30) +
                ((10 - checkIn.stress) / 10.0 * 30)
        components.add(RecoveryComponent("How you feel", subjective.roundToInt()) to 0.25)

        val totalWeight = components.sumOf { it.second }
        val score = components.sumOf { it.first.score * it.second } / totalWeight
        val rounded = score.roundToInt().coerceIn(0, 100)

        val (label, advice) = when {
            rounded >= 85 -> "Primed" to "Green light. Push hard — add weight or reps today."
            rounded >= 70 -> "Ready" to "Solid base. Train as planned and finish strong."
            rounded >= 55 -> "Steady" to "Train, but leave one rep in the tank on big lifts."
            rounded >= 40 -> "Take it easy" to "Lower the intensity — movement over maximums today."
            else -> "Recover" to "Body's asking for rest. Walk, stretch, hydrate, sleep early."
        }

        return RecoveryResult(rounded, label, advice, components.map { it.first })
    }

    /** Rolling 7-day sleep debt in minutes against an 8h/night need (positive = debt). */
    fun sleepDebtMinutes(history: List<DailyHealthEntity>, needMinutesPerNight: Int = 480): Int? {
        val nights = history.mapNotNull { it.sleepMinutes }.take(7)
        if (nights.isEmpty()) return null
        val debt = nights.sumOf { needMinutesPerNight - it }
        return debt.coerceAtLeast(0)
    }
}
