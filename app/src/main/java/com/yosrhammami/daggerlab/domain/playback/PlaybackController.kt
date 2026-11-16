package com.yosrhammami.daggerlab.domain.playback

import kotlinx.coroutines.flow.Flow

/**
 * Controls audio playback. The sleep timer calls [pause] when the countdown finishes.
 */
interface PlaybackController {
    fun observeIsPlaying(): Flow<Boolean>
    fun play()
    fun pause()
}