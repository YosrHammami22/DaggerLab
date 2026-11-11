package com.yosrhammami.daggerlab.data.repository

import com.yosrhammami.daggerlab.data.local.SleepTimerSettingsDao
import com.yosrhammami.daggerlab.data.local.SleepTimerSettingsEntity
import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomSleepTimerSettingsRepository @Inject constructor(
    private val dao: SleepTimerSettingsDao
) : SleepTimerSettingsRepository {

    override fun observeLastDuration(): Flow<Int?> =
        dao.observe().map { it?.lastDurationMinutes }


    override suspend fun saveLastDuration(minutes: Int) =
        dao.save(SleepTimerSettingsEntity(lastDurationMinutes = minutes))

}