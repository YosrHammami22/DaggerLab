package com.yosrhammami.daggerlab.ui.player

import app.cash.turbine.test
import com.yosrhammami.daggerlab.MainDispatcherRule
import com.yosrhammami.daggerlab.domain.timer.SleepTimer
import com.yosrhammami.daggerlab.fake.FakeAppLogger
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Entry
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Level
import com.yosrhammami.daggerlab.fake.FakeClock
import com.yosrhammami.daggerlab.fake.FakePlaybackController
import com.yosrhammami.daggerlab.fake.FakeSleepTimerSettingsRepository
import com.yosrhammami.daggerlab.ui.screen.SleepTimerUiState
import org.junit.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Each test passes only the fakes it inspects; the others get fresh defaults.
    // The real SleepTimer runs in the TestScope itself (not backgroundScope, which
    // advanceUntilIdle ignores), so it follows runTest's virtual clock.
    private fun TestScope.createViewModel(
        repository: FakeSleepTimerSettingsRepository = FakeSleepTimerSettingsRepository(),
        logger: FakeAppLogger = FakeAppLogger(),
        playback: FakePlaybackController = FakePlaybackController(),
        sleepTimer: SleepTimer = SleepTimer(this, FakeClock { testScheduler.currentTime }, playback, logger)
    ) = PlayerViewModel(repository, logger, playback, sleepTimer)

    @Test
    fun `lastDuration is null when nothing was saved`() = runTest {
        val viewModel = createViewModel()

        viewModel.lastDuration.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `lastDuration emits the previously saved duration`() = runTest {
        val viewModel = createViewModel(repository = FakeSleepTimerSettingsRepository(initial = 30))

        viewModel.lastDuration.test {
            assertNull(awaitItem())          // initial value of stateIn
            assertEquals(30, awaitItem())    // value from the repository
        }
    }

    @Test
    fun `selecting a duration saves it and updates lastDuration`() = runTest {
        val viewModel = createViewModel()

        viewModel.lastDuration.test {
            assertNull(awaitItem())
            viewModel.onDurationSelected(60)
            assertEquals(60, awaitItem())
        }
    }

    @Test
    fun `uiState starts as Idle with no last duration`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
        }
    }

    @Test
    fun `uiState Idle exposes the previously saved duration`() = runTest {
        val viewModel = createViewModel(repository = FakeSleepTimerSettingsRepository(initial = 30))

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())   // initial value of stateIn
            assertEquals(SleepTimerUiState.Idle(30), awaitItem())     // value from the repository
        }
    }

    @Test
    fun `selecting a duration switches to Running and counts down every second`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            assertEquals(SleepTimerUiState.Running(899), awaitItem())
            assertEquals(SleepTimerUiState.Running(898), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `countdown reaching zero switches to Finished and stops`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(1)
            for (expected in 60L downTo 1L) {
                assertEquals(SleepTimerUiState.Running(expected), awaitItem())
            }
            assertEquals(SleepTimerUiState.Finished, awaitItem())

            advanceTimeBy(5_000)
            expectNoEvents()
        }
    }

    @Test
    fun `ok on Finished goes back to Idle with the last saved duration`() = runTest {
        val repository = FakeSleepTimerSettingsRepository()
        val viewModel = createViewModel(repository = repository)

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(1)
            for (expected in 60L downTo 1L) {
                assertEquals(SleepTimerUiState.Running(expected), awaitItem())
            }
            assertEquals(SleepTimerUiState.Finished, awaitItem())

            viewModel.onOkClick()
            assertEquals(SleepTimerUiState.Idle(1), awaitItem())
        }
        assertEquals(1, repository.observeLastDuration().first())
    }

    @Test
    fun `a new timer can start after ok`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(1)
            for (expected in 60L downTo 1L) {
                assertEquals(SleepTimerUiState.Running(expected), awaitItem())
            }
            assertEquals(SleepTimerUiState.Finished, awaitItem())
            viewModel.onOkClick()
            assertEquals(SleepTimerUiState.Idle(1), awaitItem())

            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            assertEquals(SleepTimerUiState.Running(899), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancel goes back to Idle with the last duration and stops the countdown`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            assertEquals(SleepTimerUiState.Running(899), awaitItem())

            viewModel.onCancelClick()
            assertEquals(SleepTimerUiState.Idle(15), awaitItem())

            advanceTimeBy(5_000)
            expectNoEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `ok while Running goes back to Idle and stops the countdown`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            assertEquals(SleepTimerUiState.Running(899), awaitItem())

            viewModel.onOkClick()
            assertEquals(SleepTimerUiState.Idle(15), awaitItem())

            advanceTimeBy(5_000)
            expectNoEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `selecting a duration of zero or less is rejected and changes nothing`() = runTest {
        val repository = FakeSleepTimerSettingsRepository(initial = 30)
        val viewModel = createViewModel(repository = repository)

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            assertEquals(SleepTimerUiState.Idle(30), awaitItem())

            for (minutes in listOf(0, -5)) {
                assertThrows(IllegalArgumentException::class.java) {
                    viewModel.onDurationSelected(minutes)
                }
            }

            advanceTimeBy(5_000)
            expectNoEvents()
        }
        assertEquals(30, repository.observeLastDuration().first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `selecting a new duration while Running restarts from it and stops the old countdown`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            assertEquals(SleepTimerUiState.Running(899), awaitItem())

            // Restart between two ticks so a leftover old countdown would emit separately
            // (on the same tick, StateFlow would merge its value with the new one and hide it).
            advanceTimeBy(500)
            viewModel.onDurationSelected(30)
            assertEquals(SleepTimerUiState.Running(1800), awaitItem())
            assertEquals(SleepTimerUiState.Running(1799), awaitItem())
            assertEquals(SleepTimerUiState.Running(1798), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancel keeps the saved duration`() = runTest {
        val repository = FakeSleepTimerSettingsRepository()
        val viewModel = createViewModel(repository = repository)

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(30)
            assertEquals(SleepTimerUiState.Running(1800), awaitItem())
            viewModel.onCancelClick()
            assertEquals(SleepTimerUiState.Idle(30), awaitItem())
        }
        assertEquals(30, repository.observeLastDuration().first())
    }

    @Test
    fun `selecting again after cancel restarts from the full duration`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            assertEquals(SleepTimerUiState.Running(899), awaitItem())
            viewModel.onCancelClick()
            assertEquals(SleepTimerUiState.Idle(15), awaitItem())

            viewModel.onDurationSelected(30)
            assertEquals(SleepTimerUiState.Running(1800), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- Logging ---

    @Test
    fun `selecting a duration logs it at info level`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        viewModel.onDurationSelected(15)

        assertEquals(
            listOf(Entry(Level.INFO, "PlayerViewModel", "Duration selected: 15 min")),
            logger.entries
        )
    }

    @Test
    fun `rejected duration logs a warning and still throws`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        assertThrows(IllegalArgumentException::class.java) {
            viewModel.onDurationSelected(0)
        }

        assertEquals(
            listOf(Entry(Level.WARN, "PlayerViewModel", "Duration must be positive, was 0")),
            logger.entries
        )
    }

    @Test
    fun `cancel logs timer cancelled`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        viewModel.onDurationSelected(15)
        viewModel.onCancelClick()

        assertEquals(listOf("Duration selected: 15 min", "Timer cancelled"), logger.messages)
    }

    @Test
    fun `ok logs ok clicked`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        viewModel.onOkClick()

        assertEquals(listOf(Entry(Level.INFO, "PlayerViewModel", "OK clicked")), logger.entries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `countdown reaching zero logs timer finished once`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        viewModel.onDurationSelected(1)
        advanceUntilIdle()

        assertEquals(listOf("Duration selected: 1 min", "Timer finished"), logger.messages)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelled countdown never logs timer finished`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        viewModel.onDurationSelected(1)
        advanceTimeBy(500)
        viewModel.onCancelClick()
        advanceUntilIdle()

        assertEquals(listOf("Duration selected: 1 min", "Timer cancelled"), logger.messages)
    }

    // --- Playback ---

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `countdown reaching zero pauses playback once`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val viewModel = createViewModel(playback = playback)

        playback.observeIsPlaying().test {
            assertTrue(awaitItem())
            viewModel.onDurationSelected(1)
            advanceUntilIdle()
            assertFalse(awaitItem())
        }
        assertEquals(1, playback.pauseCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancel does not pause playback`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val viewModel = createViewModel(playback = playback)

        viewModel.onDurationSelected(1)
        advanceTimeBy(500)
        viewModel.onCancelClick()
        advanceUntilIdle()

        assertEquals(0, playback.pauseCount)
        assertTrue(playback.observeIsPlaying().first())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `ok while Running does not pause playback`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val viewModel = createViewModel(playback = playback)

        viewModel.onDurationSelected(1)
        advanceTimeBy(500)
        viewModel.onOkClick()
        advanceUntilIdle()

        assertEquals(0, playback.pauseCount)
    }

    @Test
    fun `selecting a duration does not start or pause playback`() = runTest {
        val playback = FakePlaybackController()
        val viewModel = createViewModel(playback = playback)

        viewModel.onDurationSelected(15)

        assertEquals(0, playback.playCount)
        assertEquals(0, playback.pauseCount)
    }

    @Test
    fun `uiState Idle exposes isPlaying from the controller`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val viewModel = createViewModel(playback = playback)

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = false), awaitItem()) // initial value of stateIn
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = true), awaitItem())
        }
    }

    @Test
    fun `play then pause on Idle updates isPlaying`() = runTest {
        val playback = FakePlaybackController()
        val viewModel = createViewModel(playback = playback)

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = false), awaitItem())
            viewModel.onPlayClick()
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = true), awaitItem())
            viewModel.onPauseClick()
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = false), awaitItem())
        }
        assertEquals(1, playback.playCount)
        assertEquals(1, playback.pauseCount)
    }

    @Test
    fun `pause while Running keeps the countdown and updates isPlaying`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val viewModel = createViewModel(playback = playback)

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = false), awaitItem())
            assertEquals(SleepTimerUiState.Idle(null, isPlaying = true), awaitItem())
            viewModel.onDurationSelected(15)
            assertEquals(SleepTimerUiState.Running(900, isPlaying = true), awaitItem())

            viewModel.onPauseClick()
            assertEquals(SleepTimerUiState.Running(900, isPlaying = false), awaitItem())
            assertEquals(SleepTimerUiState.Running(899, isPlaying = false), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `play and pause clicks are logged at info level`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = createViewModel(logger = logger)

        viewModel.onPlayClick()
        viewModel.onPauseClick()

        assertEquals(
            listOf(
                Entry(Level.INFO, "PlayerViewModel", "Play clicked"),
                Entry(Level.INFO, "PlayerViewModel", "Pause clicked")
            ),
            logger.entries
        )
    }

    // --- Timer outlives the ViewModel ---

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `timer keeps running and pauses playback after the ViewModel is cleared`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val viewModel = createViewModel(playback = playback)

        viewModel.onDurationSelected(1)
        advanceTimeBy(500)
        // What onCleared() does: cancel viewModelScope (e.g. the user pressed Back).
        viewModel.viewModelScope.cancel()
        advanceUntilIdle()

        assertEquals(1, playback.pauseCount)
        assertFalse(playback.observeIsPlaying().first())
    }

    @Test
    fun `a new ViewModel sees the timer started by a previous one`() = runTest {
        val logger = FakeAppLogger()
        val playback = FakePlaybackController()
        val sharedTimer = SleepTimer(this, FakeClock { testScheduler.currentTime }, playback, logger)
        val first = createViewModel(logger = logger, playback = playback, sleepTimer = sharedTimer)

        first.onDurationSelected(15)
        first.viewModelScope.cancel()

        val second = createViewModel(logger = logger, playback = playback, sleepTimer = sharedTimer)
        second.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem()) // initial value of stateIn
            assertEquals(SleepTimerUiState.Running(900), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}