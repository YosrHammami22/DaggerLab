package com.yosrhammami.daggerlab.fake

import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSleepTimerSettingsRepository(
    initial: Int? = null
) : SleepTimerSettingsRepository {

    private val lastDuration = MutableStateFlow(initial)

    override fun observeLastDuration(): Flow<Int?> = lastDuration

    override suspend fun saveLastDuration(minutes: Int) {
        lastDuration.value = minutes
    }
}