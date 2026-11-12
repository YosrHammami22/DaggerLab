package com.yosrhammami.daggerlab.ui.screen

sealed interface SleepTimerUiState {

    data class Idle(val lastDurationMinutes: Int?) : SleepTimerUiState

    data class Running(val remainingSeconds: Long) : SleepTimerUiState

    data object Finished : SleepTimerUiState
}
