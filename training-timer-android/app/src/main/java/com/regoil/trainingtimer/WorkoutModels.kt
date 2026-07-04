package com.regoil.trainingtimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Phases of a single interval cycle. */
enum class Phase { WORK, REST, EXERCISE_BREAK, DONE }

/**
 * Full workout description, built from the sliders on the main screen.
 *
 * The workout is an interval timer that repeats WORK -> REST cycles until the
 * total training time is reached. Every [setsPerExercise] completed sets the
 * short rest is replaced by a longer "break between exercises" (if enabled).
 */
data class WorkoutConfig(
    val totalMillis: Long,          // Установите время тренировки (total cap)
    val workMillis: Long,           // Длительность подхода (work per set)
    val restMillis: Long,           // Перерыв между подходами (rest between sets)
    val exerciseBreakMillis: Long,  // Перерыв между упражнениями (longer break)
    val useExerciseBreak: Boolean,
    val useWork: Boolean = true,    // when false: no fixed work phases, plain countdown
    val setsPerExercise: Int = 4
)

/** Snapshot of the running timer, observed by the UI and the notification. */
data class TimerUi(
    val active: Boolean = false,
    val paused: Boolean = false,
    val phase: Phase = Phase.DONE,
    val phaseRemainingMs: Long = 0,
    val totalRemainingMs: Long = 0,
    val setNumber: Int = 0,
    val plain: Boolean = false      // workless mode: only the total countdown is meaningful
)

/** Single source of truth shared between the service and the Activity. */
object TimerState {
    private val _ui = MutableStateFlow(TimerUi())
    val ui: StateFlow<TimerUi> = _ui

    fun update(value: TimerUi) {
        _ui.value = value
    }

    fun reset() {
        _ui.value = TimerUi()
    }
}
