package com.yosrhammami.daggerlab.domain.timer

import com.yosrhammami.daggerlab.domain.logging.AppLogger
import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import com.yosrhammami.daggerlab.domain.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Counts down and pauses playback when it reaches zero.
 *
 * Runs in an app-wide [scope], not in a ViewModel, so closing the screen does not stop the timer.
 * [start] and [stop] must be called from the scope's thread (the main thread in the app).
 */
@Singleton
class SleepTimer @Inject constructor(
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val playbackController: PlaybackController,
    private val logger: AppLogger
) {

    private val _state = MutableStateFlow<SleepTimerState>(SleepTimerState.Idle)
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()

    private var countdownJob: Job? = null

    fun start(totalSeconds: Long) {
        require(totalSeconds > 0) { "totalSeconds must be positive, was $totalSeconds" }
        countdownJob?.cancel()
        _state.value = SleepTimerState.Running(totalSeconds)
        // Remaining time is always measured against a fixed deadline, never by counting ticks:
        // delay() pauses during deep sleep and drifts a little each tick, but the clock does not.
        val deadlineMillis = clock.nowMillis() + totalSeconds * 1_000
        countdownJob = scope.launch {
            while (true) {
                val remainingMillis = deadlineMillis - clock.nowMillis()
                if (remainingMillis <= 0) break
                val remainingSeconds = (remainingMillis + 999) / 1_000 // round up: 0.4 s left shows 1
                _state.value = SleepTimerState.Running(remainingSeconds)
                // Wake up when the displayed second changes (at most 1 s from now).
                delay(remainingMillis - (remainingSeconds - 1) * 1_000)
            }
            _state.value = SleepTimerState.Finished
            logger.i(TAG, "Timer finished")
            // Only a countdown that reaches zero pauses playback; stop() leaves it as is.
            playbackController.pause()
        }
    }

    // Cancels any countdown and goes back to Idle. Used for both Cancel and OK.
    fun stop() {
        countdownJob?.cancel()
        countdownJob = null
        _state.value = SleepTimerState.Idle
    }

    private companion object {
        const val TAG = "SleepTimer"
    }
}