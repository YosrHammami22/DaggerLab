package com.yosrhammami.daggerlab.ui.player

import android.app.Activity
import android.util.Log
import com.yosrhammami.daggerlab.di.scope.ActivityScope
import javax.inject.Inject

@ActivityScope
class PlaybackTracker @Inject constructor(
    private val activity: Activity      // fourni par le subcomponent
) {
    init {
        Log.d("DaggerLab", "PlaybackTracker created: ${hashCode()} for ${activity.hashCode()}")
    }
}