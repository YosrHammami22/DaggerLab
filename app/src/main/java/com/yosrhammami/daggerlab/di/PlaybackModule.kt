package com.yosrhammami.daggerlab.di

import com.yosrhammami.daggerlab.data.playback.InMemoryPlaybackController
import com.yosrhammami.daggerlab.domain.playback.PlaybackController
import dagger.Binds
import dagger.Module

@Module
abstract class PlaybackModule {
    // The @Singleton scope is on InMemoryPlaybackController itself, so it stays app-wide here.
    @Binds
    abstract fun bindPlaybackController(impl: InMemoryPlaybackController): PlaybackController
}