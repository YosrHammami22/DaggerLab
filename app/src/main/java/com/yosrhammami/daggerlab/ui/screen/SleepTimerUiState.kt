package com.yosrhammami.daggerlab.ui.screen

sealed interface SleepTimerUiState {

    data class Idle(
        val lastDurationMinutes: Int?,
        val isPlaying: Boolean = false
    ) : SleepTimerUiState

    data class Running(
        val remainingSeconds: Long,
        val isPlaying: Boolean = false
    ) : SleepTimerUiState

    // No isPlaying: the timer has just paused playback, and the screen only offers OK.
    data object Finished : SleepTimerUiState
}