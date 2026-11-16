package com.yosrhammami.daggerlab.domain.logging

/**
 * App-wide logging abstraction. Kept free of Android imports so ViewModels can be
 * unit-tested on the JVM with a fake (android.util.Log throws "Method not mocked" there).
 */
interface AppLogger {
    fun d(tag: String, message: String)

    fun i(tag: String, message: String)

    fun w(tag: String, message: String)

    fun e(tag: String, message: String, throwable: Throwable? = null)
}