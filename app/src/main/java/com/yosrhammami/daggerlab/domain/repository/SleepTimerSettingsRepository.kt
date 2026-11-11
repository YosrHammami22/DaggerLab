package com.yosrhammami.daggerlab.domain.repository

import kotlinx.coroutines.flow.Flow

interface SleepTimerSettingsRepository {
    fun observeLastDuration(): Flow<Int?>
    suspend fun saveLastDuration(minutes: Int)
}