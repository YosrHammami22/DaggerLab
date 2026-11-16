package com.yosrhammami.daggerlab.di

import com.yosrhammami.daggerlab.domain.logging.AppLogger
import com.yosrhammami.daggerlab.util.LogcatAppLogger
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
abstract class LoggingModule {
    @Binds
    @Singleton
    abstract fun bindAppLogger(impl: LogcatAppLogger): AppLogger
}