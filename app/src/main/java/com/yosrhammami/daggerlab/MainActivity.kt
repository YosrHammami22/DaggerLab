package com.yosrhammami.daggerlab

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yosrhammami.daggerlab.di.viewmodel.ViewModelFactory
import com.yosrhammami.daggerlab.ui.player.PlaybackTracker
import com.yosrhammami.daggerlab.ui.player.PlayerViewModel
import com.yosrhammami.daggerlab.ui.screen.MainScreen
import com.yosrhammami.daggerlab.ui.settings.SettingsViewModel
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme
import javax.inject.Inject

class MainActivity: ComponentActivity() {


    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    @Inject lateinit var tracker1: PlaybackTracker
    @Inject lateinit var tracker2: PlaybackTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as DaggerLabApplication).appComponent
            .playerSubcomponentFactory()   // 1. le parent donne la factory de l'enfant
            .create(this)                  // 2. on crée l'enfant, avec cette Activity
            .inject(this)                  // 3. l'enfant remplit les champs @Inject
        super.onCreate(savedInstanceState)
        Log.d("DaggerLab", "same tracker in this Activity: ${tracker1 === tracker2}")
        enableEdgeToEdge()
        setContent {
            val playerViewModel: PlayerViewModel = viewModel(factory = viewModelFactory)
           // val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)

            DaggerLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel=playerViewModel
                    )
                }
            }
        }
    }
}

