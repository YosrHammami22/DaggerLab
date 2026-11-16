package com.yosrhammami.daggerlab.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.daggerlab.domain.logging.AppLogger
import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import com.yosrhammami.daggerlab.domain.timer.SleepTimer
import com.yosrhammami.daggerlab.domain.timer.SleepTimerState
import com.yosrhammami.daggerlab.ui.screen.SleepTimerUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerViewModel @Inject constructor(
    private val settingsRepository: SleepTimerSettingsRepository,
    private val logger: AppLogger,
    private val playbackController: PlaybackController,
    private val sleepTimer: SleepTimer
) : ViewModel() {

    val lastDuration: StateFlow<Int?> = settingsRepository.observeLastDuration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val uiState: StateFlow<SleepTimerUiState> =
        combine(lastDuration, sleepTimer.state, playbackController.observeIsPlaying()) { last, timer, playing ->
            when (timer) {
                SleepTimerState.Idle -> SleepTimerUiState.Idle(last, playing)
                is SleepTimerState.Running -> SleepTimerUiState.Running(timer.remainingSeconds, playing)
                SleepTimerState.Finished -> SleepTimerUiState.Finished
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SleepTimerUiState.Idle(null))

    fun onDurationSelected(minutes: Int) {
        // A countdown of 0 or less would never reach Finished, so reject it before saving anything.
        if (minutes <= 0) {
            val message = "Duration must be positive, was $minutes"
            logger.w(TAG, message)
            throw IllegalArgumentException(message)
        }
        logger.i(TAG, "Duration selected: $minutes min")
        viewModelScope.launch { settingsRepository.saveLastDuration(minutes) }
        sleepTimer.start(minutes * 60L)
    }

    // Cancel and OK both go back to Idle; uiState then shows the last saved duration from Room.
    fun onCancelClick() {
        logger.i(TAG, "Timer cancelled")
        sleepTimer.stop()
    }

    fun onOkClick() {
        logger.i(TAG, "OK clicked")
        sleepTimer.stop()
    }

    fun onPlayClick() {
        logger.i(TAG, "Play clicked")
        playbackController.play()
    }

    fun onPauseClick() {
        logger.i(TAG, "Pause clicked")
        playbackController.pause()
    }

    private companion object {
        const val TAG = "PlayerViewModel"
    }
}