package com.yosrhammami.daggerlab.util

import android.util.Log
import com.yosrhammami.daggerlab.domain.logging.AppLogger
import javax.inject.Inject

/** [AppLogger] that writes to Logcat. */
class LogcatAppLogger @Inject constructor() : AppLogger {

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String) {
        Log.w(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}