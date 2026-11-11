package com.yosrhammami.daggerlab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SleepTimerSettingsEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sleepTimerSettingsDao(): SleepTimerSettingsDao

    companion object {
        const val NAME = "daggerlab.db"
    }
}