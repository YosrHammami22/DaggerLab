package com.yosrhammami.daggerlab.di

import android.app.Activity
import com.yosrhammami.daggerlab.MainActivity
import com.yosrhammami.daggerlab.di.scope.ActivityScope
import dagger.BindsInstance
import dagger.Subcomponent

@ActivityScope
@Subcomponent
interface PlayerSubcomponent {

    fun inject(activity: MainActivity)

    @Subcomponent.Factory
    interface Factory {
        fun create(@BindsInstance activity: Activity): PlayerSubcomponent
    }
}