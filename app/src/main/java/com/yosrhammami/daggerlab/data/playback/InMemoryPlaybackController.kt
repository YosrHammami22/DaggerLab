package com.yosrhammami.daggerlab.data.playback

import com.yosrhammami.daggerlab.domain.logging.AppLogger
import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the playing state in memory. The app has no audio player yet, so play and pause
 * only update the state and write a log line.
 */
@Singleton
class InMemoryPlaybackController @Inject constructor(
    private val logger: AppLogger
) : PlaybackController {

    private val isPlaying = MutableStateFlow(false)

    override fun observeIsPlaying(): Flow<Boolean> = isPlaying

    override fun play() {
        // Already playing: nothing to start, so skip the misleading log.
        if (isPlaying.value) return
        isPlaying.value = true
        logger.i(TAG, "Playback started")
    }

    override fun pause() {
        // Nothing to pause (e.g. the sleep timer ends and Play was never pressed): skip the misleading log.
        if (!isPlaying.value) return
        isPlaying.value = false
        logger.i(TAG, "Playback paused")
    }

    private companion object {
        const val TAG = "PlaybackController"
    }
}