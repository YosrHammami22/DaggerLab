package com.yosrhammami.daggerlab.di

import com.yosrhammami.daggerlab.data.repository.RoomSleepTimerSettingsRepository
import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import dagger.Binds
import dagger.Module

@Module
abstract class RepositoryModule {
    @Binds
    abstract fun bindSleepTimerSettingsRepository(
        impl: RoomSleepTimerSettingsRepository
    ): SleepTimerSettingsRepository
}