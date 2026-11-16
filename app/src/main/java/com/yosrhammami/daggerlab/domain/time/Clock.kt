package com.yosrhammami.daggerlab.domain.time

/**
 * Monotonic time source. Unlike wall-clock time it never jumps when the user changes the date,
 * and unlike uptime it keeps counting while the device is in deep sleep.
 */
interface Clock {
    fun nowMillis(): Long
}