package com.yosrhammami.daggerlab.fake

import com.yosrhammami.daggerlab.domain.playback.PlaybackController

class FakePlaybackController : PlaybackController {
    var pauseCount = 0
        private set

    override fun pause() {
        pauseCount++
    }
}