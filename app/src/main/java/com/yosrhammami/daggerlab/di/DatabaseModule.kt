package com.yosrhammami.daggerlab.di

import android.content.Context
import androidx.room.Room
import com.yosrhammami.daggerlab.data.local.AppDatabase
import com.yosrhammami.daggerlab.data.local.SleepTimerSettingsDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun provideNoteDao(database: AppDatabase): SleepTimerSettingsDao = database.sleepTimerSettingsDao()
}
