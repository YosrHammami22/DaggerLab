package com.yosrhammami.daggerlab.fake

import com.yosrhammami.daggerlab.domain.time.Clock

/**
 * Follows the test's virtual time ([virtualTimeMillis], usually `{ testScheduler.currentTime }`),
 * plus any deep sleep simulated with [simulateDeepSleep]: real time that passes while no
 * coroutine runs.
 */
class FakeClock(private val virtualTimeMillis: () -> Long) : Clock {

    private var sleptMillis = 0L

    override fun nowMillis(): Long = virtualTimeMillis() + sleptMillis

    fun simulateDeepSleep(millis: Long) {
        sleptMillis += millis
    }
}