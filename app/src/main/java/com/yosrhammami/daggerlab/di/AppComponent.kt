package com.yosrhammami.daggerlab.di

import android.app.Application

import android.content.Context
import com.yosrhammami.daggerlab.MainActivity
import com.yosrhammami.daggerlab.util.AppInfoProvider
import com.yosrhammami.daggerlab.util.TimeFormatter
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun inject(activity: MainActivity)
    fun timeFormatter(): TimeFormatter
    fun appInfo(): AppInfoProvider
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent //@BindsInstance binds an externally created object, like the Application context, directly into the graph: no module boilerplate, and the factory makes it a required, compile-time-checked parameter.
    }
}

