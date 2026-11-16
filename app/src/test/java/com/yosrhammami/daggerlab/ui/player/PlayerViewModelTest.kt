package com.yosrhammami.daggerlab.ui.player

import app.cash.turbine.test
import com.yosrhammami.daggerlab.MainDispatcherRule
import com.yosrhammami.daggerlab.fake.FakeAppLogger
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Entry
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Level
import com.yosrhammami.daggerlab.fake.FakeSleepTimerSettingsRepository
import com.yosrhammami.daggerlab.ui.screen.SleepTimerUiState
import org.junit.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `lastDuration is null when nothing was saved`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

        viewModel.lastDuration.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `lastDuration emits the previously saved duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(initial = 30), FakeAppLogger())

        viewModel.lastDuration.test {
            assertNull(awaitItem())          // initial value of stateIn
            assertEquals(30, awaitItem())    // value from the repository
        }
    }

    @Test
    fun `selecting a duration saves it and updates lastDuration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

        viewModel.lastDuration.test {
            assertNull(awaitItem())
            viewModel.onDurationSelected(60)
            assertEquals(60, awaitItem())
        }
    }

    @Test
    fun `uiState starts as Idle with no last duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
        }
    }

    @Test
    fun `uiState Idle exposes the previously saved duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(initial = 30), FakeAppLogger())

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())   // initial value of stateIn
            assertEquals(SleepTimerUiState.Idle(30), awaitItem())     // value from the repository
        }
    }

    @Test
    fun `selecting a duration switches to Running and counts down every second`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(repository, FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(repository, FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(repository, FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), FakeAppLogger())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), logger)

        viewModel.onDurationSelected(15)

        assertEquals(
            listOf(Entry(Level.INFO, "PlayerViewModel", "Duration selected: 15 min")),
            logger.entries
        )
    }

    @Test
    fun `rejected duration logs a warning and still throws`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), logger)

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), logger)

        viewModel.onDurationSelected(15)
        viewModel.onCancelClick()

        assertEquals(listOf("Duration selected: 15 min", "Timer cancelled"), logger.messages)
    }

    @Test
    fun `ok logs ok clicked`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), logger)

        viewModel.onOkClick()

        assertEquals(listOf(Entry(Level.INFO, "PlayerViewModel", "OK clicked")), logger.entries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `countdown reaching zero logs timer finished once`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), logger)

        viewModel.onDurationSelected(1)
        advanceUntilIdle()

        assertEquals(listOf("Duration selected: 1 min", "Timer finished"), logger.messages)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `cancelled countdown never logs timer finished`() = runTest {
        val logger = FakeAppLogger()
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(), logger)

        viewModel.onDurationSelected(1)
        advanceTimeBy(500)
        viewModel.onCancelClick()
        advanceUntilIdle()

        assertEquals(listOf("Duration selected: 1 min", "Timer cancelled"), logger.messages)
    }
}