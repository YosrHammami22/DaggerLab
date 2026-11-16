package com.yosrhammami.daggerlab.di

import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
object CoroutineModule {

    // App-wide scope: lives as long as the process, never cancelled.
    // Main.immediate keeps every timer update on the main thread, so start/stop never race the countdown.
    // SupervisorJob: one failing child does not cancel the others.
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
}