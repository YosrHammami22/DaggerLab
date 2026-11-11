package com.yosrhammami.daggerlab.di.viewmodel

import androidx.lifecycle.ViewModel
import com.yosrhammami.daggerlab.ui.player.PlayerViewModel
import com.yosrhammami.daggerlab.ui.settings.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap //@IntoMap : « ajoute cette entrée à la Map » au lieu de créer un binding seul
    @ViewModelKey(PlayerViewModel::class)
    abstract fun bindPlayerViewModel(vm: PlayerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindSettingsViewModel(vm: SettingsViewModel): ViewModel
}