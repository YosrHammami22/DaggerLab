package com.yosrhammami.daggerlab.ui.player

import app.cash.turbine.test
import com.yosrhammami.daggerlab.MainDispatcherRule
import com.yosrhammami.daggerlab.fake.FakeSleepTimerSettingsRepository
import com.yosrhammami.daggerlab.ui.screen.SleepTimerUiState
import org.junit.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PlayerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `lastDuration is null when nothing was saved`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

        viewModel.lastDuration.test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `lastDuration emits the previously saved duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(initial = 30))

        viewModel.lastDuration.test {
            assertNull(awaitItem())          // valeur initiale de stateIn
            assertEquals(30, awaitItem())    // valeur venue du repository
        }
    }

    @Test
    fun `selecting a duration saves it and updates lastDuration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

        viewModel.lastDuration.test {
            assertNull(awaitItem())
            viewModel.onDurationSelected(60)
            assertEquals(60, awaitItem())
        }
    }

    @Test
    fun `uiState starts as Idle with no last duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())
        }
    }

    @Test
    fun `uiState Idle exposes the previously saved duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository(initial = 30))

        viewModel.uiState.test {
            assertEquals(SleepTimerUiState.Idle(null), awaitItem())   // valeur initiale de stateIn
            assertEquals(SleepTimerUiState.Idle(30), awaitItem())     // valeur venue du repository
        }
    }

    @Test
    fun `selecting a duration switches to Running and counts down every second`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

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
        val viewModel = PlayerViewModel(repository)

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

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
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

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

    @Test
    fun `cancel keeps the saved duration`() = runTest {
        val repository = FakeSleepTimerSettingsRepository()
        val viewModel = PlayerViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onDurationSelected(30)
            awaitItem()
            viewModel.onCancelClick()
            awaitItem()
        }
        assertEquals(30, repository.observeLastDuration().first())
    }

    @Test
    fun `selecting again after cancel restarts from the full duration`() = runTest {
        val viewModel = PlayerViewModel(FakeSleepTimerSettingsRepository())

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

    @Test
    fun `selecting a duration still saves it`() = runTest {
        val repository = FakeSleepTimerSettingsRepository()
        val viewModel = PlayerViewModel(repository)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onDurationSelected(30)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(30, repository.observeLastDuration().first())
    }
}