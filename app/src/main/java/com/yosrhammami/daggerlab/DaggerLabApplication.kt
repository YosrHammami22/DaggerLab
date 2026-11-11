package com.yosrhammami.daggerlab

import android.app.Application
import com.yosrhammami.daggerlab.di.AppComponent
import com.yosrhammami.daggerlab.di.DaggerAppComponent

class DaggerLabApplication : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.factory().create(this)
    }
}
