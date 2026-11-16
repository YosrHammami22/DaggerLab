package com.yosrhammami.daggerlab.domain.timer

sealed interface SleepTimerState {

    data object Idle : SleepTimerState

    data class Running(val remainingSeconds: Long) : SleepTimerState

    data object Finished : SleepTimerState
}