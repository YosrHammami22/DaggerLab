package com.yosrhammami.daggerlab.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class PlayerViewModel @Inject constructor(private val settingsRepository: SleepTimerSettingsRepository):
    ViewModel() {

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
        viewModelScope.launch { settingsRepository.saveLastDuration(minutes) }
        startCountdown(minutes * 60L)
    }

    fun onCancelClick() {
        countdownJob?.cancel()
        countdownJob = null
        // Back to Idle: uiState then shows the last saved duration, which stays in Room.
        timerState.value = null
    }

    fun onOkClick() {
        // Back to Idle with the last saved duration. The Job was already cleared when the countdown ended.
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
            countdownJob = null
        }
    }
}
