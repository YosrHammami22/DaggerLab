package com.yosrhammami.daggerlab.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepTimerSettingsDao {

    @Query("SELECT * FROM sleep_timer_settings WHERE id = 0")
    fun observe(): Flow<SleepTimerSettingsEntity?>

    @Upsert
    suspend fun save(settings: SleepTimerSettingsEntity)
}
