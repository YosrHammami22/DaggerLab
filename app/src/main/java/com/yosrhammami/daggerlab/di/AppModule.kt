package com.yosrhammami.daggerlab.di

import com.yosrhammami.daggerlab.data.GreetingRepository
import com.yosrhammami.daggerlab.data.GreetingRepositoryImpl
import dagger.Binds
import dagger.Module

@Module
abstract class AppModule {

    @Binds
    abstract fun bindGreetingRepository(impl: GreetingRepositoryImpl): GreetingRepository
}
