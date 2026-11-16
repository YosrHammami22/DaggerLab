package com.yosrhammami.daggerlab.data.playback

import android.util.Log
import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import javax.inject.Inject

class LoggingPlaybackController
    @Inject
    constructor() : PlaybackController {
        override fun pause() {
            Log.d("DaggerLab", "Playback paused by sleep timer")
        }
    }