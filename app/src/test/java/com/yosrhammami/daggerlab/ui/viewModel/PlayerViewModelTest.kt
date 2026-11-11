package com.yosrhammami.daggerlab.ui.viewModel

import app.cash.turbine.test
import com.yosrhammami.daggerlab.MainDispatcherRule
import com.yosrhammami.daggerlab.fakeRepository.FakeSleepTimerSettingsRepository
import com.yosrhammami.daggerlab.ui.player.PlayerViewModel
import junit.framework.Assert.assertEquals
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
}