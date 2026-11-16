package com.yosrhammami.daggerlab.di

import com.yosrhammami.daggerlab.data.playback.LoggingPlaybackController
import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import dagger.Binds
import dagger.Module

@Module
abstract class PlaybackModule {
    @Binds
    abstract fun bindPlaybackController(
        impl: LoggingPlaybackController
    ): PlaybackController
}