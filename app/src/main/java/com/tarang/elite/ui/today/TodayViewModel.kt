package com.tarang.elite.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tarang.elite.data.content.DayKind
import com.tarang.elite.data.content.DayPlan
import com.tarang.elite.data.content.MindContent
import com.tarang.elite.data.content.WorkoutLibrary
import com.tarang.elite.data.db.CheckInEntity
import com.tarang.elite.data.db.DailyHealthEntity
import com.tarang.elite.data.db.MeditationDayEntity
import com.tarang.elite.data.health.HcStatus
import com.tarang.elite.data.repo.HealthRepository
import com.tarang.elite.data.repo.TrackingRepository
import com.tarang.elite.domain.Recovery
import com.tarang.elite.domain.RecoveryResult
import com.tarang.elite.domain.StreakInfo
import com.tarang.elite.domain.Streaks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TodayUiState(
    val date: LocalDate = LocalDate.now(),
    val plan: DayPlan = WorkoutLibrary.planFor(LocalDate.now().dayOfWeek),
    val checkIn: CheckInEntity? = null,
    val recovery: RecoveryResult? = null,
    val sleepDebtMin: Int? = null,
    val streak: StreakInfo = StreakInfo(0, activeToday = false, workoutDoneToday = false),
    val waterMl: Int = 0,
    val meditation: MeditationDayEntity? = null,
    val todayHealth: DailyHealthEntity? = null,
    val fireMessage: String = "",
)

data class HcUiState(
    val status: HcStatus = HcStatus.UNAVAILABLE,
    val hasPermissions: Boolean = false,
    val checked: Boolean = false,
)

class TodayViewModel(
    private val tracking: TrackingRepository,
    private val health: HealthRepository,
) : ViewModel() {

    private val todayIso: String = LocalDate.now().toString()

    private val _hcState = MutableStateFlow(HcUiState())
    val hcState: StateFlow<HcUiState> = _hcState.asStateFlow()

    val uiState: StateFlow<TodayUiState> = combine(
        tracking.checkIn(todayIso),
        tracking.water(todayIso),
        tracking.meditation(todayIso),
        health.lastDays(31),
        combine(tracking.checkInDates(), tracking.workoutDates()) { c, w -> c to w },
    ) { checkIn, water, meditation, healthDays, dates ->
        val today = LocalDate.now()
        val plan = WorkoutLibrary.planFor(today.dayOfWeek)
        val todayHealth = healthDays.firstOrNull { it.date == today.toString() }
        val recovery = Recovery.score(todayHealth, healthDays, checkIn)
        val streak = Streaks.compute(dates.first, dates.second, today)

        val messages = if (plan.kind == DayKind.REST) MindContent.trainerRest else MindContent.trainerWorkout
        val fire = messages[today.dayOfYear % messages.size]

        TodayUiState(
            date = today,
            plan = plan,
            checkIn = checkIn,
            recovery = recovery,
            sleepDebtMin = Recovery.sleepDebtMinutes(healthDays),
            streak = streak,
            waterMl = water?.ml ?: 0,
            meditation = meditation,
            todayHealth = todayHealth,
            fireMessage = fire,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayUiState(),
    )

    init {
        refreshHcState()
    }

    fun permissionsToRequest(): Set<String> = health.permissionsToRequest()

    fun refreshHcState() {
        viewModelScope.launch {
            val status = health.status()
            val has = if (status == HcStatus.AVAILABLE) health.hasPermissions() else false
            _hcState.value = HcUiState(status = status, hasPermissions = has, checked = true)
        }
    }

    fun onHcPermissionResult() {
        viewModelScope.launch {
            refreshHcState()
            health.safeSync()
        }
    }

    fun syncNow() {
        viewModelScope.launch { health.safeSync() }
    }

    fun saveCheckIn(mood: Int, energy: Int, stress: Int, note: String) {
        viewModelScope.launch {
            tracking.saveCheckIn(todayIso, mood, energy, stress, note)
        }
    }

    fun addWater(deltaMl: Int) {
        viewModelScope.launch { tracking.addWater(todayIso, deltaMl) }
    }

    fun toggleMorningMeditation(current: Boolean) {
        viewModelScope.launch { tracking.setMeditation(todayIso, morning = !current) }
    }

    fun toggleEveningMeditation(current: Boolean) {
        viewModelScope.launch { tracking.setMeditation(todayIso, evening = !current) }
    }

    /** Logs an instructor-led class (Boxing / Upper Body Lift) as a completed session. */
    fun markClassAttended(plan: DayPlan) {
        viewModelScope.launch {
            tracking.logWorkout(
                date = todayIso,
                dayLabel = plan.dayLabel,
                name = plan.title,
                completed = 1,
                total = 1,
            )
        }
    }
}
