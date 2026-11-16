package com.yosrhammami.daggerlab.fake

import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePlaybackController(
    initialIsPlaying: Boolean = false
) : PlaybackController {

    private val isPlaying = MutableStateFlow(initialIsPlaying)

    var playCount = 0
        private set
    var pauseCount = 0
        private set

    override fun observeIsPlaying(): Flow<Boolean> = isPlaying

    override fun play() {
        playCount++
        isPlaying.value = true
    }

    override fun pause() {
        pauseCount++
        isPlaying.value = false
    }
}
