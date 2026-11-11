package com.yosrhammami.daggerlab.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "sleep_timer_settings")
data class SleepTimerSettingsEntity(
    @PrimaryKey val id: Int = 0,             // une seule ligne de réglages
    val lastDurationMinutes: Int
)

