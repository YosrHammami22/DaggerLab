package com.yosrhammami.daggerlab.di

import android.app.Application

import android.content.Context
import com.yosrhammami.daggerlab.MainActivity
import com.yosrhammami.daggerlab.util.TimeFormatter
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)
    fun timeFormatter(): TimeFormatter
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
}

