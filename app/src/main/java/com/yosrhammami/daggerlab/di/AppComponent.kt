package com.yosrhammami.daggerlab.di

import android.content.Context
import com.yosrhammami.daggerlab.MainActivity
import com.yosrhammami.daggerlab.di.viewmodel.ViewModelModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [DatabaseModule::class, RepositoryModule::class, ViewModelModule::class])
interface AppComponent {
    fun playerSubcomponentFactory(): PlayerSubcomponent.Factory

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent //@BindsInstance binds an externally created object, like the Application context, directly into the graph: no module boilerplate, and the factory makes it a required, compile-time-checked parameter.
    }
}

