package com.yosrhammami.daggerlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.yosrhammami.daggerlab.ui.compose.MainScreen
import com.yosrhammami.daggerlab.ui.theme.DaggerLabTheme
import kotlinx.coroutines.launch

class MainActivity: ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        (application as DaggerLabApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        val formatter = (application as DaggerLabApplication).appComponent.timeFormatter()
        val component = (application as DaggerLabApplication).appComponent
        val repo = component.sleepTimerSettings()
        lifecycleScope.launch { repo.saveLastDuration(30) }

        enableEdgeToEdge()
        setContent {
            DaggerLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    MainScreen(
                        nameApp = component.appInfo().appName(),
                        time = formatter.format(90_000) ,
                        modifier = Modifier.padding(innerPadding),
                        repo=repo
                    )
                }
            }
        }
    }
}

