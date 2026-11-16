package com.yosrhammami.daggerlab.data.playback

import app.cash.turbine.test
import com.yosrhammami.daggerlab.fake.FakeAppLogger
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Entry
import com.yosrhammami.daggerlab.fake.FakeAppLogger.Level
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPlaybackControllerTest {

    @Test
    fun `is not playing at start`() = runTest {
        val controller = InMemoryPlaybackController(FakeAppLogger())

        controller.observeIsPlaying().test {
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `play then pause updates isPlaying`() = runTest {
        val controller = InMemoryPlaybackController(FakeAppLogger())

        controller.observeIsPlaying().test {
            assertFalse(awaitItem())
            controller.play()
            assertTrue(awaitItem())
            controller.pause()
            assertFalse(awaitItem())
        }
    }

    @Test
    fun `play and pause are logged at info level`() {
        val logger = FakeAppLogger()
        val controller = InMemoryPlaybackController(logger)

        controller.play()
        controller.pause()

        assertEquals(
            listOf(
                Entry(Level.INFO, "PlaybackController", "Playback started"),
                Entry(Level.INFO, "PlaybackController", "Playback paused")
            ),
            logger.entries
        )
    }

    @Test
    fun `pause when not playing does nothing and logs nothing`() = runTest {
        val logger = FakeAppLogger()
        val controller = InMemoryPlaybackController(logger)

        controller.observeIsPlaying().test {
            assertFalse(awaitItem())
            controller.pause()
            expectNoEvents()
        }
        assertEquals(emptyList<Entry>(), logger.entries)
    }

    @Test
    fun `a second play while playing does nothing and logs nothing`() = runTest {
        val logger = FakeAppLogger()
        val controller = InMemoryPlaybackController(logger)

        controller.observeIsPlaying().test {
            assertFalse(awaitItem())
            controller.play()
            assertTrue(awaitItem())
            controller.play()
            expectNoEvents()
        }
        assertEquals(listOf("Playback started"), logger.messages)
    }

    @Test
    fun `a second pause after pausing logs nothing more`() {
        val logger = FakeAppLogger()
        val controller = InMemoryPlaybackController(logger)

        controller.play()
        controller.pause()
        controller.pause()

        assertEquals(listOf("Playback started", "Playback paused"), logger.messages)
    }
}