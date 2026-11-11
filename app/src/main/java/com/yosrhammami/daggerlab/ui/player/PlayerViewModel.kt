package com.yosrhammami.daggerlab.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yosrhammami.daggerlab.domain.repository.SleepTimerSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class PlayerViewModel @Inject constructor(private val settingsRepository: SleepTimerSettingsRepository):
    ViewModel() {

    val lastDuration: StateFlow<Int?> = settingsRepository.observeLastDuration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun onDurationSelected(minutes: Int) {
        viewModelScope.launch { settingsRepository.saveLastDuration(minutes) }
    }
    }