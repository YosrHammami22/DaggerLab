package com.yosrhammami.daggerlab.di

import com.yosrhammami.daggerlab.domain.time.Clock
import com.yosrhammami.daggerlab.util.ElapsedRealtimeClock
import dagger.Binds
import dagger.Module

@Module
abstract class TimeModule {
    // Unscoped: ElapsedRealtimeClock has no state, so sharing one instance gains nothing.
    @Binds
    abstract fun bindClock(impl: ElapsedRealtimeClock): Clock
}