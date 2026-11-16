package com.yosrhammami.daggerlab.fake

import com.yosrhammami.daggerlab.domain.logging.AppLogger

class FakeAppLogger : AppLogger {

    data class Entry(
        val level: Level,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    )

    enum class Level { DEBUG, INFO, WARN, ERROR }

    private val _entries = mutableListOf<Entry>()
    val entries: List<Entry> get() = _entries.toList()

    val messages: List<String> get() = _entries.map { it.message }

    override fun d(tag: String, message: String) {
        _entries += Entry(Level.DEBUG, tag, message)
    }

    override fun i(tag: String, message: String) {
        _entries += Entry(Level.INFO, tag, message)
    }

    override fun w(tag: String, message: String) {
        _entries += Entry(Level.WARN, tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        _entries += Entry(Level.ERROR, tag, message, throwable)
    }
}