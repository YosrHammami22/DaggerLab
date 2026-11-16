package com.yosrhammami.daggerlab.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.daggerlab.domain.logging.AppLogger
import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import com.yosrhammami.daggerlab.ui.screen.SleepTimerUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerViewModel @Inject constructor(
    private val settingsRepository: SleepTimerSettingsRepository,
    private val logger: AppLogger
) : ViewModel() {

    val lastDuration: StateFlow<Int?> = settingsRepository.observeLastDuration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Running or Finished while a timer is active; null = Idle (built from lastDuration).
    private val timerState = MutableStateFlow<SleepTimerUiState?>(null)

    private var countdownJob: Job? = null

    val uiState: StateFlow<SleepTimerUiState> =
        combine(lastDuration, timerState) { last, timer ->
            timer ?: SleepTimerUiState.Idle(last)
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
        startCountdown(minutes * 60L)

    }

    fun onCancelClick() {
        logger.i(TAG, "Timer cancelled")
        resetToIdle()
    }

    fun onOkClick() {
        logger.i(TAG, "OK clicked")
        resetToIdle()
    }

    // Stops any running countdown and goes back to Idle. uiState then shows the last saved
    // duration, which stays in Room.
    private fun resetToIdle() {
        countdownJob?.cancel()
        countdownJob = null
        timerState.value = null
    }

    private fun startCountdown(totalSeconds: Long) {
        countdownJob?.cancel()
        timerState.value = SleepTimerUiState.Running(totalSeconds)
        countdownJob = viewModelScope.launch {
            var remaining = totalSeconds
            while (remaining > 0) {
                delay(1_000)
                remaining--
                timerState.value =
                    if (remaining > 0) SleepTimerUiState.Running(remaining) else SleepTimerUiState.Finished
            }
            logger.i(TAG, "Timer finished")
            countdownJob = null
        }
    }

    private companion object {
        const val TAG = "PlayerViewModel"
    }
}
