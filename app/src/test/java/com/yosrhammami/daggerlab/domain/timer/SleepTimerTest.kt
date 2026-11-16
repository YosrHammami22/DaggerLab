package com.yosrhammami.daggerlab.domain.timer

import app.cash.turbine.test
import com.yosrhammami.daggerlab.fake.FakeAppLogger
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Entry
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Level
import com.yosrhammami.daggerlab.fake.FakeClock
import com.yosrhammami.daggerlab.fake.FakePlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

// No MainDispatcherRule: SleepTimer never touches Dispatchers.Main. It runs in the TestScope itself
// (not backgroundScope, which advanceUntilIdle ignores), so it follows runTest's virtual clock.
class SleepTimerTest {

    private fun TestScope.createSleepTimer(
        playback: FakePlaybackController = FakePlaybackController(),
        logger: FakeAppLogger = FakeAppLogger(),
        clock: FakeClock = FakeClock { testScheduler.currentTime }
    ) = SleepTimer(this, clock, playback, logger)

    @Test
    fun `starts Idle`() = runTest {
        val timer = createSleepTimer()

        assertEquals(SleepTimerState.Idle, timer.state.value)
    }

    @Test
    fun `start counts down every second then finishes`() = runTest {
        val timer = createSleepTimer()

        timer.state.test {
            assertEquals(SleepTimerState.Idle, awaitItem())
            timer.start(3)
            assertEquals(SleepTimerState.Running(3), awaitItem())
            assertEquals(SleepTimerState.Running(2), awaitItem())
            assertEquals(SleepTimerState.Running(1), awaitItem())
            assertEquals(SleepTimerState.Finished, awaitItem())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `finishing pauses playback once and logs it`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val logger = FakeAppLogger()
        val timer = createSleepTimer(playback, logger)

        timer.start(3)
        advanceUntilIdle()

        assertEquals(1, playback.pauseCount)
        assertEquals(listOf(Entry(Level.INFO, "SleepTimer", "Timer finished")), logger.entries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stop goes back to Idle and never pauses playback`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val logger = FakeAppLogger()
        val timer = createSleepTimer(playback, logger)

        timer.start(3)
        advanceTimeBy(1_500)
        timer.stop()
        advanceUntilIdle()

        assertEquals(SleepTimerState.Idle, timer.state.value)
        assertEquals(0, playback.pauseCount)
        assertEquals(emptyList<Entry>(), logger.entries)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start while Running restarts and the old countdown never pauses`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val timer = createSleepTimer(playback)

        timer.start(2)
        advanceTimeBy(1_500)
        timer.start(10)
        assertEquals(SleepTimerState.Running(10), timer.state.value)

        // t = 2600: past the old countdown's end (t = 2000) and the new first tick (t = 2500).
        advanceTimeBy(1_100)
        assertEquals(SleepTimerState.Running(9), timer.state.value)
        assertEquals(0, playback.pauseCount)

        advanceUntilIdle()
        assertEquals(SleepTimerState.Finished, timer.state.value)
        assertEquals(1, playback.pauseCount)
    }

    @Test
    fun `start rejects zero or negative seconds`() = runTest {
        val timer = createSleepTimer()

        for (seconds in listOf(0L, -5L)) {
            assertThrows(IllegalArgumentException::class.java) { timer.start(seconds) }
        }
        assertEquals(SleepTimerState.Idle, timer.state.value)
    }

    // --- Deadline-based countdown (deep sleep: real time passes while no coroutine runs) ---

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `after deep sleep the next tick catches up to the real remaining time`() = runTest {
        val clock = FakeClock { testScheduler.currentTime }
        val timer = createSleepTimer(clock = clock)

        timer.start(600)
        advanceTimeBy(1_100) // tick at t = 1000
        assertEquals(SleepTimerState.Running(599), timer.state.value)

        clock.simulateDeepSleep(300_000) // 5 min asleep
        advanceTimeBy(1_000) // next tick at t = 2000, real time 302 s
        assertEquals(SleepTimerState.Running(298), timer.state.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deep sleep past the deadline finishes on the next tick and pauses playback`() = runTest {
        val playback = FakePlaybackController(initialIsPlaying = true)
        val clock = FakeClock { testScheduler.currentTime }
        val timer = createSleepTimer(playback, clock = clock)

        timer.start(60)
        advanceTimeBy(500)
        clock.simulateDeepSleep(10 * 60_000) // asleep well past the 60 s deadline
        advanceTimeBy(1_000) // first tick at t = 1000

        assertEquals(SleepTimerState.Finished, timer.state.value)
        assertEquals(1, playback.pauseCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `a late tick does not push the end back`() = runTest {
        val clock = FakeClock { testScheduler.currentTime }
        val timer = createSleepTimer(clock = clock)

        timer.start(3)
        advanceTimeBy(500)
        clock.simulateDeepSleep(300) // the first tick arrives 300 ms late in real time

        // Real deadline is t = 3000 real = 2700 virtual. Tick counting would end at 3000 virtual.
        advanceTimeBy(2_150) // virtual 2650
        assertEquals(SleepTimerState.Running(1), timer.state.value)
        advanceTimeBy(100) // virtual 2750
        assertEquals(SleepTimerState.Finished, timer.state.value)
    }
}