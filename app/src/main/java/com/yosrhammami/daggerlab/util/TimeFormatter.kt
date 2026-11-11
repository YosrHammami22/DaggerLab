package com.yosrhammami.daggerlab.util

import javax.inject.Inject

class TimeFormatter  @Inject constructor()  {

    fun format(millis: Long): String {
        val totalSeconds = millis / 1000
        return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }
}