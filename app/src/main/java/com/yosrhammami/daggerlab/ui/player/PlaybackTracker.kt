package com.yosrhammami.daggerlab.ui.player

import android.app.Activity
import com.yosrhammami.daggerlab.di.scope.ActivityScope
import com.yosrhammami.daggerlab.domain.logging.AppLogger
import javax.inject.Inject

@ActivityScope
class PlaybackTracker @Inject constructor(
    private val activity: Activity,      // fourni par le subcomponent
    private val logger: AppLogger        // @Singleton, fourni par AppComponent
) {
    init {
        logger.d("DaggerLab", "PlaybackTracker created: ${hashCode()} for ${activity.hashCode()}")
    }
}