package com.yosrhammami.daggerlab.util

import android.os.SystemClock
import com.yosrhammami.daggerlab.domain.time.Clock
import javax.inject.Inject

// elapsedRealtime() includes deep sleep; uptimeMillis() (used by Handler / delay on Main) does not.
class ElapsedRealtimeClock @Inject constructor() : Clock {
    override fun nowMillis(): Long = SystemClock.elapsedRealtime()
}